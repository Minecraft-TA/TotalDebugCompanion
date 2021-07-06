package com.github.minecraft_ta.totalDebugCompanion.ui.views;

import com.github.minecraft_ta.totalDebugCompanion.CompanionApp;
import com.github.minecraft_ta.totalDebugCompanion.messages.chunkGrid.ChunkGridDataMessage;
import com.github.minecraft_ta.totalDebugCompanion.messages.chunkGrid.ReceiveDataStateMessage;
import com.github.minecraft_ta.totalDebugCompanion.util.UIUtils;

import javax.swing.*;
import java.awt.*;

public class ChunkGridWindow extends JFrame {

    private static final ChunkGridWindow INSTANCE = new ChunkGridWindow();

    private byte[][] stateArray;

    public ChunkGridWindow() {
        setTitle("Chunk Grid");
        setSize(400, 400);

        setContentPane(new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (stateArray == null)
                    return;

                for (int i = 0; i < stateArray.length; i++) {
                    byte[] bytes = stateArray[i];
                    for (int j = 0; j < bytes.length; j++) {
                        g.setColor(bytes[j] == 0 ? Color.WHITE : Color.MAGENTA);
                        g.fillRect(20 * i, 20 * j, 20, 20);
                    }
                }
            }
        });

        CompanionApp.SERVER.getMessageBus().listenAlways(ChunkGridDataMessage.class, (m) -> {
            stateArray = m.getStateArray();
            SwingUtilities.invokeLater(() -> {
                getContentPane().repaint();
            });
        });
    }

    public static void open() {
        if (INSTANCE.isVisible()) {
            INSTANCE.toFront();
            return;
        }

        CompanionApp.SERVER.getMessageProcessor().enqueueMessage(new ReceiveDataStateMessage(true));
        INSTANCE.setVisible(true);
        UIUtils.centerJFrame(INSTANCE);
    }
}
