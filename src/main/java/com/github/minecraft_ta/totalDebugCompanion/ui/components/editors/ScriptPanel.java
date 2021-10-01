package com.github.minecraft_ta.totalDebugCompanion.ui.components.editors;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.github.minecraft_ta.totalDebugCompanion.CompanionApp;
import com.github.minecraft_ta.totalDebugCompanion.GlobalConfig;
import com.github.minecraft_ta.totalDebugCompanion.messages.script.RunScriptMessage;
import com.github.minecraft_ta.totalDebugCompanion.messages.script.ScriptStatusMessage;
import com.github.minecraft_ta.totalDebugCompanion.messages.script.StopScriptMessage;
import com.github.minecraft_ta.totalDebugCompanion.model.ScriptView;
import com.github.minecraft_ta.totalDebugCompanion.ui.components.CloseButton;
import com.github.minecraft_ta.totalDebugCompanion.ui.components.FlatIconButton;
import com.github.minecraft_ta.totalDebugCompanion.ui.views.BasePopup;
import com.github.minecraft_ta.totalDebugCompanion.ui.views.CodeCompletionPopup;
import com.github.minecraft_ta.totalDebugCompanion.ui.views.SignatureHelpPopup;
import com.github.minecraft_ta.totalDebugCompanion.util.CodeUtils;
import com.github.minecraft_ta.totalDebugCompanion.util.DocumentChangeListener;
import com.github.minecraft_ta.totalDebugCompanion.util.UIUtils;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.jsonrpc.messages.Either;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.text.DocumentFilter;
import javax.swing.text.*;
import javax.swing.undo.UndoManager;
import java.awt.Color;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ScriptPanel extends AbstractCodeViewPanel {

    private static int SCRIPT_ID = 0;
    private final int scriptId = SCRIPT_ID++;
    private final ScriptView scriptView;

    private static final CodeCompletionPopup codeCompletionPopup = new CodeCompletionPopup();
    private static final SignatureHelpPopup signatureHelpPopup = new SignatureHelpPopup();

    private final FlatIconButton runButton = new FlatIconButton(new FlatSVGIcon("icons/run.svg"), false);
    private final FlatIconButton runServerButton = new FlatIconButton(new FlatSVGIcon("icons/runServer.svg"), false);
    private final FlatIconButton stopButton = new FlatIconButton(new FlatSVGIcon("icons/stop.svg"), false);
    {
        runButton.setToolTipText("Run on client");
        runServerButton.setToolTipText("Run on server");
        stopButton.setToolTipText("Stop execution");
        stopButton.setEnabled(false);
    }
    private final JComboBox<RunScriptMessage.ExecutionEnvironment> executionEnvironmentComboBox = new JComboBox<>(RunScriptMessage.ExecutionEnvironment.values());
    {
        executionEnvironmentComboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                var c = (JLabel) super.getListCellRendererComponent(list, ((RunScriptMessage.ExecutionEnvironment) value).getLabel(), index, isSelected, cellHasFocus);
                c.setIcon(((RunScriptMessage.ExecutionEnvironment) value).getIcon());
                return c;
            }
        });
        executionEnvironmentComboBox.setMaximumSize(new Dimension(360, executionEnvironmentComboBox.getPreferredSize().height));
    }

    private final JPanel logPanelHeader = new JPanel(new FlowLayout(FlowLayout.LEFT));
    private final JSplitPane centerSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT) {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (getBottomComponent() == null)
                return;
            g.setColor(Color.GRAY);

            var divider = getComponent(0);
            g.fillRect(divider.getX(), divider.getY(), getWidth(), 1);
        }
    };
    private final JTextPane logPanelTextPane = new JTextPane() {
        @Override
        public boolean getScrollableTracksViewportWidth() {
            return getUI().getPreferredSize(this).width <= getParent().getSize().width;
        }

        @Override
        public Dimension getPreferredSize() {
            return getUI().getPreferredSize(this);
        }
    };
    private final JScrollPane logPanelScrollPane = new JScrollPane(logPanelTextPane);

    private boolean didTypeBeforeCaretMove;
    private int lastSavedVersion = -1;

    public ScriptPanel(ScriptView scriptView) {
        super();
        this.scriptView = scriptView;

        var headerBar = Box.createHorizontalBox();
        headerBar.setBackground(Color.GRAY);
        headerBar.setBorder(new CompoundBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.GRAY), BorderFactory.createEmptyBorder(5, 0, 5, 0)));

        runButton.addActionListener(e -> runScript(false));
        runServerButton.addActionListener(e -> runScript(true));
        stopButton.addActionListener(e -> CompanionApp.SERVER.getMessageProcessor().enqueueMessage(new StopScriptMessage(this.scriptId)));

        headerBar.add(runButton);
        headerBar.add(runServerButton);
        headerBar.add(stopButton);
        headerBar.add(executionEnvironmentComboBox);
        setHeaderComponent(headerBar);

        var textArea = this.editorPane;
        textArea.setText(scriptView.getSourceText());
        CompanionApp.LSP.didOpen(new DidOpenTextDocumentParams(new TextDocumentItem(scriptView.getURI(), "java", 0, UIUtils.getText(textArea))));

        textArea.setBackground(new Color(60, 63, 65));
        textArea.getDocument().addDocumentListener((DocumentChangeListener) e -> {
            if (e.getType() == DocumentEvent.EventType.CHANGE)
                return;

            updateHighlighting();
        });
        textArea.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("ctrl SPACE"), "autoComplete");
        textArea.getActionMap().put("autoComplete", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleAutoCompletion();
            }
        });

        setupLogPanel();
        setupLSP();
        updateHighlighting();

        CompanionApp.SERVER.getMessageBus().listenAlways(ScriptStatusMessage.class, (m) -> {
            if (m.getScriptId() != this.scriptId)
                return;

            if (m.getType() != ScriptStatusMessage.Type.COMPILATION_COMPLETED) {
                if (centerSplitPane.getBottomComponent() == null) {
                    centerSplitPane.setBottomComponent(UIUtils.verticalLayout(logPanelHeader, logPanelScrollPane));
                    centerSplitPane.setDividerLocation(0.5d);
                }
                logPanelTextPane.setText(m.getMessage());
            }

            if (m.getType() == ScriptStatusMessage.Type.RUN_COMPLETED) {
                this.bottomInformationBar.setSuccessInfoText("Run completed!");
            } else if (m.getType() == ScriptStatusMessage.Type.COMPILATION_FAILED) {
                this.bottomInformationBar.setFailureInfoText("Compilation failed!");
            } else if (m.getType() == ScriptStatusMessage.Type.RUN_EXCEPTION) {
                this.bottomInformationBar.setFailureInfoText("Run failed!");
            } else {
                this.bottomInformationBar.setProcessInfoText("Running...");
            }

            if (m.getType() != ScriptStatusMessage.Type.COMPILATION_COMPLETED)
                setRunButtonsState(true);
        });
    }

    private void runScript(boolean server) {
        if (!CompanionApp.SERVER.isClientConnected()) {
            this.bottomInformationBar.setFailureInfoText("Not connected to game client!");
            return;
        }

        setRunButtonsState(false);
        this.bottomInformationBar.setProcessInfoText("Compiling...");
        String fullScript = CompanionApp.LSP.getBaseScript().mergeWithNormalScript(UIUtils.getText(this.editorPane));
        CompanionApp.SERVER.getMessageProcessor().enqueueMessage(new RunScriptMessage(this.scriptId, fullScript, server, (RunScriptMessage.ExecutionEnvironment) this.executionEnvironmentComboBox.getSelectedItem()));
    }

    private void setRunButtonsState(boolean state) {
        this.runButton.setEnabled(state);
        this.runServerButton.setEnabled(state);
        this.stopButton.setEnabled(!state);
    }

    private void setupLogPanel() {
        var closeButton = new CloseButton();
        closeButton.addActionListener(e -> centerSplitPane.setBottomComponent(null));

        logPanelHeader.setMaximumSize(new Dimension(10000, 30));
        logPanelHeader.add(closeButton);
        logPanelHeader.add(new JLabel("Script Log"));

        SimpleAttributeSet spacingAttributeSet = new SimpleAttributeSet();
        StyleConstants.setSpaceAbove(spacingAttributeSet, 2);
        StyleConstants.setSpaceBelow(spacingAttributeSet, 2);
        logPanelTextPane.setParagraphAttributes(spacingAttributeSet, false);
        logPanelTextPane.setEditable(false);
        logPanelTextPane.setFont(JETBRAINS_MONO_FONT.deriveFont(12f));
        logPanelTextPane.setBackground(new Color(69, 73, 74));
        logPanelTextPane.setBorder(BorderFactory.createEmptyBorder(0, 3, 0, 0));

        centerSplitPane.setTopComponent(((BorderLayout) getLayout()).getLayoutComponent(BorderLayout.CENTER));
        logPanelScrollPane.setBorder(BorderFactory.createEmptyBorder());

        add(centerSplitPane, BorderLayout.CENTER);
    }

    @Override
    protected void updateFonts() {
        super.updateFonts();
        SwingUtilities.invokeLater(() -> {
            var newFont = JETBRAINS_MONO_FONT.deriveFont(GlobalConfig.getInstance().<Float>getValue("fontSize"));
            this.codeCompletionPopup.setFont(newFont);
            this.signatureHelpPopup.setFont(newFont);
        });
    }

    private void updateHighlighting() {
        SwingUtilities.invokeLater(() -> {
            CompanionApp.LSP.semanticsTokenFull(new SemanticTokensParams(new TextDocumentIdentifier(scriptView.getURI())))
                    .thenAccept(res -> {
                        var styledDocument = this.editorPane.getStyledDocument();
                        styledDocument.setCharacterAttributes(0, styledDocument.getLength(), new SimpleAttributeSet(), true);
                        CodeUtils.highlightJavaCodeJavaParser(this.editorPane);
                        CodeUtils.highlightJavaCodeSemanticTokens(res.getData(), this.editorPane);
                    });
        });
    }

    private void setupLSP() {
        addHierarchyListener(e -> {
            if (e.getChangeFlags() == HierarchyEvent.PARENT_CHANGED && getParent() == null) {
                CompanionApp.LSP.didClose(new DidCloseTextDocumentParams(new TextDocumentIdentifier(this.scriptView.getURI())));
                CompanionApp.LSP.getDiagnosticsManager().unregister(this.scriptView.getURI());
                saveScript();
            }
        });

        this.editorPane.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                CompletableFuture.runAsync(() -> {
                    try {Thread.sleep(2000);} catch (InterruptedException ignored) {}
                    //Save if we still don't have focus
                    if (!editorPane.hasFocus())
                        saveScript();
                });
            }
        });

        //Document synchronization
        ((AbstractDocument) this.editorPane.getDocument()).setDocumentFilter(new DocumentFilter() {
            @Override
            public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
                var defaultRootElement = editorPane.getDocument().getDefaultRootElement();
                var line = defaultRootElement.getElementIndex(offset);
                var pos = new Position(line, (offset - defaultRootElement.getElement(line).getStartOffset()));

                //Add tab indentation
                if (string.equals("\n")) {
                    string = string + "\t".repeat(UIUtils.countTabsAtStartOfLine(editorPane, defaultRootElement.getElement(line)));
                }

                sendChanges(new TextDocumentContentChangeEvent(new Range(pos, pos), 0, string));

                didTypeBeforeCaretMove = true;
                super.insertString(fb, offset, string, attr);

                //Trigger auto-completion
                var c = string.charAt(string.length() - 1);
                if ((!Character.isAlphabetic(c) && c != '.') || string.contains("\n") || string.length() > 1) {
                    codeCompletionPopup.setVisible(false);
                    return;
                }
                handleAutoCompletion();
            }

            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
                remove(fb, offset, length);
                insertString(fb, offset, text, attrs);
            }

            @Override
            public void remove(FilterBypass fb, int offset, int length) throws BadLocationException {
                if (length == 0)
                    return;
                var defaultRootElement = editorPane.getDocument().getDefaultRootElement();
                var line1 = defaultRootElement.getElementIndex(offset);
                var pos1 = new Position(line1, (offset - defaultRootElement.getElement(line1).getStartOffset()));
                var pos2 = new Position(line1, pos1.getCharacter() + length);
                var oldText = fb.getDocument().getText(offset, length);

                var fullSync = oldText.contains("\n");
                super.remove(fb, offset, length);

                if (!fullSync)
                    sendChanges(new TextDocumentContentChangeEvent(new Range(pos1, pos2), length, ""));
                else
                    sendChanges(new TextDocumentContentChangeEvent(UIUtils.getText(editorPane)));
            }

            private void sendChanges(TextDocumentContentChangeEvent... changes) {
                CompanionApp.LSP.didChange(
                        new DidChangeTextDocumentParams(
                                new VersionedTextDocumentIdentifier(scriptView.getURI(), 0),
                                List.of(changes)
                        )
                );
            }
        });

        this.editorPane.getCaret().addChangeListener(e -> {
            if (!this.didTypeBeforeCaretMove)
                this.codeCompletionPopup.setVisible(false);

            this.didTypeBeforeCaretMove = false;
            this.signatureHelpPopup.setVisible(false);
        });

        var undoManager = new UndoManager();
        this.editorPane.getDocument().addUndoableEditListener(e -> {
            if (UIUtils.getDocumentEventTypeFromEdit(e.getEdit()) == DocumentEvent.EventType.CHANGE)
                return;
            undoManager.addEdit(e.getEdit());
        });

        this.editorPane.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (!codeCompletionPopup.isVisible())
                    return;

                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    e.consume();
                    doAutoCompletion();
                } else if (e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_DOWN) {
                    var selectedIndex = codeCompletionPopup.getSelectedIndex() + (e.getKeyCode() == KeyEvent.VK_UP ? -1 : 1);
                    if (selectedIndex > codeCompletionPopup.getModel().getSize() - 1)
                        selectedIndex = 0;
                    else if (selectedIndex < 0)
                        selectedIndex = codeCompletionPopup.getModel().getSize() - 1;

                    codeCompletionPopup.setSelectedIndex(selectedIndex);
                    codeCompletionPopup.scrollRectToVisible(codeCompletionPopup.getCellBounds(selectedIndex, selectedIndex));
                    e.consume();
                }
            }
        });

        this.editorPane.getActionMap().put("formatFile", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                CompanionApp.LSP.formatting(new DocumentFormattingParams(new TextDocumentIdentifier(scriptView.getURI()), new FormattingOptions(4, false)))
                        .thenAccept(res -> {
                            Collections.reverse(res);
                            SwingUtilities.invokeLater(() -> res.forEach(ScriptPanel.this::applyTextEdit));
                            bottomInformationBar.setDefaultInfoText("Formatted %d line(s)"
                                    .formatted(Stream.concat(
                                                    res.stream().map(t -> t.getRange().getEnd().getLine()),
                                                    res.stream().map(t -> t.getRange().getStart().getLine()))
                                            .distinct().count()));
                        });
            }
        });
        this.editorPane.getActionMap().put("deleteLine", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                var offset = editorPane.getCaretPosition();
                var defaultRootElement = editorPane.getDocument().getDefaultRootElement();
                var line = defaultRootElement.getElementIndex(offset);
                var element = defaultRootElement.getElement(line);
                var inLineCaretPos = offset - element.getStartOffset();

                try {
                    editorPane.getDocument().remove(Math.max(element.getStartOffset() - 1, 0), Math.min(element.getEndOffset() - element.getStartOffset(), editorPane.getDocument().getLength()));
                    element = defaultRootElement.getElement(Math.min(defaultRootElement.getElementCount() - 1, line));
                    editorPane.setCaretPosition(Math.min(element.getStartOffset() + inLineCaretPos, element.getEndOffset() - 1));
                } catch (BadLocationException ex) {
                    ex.printStackTrace();
                }
            }
        });
        this.editorPane.getActionMap().put("undo", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!undoManager.canUndo())
                    return;
                undoManager.undo();
                updateHighlighting();
            }
        });
        /*this.editorPane.getActionMap().put("redo", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!undoManager.canRedo())
                    return;
                undoManager.redo(); //TODO: Redo causes almost infinite while loop in FlowView#layout
            }
        });*/
        this.editorPane.getActionMap().put("showSignatureHelp", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                CompanionApp.LSP.signatureHelp(new SignatureHelpParams(new TextDocumentIdentifier(scriptView.getURI()), UIUtils.offsetToPosition(editorPane, editorPane.getCaretPosition())))
                        .thenAccept(res -> {
                            if (res.getSignatures().isEmpty() || res.getSignatures().stream().allMatch(s -> s.getParameters().isEmpty()) ||
                                res.getActiveSignature() == null)
                                return;
                            try {
                                var cursorRect = editorPane.modelToView2D(editorPane.getCaretPosition());
                                SwingUtilities.invokeLater(() -> {
                                    signatureHelpPopup.apply(res);
                                    signatureHelpPopup.show(editorPane, (int) cursorRect.getX(), (int) cursorRect.getY() - 5, BasePopup.Alignment.TOP_CENTER);
                                });
                            } catch (BadLocationException ignored) {}
                        });
            }
        });
        this.editorPane.getActionMap().put("closeCompletionPopup", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                codeCompletionPopup.setVisible(false);
            }
        });
        this.editorPane.getInputMap().put(KeyStroke.getKeyStroke("ctrl shift F"), "formatFile");
        this.editorPane.getInputMap().put(KeyStroke.getKeyStroke("ctrl D"), "deleteLine");
        this.editorPane.getInputMap().put(KeyStroke.getKeyStroke("ctrl Z"), "undo");
        this.editorPane.getInputMap().put(KeyStroke.getKeyStroke("ctrl Y"), "redo");
        this.editorPane.getInputMap().put(KeyStroke.getKeyStroke("ctrl P"), "showSignatureHelp");
        this.editorPane.getInputMap().put(KeyStroke.getKeyStroke("ESCAPE"), "closeCompletionPopup");

        this.codeCompletionPopup.addKeyEnterListener(this::doAutoCompletion);

        this.editorPane.getDocument().addDocumentListener((DocumentChangeListener) e -> {
            if (e.getType() == DocumentEvent.EventType.CHANGE)
                return;

            CompanionApp.LSP.getDiagnosticsManager().clearAllHighlights(this.scriptView.getURI());
        });
        CompanionApp.LSP.getDiagnosticsManager().register(this.scriptView.getURI(), this.editorPane);
    }

    private void handleAutoCompletion() {
        final var caretPosition = this.editorPane.getCaretPosition();
        CompanionApp.LSP.completion(new CompletionParams(new TextDocumentIdentifier(this.scriptView.getURI()), UIUtils.offsetToPosition(this.editorPane, caretPosition), new CompletionContext(CompletionTriggerKind.Invoked)))
                .thenAccept(res -> {
                    var items = res.getRight().getItems();
                    if (items == null || items.isEmpty() || this.editorPane.getCaretPosition() != caretPosition) {
                        codeCompletionPopup.setVisible(false);
                        return;
                    }

                    items.removeIf(item -> {
                        if (item.getTextEdit() == null)
                            return false;

                        //Exclude weird broken items?
                        return (item.getInsertText() != null && item.getInsertText().isBlank());
                    });
                    if (items.isEmpty())
                        return;

                    items.sort(Comparator.comparing(i -> Optional.ofNullable(i.getSortText()).orElse(i.getLabel())));
                    try {
                        var cursorRect = this.editorPane.modelToView2D(this.editorPane.getCaretPosition());
                        SwingUtilities.invokeLater(() -> {
                            codeCompletionPopup.setItems(items);
                            codeCompletionPopup.show(this.editorPane, (int) cursorRect.getX(), (int) (cursorRect.getY() + cursorRect.getHeight()));
                        });
                    } catch (BadLocationException e) {
                        e.printStackTrace();
                    }
                });
    }

    private void doAutoCompletion() {
        if (this.codeCompletionPopup.getSelectedIndex() == -1)
            return;

        var item = codeCompletionPopup.getSelectedValue();

        var textEdit = item.getTextEdit();
        if (textEdit.isRight()) {
            System.out.println("No insert replace");
            return;
        }

        applyTextEdit(textEdit.getLeft());
        if (item.getAdditionalTextEdits() != null)
            item.getAdditionalTextEdits().forEach(this::applyTextEdit);

        codeCompletionPopup.setVisible(false);
    }

    public boolean canSave() {
        return true;
    }

    private void saveScript() {
        try {
            if (this.lastSavedVersion == CompanionApp.LSP.getDocumentVersion(this.scriptView.getURI()) || !canSave())
                return;
            this.lastSavedVersion = CompanionApp.LSP.getDocumentVersion(this.scriptView.getURI());
            Files.writeString(this.scriptView.getPath(), UIUtils.getText(this.editorPane));
            System.out.println("Successfully saved script " + this.scriptView.getPath());
        } catch (IOException ex) {
            System.err.println("Failed to save script " + this.scriptView.getPath());
            ex.printStackTrace();
        }
    }

    private void applyTextEdit(TextEdit edit) {
        applyTextEdit(edit, false);
    }

    private void applyTextEdit(TextEdit edit, boolean snippet) {
        var forceSelectionOffset = -1;
        var forceSelectionLength = -1;
        var text = new StringBuilder(edit.getNewText());
        if (snippet) {
            var highestIndex = -1;
            var matches = Pattern.compile("(\\$[^{].*?)?\\$\\{(?<index>\\d{1,2}):?(?<variableName>\\w+)?(.*?)}").matcher(text).results().collect(Collectors.toList());
            Collections.reverse(matches);
            for (MatchResult match : matches) {
                var variableName = match.group(3);
                if (variableName == null)
                    variableName = "";
                text.replace(match.start(), match.end(), variableName);

                var index = Integer.parseInt(match.group(2));
                if (index > highestIndex) {
                    highestIndex = index;
                    forceSelectionOffset = match.start();
                    forceSelectionLength = variableName.length();
                } else {
                    forceSelectionOffset -= match.end() - match.start() - variableName.length();
                }
            }
        }

        try {
            var range = edit.getRange();
            var offset1 = UIUtils.posToOffset(this.editorPane, range.getStart());
            var offset2 = UIUtils.posToOffset(this.editorPane, range.getEnd());
            this.editorPane.getDocument().remove(offset1, offset2 - offset1);
            this.editorPane.getDocument().insertString(offset1, text.toString(), null);

            if (forceSelectionOffset != -1 && forceSelectionLength != -1) {
                this.editorPane.select(offset1 + forceSelectionOffset, offset1 + forceSelectionOffset + forceSelectionLength);
            }
        } catch (BadLocationException ex) {
            ex.printStackTrace();
        }
    }
}
