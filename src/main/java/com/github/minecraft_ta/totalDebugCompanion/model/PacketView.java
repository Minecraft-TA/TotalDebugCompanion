package com.github.minecraft_ta.totalDebugCompanion.model;

import com.github.minecraft_ta.totalDebugCompanion.ui.components.editors.PacketViewPanel;

import javax.swing.*;
import java.awt.*;

public class PacketView implements IEditorPanel {

    private final String packet;
    private PacketViewPanel packetViewPanel;

    public PacketView(String packet) {
        this.packet = packet;
    }

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
        if (packetViewPanel == null) {
            packetViewPanel = new PacketViewPanel(this);
        }
        return this.packetViewPanel;
    }

    @Override
    public boolean canClose() {
        return this.packetViewPanel.canClose(packet);
    }

    public String getPacket() {
        return packet;
    }
}
