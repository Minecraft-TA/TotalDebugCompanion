package com.github.minecraft_ta.totalDebugCompanion.ui.components.editors;

import com.github.minecraft_ta.totalDebugCompanion.GlobalConfig;
import com.github.minecraft_ta.totalDebugCompanion.ui.components.LineNumberTextPane;
import com.github.minecraft_ta.totalDebugCompanion.ui.components.global.BottomInformationBar;
import com.github.minecraft_ta.totalDebugCompanion.util.UIUtils;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.io.IOException;
import java.util.Objects;
import java.util.stream.IntStream;

public class AbstractCodeViewPanel extends JPanel {

    public static Font JETBRAINS_MONO_FONT = null;
    static {
        try {
            JETBRAINS_MONO_FONT = Font.createFont(Font.TRUETYPE_FONT, Objects.requireNonNull(CodeViewPanel.class.getResourceAsStream("/font/jetbrainsmono_regular.ttf"), "Unable to load font"));
            GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(JETBRAINS_MONO_FONT);
        } catch (FontFormatException | IOException e) {
            e.printStackTrace();
        }
    }

    protected final JScrollPane editorScrollPane = new JScrollPane();
    protected final LineNumberTextPane editorPane = new LineNumberTextPane();
    protected final BottomInformationBar bottomInformationBar = new BottomInformationBar();

    protected JComponent headerComponent;

    public AbstractCodeViewPanel() {
        super(new BorderLayout());

        this.editorScrollPane.setViewportView(this.editorPane);
        this.editorScrollPane.setBorder(BorderFactory.createEmptyBorder());

        add(this.editorScrollPane, BorderLayout.CENTER);
        add(this.bottomInformationBar, BorderLayout.SOUTH);

        //Scrolling and fonts
        updateFonts();
        updateScrollBars();
        GlobalConfig.getInstance().addPropertyChangeListener("scrollMul", event -> updateScrollBars());
        GlobalConfig.getInstance().addPropertyChangeListener("fontSize", event -> updateFonts());
    }

    public void focusLine(int line) {
        SwingUtilities.invokeLater(() -> {
            var verticalScrollBar = this.editorScrollPane.getVerticalScrollBar();
            verticalScrollBar.setValue((int) ((line - 1) * ((double) verticalScrollBar.getMaximum() / this.editorPane.getDocument().getDefaultRootElement().getElementCount())));
        });
    }

    public void focusRange(int offsetStart, int offsetEnd) {
        try {
            var rect = this.editorPane.modelToView2D(offsetStart);
            var viewport = this.editorScrollPane.getViewport();

            var viewSize = viewport.getViewSize();
            var extentSize = viewport.getExtentSize();

            int rangeWidth = UIUtils.getFontWidth(this.editorPane, "9".repeat(offsetEnd - offsetStart));
            int x = (int) Math.max(0, rect.getX() - ((extentSize.width - rangeWidth) / 2f));
            x = Math.min(x, viewSize.width - extentSize.width);
            int y = (int) Math.max(0, rect.getY() - ((extentSize.height - rect.getHeight()) / 2f));
            y = Math.min(y, viewSize.height - extentSize.height);

            viewport.setViewPosition(new Point(x, y));
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    public void setHeaderComponent(JComponent component) {
        removeHeaderComponent();
        this.headerComponent = component;
        add(component, BorderLayout.NORTH);
        revalidate();
        repaint();
        SwingUtilities.invokeLater(() -> {
            //Adjust scroll bar to keep it in place
            var verticalScrollBar = editorScrollPane.getVerticalScrollBar();
            verticalScrollBar.setValue((int) (verticalScrollBar.getValue() + component.getPreferredSize().getHeight()));
        });
    }

    public void removeHeaderComponent() {
        if (this.headerComponent == null)
            return;

        synchronized (getTreeLock()) {
            Component component = getComponent(getComponentCount() - 1);
            if (this.headerComponent == component) {
                remove(getComponentCount() - 1);
                revalidate();
                repaint();
                //Adjust scroll bar to keep it in place
                var verticalScrollBar = editorScrollPane.getVerticalScrollBar();
                verticalScrollBar.setValue(verticalScrollBar.getValue() - component.getHeight());
            }
        }

        this.headerComponent = null;
    }

    protected void updateFonts() {
        var newFont = JETBRAINS_MONO_FONT.deriveFont(GlobalConfig.getInstance().<Float>getValue("fontSize"));
        this.editorPane.setFont(newFont);

        var set = new SimpleAttributeSet();
        StyleConstants.setFontFamily(set, JETBRAINS_MONO_FONT.getFamily());
        StyleConstants.setFontSize(set, GlobalConfig.getInstance().<Float>getValue("fontSize").intValue());

        var tabWidth = this.editorPane.getFontMetrics(newFont).stringWidth("    ");
        StyleConstants.setTabSet(set,
                new TabSet(IntStream.range(1, 12)
                        .map(i -> i * tabWidth)
                        .mapToObj(i -> new TabStop(i, TabStop.ALIGN_LEFT, TabStop.LEAD_NONE)).toArray(TabStop[]::new))
        );
        this.editorPane.getStyledDocument().setParagraphAttributes(0, this.editorPane.getStyledDocument().getLength(), set, false);
    }

    private void updateScrollBars() {
        FontMetrics metrics = getFontMetrics(this.editorPane.getFont());
        int lineHeight = metrics.getHeight();
        int charWidth = metrics.getMaxAdvance();

        JScrollBar systemVBar = new JScrollBar(JScrollBar.VERTICAL);
        JScrollBar systemHBar = new JScrollBar(JScrollBar.HORIZONTAL);
        int verticalIncrement = systemVBar.getUnitIncrement();
        int horizontalIncrement = systemHBar.getUnitIncrement();

        float mul = GlobalConfig.getInstance().<Float>getValue("scrollMul");

        this.editorScrollPane.getVerticalScrollBar().setUnitIncrement((int) (lineHeight * verticalIncrement * mul));
        this.editorScrollPane.getHorizontalScrollBar().setUnitIncrement((int) (charWidth * horizontalIncrement * mul));
    }
}
