package com.github.minecraft_ta.totalDebugCompanion.ui.components.treeView;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.github.minecraft_ta.totalDebugCompanion.CompanionApp;
import com.github.minecraft_ta.totalDebugCompanion.lsp.JavaLanguageServer;
import com.github.minecraft_ta.totalDebugCompanion.model.BaseScriptView;
import com.github.minecraft_ta.totalDebugCompanion.model.CodeView;
import com.github.minecraft_ta.totalDebugCompanion.model.ScriptView;
import com.github.minecraft_ta.totalDebugCompanion.ui.components.global.EditorTabs;
import com.github.minecraft_ta.totalDebugCompanion.ui.components.treeView.lazyFileTree.*;
import com.github.minecraft_ta.totalDebugCompanion.util.TextUtils;

import javax.swing.*;
import java.nio.file.Path;

public class FileTreeView extends JScrollPane {

    public FileTreeView(EditorTabs tabs) {
        super();

        var tree = new LazyFileJTree() {
            @Override
            protected void showPopupMenu(LazyTreeNode node, TreeItem treeItem, int x, int y) {
                var popupMenu = new JPopupMenu();
                var deleteItem = popupMenu.add("Delete");
                deleteItem.setIcon(new FlatSVGIcon("icons/remove.svg"));
                deleteItem.addActionListener(event -> deleteSelectedItems());

                popupMenu.show(this, x, y);
            }
        };

        tree.addMouseDoubleClickListener((node, item) -> {
            if (item.isDirectory() || !(item instanceof FileSystemFileItem fileItem))
                return;

            if (node.getParent().getUserObject().getName().equals("src")) {
                var name = fileItem.getName().replace(".java", "");
                if (name.equals("BaseScript"))
                    tabs.openEditorTab(new BaseScriptView(name));
                else
                    tabs.openEditorTab(new ScriptView(name));
            } else {
                tabs.openEditorTab(new CodeView(fileItem.getPath(), 1));
            }
        });

        tree.setItemFactory(new FileTreeItemFactory() {
            @Override
            public FileSystemFileItem createFileSystemFileItem(Path path) {
                var item = super.createFileSystemFileItem(path);

                var fileName = item.getName();
                var splitIndex = fileName.lastIndexOf('.', fileName.length() - ".java".length() - 1);
                if (splitIndex != -1)
                    item.setRenderedName(TextUtils.htmlHighlightString(fileName.substring(splitIndex + 1), "  ", fileName.substring(0, splitIndex)));
                return item;
            }
        });

        tree.addRootNodes(
                tree.getItemFactory().createFileSystemDirectoryItem(JavaLanguageServer.SRC_DIR, true),
                tree.getItemFactory().createFileSystemDirectoryItem(CompanionApp.getRootPath().resolve("decompiled-files"), true)
        );

        setViewportView(tree);
        setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 3));
    }
}
