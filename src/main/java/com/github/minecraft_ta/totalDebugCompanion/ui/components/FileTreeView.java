package com.github.minecraft_ta.totalDebugCompanion.ui.components;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.github.minecraft_ta.totalDebugCompanion.CompanionApp;
import com.github.minecraft_ta.totalDebugCompanion.model.CodeView;
import com.github.minecraft_ta.totalDebugCompanion.util.FileUtils;

import javax.swing.*;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class FileTreeView extends JScrollPane {

    public FileTreeView(EditorTabs tabs) {
        super();

        var tree = new JTree();
        tree.setShowsRootHandles(true);
        tree.setBorder(BorderFactory.createEmptyBorder());

        tree.setModel(new DefaultTreeModel(new LazyTreeNode(new TreeItem(CompanionApp.getRootPath()))));
        tree.addTreeWillExpandListener(new TreeWillExpandListener() {
            @Override
            public void treeWillExpand(TreeExpansionEvent event) {
                var treePath = event.getPath();
                var lastComponent = treePath.getLastPathComponent();

                if (!(lastComponent instanceof LazyTreeNode treeNode))
                    return;

                if (!(treeNode.getUserObject() instanceof TreeItem))
                    return;

                loadItemsForNode(tree, treeNode);
            }

            @Override
            public void treeWillCollapse(TreeExpansionEvent event) {
            }
        });

        tree.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() != KeyEvent.VK_DELETE)
                    return;

                deleteSelectedItems(tree);
            }
        });

        tree.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                TreePath pathForRow = tree.getPathForRow(tree.getClosestRowForLocation(e.getX(), e.getY()));
                if (pathForRow == null)
                    return;

                DefaultMutableTreeNode node = (DefaultMutableTreeNode) pathForRow.getLastPathComponent();
                if (node == null)
                    return;
                TreeItem treeItem = (TreeItem) node.getUserObject();
                if (treeItem.isDirectory)
                    return;

                if (SwingUtilities.isRightMouseButton(e)) {
                    if (!tree.getSelectionModel().isPathSelected(pathForRow))
                        tree.setSelectionPath(pathForRow);
                    showPopupMenu(tree, e.getX(), e.getY());
                    return;
                }

                if (!SwingUtilities.isLeftMouseButton(e) || e.getClickCount() < 2)
                    return;

                tabs.openEditorTab(new CodeView(treeItem.path, 1));
            }
        });

        tree.setCellRenderer(new DefaultTreeCellRenderer() {
            @Override
            public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
                super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
                if (!(value instanceof LazyTreeNode treeNode))
                    return this;

                setText(((TreeItem) treeNode.getUserObject()).fileName);
                return this;
            }
        });

        //Expand root by default
        tree.collapseRow(0);
        tree.expandRow(0);
        FileUtils.startNewDirectoryWatcher(CompanionApp.getRootPath(), () -> loadItemsForNode(tree, ((LazyTreeNode) tree.getModel().getRoot())));

        this.setViewportView(tree);
    }

    private void showPopupMenu(JTree tree, int x, int y) {
        var popupMenu = new JPopupMenu();
        var deleteItem = popupMenu.add("Delete");
        deleteItem.setIcon(new FlatSVGIcon("icons/remove.svg"));
        deleteItem.addActionListener(event -> {
            deleteSelectedItems(tree);
        });

        popupMenu.show(tree, x, y);
    }

    private void loadItemsForNode(JTree tree, LazyTreeNode node) {
        CompletableFuture.supplyAsync(() -> loadItems(((TreeItem) node.getUserObject()).path)).thenAccept((items) -> {
            SwingUtilities.invokeLater(() -> {
                var selection = tree.getSelectionRows();

                node.removeAllChildren();
                items.forEach(node::add);
                ((DefaultTreeModel) tree.getModel()).nodeStructureChanged(node);

                tree.setSelectionRows(selection);
            });
        });
    }

    private List<LazyTreeNode> loadItems(Path rootPath) {
        try {
            return Files.walk(rootPath, 1)
                    .map(path -> new LazyTreeNode(new TreeItem(path)))
                    .skip(1)
                    .toList();
        } catch (IOException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    private void deleteSelectedItems(JTree tree) {
        var paths = tree.getSelectionModel().getSelectionPaths();
        if (paths == null || paths.length == 0)
            return;

        var rows = tree.getSelectionRows();

        Arrays.stream(paths)
                .map(TreePath::getLastPathComponent)
                .filter(Objects::nonNull)
                .forEach(node -> {
                    var item = (TreeItem) ((LazyTreeNode) node).getUserObject();
                    if (!item.isDirectory) {
                        try {
                            Files.deleteIfExists(item.path);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    loadItemsForNode(tree, (LazyTreeNode) ((LazyTreeNode) node).getParent());
                });

        if (rows == null || rows.length == 0)
            return;

        SwingUtilities.invokeLater(() -> {
            tree.setSelectionRow(Math.max(0, rows[0] - 1));
        });
    }

    private static final class TreeItem {

        private final Path path;
        private final boolean isDirectory;
        private final String fileName;

        private TreeItem(Path path) {
            this.path = path;
            this.isDirectory = Files.isDirectory(path);
            this.fileName = path.getFileName().toString();
        }
    }

    private static final class LazyTreeNode extends DefaultMutableTreeNode {

        public LazyTreeNode(TreeItem treeItem) {
            super(treeItem);

            if (treeItem.isDirectory)
                add(new DefaultMutableTreeNode("Loading..."));
        }
    }
}
