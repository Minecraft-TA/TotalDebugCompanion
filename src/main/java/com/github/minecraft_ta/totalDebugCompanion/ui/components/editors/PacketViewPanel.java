package com.github.minecraft_ta.totalDebugCompanion.ui.components.editors;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.github.minecraft_ta.totalDebugCompanion.CompanionApp;
import com.github.minecraft_ta.totalDebugCompanion.Icons;
import com.github.minecraft_ta.totalDebugCompanion.messages.packetLogger.CapturePacketMessage;
import com.github.minecraft_ta.totalDebugCompanion.messages.packetLogger.PacketContentMessage;
import com.github.minecraft_ta.totalDebugCompanion.model.PacketView;
import com.github.minecraft_ta.totalDebugCompanion.util.TextUtils;
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
                if (value instanceof DefaultMutableTreeNode && ((DefaultMutableTreeNode) value).getUserObject() instanceof TreeItem) {
                    setIcon(((TreeItem) ((DefaultMutableTreeNode) value).getUserObject()).getType().getIcon());
                }
                return this;
            }
        });

        //Adds a packet and its content to the tree.
        CompanionApp.SERVER.getMessageBus().listenAlways(PacketContentMessage.class, this, message -> {
            if (message.getPacketName().equals(packetView.getPacket())) {
                String packetName = TextUtils.htmlHighlightString(message.getPacketName(), " ", "channel: " + message.getChannel());
                DefaultMutableTreeNode packetNode = jsonToTree(JsonParser.parseString(message.getPacketData()), packetName, Type.VALUE);
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
    private DefaultMutableTreeNode jsonToTree(JsonElement jsonElement, String name, Type type) {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode(new TreeItem(name, type));
        if (jsonElement.isJsonObject()) {
            for (String key : jsonElement.getAsJsonObject().keySet()) {
                root.add(jsonToTree(jsonElement.getAsJsonObject().get(key), key, Type.FIELD));
            }
        } else if (jsonElement.isJsonArray()) {
            int i = 0;
            for (JsonElement element : jsonElement.getAsJsonArray()) {
                root.add(jsonToTree(element, String.valueOf(i), Type.VALUE));
                i++;
            }
            root.setUserObject(new TreeItem(TextUtils.htmlHighlightString(name, " ", "size = " + jsonElement.getAsJsonArray().size()), Type.ARRAY));
        } else {
            root.setUserObject(new TreeItem(name + " = " + jsonElement.getAsString(), Type.PRIMITIVE));
        }
        return root;
    }

    public boolean canClose(String packetName) {
        CompanionApp.SERVER.getMessageBus().unregister(PacketContentMessage.class, this);
        CompanionApp.SERVER.getMessageProcessor().enqueueMessage(new CapturePacketMessage(packetName, true));
        return true;
    }

    private enum Type {
        FIELD(Icons.FIELD),
        PRIMITIVE(Icons.PRIMITIVE),
        VALUE(Icons.VALUE),
        ARRAY(Icons.ARRAY);

        private final FlatSVGIcon icon;

        Type(FlatSVGIcon icon) {
            this.icon = icon;
        }

        public Icon getIcon() {
            return icon;
        }
    }

    private static final class TreeItem {

        private final Type type;
        private final String value;

        public TreeItem(String value, Type icon) {
            this.value = value;
            this.type = icon;
        }

        public String getValue() {
            return value;
        }

        public Type getType() {
            return this.type;
        }

        @Override
        public String toString() {
            return value;
        }

    }
}
