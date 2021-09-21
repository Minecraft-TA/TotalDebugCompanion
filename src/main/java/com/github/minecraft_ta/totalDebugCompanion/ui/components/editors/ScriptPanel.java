package com.github.minecraft_ta.totalDebugCompanion.ui.components.editors;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.github.minecraft_ta.totalDebugCompanion.CompanionApp;
import com.github.minecraft_ta.totalDebugCompanion.messages.script.RunScriptMessage;
import com.github.minecraft_ta.totalDebugCompanion.messages.script.ScriptStatusMessage;
import com.github.minecraft_ta.totalDebugCompanion.model.ScriptView;
import com.github.minecraft_ta.totalDebugCompanion.ui.components.CloseButton;
import com.github.minecraft_ta.totalDebugCompanion.ui.components.FlatIconButton;
import com.github.minecraft_ta.totalDebugCompanion.util.CodeUtils;
import com.github.minecraft_ta.totalDebugCompanion.util.DocumentChangeListener;
import com.github.minecraft_ta.totalDebugCompanion.util.UIUtils;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.*;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.text.DocumentFilter;
import javax.swing.text.*;
import javax.swing.undo.UndoManager;
import java.awt.Color;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class ScriptPanel extends AbstractCodeViewPanel {

    private static int SCRIPT_ID = 0;
    private final int scriptId = SCRIPT_ID++;
    private final ScriptView scriptView;

    private final JList<CompletionItem> completionList = new JList<>(new DefaultListModel<>());
    {
        completionList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                var item = (CompletionItem) value;
                var label = item.getLabel();
                var dividerIndex = label.indexOf('-');
                if (dividerIndex == -1)
                    dividerIndex = label.indexOf(':');

                var renderText = dividerIndex == -1 ? label : """
                        <html><span style='color: rgb(187, 187, 187)'>%s</span>  <span style='color: rgb(150, 150, 150)'>%s</span></html>
                        """.formatted(label.substring(0, dividerIndex - 1), label.substring(dividerIndex + 2));
                var component = super.getListCellRendererComponent(list, renderText, index, isSelected, cellHasFocus);

                setIcon(switch (item.getKind()) {
                    case Method -> new FlatSVGIcon("icons/method.svg");
                    case Class -> new FlatSVGIcon("icons/class.svg");
                    case Constant -> new FlatSVGIcon("icons/constant.svg");
                    case Field -> new FlatSVGIcon("icons/property.svg");
                    case Variable -> new FlatSVGIcon("icons/variable.svg");
                    case Interface -> new FlatSVGIcon("icons/interface.svg");
                    case Enum -> new FlatSVGIcon("icons/enum.svg");
                    case Constructor -> new FlatSVGIcon("icons/constructor.svg");
                    default -> null;
                });

                return component;
            }
        });
        completionList.setFont(JETBRAINS_MONO_FONT.deriveFont(14f));
        completionList.setSelectionBackground(new Color(5 / 255f, 127 / 255f, 242 / 255f, 0.5f));
    }
    private final JScrollPane completionPopupScrollPane = new JScrollPane(completionList);
    {
        completionPopupScrollPane.setPreferredSize(new Dimension(450, 200));
        completionPopupScrollPane.setBorder(BorderFactory.createEmptyBorder());
    }
    private final JPopupMenu completionPopupMenu = new JPopupMenu();
    {
        completionPopupMenu.add(completionPopupScrollPane);
    }

    private boolean didTypeBeforeCaretMove;

    public ScriptPanel(ScriptView scriptView) {
        super();
        this.scriptView = scriptView;

        var headerBar = Box.createHorizontalBox();
        headerBar.setBackground(Color.GRAY);
        headerBar.setBorder(new CompoundBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.GRAY), BorderFactory.createEmptyBorder(5, 0, 5, 0)));

        var runExecutor = (Consumer<Boolean>) (server) -> {
            if (!CompanionApp.SERVER.isClientConnected()) {
                this.bottomInformationBar.setFailureInfoText("Not connected to game client!");
                return;
            }

            this.bottomInformationBar.setProcessInfoText("Compiling...");
            CompanionApp.SERVER.getMessageProcessor().enqueueMessage(new RunScriptMessage(this.scriptId, this.editorPane.getText(), server));
        };
        var runButton = new FlatIconButton(new FlatSVGIcon("icons/run.svg"), false);
        runButton.addActionListener(e -> runExecutor.accept(false));
        runButton.setToolTipText("Run on client");

        var runServerButton = new FlatIconButton(new FlatSVGIcon("icons/runServer.svg"), false);
        runServerButton.addActionListener(e -> runExecutor.accept(true));
        runServerButton.setToolTipText("Run on server");

        headerBar.add(runButton);
        headerBar.add(runServerButton);
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

        var centerSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT) {
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

        var closeButton = new CloseButton();
        closeButton.addActionListener(e -> centerSplitPane.setBottomComponent(null));

        var logPanelHeader = new JPanel(new FlowLayout(FlowLayout.LEFT));
        logPanelHeader.setMaximumSize(new Dimension(10000, 30));
        logPanelHeader.add(closeButton);
        logPanelHeader.add(new JLabel("Script Log"));

        var logPanelTextPane = new JTextPane() {
            @Override
            public boolean getScrollableTracksViewportWidth() {
                return getUI().getPreferredSize(this).width <= getParent().getSize().width;
            }

            @Override
            public Dimension getPreferredSize() {
                return getUI().getPreferredSize(this);
            }
        };

        SimpleAttributeSet spacingAttributeSet = new SimpleAttributeSet();
        StyleConstants.setSpaceAbove(spacingAttributeSet, 2);
        StyleConstants.setSpaceBelow(spacingAttributeSet, 2);
        logPanelTextPane.setParagraphAttributes(spacingAttributeSet, false);
        logPanelTextPane.setEditable(false);
        logPanelTextPane.setFont(JETBRAINS_MONO_FONT.deriveFont(12f));
        logPanelTextPane.setBackground(new Color(69, 73, 74));
        logPanelTextPane.setBorder(BorderFactory.createEmptyBorder(0, 3, 0, 0));

        centerSplitPane.setTopComponent(((BorderLayout) getLayout()).getLayoutComponent(BorderLayout.CENTER));
        var logPanelScrollPane = new JScrollPane(logPanelTextPane);
        logPanelScrollPane.setBorder(BorderFactory.createEmptyBorder());

        add(centerSplitPane, BorderLayout.CENTER);

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
                System.out.println("Output: \n" + m.getMessage());
            } else if (m.getType() == ScriptStatusMessage.Type.COMPILATION_FAILED) {
                this.bottomInformationBar.setFailureInfoText("Compilation failed!");
                System.out.println("Error: \n" + m.getMessage());
            } else if (m.getType() == ScriptStatusMessage.Type.RUN_EXCEPTION) {
                this.bottomInformationBar.setFailureInfoText("Run failed!");
                System.out.println("Error: \n" + m.getMessage());
            } else {
                this.bottomInformationBar.setProcessInfoText("Running...");
            }
        });

        setupLSP();
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
        //Document synchronization
        ((AbstractDocument) this.editorPane.getDocument()).setDocumentFilter(new DocumentFilter() {
            @Override
            public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
                var defaultRootElement = editorPane.getDocument().getDefaultRootElement();
                var line = defaultRootElement.getElementIndex(offset);
                var pos = new Position(line, (offset - defaultRootElement.getElement(line).getStartOffset()));

                //Add tab indentation
                if (string.equals("\n")) {
                    var lineContent = editorPane.getDocument().getText(defaultRootElement.getElement(line).getStartOffset(), defaultRootElement.getElement(line).getEndOffset() - defaultRootElement.getElement(line).getStartOffset());
                    var tabs = 0;
                    for (; tabs < lineContent.length(); tabs++) if (lineContent.charAt(tabs) != '\t') break;
                    string = string + "\t".repeat(tabs);
                }

                sendChanges(new TextDocumentContentChangeEvent(new Range(pos, pos), 0, string));

                didTypeBeforeCaretMove = true;
                super.insertString(fb, offset, string, attr);

                //Trigger auto-completion
                var c = string.charAt(string.length() - 1);
                if ((!Character.isAlphabetic(c) && c != '.') || string.contains("\n") || string.length() > 1) {
                    completionPopupMenu.setVisible(false);
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
            if (!didTypeBeforeCaretMove)
                completionPopupMenu.setVisible(false);

            didTypeBeforeCaretMove = false;
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
                if (!completionPopupMenu.isVisible() || completionList.hasFocus())
                    return;

                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    e.consume();
                    doAutoCompletion();
                } else if (e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_DOWN) {
                    var selectedIndex = completionList.getSelectedIndex() + (e.getKeyCode() == KeyEvent.VK_UP ? -1 : 1);
                    if (selectedIndex > completionList.getModel().getSize() - 1)
                        selectedIndex = 0;
                    else if (selectedIndex < 0)
                        selectedIndex = completionList.getModel().getSize() - 1;

                    completionList.setSelectedIndex(selectedIndex);
                    completionList.scrollRectToVisible(completionList.getCellBounds(selectedIndex, selectedIndex));
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
        this.editorPane.getInputMap().put(KeyStroke.getKeyStroke("ctrl shift F"), "formatFile");
        this.editorPane.getInputMap().put(KeyStroke.getKeyStroke("ctrl D"), "deleteLine");
        this.editorPane.getInputMap().put(KeyStroke.getKeyStroke("ctrl Z"), "undo");
        this.editorPane.getInputMap().put(KeyStroke.getKeyStroke("ctrl Y"), "redo");

        this.completionList.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() != KeyEvent.VK_ENTER)
                    return;

                doAutoCompletion();
            }
        });
    }

    private void handleAutoCompletion() {
        var offset = this.editorPane.getCaretPosition();
        var defaultRootElement = editorPane.getDocument().getDefaultRootElement();
        var line = defaultRootElement.getElementIndex(offset);
        var lineElement = defaultRootElement.getElement(line);
        var column = (offset - lineElement.getStartOffset());
        final var caretPosition = this.editorPane.getCaretPosition();
        CompanionApp.LSP.completion(new CompletionParams(new TextDocumentIdentifier(this.scriptView.getURI()), new Position(line, column), new CompletionContext(CompletionTriggerKind.Invoked)))
                .thenAccept(res -> {
                    var items = res.getRight().getItems();
                    if (items == null || items.isEmpty() || this.editorPane.getCaretPosition() != caretPosition) {
                        completionPopupMenu.setVisible(false);
                        return;
                    }

                    items.removeIf(item -> {
                        if (item.getTextEdit() == null)
                            return false;

//                                try {
                        //Exclude weird broken items?
                        return (item.getInsertText() != null && item.getInsertText().isBlank()) /*||
                                           //Exclude completions that don't to anything
                                           this.editorPane.getDocument().getText(
                                                   lineElement.getStartOffset(),
                                                   caretPosition - lineElement.getStartOffset()
                                           ).endsWith(item.getTextEdit().getLeft().getNewText())*/;
                               /* } catch (BadLocationException e) {
                                    e.printStackTrace();
                                }*/

//                                return false;
                    });
                    if (items.isEmpty())
                        return;

                    items.sort(Comparator.comparing(i -> Optional.ofNullable(i.getSortText()).orElse(i.getLabel())));
                    try {
                        var cursorRect = this.editorPane.modelToView2D(this.editorPane.getCaretPosition());
                        SwingUtilities.invokeLater(() -> {
                            completionPopupScrollPane.getVerticalScrollBar().setValue(0);

                            var model = (DefaultListModel<CompletionItem>) completionList.getModel();
                            model.removeAllElements();
                            model.addAll(items);
                            completionList.setSelectedIndex(0);
                            completionPopupMenu.show(this.editorPane, (int) cursorRect.getX(), (int) (cursorRect.getY() + cursorRect.getHeight()));
                            this.editorPane.requestFocus();
                        });
                    } catch (BadLocationException e) {
                        e.printStackTrace();
                    }
                });
    }

    private void doAutoCompletion() {
        if (this.completionList.getSelectedIndex() == -1)
            return;

        var item = completionList.getSelectedValue();

        var textEdit = item.getTextEdit();
        if (textEdit.isRight()) {
            System.out.println("No insert replace");
            return;
        }

        applyTextEdit(textEdit.getLeft());
        if (item.getAdditionalTextEdits() != null)
            item.getAdditionalTextEdits().forEach(this::applyTextEdit);

        completionPopupMenu.setVisible(false);
    }

    private void applyTextEdit(TextEdit edit) {
        try {
            var range = edit.getRange();
            var offset1 = posToOffset(this.editorPane, range.getStart());
            var offset2 = posToOffset(this.editorPane, range.getEnd());
            this.editorPane.getDocument().remove(offset1, offset2 - offset1);
            this.editorPane.getDocument().insertString(offset1, edit.getNewText(), null);
        } catch (BadLocationException ex) {
            ex.printStackTrace();
        }
    }

    private int posToOffset(JTextComponent c, Position pos) {
        var offset = c.getDocument().getDefaultRootElement().getElement(pos.getLine()).getStartOffset();
        offset += pos.getCharacter();
        return offset;
    }
}
