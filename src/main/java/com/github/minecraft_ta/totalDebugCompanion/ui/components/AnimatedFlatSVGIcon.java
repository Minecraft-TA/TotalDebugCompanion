package com.github.minecraft_ta.totalDebugCompanion.ui.components;

import com.formdev.flatlaf.extras.FlatSVGIcon;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class AnimatedFlatSVGIcon implements Icon {

    private Component component;
    private int currentIndex;

    private final FlatSVGIcon[] icons;
    private Timer timer = null;

    public AnimatedFlatSVGIcon(String folderName) {
        List<FlatSVGIcon> icons = new ArrayList<>();

        int i = 1;
        while (AnimatedFlatSVGIcon.class.getClassLoader().getResource(folderName + "/step_" + i + ".svg") != null) {
            var icon = new FlatSVGIcon(folderName + "/step_" + i + ".svg");
            icons.add(icon);
            i++;
        }

        if (icons.size() < 1)
            throw new IllegalArgumentException();

        this.icons = icons.toArray(new FlatSVGIcon[0]);

        this.timer = new Timer(100, e -> {
            System.out.println("timer");
            if (this.component == null) {
                this.timer.stop();
            } else {
                this.component.repaint();
                this.component = null;
            }
        });
        this.timer.start();
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        if (this.component == null)
            this.component = c;

        this.icons[this.currentIndex].paintIcon(c, g, x, y);
        this.currentIndex++;

        if (this.currentIndex >= this.icons.length)
            this.currentIndex = 0;
    }

    @Override
    public int getIconWidth() {
        return icons[0].getIconWidth();
    }

    @Override
    public int getIconHeight() {
        return icons[0].getIconHeight();
    }
}
