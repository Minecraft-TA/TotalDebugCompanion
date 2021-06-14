package com.github.minecraft_ta.totalDebugCompanion.model;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.formdev.flatlaf.util.StringUtils;
import com.github.minecraft_ta.totalDebugCompanion.ui.components.CodeViewPanel;
import com.github.minecraft_ta.totalDebugCompanion.ui.components.FontSizeSliderBar;
import com.github.minecraft_ta.totalDebugCompanion.util.CodeUtils;
import com.github.minecraft_ta.totalDebugCompanion.util.UIUtils;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

public class CodeView implements IEditorPanel {

    private static final Icon CLASS_ICON = new FlatSVGIcon("icons/class.svg");

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
        String fullClassName = this.path.getFileName().toString().replace(".java", "");
        return fullClassName.substring(fullClassName.lastIndexOf('.') + 1);
    }

    @Override
    public Icon getIcon() {
        return CLASS_ICON;
    }

    @Override
    public String getTooltip() {
        return this.path.getFileName().toString();
    }

    @Override
    public Component getComponent() {
        return UIUtils.topAndBottomStickyLayout(this.codeViewPanel, new FontSizeSliderBar());
    }
}
