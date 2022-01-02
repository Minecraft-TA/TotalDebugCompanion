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
                super.setToggled(b);
                this.setIcon(b ? new FlatSVGIcon("icons/pause.svg") : new FlatSVGIcon("icons/run.svg"));
            }
        };

        //Add a clear button to send a message to the game to clear the packet map
        FlatIconButton clearButton = new FlatIconButton(new FlatSVGIcon("icons/clear.svg"), false);

        //Add a combo box to select the packets to view
        FlatComboBox<String> packetSelector = new FlatComboBox<>();
        packetSelector.addItem("Incoming Packets");
        packetSelector.addItem("Outgoing Packets");
        packetSelector.setEditable(false);
        packetSelector.setMaximumSize(new Dimension(200, (int) packetSelector.getPreferredSize().getHeight()));

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
            updateTable(table, incomingPacketsMessage.getIncomingPackets());
        });

        CompanionApp.SERVER.getMessageBus().listenAlways(OutgoingPacketsMessage.class, outgoingPacketsMessage -> {
            updateTable(table, outgoingPacketsMessage.getOutgoingPackets());
        });

        //Add a listener to the run button to send a message to the game to start or stop logging packets
        runButton.addToggleListener(b -> {
            int selectedIndex = packetSelector.getSelectedIndex();
            CompanionApp.SERVER.getMessageProcessor().enqueueMessage(new PacketLoggerStateChangeMessage(selectedIndex == 0 && b, selectedIndex == 1 && b));
        });

        //Add a listener to the clear button to send a message to the game to clear the packet map also clears the table
        clearButton.addActionListener(e -> {
            CompanionApp.SERVER.getMessageProcessor().enqueueMessage(new PacketClearMessage());
            ((DefaultTableModel) table.getModel()).setRowCount(0);
        });

        //Add a listener to the direction selector to send a message to the game to change the direction of the packets also clears the table
        packetSelector.addActionListener(e -> {
            if (runButton.isToggled()) {
                int selectedIndex = packetSelector.getSelectedIndex();
                CompanionApp.SERVER.getMessageProcessor().enqueueMessage(new PacketLoggerStateChangeMessage(selectedIndex == 0, selectedIndex == 1));
            }
            ((DefaultTableModel) table.getModel()).setRowCount(0);
        });
    }

    /**
     * Updates the table with the given packet map
     *
     * @param table   The table to update
     * @param packets The packet map to update the table with
     */
    private void updateTable(JTable table, Map<String, Integer> packets) {
        //Updates the already existing rows with the new values
        for (int i = 0; i < table.getRowCount(); i++) {
            String value = (String) table.getValueAt(i, 0);
            if (packets.containsKey(value)) {
                table.setValueAt(packets.remove(value), i, 1);
            }
        }

        //Adds any new packets to the table
        for (Map.Entry<String, Integer> entry : packets.entrySet()) {
            ((DefaultTableModel) table.getModel()).addRow(new Object[]{entry.getKey(), entry.getValue()});
        }
    }
}
