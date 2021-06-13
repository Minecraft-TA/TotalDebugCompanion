package com.github.minecraft_ta.totalDebugCompanion.util;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class LabelWithButtonTabComponent extends JPanel {
    private final JTabbedPane pane;

    public LabelWithButtonTabComponent(final JTabbedPane pane) {
        super(new FlowLayout(FlowLayout.LEFT, 0, 0));
        if (pane == null)
            throw new NullPointerException("TabbedPane is null");

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
                public void mouseEntered(MouseEvent e) {
                    Component component = e.getComponent();
                    if (component instanceof CloseButton) {
                        CloseButton button = (CloseButton) component;
                        button.hovered = true;
                    }
                }

                public void mouseExited(MouseEvent e) {
                    Component component = e.getComponent();
                    if (component instanceof CloseButton) {
                        CloseButton button = (CloseButton) component;
                        button.hovered = false;
                    }
                }
            });
            addActionListener(this);
        }

        public void actionPerformed(ActionEvent e) {
            int i = pane.indexOfTabComponent(LabelWithButtonTabComponent.this);
            if (i != -1) {
                pane.remove(i);
            }
        }

        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();

            if (this.hovered) {
                g2.setColor(new Color(10, 10, 10, 50));
                g2.fillOval(0, 0, getWidth() - 1, getHeight() - 1);
            }

            g2.setStroke(new BasicStroke(2));
            if(this.hovered) {
                g2.setColor(Color.WHITE);
            } else {
                g2.setColor(Color.LIGHT_GRAY);
            }
            int delta = 6;
            g2.drawLine(delta, delta, getWidth() - delta - 1, getHeight() - delta - 1);
            g2.drawLine(getWidth() - delta - 1, delta, delta, getHeight() - delta - 1);
            g2.dispose();
        }
    }
}
