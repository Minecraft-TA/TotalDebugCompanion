package com.github.minecraft_ta.totalDebugCompanion.model;

import com.github.minecraft_ta.totalDebugCompanion.ui.components.editors.PacketViewPanel;

import javax.swing.*;
import java.awt.*;

public record PacketView(String packet) implements IEditorPanel {

    @Override
    public String getTitle() {
        return packet;
    }

    @Override
    public String getTooltip() {
        return packet;
    }

    @Override
    public Icon getIcon() {
        return null;
    }

    @Override
    public Component getComponent() {
        return new PacketViewPanel(this);
    }

}
