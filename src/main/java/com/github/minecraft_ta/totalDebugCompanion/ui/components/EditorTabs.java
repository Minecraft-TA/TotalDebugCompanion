package com.github.minecraft_ta.totalDebugCompanion.ui.components;

import com.github.minecraft_ta.totalDebugCompanion.model.IEditorPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;

public class EditorTabs extends JTabbedPane {

    private final List<IEditorPanel> editors = new ArrayList<>();

    public EditorTabs() {
        super();
        setTabLayoutPolicy(SCROLL_TAB_LAYOUT);
    }

    /**
     * Allow closing with middle mouse button.
     *
     * We do this here to prevent selecting the tab when closing it. {@link #addMouseListener(MouseListener)} gets
     * called too late
     */
    @Override
    protected void processMouseEvent(MouseEvent e) {
        if (!SwingUtilities.isMiddleMouseButton(e) || e.getID() != MouseEvent.MOUSE_PRESSED) {
            super.processMouseEvent(e);
            return;
        }

        int tabIndex = indexAtLocation(e.getX(), e.getY());
        if (tabIndex == -1) {
            super.processMouseEvent(e);
            return;
        }

        removeTabAt(tabIndex);
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
