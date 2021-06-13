package com.github.minecraft_ta.totalDebugCompanion.ui;

import com.github.minecraft_ta.totalDebugCompanion.ui.components.CodePanel;
import com.github.minecraft_ta.totalDebugCompanion.ui.components.EditorTabs;
import com.github.minecraft_ta.totalDebugCompanion.ui.components.TreeView;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.nio.file.Path;

public class MainWindow extends JFrame {

    public MainWindow(Path rootPath) {
        var root = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);

        var tree = new TreeView(rootPath);

        var tabs = new EditorTabs();

        var editor = new CodePanel();
        editor.setMinimumSize(new Dimension(100, 100));

        tabs.addTab("Test", editor);
        tabs.addTab("Test1", new JLabel("yo"));
        tabs.addTab("Tes2t", new JTextArea("yo"));
        tabs.setTabComponentAt(0, getTitlePanel(tabs, null, "Test"));

        root.setLeftComponent(tree);
        root.setRightComponent(tabs);
        root.setDividerSize(10);
        root.setDividerLocation(350);
        root.setOneTouchExpandable(true);

        this.getContentPane().add(root);

        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

    private static JPanel getTitlePanel(final JTabbedPane tabbedPane, final JPanel panel, String title)
    {
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        titlePanel.setOpaque(false);
        JLabel titleLbl = new JLabel(title);
        titleLbl.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
        titlePanel.add(titleLbl);
        JButton closeButton = new JButton("x");

        closeButton.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                tabbedPane.remove(panel);
            }
        });
        titlePanel.add(closeButton);

        return titlePanel;
    }
}
