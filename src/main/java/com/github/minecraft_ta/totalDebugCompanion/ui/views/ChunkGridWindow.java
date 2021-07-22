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
import java.awt.geom.Area;

public class ChunkGridWindow extends JFrame {

    private static final ChunkGridWindow INSTANCE = new ChunkGridWindow();
    private static final Color[] COLORS = new Color[]{
            new Color(40, 40, 40),
            new Color(35, 35, 35),
            new Color(150, 50, 133),
            new Color(165, 65, 150),
            new Color(50, 100, 150),
            new Color(65, 115, 165)
    };

    private int chunkRenderSize = 20;

    private final ChunkGridRequestInfo chunkGridRequestInfo = new ChunkGridRequestInfo(0, 0, 21, 21, 0);
    private byte[][] stateArray;
    private GridStyle gridStyle = GridStyle.CHECKER_BOARD;

    public ChunkGridWindow() {
        setTitle("Chunk Grid");

        setContentPane(new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (stateArray == null)
                    return;

                int outerPaddingX = (getWidth() - (chunkRenderSize * chunkGridRequestInfo.getWidth())) / 2;
                int outerPaddingY = (getHeight() - (chunkRenderSize * chunkGridRequestInfo.getHeight())) / 2;

                for (int i = 0; i < stateArray.length; i++) {
                    byte[] bytes = stateArray[i];
                    for (int j = 0; j < bytes.length; j++) {
                        byte b = bytes[j];
                        if (b == 0 && gridStyle != GridStyle.CHECKER_BOARD)
                            continue;

                        g.setColor(COLORS[gridStyle == GridStyle.CHECKER_BOARD && (i % 2 == 0) == (j % 2 == 0) ? (b * 2) + 1 : (b * 2)]);
                        g.fillRect(chunkRenderSize * i + outerPaddingX, chunkRenderSize * j + outerPaddingY, chunkRenderSize, chunkRenderSize);
                    }
                }

                g.setColor(Color.BLACK);

                if (gridStyle == GridStyle.LINES) {
                    //Vertical grid lines
                    for (int i = 1; i < chunkGridRequestInfo.getWidth(); i++) {
                        g.drawLine(i * chunkRenderSize + outerPaddingX, 0, i * chunkRenderSize + outerPaddingX, getHeight());
                    }

                    //Horizontal grid lines
                    for (int i = 1; i < chunkGridRequestInfo.getHeight(); i++) {
                        g.drawLine(0, i * chunkRenderSize + outerPaddingY, getWidth(), i * chunkRenderSize + outerPaddingY);
                    }
                }

                //Draw padding border
                var area = new Area(new Rectangle(0, 0, getWidth(), getHeight()));
                area.subtract(new Area(new Rectangle(outerPaddingX, outerPaddingY, getWidth() - 1 - outerPaddingX * 2, getHeight() - 1 - outerPaddingY * 2)));
                g.setClip(area);
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        });
        getContentPane().setPreferredSize(new Dimension(420, 420));

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
                        getContentPane().getWidth() / chunkRenderSize,
                        getContentPane().getHeight() / chunkRenderSize
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

                int cellX = e.getX() / chunkRenderSize;
                int cellY = e.getY() / chunkRenderSize;

                if ((this.prevCellX == -1 || this.prevCellY == -1) ||
                    (this.prevCellX == cellX && this.prevCellY == cellY)) {
                    this.prevCellX = cellX;
                    this.prevCellY = cellY;
                    return;
                }

                int xDiff = this.prevCellX - cellX;
                int zDiff = this.prevCellY - cellY;

                chunkGridRequestInfo.addToAll(xDiff, zDiff, xDiff, zDiff);

                this.prevCellX = cellX;
                this.prevCellY = cellY;
                CompanionApp.SERVER.getMessageProcessor().enqueueMessage(new ChunkGridRequestInfoUpdateMessage(chunkGridRequestInfo));
            }

            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                int wheelRotation = e.getWheelRotation();

                chunkRenderSize = Math.max(1, chunkRenderSize - wheelRotation);
                chunkGridRequestInfo.setSize(
                        getContentPane().getWidth() / chunkRenderSize,
                        getContentPane().getHeight() / chunkRenderSize
                );

                CompanionApp.SERVER.getMessageProcessor().enqueueMessage(new ChunkGridRequestInfoUpdateMessage(chunkGridRequestInfo));
            }
        };

        getContentPane().addMouseWheelListener(mouseAdapter);
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

    private enum GridStyle {
        NONE,
        LINES,
        CHECKER_BOARD
    }
}
