package com.github.minecraft_ta.totalDebugCompanion.ui.components;

import com.github.minecraft_ta.totalDebugCompanion.model.IEditorPanel;
import com.github.minecraft_ta.totalDebugCompanion.util.LabelWithButtonTabComponent;

import javax.swing.*;

public class EditorTabs extends JTabbedPane {

    public EditorTabs() {
        super();
        setTabLayoutPolicy(SCROLL_TAB_LAYOUT);
    }

    public void openEditorTab(IEditorPanel editorPanel) {
        JComponent component = editorPanel.getComponent();
        addTab(editorPanel.getTitle(), component);
        int index = indexOfComponent(component);
        setTabComponentAt(index, new LabelWithButtonTabComponent(this));
        setSelectedIndex(index);
    }

}
