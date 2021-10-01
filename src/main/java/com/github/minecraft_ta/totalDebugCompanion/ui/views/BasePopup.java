package com.github.minecraft_ta.totalDebugCompanion.ui.views;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.HierarchyBoundsAdapter;
import java.awt.event.HierarchyEvent;

public class BasePopup extends JFrame {

    public BasePopup() {
        setLayout(new BorderLayout());
        setUndecorated(true);
        setAlwaysOnTop(true);
    }

    public void show(Component invoker, int x, int y) {
        show(invoker, x, y, Alignment.BOTTOM_RIGHT);
    }

    public void show(Component invoker, int x, int y, Alignment alignment) {
        var base = invoker.getLocationOnScreen();
        x = base.x + x;
        y = base.y + y;
        switch (alignment) {
            case BOTTOM_RIGHT -> {}
            case TOP_CENTER -> {
                x -= getWidth() / 2;
                y -= getHeight();
            }
        }

        setLocation(x, y);
        setFocusableWindowState(false);
        setVisible(true);
        setFocusableWindowState(true);

        //Detect focus lost
        invoker.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                setVisible(false);
                invoker.removeFocusListener(this);
            }
        });
        //Detect window move and resize
        invoker.addHierarchyBoundsListener(new HierarchyBoundsAdapter() {
            @Override
            public void ancestorMoved(HierarchyEvent e) {
                setVisible(false);
                invoker.removeHierarchyBoundsListener(this);
            }

            @Override
            public void ancestorResized(HierarchyEvent e) {
                setVisible(false);
                invoker.removeHierarchyBoundsListener(this);
            }
        });
    }

    public enum Alignment {
        TOP_CENTER,
        BOTTOM_RIGHT;
    }
}