package com.github.minecraft_ta.totalDebugCompanion.ui.components;

import javax.swing.*;
import java.awt.*;

public class FileTreeViewHeader extends JPanel {

    public FileTreeViewHeader() {
        super();
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        setMaximumSize(new Dimension(10000, 3));
        JLabel files = new JLabel("Files");
        files.setBorder(BorderFactory.createEmptyBorder(3, 10, 3, 0));
        files.setFont(files.getFont().deriveFont(files.getFont().getSize() * 1.2f));
        add(files);
    }
}
