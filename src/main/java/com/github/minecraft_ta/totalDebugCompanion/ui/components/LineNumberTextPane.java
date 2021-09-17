package com.github.minecraft_ta.totalDebugCompanion.ui.components;

import com.formdev.flatlaf.ui.FlatUIUtils;
import com.github.minecraft_ta.totalDebugCompanion.util.DocumentChangeListener;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import java.awt.*;

public class LineNumberTextPane extends JTextPane {

    private Font currentFont;
    private int lineCount;

    public LineNumberTextPane() {
        getDocument().addDocumentListener((DocumentChangeListener) e -> {
            if (e.getType() == DocumentEvent.EventType.CHANGE)
                return;
            var newLineCount = e.getDocument().getDefaultRootElement().getElementCount();
            if (newLineCount == this.lineCount)
                return;

            var fontMetrics = getFontMetrics(currentFont);
            var lineNumberLength = (this.lineCount + "").length();
            if (lineNumberLength < 3)
                lineNumberLength = 3;
            var charsWidth = fontMetrics.stringWidth("9".repeat(lineNumberLength));

            setBorder(BorderFactory.createEmptyBorder(0, charsWidth + 14, 0, 0));
            this.lineCount = newLineCount;
        });
    }

    //Disables line wrapping
    @Override
    public boolean getScrollableTracksViewportWidth() {
        return getUI().getPreferredSize(this).width <= getParent().getSize().width;
    }

    /*@Override
    public Dimension getPreferredSize() {
        try {
            return getUI().getPreferredSize(this);
        } catch (Exception e) {
            return super.getPreferredSize();
        }
    }*/

    //Render line numbers
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g = g.create();

        var fontMetrics = getFontMetrics(currentFont);
        var charHeight = fontMetrics.getHeight();
        var maxCharWidth = fontMetrics.stringWidth((this.lineCount < 999 ? "999" : this.lineCount) + "");
        var baseY = fontMetrics.getHeight() - fontMetrics.getDescent();

        FlatUIUtils.setRenderingHints(g);
        g.setFont(currentFont);
        g.setColor(Color.GRAY);
        for (int i = 0; i < this.lineCount; i++) {
            var x = 4;
            var y = baseY + i * charHeight;
            if (!g.getClip().getBounds2D().contains(x, y - 10))
                continue;

            var label = (i + 1) + "";
            var iCharWidth = fontMetrics.stringWidth(label);
            if (iCharWidth < maxCharWidth)
                x += maxCharWidth - iCharWidth;

            g.drawString(label, x, y);
        }

        g.dispose();
    }

    @Override
    public void setFont(Font font) {
        this.currentFont = font;
    }
}
