package com.github.minecraft_ta.totalDebugCompanion.ui.views;

import com.github.minecraft_ta.totalDebugCompanion.CompanionApp;
import com.github.minecraft_ta.totalDebugCompanion.messages.chunkGrid.ChunkGridDataMessage;
import com.github.minecraft_ta.totalDebugCompanion.messages.chunkGrid.ChunkGridRequestInfoUpdateMessage;
import com.github.minecraft_ta.totalDebugCompanion.messages.chunkGrid.ReceiveDataStateMessage;
import com.github.minecraft_ta.totalDebugCompanion.util.ChunkGridRequestInfo;
import com.github.minecraft_ta.totalDebugCompanion.util.DocumentChangeListener;
import com.github.minecraft_ta.totalDebugCompanion.util.UIUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Area;
import java.util.Map;

public class ChunkGridWindow extends JFrame {

    private static final ChunkGridWindow INSTANCE = new ChunkGridWindow();
    private static final Color[] COLORS = new Color[]{
            new Color(40, 40, 40),
            new Color(35, 35, 35),
            new Color(165, 65, 150),
            new Color(150, 50, 133),
            new Color(65, 115, 165),
            new Color(50, 100, 150)
    };

    private int chunkRenderSize = 20;

    private final ChunkGridRequestInfo chunkGridRequestInfo = new ChunkGridRequestInfo(0, 0, 21, 21, 0);
    private Map<Long, Byte> stateMap;
    private GridStyle gridStyle = GridStyle.LINES;

    public ChunkGridWindow() {
        setTitle("Chunk Grid");

        final JPanel contentPane = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (stateMap == null)
                    return;

                int outerPaddingX = (getWidth() - (chunkRenderSize * chunkGridRequestInfo.getWidth())) / 2;
                int outerPaddingY = (getHeight() - (chunkRenderSize * chunkGridRequestInfo.getHeight())) / 2;

                for (int x = chunkGridRequestInfo.getMinChunkX(); x < chunkGridRequestInfo.getMaxChunkX(); x++) {
                    for (int z = chunkGridRequestInfo.getMinChunkZ(); z < chunkGridRequestInfo.getMaxChunkZ(); z++) {
                        long posLong = (long) x << 32 | (z & 0xffffffffL);

                        byte b = stateMap.getOrDefault(posLong, (byte) 0);
                        if (b == 0 && gridStyle != GridStyle.CHECKER_BOARD)
                            continue;

                        g.setColor(COLORS[gridStyle == GridStyle.CHECKER_BOARD && (x % 2 == 0) == (z % 2 == 0) ? (b * 2) + 1 : (b * 2)]);

                        int renderX = x - chunkGridRequestInfo.getMinChunkX();
                        int renderZ = z - chunkGridRequestInfo.getMinChunkZ();
                        g.fillRect(chunkRenderSize * renderX + outerPaddingX, chunkRenderSize * renderZ + outerPaddingY, chunkRenderSize, chunkRenderSize);
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
        };

        setContentPane(contentPane);
        contentPane.setPreferredSize(new Dimension(420, 420));

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                CompanionApp.SERVER.getMessageProcessor().enqueueMessage(new ReceiveDataStateMessage(false));
            }

        });

        contentPane.addComponentListener(new ComponentAdapter() {

            @Override
            public void componentResized(ComponentEvent e) {
                chunkGridRequestInfo.setSize(
                        contentPane.getWidth() / chunkRenderSize,
                        contentPane.getHeight() / chunkRenderSize
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
                        contentPane.getWidth() / chunkRenderSize,
                        contentPane.getHeight() / chunkRenderSize
                );

                CompanionApp.SERVER.getMessageProcessor().enqueueMessage(new ChunkGridRequestInfoUpdateMessage(chunkGridRequestInfo));
            }
        };

        contentPane.addMouseWheelListener(mouseAdapter);
        contentPane.addMouseListener(mouseAdapter);
        contentPane.addMouseMotionListener(mouseAdapter);

        contentPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("shift F"), "openCoordinatePopup");
        contentPane.getActionMap().put("openCoordinatePopup", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                var popupMenu = new JPopupMenu();
                var chunkXTextField = new JTextField();
                chunkXTextField.setText(chunkGridRequestInfo.getMinChunkX() + "");
                chunkXTextField.getDocument().addDocumentListener((DocumentChangeListener) e1 -> {
                    try {
                        int x = Integer.parseInt(chunkXTextField.getText());
                        int width = chunkGridRequestInfo.getWidth();
                        chunkGridRequestInfo.setMinChunkX(x);
                        chunkGridRequestInfo.setWidth(width);
                        CompanionApp.SERVER.getMessageProcessor().enqueueMessage(new ChunkGridRequestInfoUpdateMessage(chunkGridRequestInfo));
                    } catch (Throwable t) {
                    }
                });

                var chunkZTextField = new JTextField();
                chunkZTextField.setText(chunkGridRequestInfo.getMinChunkZ() + "");
                chunkZTextField.getDocument().addDocumentListener((DocumentChangeListener) e1 -> {
                    try {
                        int z = Integer.parseInt(chunkZTextField.getText());
                        int height = chunkGridRequestInfo.getHeight();
                        chunkGridRequestInfo.setMinChunkZ(z);
                        chunkGridRequestInfo.setHeight(height);
                        CompanionApp.SERVER.getMessageProcessor().enqueueMessage(new ChunkGridRequestInfoUpdateMessage(chunkGridRequestInfo));
                    } catch (Throwable t) {
                    }
                });

                popupMenu.add(chunkXTextField);
                popupMenu.add(chunkZTextField);
                popupMenu.show(contentPane, 10, 10);
            }
        });

        CompanionApp.SERVER.getMessageBus().listenAlways(ChunkGridDataMessage.class, (m) -> {
            if (!isVisible())
                return;

            Map<Long, Byte> newStateMap = m.getStateArray();

            this.stateMap = newStateMap;
            SwingUtilities.invokeLater(contentPane::repaint);
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
