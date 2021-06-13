package com.github.minecraft_ta.totalDebugCompanion.ui.components;

import javax.swing.*;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class TreeView extends JScrollPane {

    public TreeView(Path rootPath) {
        super();

        var tree = new JTree();
        tree.setShowsRootHandles(true);

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

                treeNode.removeAllChildren();
                loadItems(userObject.path).forEach(treeNode::add);
                ((DefaultTreeModel) tree.getModel()).nodeStructureChanged(treeNode);
            }

            @Override
            public void treeWillCollapse(TreeExpansionEvent event) {
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

        this.setViewportView(tree);
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
                add(new DefaultMutableTreeNode("Loading"));
        }

       /* @Override
        public boolean isLeaf() {
            return !((TreeItem) this.getUserObject()).isDirectory;
        }*/
    }
}
