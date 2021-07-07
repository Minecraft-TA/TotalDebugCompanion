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
                        byte b = bytes[j];
                        if (b == 0)
                            continue;

                        g.setColor(switch (b) {
                            case 1 -> Color.MAGENTA;
                            case 2 -> Color.BLUE.brighter();
                            default -> throw new IllegalStateException("Unknown state received");
                        });
                        g.fillRect(renderChunkSize * i, renderChunkSize * j, renderChunkSize, renderChunkSize);
                    }
                }

                g.setColor(Color.BLACK);
                //Vertical grid lines
                for (int i = 1; i < chunkGridRequestInfo.getWidth(); i++) {
                    g.drawLine(i * renderChunkSize, 0, i * renderChunkSize, getHeight());
                }

                //Horizontal grid lines
                for (int i = 1; i < chunkGridRequestInfo.getHeight(); i++) {
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

        getContentPane().addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                chunkGridRequestInfo.setSize(
                        getContentPane().getWidth() / renderChunkSize + 1,
                        getContentPane().getHeight() / renderChunkSize + 1
                );
                CompanionApp.SERVER.getMessageProcessor().enqueueMessage(new ChunkGridRequestInfoUpdateMessage(chunkGridRequestInfo));
            }
        });

        var mouseAdapter = new MouseAdapter() {

            private int prevCellX = -1;
            private int prevCellY = -1;

            @Override
            public void mouseReleased(MouseEvent e) {
                if (!SwingUtilities.isRightMouseButton(e))
                    return;

                this.prevCellX = -1;
                this.prevCellY = -1;
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (!SwingUtilities.isRightMouseButton(e))
                    return;

                int cellX = e.getX() / renderChunkSize;
                int cellY = e.getY() / renderChunkSize;

                if ((this.prevCellX == -1 || this.prevCellY == -1) ||
                    (this.prevCellX == cellX && this.prevCellY == cellY)) {
                    this.prevCellX = cellX;
                    this.prevCellY = cellY;
                    return;
                }

                int xDiff = this.prevCellX - cellX;
                int zDiff = this.prevCellY - cellY;

                chunkGridRequestInfo.setMinChunkX(chunkGridRequestInfo.getMinChunkX() + xDiff);
                chunkGridRequestInfo.setMinChunkZ(chunkGridRequestInfo.getMinChunkZ() + zDiff);
                chunkGridRequestInfo.setMaxChunkX(chunkGridRequestInfo.getMaxChunkX() + xDiff);
                chunkGridRequestInfo.setMaxChunkZ(chunkGridRequestInfo.getMaxChunkZ() + zDiff);

                this.prevCellX = cellX;
                this.prevCellY = cellY;
                CompanionApp.SERVER.getMessageProcessor().enqueueMessage(new ChunkGridRequestInfoUpdateMessage(INSTANCE.chunkGridRequestInfo));
            }
        };

        getContentPane().addMouseListener(mouseAdapter);
        getContentPane().addMouseMotionListener(mouseAdapter);

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
