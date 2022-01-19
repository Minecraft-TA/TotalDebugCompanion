package com.github.minecraft_ta.totalDebugCompanion.ui.views;

import com.github.minecraft_ta.totalDebugCompanion.Icons;
import com.github.minecraft_ta.totalDebugCompanion.util.UIUtils;

import javax.swing.*;
import java.awt.*;

public class DownloadProgressWindow extends JFrame {

    private final JLabel progressBarLabel = new JLabel();
    {
        this.progressBarLabel.setFont(this.progressBarLabel.getFont().deriveFont(20f));
        this.progressBarLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
    }
    private final JLabel linkLabel = new JLabel();
    {
        this.linkLabel.setFont(this.linkLabel.getFont().deriveFont(12f));
        this.linkLabel.setBorder(BorderFactory.createEmptyBorder(4, 0, 6, 0));
    }
    private final JProgressBar progressBar = new JProgressBar();
    {
        this.progressBar.setMaximumSize(new Dimension(1000, 30));
    }

    public DownloadProgressWindow() {
        setTitle("Downloading...");
        setIconImage(Icons.DOWNLOAD.getImage());
        setResizable(false);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        this.progressBar.setIndeterminate(true);

        var pane = (JPanel) getContentPane();
        pane.setLayout(new BorderLayout());
        pane.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        pane.add(UIUtils.verticalLayout(this.progressBarLabel, this.linkLabel), BorderLayout.NORTH);
        pane.add(this.progressBar, BorderLayout.CENTER);
    }

    public void setText(String text) {
        this.progressBarLabel.setText(text);
        fixLabelMinSize(this.progressBarLabel);
    }

    public void setLinkText(String text) {
        this.linkLabel.setText("<html><a style=\"color: #34c3eb; text-decoration:underlined\">" + text + "</a></html>");
        fixLabelMinSize(this.linkLabel);
    }

    @Override
    public void setVisible(boolean b) {
        super.setVisible(b);
        if (b) {
            UIUtils.centerJFrame(this);
        }
    }

    private void fixLabelMinSize(JLabel label) {
        label.setMinimumSize(new Dimension(
                label.getFontMetrics(label.getFont()).stringWidth(label.getText()),
                0
        ));
        pack();
    }
}
