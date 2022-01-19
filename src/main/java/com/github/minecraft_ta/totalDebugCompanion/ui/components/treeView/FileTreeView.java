package com.github.minecraft_ta.totalDebugCompanion.ui.components.treeView;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.github.minecraft_ta.totalDebugCompanion.CompanionApp;
import com.github.minecraft_ta.totalDebugCompanion.lsp.JavaLanguageServer;
import com.github.minecraft_ta.totalDebugCompanion.messages.codeView.DecompileAndOpenRequestMessage;
import com.github.minecraft_ta.totalDebugCompanion.model.BaseScriptView;
import com.github.minecraft_ta.totalDebugCompanion.model.CodeView;
import com.github.minecraft_ta.totalDebugCompanion.model.ScriptView;
import com.github.minecraft_ta.totalDebugCompanion.ui.components.global.EditorTabs;
import com.github.minecraft_ta.totalDebugCompanion.ui.components.treeView.lazyFileTree.*;
import com.github.minecraft_ta.totalDebugCompanion.util.TextUtils;

import javax.swing.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class FileTreeView extends JScrollPane {

    private static final Path MODS_PATH = CompanionApp.getRootPath().getParent().getParent().resolve("mods");

    public FileTreeView(EditorTabs tabs) {
        super();

        var tree = new LazyFileJTree() {
            @Override
            protected void showPopupMenu(LazyTreeNode node, TreeItem treeItem, int x, int y) {
                if (!(treeItem instanceof FileSystemFileItem))
                    return;

                var popupMenu = new JPopupMenu();
                var deleteItem = popupMenu.add("Delete");
                deleteItem.setIcon(new FlatSVGIcon("icons/remove.svg"));
                deleteItem.addActionListener(event -> deleteSelectedItems());

                popupMenu.show(this, x, y);
            }
        };

        tree.addMouseDoubleClickListener((node, item) -> {
            if (item.isDirectory())
                return;

            if (item instanceof FileSystemFileItem fileItem) {
                if (node.getParent().getUserObject().getName().equals("src")) {
                    var name = fileItem.getName().replace(".java", "");
                    if (name.equals("BaseScript"))
                        tabs.openEditorTab(new BaseScriptView(name));
                    else
                        tabs.openEditorTab(new ScriptView(name));
                } else {
                    tabs.openEditorTab(new CodeView(fileItem.getPath(), 1));
                }
            } else if (item instanceof ZipFileRootItem.Entry) {
                if (!item.getName().endsWith(".class"))
                    return;

                //Reconstruct the class name
                StringBuilder fullName = new StringBuilder(item.getName().substring(0, item.getName().length() - 6));
                while (!((node = node.getParent()).getUserObject() instanceof ZipFileRootItem)) {
                    fullName.insert(0, '.').insert(0, node.getUserObject().getName());
                }
                CompanionApp.SERVER.getMessageProcessor().enqueueMessage(new DecompileAndOpenRequestMessage(fullName.toString()));
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
                tree.getItemFactory().createFileSystemDirectoryItem(CompanionApp.getRootPath().resolve("decompiled-files"), true),
                new DirectoryTreeItem("mods") {
                    @Override
                    public List<TreeItem> loadChildren() {
                        try {
                            return Files.walk(MODS_PATH, 1)
                                    .skip(1)
                                    .filter(p -> p.getFileName().toString().endsWith(".jar"))
                                    .<TreeItem>map(ZipFileRootItem::new)
                                    .toList();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
        );

        setViewportView(tree);
        setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 3));
    }
}
