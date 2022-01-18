package com.github.minecraft_ta.totalDebugCompanion.ui.components.treeView.lazyFileTree;

import java.util.Collection;

public abstract class DirectoryTreeItem extends TreeItem {

    DirectoryTreeItem(String name) {
        super(name, false);
    }

    public abstract Collection<TreeItem> loadChildren();

    @Override
    public final boolean isDirectory() {
        return true;
    }
}
