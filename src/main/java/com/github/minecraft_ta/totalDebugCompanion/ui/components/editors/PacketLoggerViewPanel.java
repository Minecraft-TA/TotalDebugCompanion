package com.github.minecraft_ta.totalDebugCompanion.ui.components.editors;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.formdev.flatlaf.extras.components.FlatComboBox;
import com.github.minecraft_ta.totalDebugCompanion.CompanionApp;
import com.github.minecraft_ta.totalDebugCompanion.messages.packetLogger.IncomingPacketsMessage;
import com.github.minecraft_ta.totalDebugCompanion.messages.packetLogger.OutgoingPacketsMessage;
import com.github.minecraft_ta.totalDebugCompanion.messages.packetLogger.PacketClearMessage;
import com.github.minecraft_ta.totalDebugCompanion.messages.packetLogger.PacketLoggerStateChangeMessage;
import com.github.minecraft_ta.totalDebugCompanion.ui.components.FlatIconButton;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class PacketLoggerViewPanel extends JPanel {

    /**
     * Creates a JTable with two columns: Packet Name and amount of times it was received.
     * Updates the table every time a new packet is received using the companion app's message bus inside the lambda.
     */
    public PacketLoggerViewPanel() {
        setLayout(new BorderLayout(0, 0));

        //Adds a toggleable run button to the top of the panel
        FlatIconButton runButton = new FlatIconButton(new FlatSVGIcon("icons/run.svg"), true) {
            @Override
            public void setToggled(boolean b) {
                this.state = b;
                this.setIcon(state ? new FlatSVGIcon("icons/pause.svg") : new FlatSVGIcon("icons/run.svg"));
            }
        };

        //Add a clear button to send a message to the game to clear the packet map
        FlatIconButton clearButton = new FlatIconButton(new FlatSVGIcon("icons/clear.svg"), false);

        //Add a combo box to select the packets to view
        FlatComboBox<String> packetSelector = new FlatComboBox<>();
        packetSelector.addItem("Incoming Packets");
        packetSelector.addItem("Outgoing Packets");
        packetSelector.setEditable(false);
        packetSelector.setPreferredSize(new Dimension(50, 20));

        //Add a header for the buttons and the direction selector
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.X_AXIS));
        header.add(runButton);
        header.add(Box.createHorizontalStrut(5));
        header.add(clearButton);
        header.add(Box.createHorizontalStrut(5));
        header.add(packetSelector);

        add(header, BorderLayout.NORTH);

        //Creates the table
        JTable table = new JTable();
        table.setModel(new DefaultTableModel(new Object[][]{}, new String[]{"Packet Name", "Amount"}) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return columnIndex == 0 ? String.class : Integer.class;
            }
        });
        table.setAutoCreateRowSorter(true);
        table.setFillsViewportHeight(true);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        table.setGridColor(getBackground());
        add(new JScrollPane(table));

        CompanionApp.SERVER.getMessageBus().listenAlways(IncomingPacketsMessage.class, incomingPacketsMessage -> {
            //Updates the table every time a new packet is received with the data from the map in the message
            updateTable(table, incomingPacketsMessage.getIncomingPackets());
        });

        CompanionApp.SERVER.getMessageBus().listenAlways(OutgoingPacketsMessage.class, outgoingPacketsMessage -> {
            //Updates the table every time a new packet is received with the data from the map in the message
            updateTable(table, outgoingPacketsMessage.getOutgoingPackets());
        });

        runButton.addToggleListener(b -> {
            int selectedIndex = packetSelector.getSelectedIndex();
            CompletableFuture.runAsync(() -> {
                CompanionApp.SERVER.getMessageProcessor().enqueueMessage(new PacketLoggerStateChangeMessage(selectedIndex == 0 && b, selectedIndex == 1 && b));
            });
        });

        //Add a listener to the clear button to send a message to the game to clear the packet map also clears the table
        clearButton.addActionListener(e -> {
            CompletableFuture.runAsync(() -> {
                CompanionApp.SERVER.getMessageProcessor().enqueueMessage(new PacketClearMessage());
            });
            ((DefaultTableModel) table.getModel()).setRowCount(0);
        });

        //Add a listener to the direction selector to send a message to the game to change the direction of the packets also clears the table
        packetSelector.addActionListener(e -> {
            int selectedIndex = packetSelector.getSelectedIndex();
            CompletableFuture.runAsync(() -> {
                CompanionApp.SERVER.getMessageProcessor().enqueueMessage(new PacketLoggerStateChangeMessage(selectedIndex == 0, selectedIndex == 1));
            });
            ((DefaultTableModel) table.getModel()).setRowCount(0);
        });
    }

    private void updateTable(JTable table, Map<String, Integer> incomingPackets) {
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        for (int i = 0; i < table.getRowCount(); i++) {
            String value = (String) table.getValueAt(i, 0);
            if (incomingPackets.containsKey(value)) {
                table.setValueAt(incomingPackets.remove(value), i, 1);
                model.fireTableCellUpdated(i, 1);
            }
        }

        //Adds any new packets to the table
        for (Map.Entry<String, Integer> entry : incomingPackets.entrySet()) {
            model.addRow(new Object[]{entry.getKey(), entry.getValue()});
        }
    }
}
