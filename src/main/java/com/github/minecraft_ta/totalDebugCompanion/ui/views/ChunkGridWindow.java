package com.github.minecraft_ta.totalDebugCompanion.ui.views;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.github.minecraft_ta.totalDebugCompanion.CompanionApp;
import com.github.minecraft_ta.totalDebugCompanion.messages.chunkGrid.ChunkGridDataMessage;
import com.github.minecraft_ta.totalDebugCompanion.messages.chunkGrid.ChunkGridRequestInfoUpdateMessage;
import com.github.minecraft_ta.totalDebugCompanion.messages.chunkGrid.ReceiveDataStateMessage;
import com.github.minecraft_ta.totalDebugCompanion.messages.chunkGrid.RequestCenterOnPlayerMessage;
import com.github.minecraft_ta.totalDebugCompanion.ui.components.FlatIconButton;
import com.github.minecraft_ta.totalDebugCompanion.ui.components.TextFieldWithInlineLabel;
import com.github.minecraft_ta.totalDebugCompanion.util.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Area;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class ChunkGridWindow extends JFrame {

    private static final ChunkGridWindow INSTANCE = new ChunkGridWindow();

    private final ChunkGridPanel chunkGridPanel = new ChunkGridPanel();
    private final TextFieldWithInlineLabel chunkXTextField;
    private final TextFieldWithInlineLabel chunkZTextField;
    private final TextFieldWithInlineLabel xTextField;
    private final TextFieldWithInlineLabel zTextField;
    private final TextFieldWithInlineLabel dimensionTextField;

    private final AtomicBoolean textFieldUpdateLock = new AtomicBoolean();

    public ChunkGridWindow() {
        setTitle("Chunk Grid");
        setLayout(new BorderLayout());
        add(this.chunkGridPanel, BorderLayout.CENTER);

        //Bottom control bar
        var bottomPanel = Box.createHorizontalBox();
        this.chunkXTextField = new TextFieldWithInlineLabel(getChunkGridRequestInfo().getMinChunkX() + "", "CX", "ChunkX");
        this.chunkZTextField = new TextFieldWithInlineLabel(getChunkGridRequestInfo().getMinChunkZ() + "", "CZ", "ChunkZ");
        this.xTextField = new TextFieldWithInlineLabel((getChunkGridRequestInfo().getMinChunkX() << 4) + "", "X", null);
        this.zTextField = new TextFieldWithInlineLabel((getChunkGridRequestInfo().getMinChunkX() << 4) + "", "Z", null);
        this.dimensionTextField = new TextFieldWithInlineLabel(getChunkGridRequestInfo().getDimension() + "", "Dim", null);

        var updateChunkGridCoordinatesAndTextFields = (TriConsumer<Integer, Integer, Boolean>) (minChunkX, minChunkZ, keepOffset) -> {
            if (this.textFieldUpdateLock.get())
                return;
            var info = getChunkGridRequestInfo();
            if (info.getMinChunkX() == minChunkX && info.getMinChunkZ() == minChunkZ)
                return;
            info.moveTo(minChunkX, minChunkZ);
            updateCoordinateTextFields(keepOffset);
            CompanionApp.SERVER.getMessageProcessor().enqueueMessage(new ChunkGridRequestInfoUpdateMessage(info));
        };

        CompanionApp.SERVER.getMessageBus().listenAlways(ChunkGridRequestInfoUpdateMessage.class, (m) -> {
            var requestInfo = m.getChunkGridRequestInfo();
            getChunkGridRequestInfo().setDimension(requestInfo.getDimension());
            this.dimensionTextField.setText(requestInfo.getDimension() + "");
            updateChunkGridCoordinatesAndTextFields.accept(requestInfo.getMinChunkX(), requestInfo.getMinChunkZ(), false);
        });

        //ChunkX TextField
        UIUtils.setIntegerTextFieldEnabled(this.chunkXTextField);
        this.chunkXTextField.getDocument().addDocumentListener((DocumentChangeListener) e -> {
            updateChunkGridCoordinatesAndTextFields.accept(
                    TextUtils.asIntOrDefault(this.chunkXTextField.getText(), getChunkGridRequestInfo().getMinChunkX()),
                    getChunkGridRequestInfo().getMinChunkZ(),
                    false
            );
        });
        //ChunkZ TextField
        UIUtils.setIntegerTextFieldEnabled(this.chunkZTextField);
        this.chunkZTextField.getDocument().addDocumentListener((DocumentChangeListener) e -> {
            updateChunkGridCoordinatesAndTextFields.accept(
                    getChunkGridRequestInfo().getMinChunkX(),
                    TextUtils.asIntOrDefault(this.chunkZTextField.getText(), getChunkGridRequestInfo().getMinChunkZ()),
                    false
            );
        });
        //X TextField
        UIUtils.setIntegerTextFieldEnabled(this.xTextField);
        this.xTextField.getDocument().addDocumentListener((DocumentChangeListener) e -> {
            updateChunkGridCoordinatesAndTextFields.accept(
                    TextUtils.asIntOrDefault(this.xTextField.getText(), getChunkGridRequestInfo().getMinChunkX() << 4) >> 4,
                    getChunkGridRequestInfo().getMinChunkZ(),
                    true
            );
        });
        //Z TextField
        UIUtils.setIntegerTextFieldEnabled(this.zTextField);
        this.zTextField.getDocument().addDocumentListener((DocumentChangeListener) e -> {
            updateChunkGridCoordinatesAndTextFields.accept(
                    getChunkGridRequestInfo().getMinChunkX(),
                    TextUtils.asIntOrDefault(this.zTextField.getText(), getChunkGridRequestInfo().getMinChunkZ() << 4) >> 4,
                    true
            );
        });
        //Dimension TextField
        UIUtils.setIntegerTextFieldEnabled(this.dimensionTextField);
        this.dimensionTextField.setPreferredSize(new Dimension(42, (int) this.dimensionTextField.getPreferredSize().getHeight()));
        this.dimensionTextField.getDocument().addDocumentListener((DocumentChangeListener) e -> {
            getChunkGridRequestInfo().setDimension(TextUtils.asIntOrDefault(this.dimensionTextField.getText(), 0));
            CompanionApp.SERVER.getMessageProcessor().enqueueMessage(new ChunkGridRequestInfoUpdateMessage(getChunkGridRequestInfo()));
        });
        var centerOnPlayerButton = new FlatIconButton(new FlatSVGIcon("icons/target.svg"), false);
        centerOnPlayerButton.setToolTipText("Center on player");
        centerOnPlayerButton.addActionListener(e -> CompanionApp.SERVER.getMessageProcessor().enqueueMessage(new RequestCenterOnPlayerMessage()));

        bottomPanel.add(this.chunkXTextField);
        bottomPanel.add(this.chunkZTextField);
        bottomPanel.add(this.xTextField);
        bottomPanel.add(this.zTextField);
        bottomPanel.add(this.dimensionTextField);
        bottomPanel.add(centerOnPlayerButton);
        add(bottomPanel, BorderLayout.SOUTH);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                CompanionApp.SERVER.getMessageProcessor().enqueueMessage(new ReceiveDataStateMessage(false));
            }
        });
        Runtime.getRuntime().addShutdownHook(new Thread(() -> CompanionApp.SERVER.getMessageProcessor().enqueueMessage(new ReceiveDataStateMessage(false))));

        pack();
    }

    private void updateCoordinateTextFields(boolean keepOffset) {
        if (this.textFieldUpdateLock.get())
            return;
        var info = getChunkGridRequestInfo();
        this.textFieldUpdateLock.set(true);

        var minChunkX = info.getMinChunkX();
        var minChunkZ = info.getMinChunkZ();

        SwingUtilities.invokeLater(() -> {
            UIUtils.setTextAndKeepCaret(this.chunkXTextField, minChunkX + "");
            UIUtils.setTextAndKeepCaret(this.chunkZTextField, minChunkZ + "");
            var xOffset = keepOffset ? TextUtils.asIntOrDefault(this.xTextField.getText(), minChunkX << 4) - (minChunkX << 4) : 0;
            UIUtils.setTextAndKeepCaret(this.xTextField, ((minChunkX << 4) + xOffset) + "");
            var zOffset = keepOffset ? TextUtils.asIntOrDefault(this.zTextField.getText(), minChunkZ << 4) - (minChunkZ << 4) : 0;
            UIUtils.setTextAndKeepCaret(this.zTextField, ((minChunkZ << 4) + zOffset) + "");
            this.textFieldUpdateLock.set(false);
        });
    }

    public ChunkGridRequestInfo getChunkGridRequestInfo() {
        return this.chunkGridPanel.chunkGridRequestInfo;
    }

    public static void open() {
        if (INSTANCE.isVisible()) {
            INSTANCE.toFront();
            return;
        }

        CompanionApp.SERVER.getMessageProcessor().enqueueMessage(new ReceiveDataStateMessage(true));
        CompanionApp.SERVER.getMessageProcessor().enqueueMessage(new ChunkGridRequestInfoUpdateMessage(INSTANCE.getChunkGridRequestInfo()));
        INSTANCE.setVisible(true);
        UIUtils.centerJFrame(INSTANCE);
    }

    private enum GridStyle {
        NONE,
        LINES,
        CHECKER_BOARD
    }

    private class ChunkGridPanel extends JPanel {

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
        private GridStyle gridStyle = GridStyle.CHECKER_BOARD;

        public ChunkGridPanel() {
            setMinimumSize(new Dimension(100, 100));
            setPreferredSize(new Dimension(420, 420));

            addComponentListener(new ComponentAdapter() {
                @Override
                public void componentResized(ComponentEvent e) {
                    updateGridSize();
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

                    int cellX = e.getX() / ChunkGridPanel.this.chunkRenderSize;
                    int cellY = e.getY() / ChunkGridPanel.this.chunkRenderSize;

                    if ((this.prevCellX == -1 || this.prevCellY == -1) ||
                        (this.prevCellX == cellX && this.prevCellY == cellY)) {
                        this.prevCellX = cellX;
                        this.prevCellY = cellY;
                        return;
                    }

                    int xDiff = this.prevCellX - cellX;
                    int zDiff = this.prevCellY - cellY;

                    ChunkGridPanel.this.chunkGridRequestInfo.addToAll(xDiff, zDiff, xDiff, zDiff);

                    this.prevCellX = cellX;
                    this.prevCellY = cellY;
                    CompanionApp.SERVER.getMessageProcessor().enqueueMessage(new ChunkGridRequestInfoUpdateMessage(ChunkGridPanel.this.chunkGridRequestInfo));

                    updateCoordinateTextFields(false);
                }

                @Override
                public void mouseWheelMoved(MouseWheelEvent e) {
                    int wheelRotation = e.getWheelRotation();

                    ChunkGridPanel.this.chunkRenderSize = Math.max(1, ChunkGridPanel.this.chunkRenderSize - wheelRotation);
                    updateGridSize();
                    updateCoordinateTextFields(false);
                }

                @Override
                public void mousePressed(MouseEvent e) {
                    requestFocus();
                }
            };

            addMouseWheelListener(mouseAdapter);
            addMouseListener(mouseAdapter);
            addMouseMotionListener(mouseAdapter);

            CompanionApp.SERVER.getMessageBus().listenAlways(ChunkGridDataMessage.class, (m) -> {
                if (!isVisible())
                    return;

                this.stateMap = m.getStateMap();
                SwingUtilities.invokeLater(this::repaint);
            });

            SwingUtilities.invokeLater(this::requestFocus);
        }

        private void updateGridSize() {
            this.chunkGridRequestInfo.setSize(
                    getWidth() / this.chunkRenderSize,
                    getHeight() / this.chunkRenderSize
            );

            CompanionApp.SERVER.getMessageProcessor().enqueueMessage(new ChunkGridRequestInfoUpdateMessage(this.chunkGridRequestInfo));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (this.stateMap == null)
                return;

            int outerPaddingX = (getWidth() - (this.chunkRenderSize * this.chunkGridRequestInfo.getWidth())) / 2;
            int outerPaddingY = (getHeight() - (this.chunkRenderSize * this.chunkGridRequestInfo.getHeight())) / 2;

            for (int x = this.chunkGridRequestInfo.getMinChunkX(); x < this.chunkGridRequestInfo.getMaxChunkX(); x++) {
                for (int z = this.chunkGridRequestInfo.getMinChunkZ(); z < this.chunkGridRequestInfo.getMaxChunkZ(); z++) {
                    long posLong = (long) x << 32 | (z & 0xffffffffL);

                    byte b = this.stateMap.getOrDefault(posLong, (byte) 0);
                    if (b == 0 && this.gridStyle != GridStyle.CHECKER_BOARD)
                        continue;

                    g.setColor(COLORS[this.gridStyle == GridStyle.CHECKER_BOARD && (x % 2 == 0) == (z % 2 == 0) ? (b * 2) + 1 : (b * 2)]);

                    int renderX = x - this.chunkGridRequestInfo.getMinChunkX();
                    int renderZ = z - this.chunkGridRequestInfo.getMinChunkZ();
                    g.fillRect(this.chunkRenderSize * renderX + outerPaddingX, this.chunkRenderSize * renderZ + outerPaddingY, this.chunkRenderSize, this.chunkRenderSize);
                }
            }

            g.setColor(Color.BLACK);

            if (this.gridStyle == GridStyle.LINES) {
                ((Graphics2D) g).setStroke(new BasicStroke(1f));
                //Vertical grid lines
                for (int i = 1; i < this.chunkGridRequestInfo.getWidth(); i++) {
                    g.drawLine(i * this.chunkRenderSize + outerPaddingX, 0, i * this.chunkRenderSize + outerPaddingX, getHeight());
                }

                //Horizontal grid lines
                for (int i = 1; i < this.chunkGridRequestInfo.getHeight(); i++) {
                    g.drawLine(0, i * this.chunkRenderSize + outerPaddingY, getWidth(), i * this.chunkRenderSize + outerPaddingY);
                }
            }

            //Draw padding border
            var clipArea = new Area(new Rectangle(0, 0, getWidth(), getHeight()));
            clipArea.subtract(new Area(new Rectangle(outerPaddingX, outerPaddingY, getWidth() - outerPaddingX * 2, getHeight() - outerPaddingY * 2)));
            g.setClip(clipArea);
            g.fillRect(0, 0, getWidth(), getHeight());
        }
    }
}
