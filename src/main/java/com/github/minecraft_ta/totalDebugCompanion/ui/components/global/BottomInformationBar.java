package com.github.minecraft_ta.totalDebugCompanion.ui.components.global;

import com.github.minecraft_ta.totalDebugCompanion.GlobalConfig;
import com.github.minecraft_ta.totalDebugCompanion.Icons;
import com.github.minecraft_ta.totalDebugCompanion.ui.components.AnimatedFlatSVGIcon;

import javax.swing.*;
import java.awt.*;

public class BottomInformationBar extends JPanel {

    private final JLabel infoLabel = new JLabel();

    public BottomInformationBar() {
        super();
        setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));

        add(this.infoLabel);
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

    public void setDefaultInfoText(String text, Color color) {
        this.infoLabel.setText(text);
        this.infoLabel.setForeground(color);
    }

    public void setDefaultInfoText(String text) {
        this.infoLabel.setIcon(Icons.INFORMATION);
        this.infoLabel.setText(text);
    }

    public void setProcessInfoText(String text) {
        this.infoLabel.setIcon(new AnimatedFlatSVGIcon("icons/process"));
        this.infoLabel.setText(text);
    }

    public void setSuccessInfoText(String text) {
        this.infoLabel.setIcon(Icons.SUCCESS);
        this.infoLabel.setText(text);
    }

    public void setFailureInfoText(String text) {
        this.infoLabel.setIcon(Icons.ERROR);
        this.infoLabel.setText(text);
    }

    public void clearInfoText() {
        this.infoLabel.setIcon(null);
        setDefaultInfoText("", new Color(187, 187, 187));
    }
}
