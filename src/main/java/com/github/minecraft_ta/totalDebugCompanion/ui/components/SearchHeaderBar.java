package com.github.minecraft_ta.totalDebugCompanion.ui.components;

import com.formdev.flatlaf.extras.FlatSVGIcon;

import javax.swing.*;

public class SearchHeaderBar extends Box {

    public SearchHeaderBar() {
        super(BoxLayout.X_AXIS);
        var textField = new JTextField();
        add(textField);
        add(new JButton(new FlatSVGIcon("icons/remove.svg")));
        add(new JButton(new FlatSVGIcon("icons/remove.svg")));
    }
}
