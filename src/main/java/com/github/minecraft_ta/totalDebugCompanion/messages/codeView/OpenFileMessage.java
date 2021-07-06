package com.github.minecraft_ta.totalDebugCompanion.messages.codeView;

import com.github.minecraft_ta.totalDebugCompanion.CompanionApp;
import com.github.minecraft_ta.totalDebugCompanion.model.CodeView;
import com.github.minecraft_ta.totalDebugCompanion.ui.views.MainWindow;
import com.github.minecraft_ta.totalDebugCompanion.ui.components.EditorTabs;
import com.github.minecraft_ta.totalDebugCompanion.util.FileUtils;
import com.github.minecraft_ta.totalDebugCompanion.util.UIUtils;
import com.github.tth05.scnet.message.AbstractMessageIncoming;
import com.github.tth05.scnet.util.ByteBufferInputStream;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class OpenFileMessage extends AbstractMessageIncoming {

    private Path path;
    private int row;

    @Override
    public void read(ByteBufferInputStream messageStream) {
        this.path = Paths.get(messageStream.readString());
        this.row = messageStream.readInt();
    }

    public static void handle(OpenFileMessage message, MainWindow window) {
        if (!Files.exists(message.path) || !FileUtils.isSubPathOf(CompanionApp.getRootPath(), message.path))
            return;

        System.out.printf("Opening %s at line %d%n", message.path, message.row);

        EditorTabs editorTabs = window.getEditorTabs();
        editorTabs.getEditors().stream()
                .filter(e -> e instanceof CodeView)
                .map(e -> (CodeView) e)
                .filter(e -> e.getPath().equals(message.path))
                .findFirst().ifPresentOrElse(c -> {
                    editorTabs.setSelectedIndex(editorTabs.getEditors().indexOf(c));
                    c.focusLine(message.row);
                }, () -> {
                    editorTabs.openEditorTab(new CodeView(message.path, message.row));
                });

        UIUtils.focusWindow(window);
    }
}
