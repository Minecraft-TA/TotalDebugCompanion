package com.github.minecraft_ta.totalDebugCompanion.model;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.github.minecraft_ta.totalDebugCompanion.ui.components.editors.ScriptPanel;

import javax.swing.*;
import java.awt.*;

public class ScriptView implements IEditorPanel {

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
