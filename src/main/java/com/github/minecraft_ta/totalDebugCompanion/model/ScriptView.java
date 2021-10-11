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
    protected ScriptPanel scriptPanel;

    public ScriptView(String scriptName) {
        this.path = JavaLanguageServer.SRC_DIR.resolve(scriptName + ".java");
        try {
            if (!Files.exists(this.path)) {
                this.text = """
                        public class %s extends BaseScript {
                        \t@Override
                        \tpublic void run() throws Throwable {
                        \t\t
                        \t}
                        }
                        """.formatted(scriptName);
                Files.writeString(this.path, this.text);
            } else {
                this.text = Files.readString(this.path);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean canClose() {
        return this.scriptPanel.canSave();
    }

    public String getSourceText() {
        return text;
    }

    public String getURI() {
        return this.path.toUri().toString();
    }

    public Path getPath() {
        return path;
    }

    @Override
    public String getTitle() {
        return this.path.getFileName().toString();
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
        if (this.scriptPanel == null)
            this.scriptPanel = new ScriptPanel(this);
        return this.scriptPanel;
    }
}
