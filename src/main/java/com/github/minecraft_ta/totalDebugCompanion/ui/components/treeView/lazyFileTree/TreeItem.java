package com.github.minecraft_ta.totalDebugCompanion.ui.components.treeView.lazyFileTree;

import javax.swing.*;

public class TreeItem {

    private final boolean isHiddenRoot;
    private final String name;
    private String renderedName;

    protected TreeItem(String name) {
        this(name, false);
    }

    TreeItem(String name, boolean isHiddenRoot) {
        this.name = name;
        this.isHiddenRoot = isHiddenRoot;
    }

    public void delete() {

    }

    public boolean isHiddenRoot() {
        return isHiddenRoot;
    }

    public boolean isDirectory() {
        return false;
    }

    public String getName() {
        return name;
    }

    public void setRenderedName(String renderedName) {
        this.renderedName = renderedName;
    }

    public String getRenderedName() {
        if (this.renderedName == null)
            return this.name;
        return renderedName;
    }

    public Icon getIcon() {
        return null;
    }
}
