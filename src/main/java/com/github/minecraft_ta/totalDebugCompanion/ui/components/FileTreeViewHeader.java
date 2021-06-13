package com.github.minecraft_ta.totalDebugCompanion.ui.components;

import javax.swing.*;
import java.awt.*;

public class FileTreeViewHeader extends JPanel {

    public FileTreeViewHeader() {
        super();
        setMaximumSize(new Dimension(10000, 30));
        setLayout(new FlowLayout(FlowLayout.LEFT));
        JLabel files = new JLabel("Files");
        files.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
        files.setFont(files.getFont().deriveFont(files.getFont().getSize() * 1.2f));
        add(files);
    }
}
