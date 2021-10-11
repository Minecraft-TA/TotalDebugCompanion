package com.github.minecraft_ta.totalDebugCompanion.ui.components;

import com.formdev.flatlaf.extras.FlatSVGIcon;

import javax.swing.*;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class CloseButton extends JButton {

    private static final FlatSVGIcon CLOSE_ICON = new FlatSVGIcon("icons/close.svg");
    private static final FlatSVGIcon CLOSE_HOVERED_ICON = new FlatSVGIcon("icons/closeHovered.svg");

    private boolean hovered;

    public CloseButton() {
        int size = 16;
        setPreferredSize(new Dimension(size, size));
        setContentAreaFilled(false);
        setFocusable(false);
        addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                hovered = true;
            }

            public void mouseExited(MouseEvent e) {
                hovered = false;
            }
        });

        addAncestorListener(new AncestorListener() {
            @Override
            public void ancestorAdded(AncestorEvent event) {
            }

            @Override
            public void ancestorRemoved(AncestorEvent event) {
                hovered = false;
            }

            @Override
            public void ancestorMoved(AncestorEvent event) {
            }
        });
    }

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();

        if (this.hovered) {
            CLOSE_HOVERED_ICON.paintIcon(this, g, 0, 0);
        } else {
            CLOSE_ICON.paintIcon(this, g, 0, 0);
        }

        g2.dispose();
    }
}
