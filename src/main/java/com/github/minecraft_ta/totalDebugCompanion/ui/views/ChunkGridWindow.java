package com.github.minecraft_ta.totalDebugCompanion.ui.views;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.formdev.flatlaf.ui.FlatUIUtils;
import com.github.minecraft_ta.totalDebugCompanion.CompanionApp;
import com.github.minecraft_ta.totalDebugCompanion.messages.chunkGrid.ChunkGridDataMessage;
import com.github.minecraft_ta.totalDebugCompanion.messages.chunkGrid.ChunkGridRequestInfoUpdateMessage;
import com.github.minecraft_ta.totalDebugCompanion.messages.chunkGrid.ReceiveDataStateMessage;
import com.github.minecraft_ta.totalDebugCompanion.messages.chunkGrid.UpdateFollowPlayerStateMessage;
import com.github.minecraft_ta.totalDebugCompanion.ui.components.FlatIconButton;
import com.github.minecraft_ta.totalDebugCompanion.ui.components.TextFieldWithInlineLabel;
import com.github.minecraft_ta.totalDebugCompanion.util.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
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

    private final Box topSettingsPanel;
    private final Box bottomInputPanel;

    private boolean overlayModeEnabled;

    public ChunkGridWindow() {
        setTitle("Chunk Grid");
        setLayout(new BorderLayout());
        add(this.chunkGridPanel, BorderLayout.CENTER);

        //Bottom control bar
        this.bottomInputPanel = Box.createHorizontalBox();
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
        centerOnPlayerButton.addActionListener(e -> {
            CompanionApp.SERVER.getMessageProcessor().enqueueMessage(new UpdateFollowPlayerStateMessage(UpdateFollowPlayerStateMessage.STATE_ONCE));
        });

        this.bottomInputPanel.add(this.chunkXTextField);
        this.bottomInputPanel.add(this.chunkZTextField);
        this.bottomInputPanel.add(this.xTextField);
        this.bottomInputPanel.add(this.zTextField);
        this.bottomInputPanel.add(this.dimensionTextField);
        this.bottomInputPanel.add(centerOnPlayerButton);
        add(this.bottomInputPanel, BorderLayout.SOUTH);

        //Top control bar
        this.topSettingsPanel = Box.createHorizontalBox();
        var gridStyleComboBox = new JComboBox<>(GridStyle.values());
        gridStyleComboBox.setPreferredSize(new Dimension(200, gridStyleComboBox.getPreferredSize().height));
        gridStyleComboBox.setMaximumSize(new Dimension(250, gridStyleComboBox.getPreferredSize().height));
        gridStyleComboBox.setSelectedItem(this.chunkGridPanel.gridStyle);
        gridStyleComboBox.addActionListener(e -> this.chunkGridPanel.gridStyle = (GridStyle) gridStyleComboBox.getSelectedItem());
        var followPlayerCheckBox = new JCheckBox("Follow player");
        followPlayerCheckBox.addItemListener(e -> {
            this.chunkXTextField.setEnabled(e.getStateChange() == ItemEvent.DESELECTED);
            this.chunkZTextField.setEnabled(e.getStateChange() == ItemEvent.DESELECTED);
            this.xTextField.setEnabled(e.getStateChange() == ItemEvent.DESELECTED);
            this.zTextField.setEnabled(e.getStateChange() == ItemEvent.DESELECTED);
            this.dimensionTextField.setEnabled(e.getStateChange() == ItemEvent.DESELECTED);
            centerOnPlayerButton.setEnabled(e.getStateChange() == ItemEvent.DESELECTED);

            CompanionApp.SERVER.getMessageProcessor().enqueueMessage(new UpdateFollowPlayerStateMessage(
                    e.getStateChange() == ItemEvent.DESELECTED ? UpdateFollowPlayerStateMessage.STATE_NONE : UpdateFollowPlayerStateMessage.STATE_FOLLOW
            ));
        });
        var overlayModeButton = new FlatIconButton(new FlatSVGIcon("icons/overlayMode.svg"), false);
        overlayModeButton.addActionListener(e -> {
            toggleOverlayMode();
        });

        this.topSettingsPanel.add(new JLabel(" Grid style: "));
        this.topSettingsPanel.add(gridStyleComboBox);
        this.topSettingsPanel.add(followPlayerCheckBox);
        this.topSettingsPanel.add(Box.createHorizontalGlue());
        this.topSettingsPanel.add(overlayModeButton);
        add(this.topSettingsPanel, BorderLayout.NORTH);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                CompanionApp.SERVER.getMessageProcessor().enqueueMessage(new ReceiveDataStateMessage(false));
            }
        });
        Runtime.getRuntime().addShutdownHook(new Thread(() -> CompanionApp.SERVER.getMessageProcessor().enqueueMessage(new ReceiveDataStateMessage(false))));

        pack();
    }

    private void toggleOverlayMode() {
        this.overlayModeEnabled = !this.overlayModeEnabled;

        var size = this.chunkGridPanel.getSize();
        dispose();
        remove(this.chunkGridPanel);
        setLayout(new BorderLayout());
        this.chunkGridPanel.setPreferredSize(size);
        add(this.chunkGridPanel, BorderLayout.CENTER);
        if (this.overlayModeEnabled) {
            setUndecorated(true);
            remove(this.bottomInputPanel);
            remove(this.topSettingsPanel);
        } else {
            setUndecorated(false);
            add(this.bottomInputPanel, BorderLayout.SOUTH);
            add(this.topSettingsPanel, BorderLayout.NORTH);
        }
        pack();
        setVisible(true);
        setAlwaysOnTop(this.overlayModeEnabled);
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

        if (!CompanionApp.SERVER.isClientConnected())
            return;

        CompanionApp.SERVER.getMessageProcessor().enqueueMessage(new ReceiveDataStateMessage(true));
        CompanionApp.SERVER.getMessageProcessor().enqueueMessage(new ChunkGridRequestInfoUpdateMessage(INSTANCE.getChunkGridRequestInfo()));
        CompanionApp.SERVER.getMessageProcessor().enqueueMessage(new UpdateFollowPlayerStateMessage(UpdateFollowPlayerStateMessage.STATE_ONCE));
        INSTANCE.setVisible(true);
        UIUtils.centerJFrame(INSTANCE);
    }

    private enum GridStyle {
        NONE("None"),
        LINES("Grid lines"),
        CHECKER_BOARD("Checker board"),
        GRADIENT("Gradient");

        private final String displayName;

        GridStyle(String displayName) {
            this.displayName = displayName;
        }

        @Override
        public String toString() {
            return this.displayName;
        }
    }

    private class ChunkGridPanel extends JPanel {

        private static final FlatSVGIcon CLOSE_ICON = new FlatSVGIcon("icons/overlayMode.svg");

        private static final Color[] COLORS = new Color[]{
                new Color(40, 40, 40),
                new Color(35, 35, 35),
                new Color(165, 65, 150),
                new Color(150, 50, 133),
                new Color(65, 115, 165),
                new Color(50, 100, 150),
                new Color(27, 115, 3),
                new Color(23, 110, 0),
                new Color(165, 165, 7),
                new Color(155, 150, 0)
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

            var panZoomMouseAdapter = new MouseAdapter() {

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

                    if (!SwingUtilities.isLeftMouseButton(e) || !new Rectangle(getWidth() - 22, 2, 20, 20).contains(e.getPoint()))
                        return;

                    ChunkGridWindow.this.toggleOverlayMode();
                }
            };

            addMouseWheelListener(panZoomMouseAdapter);
            addMouseListener(panZoomMouseAdapter);
            addMouseMotionListener(panZoomMouseAdapter);

            var windowMoveMouseAdapter = new MouseAdapter() {

                private boolean dragging;
                private int windowXOffset;
                private int windowYOffset;

                @Override
                public void mousePressed(MouseEvent e) {
                    if (!SwingUtilities.isMiddleMouseButton(e))
                        return;

                    this.dragging = true;
                    this.windowXOffset = e.getXOnScreen() - ChunkGridWindow.this.getX();
                    this.windowYOffset = e.getYOnScreen() - ChunkGridWindow.this.getY();
                }

                @Override
                public void mouseDragged(MouseEvent e) {
                    if (!SwingUtilities.isMiddleMouseButton(e) || !this.dragging)
                        return;

                    ChunkGridWindow.this.setLocation(e.getXOnScreen() - this.windowXOffset, e.getYOnScreen() - this.windowYOffset);
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    if (!SwingUtilities.isMiddleMouseButton(e))
                        return;

                    this.dragging = false;
                }
            };

            addMouseListener(windowMoveMouseAdapter);
            addMouseMotionListener(windowMoveMouseAdapter);

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
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            if (this.stateMap == null)
                return;

            var g = ((Graphics2D) graphics);

            int outerPaddingX = (getWidth() - (this.chunkRenderSize * this.chunkGridRequestInfo.getWidth())) / 2;
            int outerPaddingY = (getHeight() - (this.chunkRenderSize * this.chunkGridRequestInfo.getHeight())) / 2;

            for (int x = this.chunkGridRequestInfo.getMinChunkX(); x < this.chunkGridRequestInfo.getMaxChunkX(); x++) {
                for (int z = this.chunkGridRequestInfo.getMinChunkZ(); z < this.chunkGridRequestInfo.getMaxChunkZ(); z++) {
                    long posLong = (long) x << 32 | (z & 0xffffffffL);

                    byte b = this.stateMap.getOrDefault(posLong, (byte) 0);
                    if (b == 0 && this.gridStyle != GridStyle.CHECKER_BOARD)
                        continue;

                    int renderX = this.chunkRenderSize * (x - this.chunkGridRequestInfo.getMinChunkX()) + outerPaddingX;
                    int renderZ = this.chunkRenderSize * (z - this.chunkGridRequestInfo.getMinChunkZ()) + outerPaddingY;

                    var oldG = g;
                    g = (Graphics2D) g.create();

                    if (this.gridStyle == GridStyle.GRADIENT) {
                        g.setPaint(new GradientPaint(0, 0, COLORS[b * 2 + 1], this.chunkRenderSize, this.chunkRenderSize, COLORS[b * 2]));
                        g.setTransform(AffineTransform.getTranslateInstance(
                                renderX,
                                renderZ
                        ));
                        renderX = 0;
                        renderZ = 0;
                    } else {
                        g.setColor(COLORS[this.gridStyle == GridStyle.CHECKER_BOARD && (x % 2 == 0) == (z % 2 == 0) ? (b * 2) + 1 : (b * 2)]);
                    }

                    g.fillRect(renderX, renderZ, this.chunkRenderSize, this.chunkRenderSize);
                    g.dispose();
                    g = oldG;
                }
            }

            g.setColor(Color.BLACK);

            if (this.gridStyle == GridStyle.LINES) {
                g.setStroke(new BasicStroke(1f));
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

            //Draw overlay mode exit button
            if (ChunkGridWindow.this.overlayModeEnabled) {
                g.setClip(0, 0, getWidth(), getHeight());

                var mouseLocation = MouseInfo.getPointerInfo().getLocation();
                var relativeMouseLocation = new Point(
                        mouseLocation.x - getLocationOnScreen().x,
                        mouseLocation.y - getLocationOnScreen().y
                );

                if (new Rectangle(getWidth() - 22, 2, 20, 20).contains(relativeMouseLocation)) {
                    FlatUIUtils.setRenderingHints(g);
                    g.setColor(new Color(80, 80, 80));
                    g.fillRoundRect(getWidth() - 22, 2, 20, 20, 5, 5);
                }

                CLOSE_ICON.paintIcon(this, g, getWidth() - 20, 4);
            }
        }
    }
}
