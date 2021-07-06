package com.github.minecraft_ta.totalDebugCompanion.ui.views;

import com.github.minecraft_ta.totalDebugCompanion.util.UIUtils;

import javax.swing.*;

public class ChunkGridWindow extends JFrame {

    private static final ChunkGridWindow INSTANCE = new ChunkGridWindow();

    public ChunkGridWindow() {
        setTitle("Chunk Grid");
        setSize(400, 400);
    }

    public static void open() {
        if (INSTANCE.isVisible()) {
            INSTANCE.toFront();
            return;
        }

        INSTANCE.setVisible(true);
        UIUtils.centerJFrame(INSTANCE);
    }
}
