package com.github.minecraft_ta.totalDebugCompanion.ui.components.editors;

import com.github.minecraft_ta.totalDebugCompanion.CompanionApp;
import com.github.minecraft_ta.totalDebugCompanion.GlobalConfig;
import com.github.minecraft_ta.totalDebugCompanion.Icons;
import com.github.minecraft_ta.totalDebugCompanion.jdt.BaseScript;
import com.github.minecraft_ta.totalDebugCompanion.jdt.completion.CompletionItem;
import com.github.minecraft_ta.totalDebugCompanion.jdt.completion.CustomCompletionRequestor;
import com.github.minecraft_ta.totalDebugCompanion.jdt.diagnostics.CustomJavaParser;
import com.github.minecraft_ta.totalDebugCompanion.jdt.impls.CompilationUnitImpl;
import com.github.minecraft_ta.totalDebugCompanion.messages.script.RunScriptMessage;
import com.github.minecraft_ta.totalDebugCompanion.messages.script.ScriptStatusMessage;
import com.github.minecraft_ta.totalDebugCompanion.messages.script.StopScriptMessage;
import com.github.minecraft_ta.totalDebugCompanion.model.ScriptView;
import com.github.minecraft_ta.totalDebugCompanion.ui.components.CloseButton;
import com.github.minecraft_ta.totalDebugCompanion.ui.components.FlatIconButton;
import com.github.minecraft_ta.totalDebugCompanion.ui.views.CodeCompletionPopup;
import com.github.minecraft_ta.totalDebugCompanion.ui.views.SignatureHelpPopup;
import com.github.minecraft_ta.totalDebugCompanion.util.DocumentChangeListener;
import com.github.minecraft_ta.totalDebugCompanion.util.UIUtils;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.text.edits.TextEdit;
import org.fife.ui.rsyntaxtextarea.RSyntaxDocument;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ScriptPanel extends AbstractCodeViewPanel {

    private static int SCRIPT_ID = 0;
    private final int scriptId = SCRIPT_ID++;
    private final ScriptView scriptView;

    private static final CodeCompletionPopup codeCompletionPopup = new CodeCompletionPopup();
    private static final SignatureHelpPopup signatureHelpPopup = new SignatureHelpPopup();

    private final FlatIconButton runButton = new FlatIconButton(Icons.RUN, false);
    private final FlatIconButton runServerButton = new FlatIconButton(Icons.RUN_SERVER, false);
    private final FlatIconButton stopButton = new FlatIconButton(Icons.STOP, false);
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

    private CustomCompletionRequestor completionRequestor;
    private boolean didTypeBeforeCaretMove;
    private int lastSavedVersion = -1;

    public ScriptPanel(ScriptView scriptView) {
        super(scriptView.getURI());
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

        this.editorPane.setParserDelay(400);
        this.editorPane.addParser(new CustomJavaParser(scriptView.getURI()));
        this.editorPane.setText(scriptView.getSourceText());

        setupLogPanel();
        setupSaveBehavior();
        setupAutocompletion();

        CompanionApp.SERVER.getMessageBus().listenAlways(ScriptStatusMessage.class, this, (m) -> {
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
        String fullScript = BaseScript.mergeWithNormalScript(UIUtils.getText(this.editorPane));
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
            codeCompletionPopup.setFont(newFont);
            signatureHelpPopup.setFont(newFont);
        });
    }

    private void setupSaveBehavior() {
        addHierarchyListener(e -> {
            if (e.getChangeFlags() == HierarchyEvent.PARENT_CHANGED && getParent() == null) {
                CompanionApp.SERVER.getMessageProcessor().enqueueMessage(new StopScriptMessage(this.scriptId));
                saveScript();
            }
        });

        //TODO: Add Ctrl+S keybind
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
    }

    private void setupAutocompletion() {
        this.editorPane.getCaret().addChangeListener(e -> {
            if (this.completionRequestor != null)
                this.completionRequestor.setCanceled(true);

            if (!this.didTypeBeforeCaretMove)
                codeCompletionPopup.setVisible(false);

            this.didTypeBeforeCaretMove = false;
            signatureHelpPopup.setVisible(false);
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
                    //TODO: Completion popup up and down
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

        this.editorPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("ctrl SPACE"), "autoComplete");
        this.editorPane.getActionMap().put("autoComplete", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                requestCompletionProposals();
            }
        });

        this.editorPane.getActionMap().put("closeCompletionPopup", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                codeCompletionPopup.setVisible(false);
            }
        });
        this.editorPane.getInputMap().put(KeyStroke.getKeyStroke("ESCAPE"), "closeCompletionPopup");

        //Document synchronization
        ((RSyntaxDocument) this.editorPane.getDocument()).setDocumentFilter(new DocumentFilter() {

            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
                if (text == null || text.isEmpty()) {
                    super.replace(fb, offset, length, text, attrs);
                    return;
                }

                didTypeBeforeCaretMove = true;
                super.replace(fb, offset, length, text, attrs);

                //Trigger auto-completion
                var c = text.charAt(text.length() - 1);
                if ((!Character.isAlphabetic(c) && c != '.') || text.contains("\n") || text.length() > 1) {
                    codeCompletionPopup.setVisible(false);
                    return;
                }
                requestCompletionProposals();
            }
        });

        codeCompletionPopup.addKeyEnterListener(this::doAutoCompletion);
    }

    private void setupAutocompletionOld() {

        this.editorPane.getActionMap().put("formatFile", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //TODO: Format the file
             /*   Collections.reverse(res);
                SwingUtilities.invokeLater(() -> res.forEach(ScriptPanel.this::applyTextEdit));
                bottomInformationBar.setDefaultInfoText("Formatted %d line(s)"
                        .formatted(Stream.concat(
                                        res.stream().map(t -> t.getRange().getEnd().getLine()),
                                        res.stream().map(t -> t.getRange().getStart().getLine()))
                                .distinct().count()));*/
            }
        });
        this.editorPane.getActionMap().put("showSignatureHelp", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //TODO: Signature help
                /*CompanionApp.LSP.signatureHelp(new SignatureHelpParams(new TextDocumentIdentifier(scriptView.getURI()), UIUtils.offsetToPosition(editorPane, editorPane.getCaretPosition())))
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
                        });*/
            }
        });
        this.editorPane.getActionMap().put("closeCompletionPopup", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                codeCompletionPopup.setVisible(false);
            }
        });
        this.editorPane.getInputMap().put(KeyStroke.getKeyStroke("ctrl shift F"), "formatFile");
        this.editorPane.getInputMap().put(KeyStroke.getKeyStroke("ctrl P"), "showSignatureHelp");
        this.editorPane.getInputMap().put(KeyStroke.getKeyStroke("ESCAPE"), "closeCompletionPopup");

        codeCompletionPopup.addKeyEnterListener(this::doAutoCompletion);

        this.editorPane.getDocument().addDocumentListener((DocumentChangeListener) e -> {
            if (e.getType() == DocumentEvent.EventType.CHANGE)
                return;

            //TODO: Diagnostics clear?
//            CompanionApp.LSP.getDiagnosticsManager().clearAllHighlights(this.scriptView.getURI());
        });
    }

    private void requestCompletionProposals() {
        if (this.completionRequestor != null)
            this.completionRequestor.setCanceled(true);

        var unit = new CompilationUnitImpl("SomeName", UIUtils.getText(this.editorPane));
        var newRequestor = new CustomCompletionRequestor(unit, this.editorPane.getCaretPosition(), this::acceptCompletionList);
        this.completionRequestor = newRequestor;

        CompletableFuture.runAsync(() -> {
            try {
                unit.codeComplete(this.editorPane.getCaretPosition(), newRequestor, newRequestor);
            } catch (OperationCanceledException ignored) {} catch (Throwable e) {
                e.printStackTrace();
            }
        });
    }

    private void doAutoCompletion() {
        if (codeCompletionPopup.getSelectedIndex() == -1)
            return;
        var item = codeCompletionPopup.getSelectedValue();
        //The item is outdated
        if (item.getRequestor().isCanceled())
            return;

        this.editorPane.beginAtomicEdit();
        item.getTextEdits().forEach(edit -> {
            var range = edit.getRange();
            try {
                ((RSyntaxDocument) this.editorPane.getDocument()).replace(range.getOffset(), range.getLength(), edit.getNewText(), null);
//                (range.getOffset(), range.getLength());
//                this.editorPane.getDocument().insertString(range.getOffset(), edit.getNewText(), null);
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
        });
        this.editorPane.endAtomicEdit();

        codeCompletionPopup.setVisible(false);
    }

    private void acceptCompletionList(List<CompletionItem> completions) {
        if (completions.isEmpty()) {
            ScriptPanel.codeCompletionPopup.setVisible(false);
            return;
        }

        SwingUtilities.invokeLater(() -> {
            try {
                var cursorRect = editorPane.modelToView2D(editorPane.getCaretPosition());
                codeCompletionPopup.setItems(completions);
                codeCompletionPopup.show(editorPane, (int) cursorRect.getX(), (int) (cursorRect.getY() + cursorRect.getHeight()));
            } catch (Throwable t) {
                throw new RuntimeException(t);
            }
        });
    }

    public boolean canSave() {
        return true;
    }

    private void saveScript() {
        try {
            //TODO: Maybe change to lastSavedHashCode or something
            /*if (this.lastSavedVersion == CompanionApp.LSP.getDocumentVersion(this.scriptView.getURI()) || !canSave())
                return;
            this.lastSavedVersion = CompanionApp.LSP.getDocumentVersion(this.scriptView.getURI());*/
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
        //TODO: Apply text edit, what about snippets?
        /*var forceSelectionOffset = -1;
        var forceSelectionLength = -1;
        var text = new StringBuilder(edit.getNewText());
        if (snippet) {
            var lowest = 100;
            var matches = Pattern.compile("(\\$[^{].*?)?\\$\\{(?<index>\\d{1,2}):?(?<variableName>\\w+)?(.*?)}").matcher(text).results().collect(Collectors.toList());
            Collections.reverse(matches);
            for (MatchResult match : matches) {
                var variableName = match.group(3);
                if (variableName == null)
                    variableName = "";
                text.replace(match.start(), match.end(), variableName);

                var index = Integer.parseInt(match.group(2));
                if (index < lowest) {
                    lowest = index;
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
        }*/
    }

}
