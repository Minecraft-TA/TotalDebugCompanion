package com.github.minecraft_ta.totalDebugCompanion.ui.components;

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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class FileTreeView extends JScrollPane {

    public FileTreeView(Path rootPath, EditorTabs tabs) {
        super();

        var tree = new JTree();
        tree.setShowsRootHandles(true);
        tree.setBorder(BorderFactory.createEmptyBorder());

        tree.setModel(new DefaultTreeModel(new LazyTreeNode(new TreeItem(rootPath))));
        tree.collapseRow(0);
        tree.addTreeWillExpandListener(new TreeWillExpandListener() {
            @Override
            public void treeWillExpand(TreeExpansionEvent event) {
                var treePath = event.getPath();
                var lastComponent = treePath.getLastPathComponent();

                if (!(lastComponent instanceof LazyTreeNode))
                    return;
                var treeNode = (LazyTreeNode) lastComponent;
                if (!(treeNode.getUserObject() instanceof TreeItem))
                    return;
                var userObject = (TreeItem) treeNode.getUserObject();

                loadItemsForNode(tree, treeNode);
            }

            @Override
            public void treeWillCollapse(TreeExpansionEvent event) {
            }
        });

        tree.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() != MouseEvent.BUTTON1 || e.getClickCount() != 2)
                    return;
                TreePath pathForRow = tree.getPathForRow(tree.getRowForLocation(e.getX(), e.getY()));
                if (pathForRow == null)
                    return;
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) pathForRow.getLastPathComponent();
                if (node == null)
                    return;
                TreeItem treeItem = (TreeItem) node.getUserObject();
                if (treeItem.isDirectory)
                    return;

                tabs.openEditorTab(new CodeView(treeItem.path));
            }
        });

        tree.setCellRenderer(new DefaultTreeCellRenderer() {
            @Override
            public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
                super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
                if (!(value instanceof LazyTreeNode))
                    return this;
                setText(((TreeItem) ((LazyTreeNode) value).getUserObject()).fileName);
                return this;
            }
        });

        FileUtils.startNewDirectoryWatcher(rootPath, () -> loadItemsForNode(tree, ((LazyTreeNode) tree.getModel().getRoot())));

        this.setViewportView(tree);
    }

    private void loadItemsForNode(JTree tree, LazyTreeNode node) {
        CompletableFuture.supplyAsync(() -> loadItems(((TreeItem) node.getUserObject()).path)).thenAccept((items) -> {
            SwingUtilities.invokeLater(() -> {
                node.removeAllChildren();
                items.forEach(node::add);
                ((DefaultTreeModel) tree.getModel()).nodeStructureChanged(node);
            });
        });
    }

    private List<LazyTreeNode> loadItems(Path rootPath) {
        try {
            var files = Files.walk(rootPath, 1)
                    .map(path -> new LazyTreeNode(new TreeItem(path)))
                    .collect(Collectors.toList());
            files.remove(0);
            return files;
        } catch (IOException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
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
