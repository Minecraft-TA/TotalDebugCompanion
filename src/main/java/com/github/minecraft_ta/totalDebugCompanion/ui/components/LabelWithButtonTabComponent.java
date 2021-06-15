package com.github.minecraft_ta.totalDebugCompanion.ui.components;

import com.formdev.flatlaf.extras.FlatSVGIcon;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class LabelWithButtonTabComponent extends JPanel {

    private static final FlatSVGIcon CLOSE_ICON = new FlatSVGIcon("icons/close.svg");
    private static final FlatSVGIcon CLOSE_HOVERED_ICON = new FlatSVGIcon("icons/closeHovered.svg");

    private final JTabbedPane pane;

    public LabelWithButtonTabComponent(JTabbedPane pane, Icon icon) {
        super(new FlowLayout(FlowLayout.LEFT, 0, 0));
        this.pane = pane;
        setOpaque(false);

        JLabel label = new JLabel() {
            public String getText() {
                int i = pane.indexOfTabComponent(LabelWithButtonTabComponent.this);
                if (i != -1) {
                    return pane.getTitleAt(i);
                }
                return null;
            }
        };

        add(label);
        label.setIcon(icon);
        label.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));

        JButton button = new CloseButton();
        add(button);
        setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));
    }

    private class CloseButton extends JButton implements ActionListener {

        private boolean hovered;

        public CloseButton() {
            int size = 17;
            setPreferredSize(new Dimension(size, size));
            setContentAreaFilled(false);
            setFocusable(false);
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (!SwingUtilities.isMiddleMouseButton(e))
                        return;

                    closeTab();
                }

                public void mouseEntered(MouseEvent e) {
                    var component = (CloseButton) e.getComponent();
                    component.hovered = true;
                }

                public void mouseExited(MouseEvent e) {
                    var component = (CloseButton) e.getComponent();
                    component.hovered = false;
                }
            });
            addActionListener(this);
        }

        public void actionPerformed(ActionEvent e) {
            closeTab();
        }

        private void closeTab() {
            int i = pane.indexOfTabComponent(LabelWithButtonTabComponent.this);
            if (i != -1) {
                pane.remove(i);
            }
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
}
