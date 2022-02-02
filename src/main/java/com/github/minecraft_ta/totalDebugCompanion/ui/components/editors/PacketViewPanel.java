package com.github.minecraft_ta.totalDebugCompanion.ui.components.editors;

import com.github.minecraft_ta.totalDebugCompanion.CompanionApp;
import com.github.minecraft_ta.totalDebugCompanion.Icons;
import com.github.minecraft_ta.totalDebugCompanion.messages.packetLogger.CapturePacketMessage;
import com.github.minecraft_ta.totalDebugCompanion.messages.packetLogger.PacketContentMessage;
import com.github.minecraft_ta.totalDebugCompanion.model.PacketView;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;

public class PacketViewPanel extends JPanel {

    public PacketViewPanel(PacketView packetView) {
        setLayout(new BorderLayout(0, 0));

        //Sends a packet to the game to get the packet content
        CompanionApp.SERVER.getMessageProcessor().enqueueMessage(new CapturePacketMessage(packetView.getPacket(), false));

        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Packets");
        JTree tree = new JTree(root);
        tree.setCellRenderer(new DefaultTreeCellRenderer() {
            @Override
            public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
                super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
                setIcon(Icons.FIELD);
                return this;
            }
        });

        //Adds a packet and its content to the tree.
        CompanionApp.SERVER.getMessageBus().listenAlways(PacketContentMessage.class, message -> {
            if (message.getPacketName().equals(packetView.getPacket())) {
                DefaultMutableTreeNode packetNode = jsonToTree(JsonParser.parseString(message.getPacketData()), message.getPacketName());
                ((DefaultTreeModel) tree.getModel()).insertNodeInto(packetNode, root, root.getChildCount());
                tree.expandRow(0);
            }
        });

        add(new JScrollPane(tree));
    }

    /**
     * Converts a json string to a tree made of nodes using recursion.
     *
     * @param jsonElement The json element to convert.
     * @return The tree made of nodes.
     */
    private DefaultMutableTreeNode jsonToTree(JsonElement jsonElement, String name) {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode(name);
        if (jsonElement.isJsonObject()) {
            for (String key : jsonElement.getAsJsonObject().keySet()) {
                root.add(jsonToTree(jsonElement.getAsJsonObject().get(key), key));
            }
        } else {
            root.setUserObject(name + " = " + jsonElement.getAsString());
        }
        return root;
    }


    public boolean canClose(String packetName) {
        CompanionApp.SERVER.getMessageBus().unregister(PacketContentMessage.class, this);
        CompanionApp.SERVER.getMessageProcessor().enqueueMessage(new CapturePacketMessage(packetName, true));
        return true;
    }
}
