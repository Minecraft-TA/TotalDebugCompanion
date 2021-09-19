package com.github.minecraft_ta.totalDebugCompanion.ui.components.editors;

import com.github.minecraft_ta.totalDebugCompanion.CompanionApp;
import com.github.minecraft_ta.totalDebugCompanion.messages.codeView.CodeViewClickMessage;
import com.github.minecraft_ta.totalDebugCompanion.model.CodeView;
import com.github.minecraft_ta.totalDebugCompanion.search.SearchManager;
import com.github.minecraft_ta.totalDebugCompanion.ui.components.SearchHeaderBar;
import com.github.minecraft_ta.totalDebugCompanion.util.CodeUtils;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Utilities;
import java.awt.event.ActionEvent;
import java.awt.event.HierarchyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;

public class CodeViewPanel extends AbstractCodeViewPanel {

    private final SearchManager searchManager = new SearchManager(editorPane);
    private final CodeView codeView;

    public CodeViewPanel(CodeView codeView) {
        this.codeView = codeView;
        this.editorPane.setEditable(false);
        this.editorPane.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int offset = editorPane.viewToModel2D(e.getPoint());
                Rectangle2D modelView;
                try {
                    modelView = editorPane.modelToView2D(offset);
                    //Did we click to the right of a line, and the cursor got adjusted to the left?
                    if (modelView.getX() < e.getX() && offset == Utilities.getRowEnd(editorPane, offset))
                        return;
                } catch (BadLocationException ex) {
                    throw new RuntimeException("Offset not in view", ex);
                }

                int line = editorPane.getDocument().getDefaultRootElement().getElementIndex(offset);
                int column = (offset - editorPane.getDocument().getDefaultRootElement().getElement(line).getStartOffset());

                CompanionApp.SERVER.getMessageProcessor().enqueueMessage(
                        new CodeViewClickMessage(codeView.getPath().getFileName().toString(), line, column)
                );
            }
        });

        //Ctrl+F keybind for search
        this.editorPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("ctrl pressed F"), "openSearchPopup");
        this.editorPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("ESCAPE"), "closeSearchPopup");
        this.editorPane.getActionMap().put("closeSearchPopup", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                removeHeaderComponent();
                searchManager.hideHighlights();
            }
        });
        this.editorPane.getActionMap().put("openSearchPopup", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setHeaderComponent(new SearchHeaderBar(searchManager));
            }
        });

        //Stop search thread if the tab is closed
        addHierarchyListener(e -> {
            if (e.getChangeFlags() == HierarchyEvent.PARENT_CHANGED && getParent() == null) {
                this.searchManager.stopThread();
            }
        });

        //Scroll to focused search position
        this.searchManager.addFocusedIndexChangedListener(i -> {
            if (this.searchManager.getMatchCount() == 0)
                return;

            SwingUtilities.invokeLater(() -> focusRange(this.searchManager.getFocusedRangeStart(), this.searchManager.getFocusedRangeEnd()));
        });
    }

    public void setCode(String code) {
        this.editorPane.setText(code);
        CodeUtils.highlightJavaCode(this.editorPane);
    }

    public JTextPane getEditorPane() {
        return this.editorPane;
    }
}
