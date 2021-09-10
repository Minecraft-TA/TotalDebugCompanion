package com.github.minecraft_ta.totalDebugCompanion.ui.components.editors;

import com.github.minecraft_ta.totalDebugCompanion.GlobalConfig;
import com.github.minecraft_ta.totalDebugCompanion.ui.components.FontSizeSliderBar;
import com.github.minecraft_ta.totalDebugCompanion.util.UIUtils;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import java.awt.*;
import java.io.IOException;
import java.util.Objects;

public class AbstractCodeViewPanel extends JPanel {

    public static Font JETBRAINS_MONO_FONT = null;
    static {
        try {
            JETBRAINS_MONO_FONT = Font.createFont(Font.TRUETYPE_FONT, Objects.requireNonNull(CodeViewPanel.class.getResourceAsStream("/font/jetbrainsmono_regular.ttf"), "Unable to load font"));
        } catch (FontFormatException | IOException e) {
            e.printStackTrace();
        }
    }

    protected final JScrollPane scrollPane = new JScrollPane();
    protected final JTextPane editorPane = new JTextPane();
    protected final JTextArea lineNumbers = new JTextArea();

    protected int lineCount;
    protected JComponent headerComponent;

    public AbstractCodeViewPanel() {
        super(new BorderLayout());

        this.lineNumbers.setEditable(false);
        this.lineNumbers.setForeground(Color.GRAY);
        this.lineNumbers.setHighlighter(null);

        this.scrollPane.setViewportView(UIUtils.horizontalLayout(this.lineNumbers, this.editorPane));
        this.scrollPane.setBorder(BorderFactory.createEmptyBorder());
        add(this.scrollPane, BorderLayout.CENTER);
        add(new FontSizeSliderBar(), BorderLayout.SOUTH);

        //Scrolling and fonts
        updateFonts();
        updateScrollBars();
        GlobalConfig.getInstance().addPropertyChangeListener("scrollMul", event -> updateScrollBars());
        GlobalConfig.getInstance().addPropertyChangeListener("fontSize", event -> updateFonts());
    }

    public void focusLine(int line) {
        var verticalScrollBar = this.scrollPane.getVerticalScrollBar();
        verticalScrollBar.setValue((line - 1) * (verticalScrollBar.getMaximum() / this.lineCount));
    }

    public void focusRange(int offsetStart, int offsetEnd) {
        try {
            var rect = this.editorPane.modelToView2D(offsetStart);
            var viewport = this.scrollPane.getViewport();

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
            var verticalScrollBar = scrollPane.getVerticalScrollBar();
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
                var verticalScrollBar = scrollPane.getVerticalScrollBar();
                verticalScrollBar.setValue(verticalScrollBar.getValue() - component.getHeight());
            }
        }

        this.headerComponent = null;
    }

    protected void updateLineNumbers() {
        this.lineCount = this.editorPane.getDocument().getDefaultRootElement().getElementCount();
        int lineNumberLength = (this.lineCount + "").length();

        var lineNumberTextBuilder = new StringBuilder();
        for (int i = 0; i < this.lineCount; i++) {
            lineNumberTextBuilder.append(String.format("%" + lineNumberLength + "d", i + 1));

            if (i != this.lineCount - 1)
                lineNumberTextBuilder.append("\n");
        }

        this.lineNumbers.setText(lineNumberTextBuilder.toString());
        //Adjust max width, otherwise it will take up too much space
        int charsWidth = UIUtils.getFontWidth(this.lineNumbers, "9".repeat(lineNumberLength));
        this.lineNumbers.setColumns(lineNumberLength);
        this.lineNumbers.setMaximumSize(new Dimension(charsWidth, Integer.MAX_VALUE));
    }

    private void updateFonts() {
        this.editorPane.setFont(JETBRAINS_MONO_FONT.deriveFont(GlobalConfig.getInstance().<Float>getValue("fontSize")));
        this.lineNumbers.setFont(this.editorPane.getFont());
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

        this.scrollPane.getVerticalScrollBar().setUnitIncrement((int) (lineHeight * verticalIncrement * mul));
        this.scrollPane.getHorizontalScrollBar().setUnitIncrement((int) (charWidth * horizontalIncrement * mul));
    }
}
