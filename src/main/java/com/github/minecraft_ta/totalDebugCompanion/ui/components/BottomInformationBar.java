package com.github.minecraft_ta.totalDebugCompanion.ui.components;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.github.minecraft_ta.totalDebugCompanion.GlobalConfig;

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

    public void setInfoText(String text) {
        this.infoLabel.setText(text);
    }

    public void setInfoText(String text, Color color) {
        this.infoLabel.setText(text);
        this.infoLabel.setForeground(color);
    }

    public void setProcessInfoText(String text) {
        this.infoLabel.setIcon(new AnimatedFlatSVGIcon("icons/process"));
        setInfoText(text);
    }

    public void setSuccessInfoText(String text) {
        this.infoLabel.setIcon(new FlatSVGIcon("icons/success.svg"));
        setInfoText(text);
    }

    public void setFailureInfoText(String text) {
        this.infoLabel.setIcon(new FlatSVGIcon("icons/error.svg"));
        setInfoText(text);
    }

    public void clearInfoText() {
        this.infoLabel.setIcon(null);
        setInfoText("", new Color(187, 187, 187));
    }
}
