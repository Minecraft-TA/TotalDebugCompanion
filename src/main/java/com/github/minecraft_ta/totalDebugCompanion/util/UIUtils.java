package com.github.minecraft_ta.totalDebugCompanion.util;

import javax.swing.*;

public class UIUtils {

    public static JComponent verticalLayout(JComponent... component) {
        var box = Box.createVerticalBox();
        for (JComponent c : component) {
            box.add(c);
        }

        return box;
    }

    public static JComponent horizontalLayout(JComponent... component) {
        var box = Box.createHorizontalBox();
        for (JComponent c : component) {
            box.add(c, Box.LEFT_ALIGNMENT);
        }

        return box;
    }
}
