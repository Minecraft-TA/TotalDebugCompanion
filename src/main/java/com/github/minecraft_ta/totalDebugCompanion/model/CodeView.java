package com.github.minecraft_ta.totalDebugCompanion.model;

import com.formdev.flatlaf.util.StringUtils;
import com.github.minecraft_ta.totalDebugCompanion.ui.components.CodeViewPanel;
import com.github.minecraft_ta.totalDebugCompanion.util.CodeUtils;

import javax.swing.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

public class CodeView implements IEditorPanel {

    private final Path path;
    private final CodeViewPanel codeViewPanel;

    public CodeView(Path path) {
        this.path = path;
        this.codeViewPanel = new CodeViewPanel();

        CompletableFuture.runAsync(() -> {
            try {
                String code = Files.readString(this.path);
                code = StringUtils.removeTrailing(code, "\n");

                codeViewPanel.setCode(code);
                CodeUtils.highlightJavaCode(code, codeViewPanel.getEditorPane());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public String getTitle() {
        return this.path.getFileName().toString().replace(".java", "");
    }

    @Override
    public JComponent getComponent() {
        return this.codeViewPanel;
    }
}
