package com.github.minecraft_ta.totalDebugCompanion.ui.components;

import com.github.minecraft_ta.totalDebugCompanion.GlobalConfig;

import javax.swing.*;
import java.awt.*;

public class FontSizeSliderBar extends JPanel {

    public FontSizeSliderBar() {
        super();
        setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));

        add(Box.createHorizontalGlue());
        add(new JLabel("Font size: "));

        JSlider slider = new JSlider(10, 30, GlobalConfig.getInstance().<Float>getValue("fontSize").intValue());
        slider.setMaximumSize(new Dimension(30, (int) slider.getPreferredSize().getHeight()));
        slider.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
        slider.addChangeListener(event -> {
            GlobalConfig.getInstance().setValue("fontSize", (float) slider.getValue());
        });
        add(slider);

        setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.GRAY));
    }
}
