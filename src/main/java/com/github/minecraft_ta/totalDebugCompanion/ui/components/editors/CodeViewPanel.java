package com.github.minecraft_ta.totalDebugCompanion.ui.components.editors;

import com.github.minecraft_ta.totalDebugCompanion.model.CodeView;
import com.github.minecraft_ta.totalDebugCompanion.search.SearchManager;
import com.github.minecraft_ta.totalDebugCompanion.ui.components.global.SearchHeaderBar;
import com.github.minecraft_ta.totalDebugCompanion.util.CodeUtils;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.HierarchyEvent;

public class CodeViewPanel extends AbstractCodeViewPanel {

    private final SearchManager searchManager = new SearchManager(editorPane);

    public CodeViewPanel(CodeView codeView) {
        super(codeView.getPath().toString(), codeView.getTitle());
        this.editorPane.setEditable(false);

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
        CodeUtils.initSyntaxScheme(this.editorPane);
        this.editorPane.setText(code);
    }
}
