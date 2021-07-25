package com.github.minecraft_ta.totalDebugCompanion.ui.components;

import com.formdev.flatlaf.ui.FlatUIUtils;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import java.awt.*;

public class TextFieldWithInlineLabel extends JTextField {

    private static final int LABEL_RIGHT_MARGIN = 4;

    private final String labelText;
    private final String placeholderText;

    private final int leftInset;
    private CompoundBorder border;

    public TextFieldWithInlineLabel(String labelText) {
        this(labelText, null);
    }

    public TextFieldWithInlineLabel(String labelText, String placeholderText) {
        this(null, labelText, placeholderText);
    }

    public TextFieldWithInlineLabel(String text, String labelText, String placeholderText) {
        super(text);
        this.labelText = labelText;
        this.placeholderText = placeholderText;

        this.leftInset = LABEL_RIGHT_MARGIN + getFontMetrics(getFont().deriveFont(Font.ITALIC)).stringWidth(this.labelText);
        this.border = new CompoundBorder(getBorder(), BorderFactory.createEmptyBorder(0, this.leftInset, 0, 0));

        setBorder(this.border);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(UIManager.getColor("TextField.placeholderForeground"));

        var italicFont = g.getFont().deriveFont(Font.ITALIC);
        g.setFont(italicFont);

        Insets insets = getInsets();
        FontMetrics fm = getFontMetrics(italicFont);

        int firstTwoCharWidth = fm.stringWidth("99");
        int availableTextWidth = getWidth() - this.border.getOutsideBorder().getBorderInsets(this).left - firstTwoCharWidth - insets.right - LABEL_RIGHT_MARGIN;
        int labelTextWidth = fm.stringWidth(this.labelText);

        //We only draw it as a placeholder when the two first chars of the actual text are not visible anymore
        boolean drawAsPlaceholder = availableTextWidth < labelTextWidth || isFocusOwner();

        //Remove left padding
        if (drawAsPlaceholder && this.border.getInsideBorder().getBorderInsets(this).left == this.leftInset) {
            this.border = new CompoundBorder(this.border.getOutsideBorder(), BorderFactory.createEmptyBorder(0, 0, 0, 0));
            setBorder(this.border);
        }

        if (drawAsPlaceholder && !getText().isEmpty())
            return;

        //Add back left padding
        if (!drawAsPlaceholder && this.border.getInsideBorder().getBorderInsets(this).left == 0) {
            this.border = new CompoundBorder(this.border.getOutsideBorder(), BorderFactory.createEmptyBorder(0, this.leftInset, 0, 0));
            this.setBorder(this.border);
        }

        //Default left inset
        int x = this.border.getOutsideBorder().getBorderInsets(this).left;
        int y = insets.top + fm.getAscent() + ((getHeight() - insets.top - insets.bottom - fm.getHeight()) / 2);

        // paint placeholder
        FlatUIUtils.drawString(this, g, this.placeholderText != null && drawAsPlaceholder ? this.placeholderText : this.labelText, x, y);
    }
}
