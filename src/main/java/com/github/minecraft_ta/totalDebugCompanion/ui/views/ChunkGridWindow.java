package com.github.minecraft_ta.totalDebugCompanion.ui.views;

import com.github.minecraft_ta.totalDebugCompanion.CompanionApp;
import com.github.minecraft_ta.totalDebugCompanion.messages.chunkGrid.ChunkGridDataMessage;
import com.github.minecraft_ta.totalDebugCompanion.messages.chunkGrid.ChunkGridRequestInfoUpdateMessage;
import com.github.minecraft_ta.totalDebugCompanion.messages.chunkGrid.ReceiveDataStateMessage;
import com.github.minecraft_ta.totalDebugCompanion.util.ChunkGridRequestInfo;
import com.github.minecraft_ta.totalDebugCompanion.util.UIUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class ChunkGridWindow extends JFrame {

    private static final ChunkGridWindow INSTANCE = new ChunkGridWindow();

    private int renderChunkSize = 20;

    private final ChunkGridRequestInfo chunkGridRequestInfo = new ChunkGridRequestInfo(0, 0, 20, 20, 0);
    private byte[][] stateArray;

    public ChunkGridWindow() {
        setTitle("Chunk Grid");

        setContentPane(new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (stateArray == null)
                    return;

                for (int i = 0; i < stateArray.length; i++) {
                    byte[] bytes = stateArray[i];
                    for (int j = 0; j < bytes.length; j++) {
                        g.setColor(switch (bytes[j]) {
                            case 1 -> Color.MAGENTA;
                            case 2 -> Color.BLUE.brighter();
                            default -> new Color(60, 63, 65);
                        });
                        g.fillRect(renderChunkSize * i, renderChunkSize * j, renderChunkSize, renderChunkSize);
                    }
                }

                g.setColor(Color.BLACK);
                //Grid lines
                for (int i = 1; i < chunkGridRequestInfo.getWidth(); i++) {
                    g.drawLine(i * renderChunkSize, 0, i * renderChunkSize, getHeight());
                    g.drawLine(0, i * renderChunkSize, getWidth(), i * renderChunkSize);
                }
            }
        });
        getContentPane().setPreferredSize(new Dimension(400, 400));

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                CompanionApp.SERVER.getMessageProcessor().enqueueMessage(new ReceiveDataStateMessage(false));
            }

        });

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                chunkGridRequestInfo.setMaxChunkX(getWidth() / renderChunkSize);
                chunkGridRequestInfo.setMaxChunkZ(getHeight() / renderChunkSize);
                CompanionApp.SERVER.getMessageProcessor().enqueueMessage(new ChunkGridRequestInfoUpdateMessage(chunkGridRequestInfo));
            }
        });

        CompanionApp.SERVER.getMessageBus().listenAlways(ChunkGridDataMessage.class, (m) -> {
            if (!isVisible())
                return;

            byte[][] newStateArray = m.getStateArray();
            //Invalid data
            if (newStateArray.length != chunkGridRequestInfo.getWidth() ||
                newStateArray[0].length != chunkGridRequestInfo.getHeight())
                return;

            this.stateArray = newStateArray;
            SwingUtilities.invokeLater(() -> getContentPane().repaint());
        });

        pack();
    }

    public static void open() {
        if (INSTANCE.isVisible()) {
            INSTANCE.toFront();
            return;
        }

        CompanionApp.SERVER.getMessageProcessor().enqueueMessage(new ReceiveDataStateMessage(true));
        CompanionApp.SERVER.getMessageProcessor().enqueueMessage(new ChunkGridRequestInfoUpdateMessage(INSTANCE.chunkGridRequestInfo));
        INSTANCE.setVisible(true);
        UIUtils.centerJFrame(INSTANCE);
    }
}
