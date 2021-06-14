package com.github.minecraft_ta.totalDebugCompanion.util;

import javax.swing.*;
import java.awt.*;

public class UIUtils {

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
        var state = frame.getExtendedState();
        frame.setExtendedState(JFrame.ICONIFIED);
        frame.setExtendedState(state);
    }
}
