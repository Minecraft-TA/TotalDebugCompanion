package com.github.minecraft_ta.totalDebugCompanion.ui.views;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.github.minecraft_ta.totalDebugCompanion.model.ScriptView;
import com.github.minecraft_ta.totalDebugCompanion.ui.components.EditorTabs;
import com.github.minecraft_ta.totalDebugCompanion.ui.components.FileTreeView;
import com.github.minecraft_ta.totalDebugCompanion.ui.components.FileTreeViewHeader;
import com.github.minecraft_ta.totalDebugCompanion.util.UIUtils;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class MainWindow extends JFrame {

    private final EditorTabs editorTabs = new EditorTabs();

    public MainWindow() {
        var root = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);

        root.setLeftComponent(UIUtils.verticalLayout(new FileTreeViewHeader(), new FileTreeView(this.editorTabs)));
        root.setRightComponent(this.editorTabs);
        root.setDividerSize(5);
        root.setDividerLocation(350);

        getContentPane().add(root);

        var menuBar = new JMenuBar();
        var toolsMenu = new JMenu("Tools");
        toolsMenu.add(new AbstractAction("Chunk Grid") {
            @Override
            public void actionPerformed(ActionEvent e) {
                ChunkGridWindow.open();
            }
        });

        var scriptMenu = new JMenu("Script");
        scriptMenu.add(new AbstractAction("New Script", new FlatSVGIcon("icons/script.svg")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                editorTabs.openEditorTab(new ScriptView());
            }
        });

        menuBar.add(toolsMenu);
        menuBar.add(scriptMenu);

        setJMenuBar(menuBar);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setTitle("TotalDebugCompanion");
    }

    public EditorTabs getEditorTabs() {
        return this.editorTabs;
    }
}
