package com.github.minecraft_ta.totalDebugCompanion.ui.components.treeView.lazyFileTree;

import com.github.minecraft_ta.totalDebugCompanion.util.FileUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;

public class FileSystemDirectoryItem extends DirectoryTreeItem {

    private final LazyFileJTree tree;
    private final Path path;

    FileSystemDirectoryItem(LazyFileJTree lazyFileJTree, Path path, boolean watch) {
        super(path.getFileName().toString());
        this.tree = lazyFileJTree;
        if (!Files.isDirectory(path))
            throw new IllegalArgumentException("Not a directory");

        this.path = path;

        if (watch) {
            FileUtils.startNewDirectoryWatcher(path, () -> lazyFileJTree.loadItemsForTopLevelItem(this));
        }
    }

    @Override
    public Collection<TreeItem> loadChildren() {
        try {
            return Files.walk(this.path, 1)
                    .map(path -> {
                        if (Files.isDirectory(path))
                            return tree.getItemFactory().createFileSystemDirectoryItem(path, false);
                        return tree.getItemFactory().createFileSystemFileItem(path);
                    })
                    .skip(1)
                    .toList();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
