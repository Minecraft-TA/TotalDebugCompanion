package com.github.minecraft_ta.totalDebugCompanion.ui.components;

import com.github.minecraft_ta.totalDebugCompanion.GlobalConfig;

import javax.swing.*;
import java.awt.*;

public class FontSizeSliderBar extends JPanel {

    public FontSizeSliderBar() {
        super(new FlowLayout(FlowLayout.RIGHT));

        add(new JLabel("Font size: "));

        JSlider slider = new JSlider(5, 30, GlobalConfig.getInstance().<Float>getValue("fontSize").intValue());
        slider.addChangeListener(event -> {
            GlobalConfig.getInstance().setValue("fontSize", (float) slider.getValue());
        });
        add(slider);

        this.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.GRAY));
    }
}
