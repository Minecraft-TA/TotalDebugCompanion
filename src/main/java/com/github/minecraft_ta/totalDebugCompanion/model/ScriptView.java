package com.github.minecraft_ta.totalDebugCompanion.model;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.github.minecraft_ta.totalDebugCompanion.lsp.JavaLanguageServer;
import com.github.minecraft_ta.totalDebugCompanion.ui.components.editors.ScriptPanel;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ScriptView implements IEditorPanel {

    private final String text;
    private final Path path;

    public ScriptView() {
        this.path = JavaLanguageServer.SRC_DIR.resolve("Script.java");
        try {
            if (!Files.exists(this.path))
                Files.createFile(this.path);

            this.text = Files.readString(this.path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String getSourceText() {
        return text;
    }

    public String getURI() {
        return this.path.toUri().toString();
    }

    @Override
    public String getTitle() {
        return "Script";
    }

    @Override
    public String getTooltip() {
        return null;
    }

    @Override
    public Icon getIcon() {
        return new FlatSVGIcon("icons/script.svg");
    }

    @Override
    public Component getComponent() {
        return new ScriptPanel(this);
    }
}
