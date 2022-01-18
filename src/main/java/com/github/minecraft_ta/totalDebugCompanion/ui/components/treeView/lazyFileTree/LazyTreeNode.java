package com.github.minecraft_ta.totalDebugCompanion.ui.components.treeView.lazyFileTree;

import javax.swing.tree.DefaultMutableTreeNode;

public class LazyTreeNode extends DefaultMutableTreeNode {

    LazyTreeNode(TreeItem treeItem) {
        super(treeItem);

        if (treeItem.isDirectory() && !treeItem.isHiddenRoot())
            add(new DefaultMutableTreeNode("Loading..."));
    }

    @Override
    public LazyTreeNode getParent() {
        return (LazyTreeNode) super.getParent();
    }

    @Override
    public TreeItem getUserObject() {
        return (TreeItem) super.getUserObject();
    }
}
