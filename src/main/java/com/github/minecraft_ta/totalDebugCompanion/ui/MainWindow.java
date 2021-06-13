package com.github.minecraft_ta.totalDebugCompanion.ui;

import com.github.minecraft_ta.totalDebugCompanion.ui.components.CodeViewPanel;
import com.github.minecraft_ta.totalDebugCompanion.ui.components.EditorTabs;
import com.github.minecraft_ta.totalDebugCompanion.ui.components.FileTreeView;
import com.github.minecraft_ta.totalDebugCompanion.ui.components.FileTreeViewHeader;
import com.github.minecraft_ta.totalDebugCompanion.util.UIUtils;

import javax.swing.*;
import java.awt.*;
import java.nio.file.Path;

public class MainWindow extends JFrame {

    public MainWindow(Path rootPath) {
        var root = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);

        var tabs = new EditorTabs();
        var tree = new FileTreeView(rootPath, tabs);

        var editor = new CodeViewPanel();
        editor.setMinimumSize(new Dimension(100, 100));

        root.setLeftComponent(UIUtils.verticalLayout(new FileTreeViewHeader(), tree));
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
        titlePanel.setBorder(BorderFactory.createEmptyBorder());
        titlePanel.setOpaque(false);
        JLabel titleLbl = new JLabel(title);
        titleLbl.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
        titlePanel.add(titleLbl);

        return titlePanel;
    }
}
