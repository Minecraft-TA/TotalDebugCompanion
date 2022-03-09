package com.github.minecraft_ta.totalDebugCompanion.model;

import com.github.minecraft_ta.totalDebugCompanion.Icons;
import com.github.minecraft_ta.totalDebugCompanion.ui.components.editors.PacketLoggerViewPanel;

import javax.swing.*;
import java.awt.*;

public class PacketLoggerView implements IEditorPanel {

    private PacketLoggerViewPanel packetLoggerViewPanel;

    @Override
    public String getTitle() {
        return "Packet Logger";
    }

    @Override
    public String getTooltip() {
        return "Packet Logger";
    }

    @Override
    public Icon getIcon() {
        return Icons.UP_DOWN;
    }

    @Override
    public Component getComponent() {
        if (this.packetLoggerViewPanel == null) {
            this.packetLoggerViewPanel = new PacketLoggerViewPanel();
        }
        return this.packetLoggerViewPanel;
    }

    @Override
    public boolean canClose() {
        return this.packetLoggerViewPanel.canClose();
    }
}
