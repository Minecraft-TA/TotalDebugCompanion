package com.github.minecraft_ta.totalDebugCompanion.ui;

import com.github.minecraft_ta.totalDebugCompanion.server.CompanionAppServer;
import com.github.minecraft_ta.totalDebugCompanion.ui.components.editors.CodeViewPanel;
import com.github.minecraft_ta.totalDebugCompanion.ui.components.EditorTabs;
import com.github.minecraft_ta.totalDebugCompanion.ui.components.FileTreeView;
import com.github.minecraft_ta.totalDebugCompanion.ui.components.FileTreeViewHeader;
import com.github.minecraft_ta.totalDebugCompanion.util.UIUtils;

import javax.swing.*;
import java.awt.*;
import java.nio.file.Path;

public class MainWindow extends JFrame {

    private final EditorTabs editorTabs = new EditorTabs();
    private final Path rootPath;

    public MainWindow(Path rootPath) {
        this.rootPath = rootPath;
        var root = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);

        var tree = new FileTreeView(rootPath, editorTabs);

        var editor = new CodeViewPanel();
        editor.setMinimumSize(new Dimension(100, 100));

        root.setLeftComponent(UIUtils.verticalLayout(new FileTreeViewHeader(), tree));
        root.setRightComponent(editorTabs);
        root.setDividerSize(10);
        root.setDividerLocation(350);

        getContentPane().add(root);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setTitle("TotalDebugCompanion");

        CompanionAppServer.getInstance().run(25570, this);
    }

    public EditorTabs getEditorTabs() {
        return editorTabs;
    }

    public Path getRootPath() {
        return rootPath;
    }
}
