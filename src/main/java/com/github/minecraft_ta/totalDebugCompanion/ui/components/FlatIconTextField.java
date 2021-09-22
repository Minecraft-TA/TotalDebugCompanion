package com.github.minecraft_ta.totalDebugCompanion.ui.components;

import com.formdev.flatlaf.extras.FlatSVGIcon;

import javax.swing.*;
import java.awt.*;

public class FlatIconTextField extends JTextField {

    private final FlatSVGIcon icon;

    public FlatIconTextField(FlatSVGIcon icon) {
        this.icon = icon;
        setBorder(BorderFactory.createEmptyBorder(5, 25, 5, 5));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        var graphics = g.create();
        var insets = getBorder().getBorderInsets(this);
        this.icon.paintIcon(this, graphics, 5, insets.top);
        graphics.dispose();
    }

    public void setIconFilter(FlatSVGIcon.ColorFilter colorFilter) {
        this.icon.setColorFilter(colorFilter);
        repaint();
    }
}
