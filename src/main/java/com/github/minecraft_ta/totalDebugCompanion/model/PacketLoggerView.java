package com.github.minecraft_ta.totalDebugCompanion.model;

import com.github.minecraft_ta.totalDebugCompanion.CompanionApp;
import com.github.minecraft_ta.totalDebugCompanion.messages.packetLogger.PacketClearMessage;
import com.github.minecraft_ta.totalDebugCompanion.messages.packetLogger.PacketLoggerStateChangeMessage;
import com.github.minecraft_ta.totalDebugCompanion.ui.components.editors.PacketLoggerViewPanel;

import javax.swing.*;
import java.awt.*;

public class PacketLoggerView implements IEditorPanel {

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
        return null;
    }

    @Override
    public Component getComponent() {
        return new PacketLoggerViewPanel();
    }

    @Override
    public boolean canClose() {
        CompanionApp.SERVER.getMessageProcessor().enqueueMessage(new PacketLoggerStateChangeMessage(false, false));
        CompanionApp.SERVER.getMessageProcessor().enqueueMessage(new PacketClearMessage());
        return true;
    }
}
