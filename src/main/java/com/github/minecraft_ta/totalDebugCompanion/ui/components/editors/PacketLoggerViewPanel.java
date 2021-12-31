package com.github.minecraft_ta.totalDebugCompanion.ui.components.editors;

import com.github.minecraft_ta.totalDebugCompanion.CompanionApp;
import com.github.minecraft_ta.totalDebugCompanion.messages.packetLogger.IncomingPacketsMessage;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Map;

public class PacketLoggerViewPanel extends JPanel {

    /**
     * Creates a JTable with two columns: Packet Name and amount of times it was received.
     * Updates the table every time a new packet is received using the companion app's message bus inside the lambda.
     */
    public PacketLoggerViewPanel() {
        setLayout(new BorderLayout(0, 0));
        JTable table = new JTable();
        table.setModel(new DefaultTableModel(new Object[][]{}, new String[]{"Packet Name", "Amount"}) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        });
        table.setAutoCreateRowSorter(true);
        table.setFillsViewportHeight(true);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        table.setGridColor(getBackground());

        CompanionApp.SERVER.getMessageBus().listenAlways(IncomingPacketsMessage.class, incomingPacketsMessage -> {
            //Updates the table every time a new packet is received with the data from the map in the message
            Map<String, Integer> incomingPackets = incomingPacketsMessage.getIncomingPackets();
            for (int i = 0; i < table.getRowCount(); i++) {
                String value = (String) table.getValueAt(i, 0);
                if (incomingPackets.containsKey(value)) {
                    table.setValueAt(incomingPackets.remove(value), i, 1);
                }
            }

            //Adds any new packets to the table
            for (Map.Entry<String, Integer> entry : incomingPackets.entrySet()) {
                ((DefaultTableModel) table.getModel()).addRow(new Object[]{entry.getKey(), entry.getValue()});
            }
        });
        add(new JScrollPane(table));
    }
}
