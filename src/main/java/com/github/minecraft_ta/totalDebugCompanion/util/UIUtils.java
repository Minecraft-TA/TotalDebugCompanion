package com.github.minecraft_ta.totalDebugCompanion.util;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

public class UIUtils {

    public static int getFontWidth(JComponent component, String s) {
        return component.getFontMetrics(component.getFont()).stringWidth(s);
    }

    public static <T extends JComponent> T withBorder(T c, Border border) {
        c.setBorder(border);
        return c;
    }

    public static Component topAndBottomStickyLayout(Component top, Component bottom) {
        return verticalLayout(top, Box.createVerticalGlue(), bottom);
    }

    public static JComponent verticalLayout(Component... component) {
        var box = Box.createVerticalBox();
        for (Component c : component) {
            box.add(c);
        }

        return box;
    }

    public static Component horizontalLayout(Component... component) {
        var box = Box.createHorizontalBox();
        for (Component c : component) {
            box.add(c, Box.LEFT_ALIGNMENT);
        }

        return box;
    }

    public static void focusWindow(JFrame frame) {
        frame.setVisible(true);
        int state = frame.getExtendedState();
        state &= ~JFrame.ICONIFIED;
        frame.setExtendedState(state);
        frame.setAlwaysOnTop(true);
        frame.toFront();
        frame.requestFocus();
        frame.setAlwaysOnTop(false);
    }
}
