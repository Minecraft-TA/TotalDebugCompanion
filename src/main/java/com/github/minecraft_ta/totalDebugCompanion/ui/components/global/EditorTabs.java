package com.github.minecraft_ta.totalDebugCompanion.ui.components.global;

import com.github.minecraft_ta.totalDebugCompanion.model.IEditorPanel;
import com.github.minecraft_ta.totalDebugCompanion.ui.components.LabelWithButtonTabComponent;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class EditorTabs extends JTabbedPane {

    private final List<IEditorPanel> editors = new ArrayList<>();

    public EditorTabs() {
        super();
        setTabLayoutPolicy(SCROLL_TAB_LAYOUT);
        setBorder(BorderFactory.createEmptyBorder(0, 3, 0, 0));
    }

    /**
     * Allow closing with middle mouse button.
     * <p>
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
        if (!this.editors.get(index).canClose())
            return;
        super.removeTabAt(index);
        editors.remove(index);
    }

    public CompletableFuture<Void> openEditorTab(IEditorPanel editorPanel) {
        var future = new CompletableFuture<Void>();
        SwingUtilities.invokeLater(() -> {
            editors.add(editorPanel);
            Component component = editorPanel.getComponent();
            addTab(editorPanel.getTitle(), component);
            int index = indexOfComponent(component);
            setToolTipTextAt(index, editorPanel.getTooltip());
            setTabComponentAt(index, new LabelWithButtonTabComponent(this, editorPanel.getIcon()));
            setSelectedIndex(index);

            future.complete(null);
        });

        return future;
    }

    public <T extends IEditorPanel> CompletableFuture<T> focusOrCreateIfAbsent(Class<T> clazz, Predicate<T> filter, Supplier<T> supplier) {
        for (IEditorPanel editor : editors) {
            if (clazz.isAssignableFrom(editor.getClass()) && filter.test((T) editor)) {
                setSelectedIndex(this.editors.indexOf(editor));
                return CompletableFuture.completedFuture((T) editor);
            }
        }

        var tab = supplier.get();
        return openEditorTab(tab).thenApply(v -> tab);
    }

    public IEditorPanel getSelectedEditor() {
        if (getSelectedIndex() == -1)
            return null;

        return editors.get(getSelectedIndex());
    }
}
