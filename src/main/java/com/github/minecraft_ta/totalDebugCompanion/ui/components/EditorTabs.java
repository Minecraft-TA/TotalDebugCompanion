package com.github.minecraft_ta.totalDebugCompanion.ui.components;

import com.github.minecraft_ta.totalDebugCompanion.model.IEditorPanel;

import javax.swing.*;
import java.awt.*;

public class EditorTabs extends JTabbedPane {

    public EditorTabs() {
        super();
        setTabLayoutPolicy(SCROLL_TAB_LAYOUT);
    }

    public void openEditorTab(IEditorPanel editorPanel) {
        Component component = editorPanel.getComponent();
        addTab(editorPanel.getTitle(), component);
        int index = indexOfComponent(component);
        setToolTipTextAt(index, editorPanel.getTooltip());
        setTabComponentAt(index, new LabelWithButtonTabComponent(this, editorPanel.getIcon()));
        setSelectedIndex(index);
    }

}
