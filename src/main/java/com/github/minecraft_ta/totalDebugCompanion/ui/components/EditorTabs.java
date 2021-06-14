package com.github.minecraft_ta.totalDebugCompanion.ui.components;

import com.github.minecraft_ta.totalDebugCompanion.model.IEditorPanel;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class EditorTabs extends JTabbedPane {

    private final List<IEditorPanel> editors = new ArrayList<>();

    public EditorTabs() {
        super();
        setTabLayoutPolicy(SCROLL_TAB_LAYOUT);
    }

    @Override
    public void removeTabAt(int index) {
        super.removeTabAt(index);
        editors.remove(index);
    }

    public void openEditorTab(IEditorPanel editorPanel) {
        Component component = editorPanel.getComponent();
        addTab(editorPanel.getTitle(), component);
        int index = indexOfComponent(component);
        setToolTipTextAt(index, editorPanel.getTooltip());
        setTabComponentAt(index, new LabelWithButtonTabComponent(this, editorPanel.getIcon()));
        setSelectedIndex(index);

        editors.add(editorPanel);
    }

    public List<IEditorPanel> getEditors() {
        return this.editors;
    }
}
