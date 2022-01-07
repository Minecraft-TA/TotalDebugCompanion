package com.github.minecraft_ta.totalDebugCompanion.ui.components.editors;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.formdev.flatlaf.extras.components.FlatComboBox;
import com.github.javaparser.utils.Pair;
import com.github.minecraft_ta.totalDebugCompanion.CompanionApp;
import com.github.minecraft_ta.totalDebugCompanion.messages.packetLogger.ClearPacketsMessage;
import com.github.minecraft_ta.totalDebugCompanion.messages.packetLogger.IncomingPacketsMessage;
import com.github.minecraft_ta.totalDebugCompanion.messages.packetLogger.OutgoingPacketsMessage;
import com.github.minecraft_ta.totalDebugCompanion.messages.packetLogger.PacketLoggerStateChangeMessage;
import com.github.minecraft_ta.totalDebugCompanion.ui.components.FlatIconButton;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

public class PacketLoggerViewPanel extends JPanel {

    private long startTime = -1;

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

        //Adds a timer at the rop right corner of the panel to display how long the packet logger has been running
        JLabel timeLabel = new JLabel("00:00:00");
        timeLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        Timer timer = new Timer(50, e -> {
            if (startTime == -1) {
                startTime = System.currentTimeMillis();
            }
            long time = System.currentTimeMillis() - startTime;
            SimpleDateFormat sdf = new SimpleDateFormat("mm:ss:SSS");
            timeLabel.setText(sdf.format(time));
        });

        //Add a header for the buttons and the direction selector
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.X_AXIS));
        header.add(runButton);
        header.add(Box.createHorizontalStrut(5));
        header.add(clearButton);
        header.add(Box.createHorizontalStrut(5));
        header.add(packetSelector);
        header.add(Box.createHorizontalStrut(5));
        header.add(timeLabel);

        add(header, BorderLayout.NORTH);

        //Creates the table
        JTable table = new JTable();
        table.setModel(new DefaultTableModel(new Object[][]{}, new String[]{"Packet Name", "Amount", "Bytes"}) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return columnIndex == 0 ? String.class : Integer.class;
            }
        });
        //Adds a sorter to the table
        TableRowSorter<TableModel> sorter = new TableRowSorter<>(table.getModel());
        sorter.setSortsOnUpdates(true);
        table.setRowSorter(sorter);

        table.setFillsViewportHeight(true);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        table.setGridColor(getBackground());
        table.getColumnModel().getColumn(0).setPreferredWidth(280);
        add(new JScrollPane(table));

        CompanionApp.SERVER.getMessageBus().listenAlways(IncomingPacketsMessage.class, this, incomingPacketsMessage -> {
            updateTable(table, incomingPacketsMessage.getIncomingPackets());
        });

        CompanionApp.SERVER.getMessageBus().listenAlways(OutgoingPacketsMessage.class, this, outgoingPacketsMessage -> {
            updateTable(table, outgoingPacketsMessage.getOutgoingPackets());
        });

        //Add a listener to the run button to send a message to the game to start or stop logging packets
        runButton.addToggleListener(b -> {
            if (b) {
                timer.start();
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("mm:ss:SSS");
                    Date date = sdf.parse(timeLabel.getText());
                    startTime = System.currentTimeMillis() - date.getTime();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            } else {
                timer.stop();
            }
            int selectedIndex = packetSelector.getSelectedIndex();
            CompanionApp.SERVER.getMessageProcessor().enqueueMessage(new PacketLoggerStateChangeMessage(selectedIndex == 0 && b, selectedIndex == 1 && b));
        });

        //Add a listener to the clear button to send a message to the game to clear the packet map also clears the table
        clearButton.addActionListener(e -> {
            startTime = -1;
            timeLabel.setText("00:00:00");
            CompanionApp.SERVER.getMessageProcessor().enqueueMessage(new ClearPacketsMessage());
            ((DefaultTableModel) table.getModel()).setRowCount(0);
        });

        //Add a listener to the direction selector to send a message to the game to change the direction of the packets also clears the table
        packetSelector.addActionListener(e -> {
            if (runButton.isToggled()) {
                int selectedIndex = packetSelector.getSelectedIndex();
                CompanionApp.SERVER.getMessageProcessor().enqueueMessage(new PacketLoggerStateChangeMessage(selectedIndex == 0, selectedIndex == 1));
            }
            startTime = -1;
            timeLabel.setText("00:00:00");
            ((DefaultTableModel) table.getModel()).setRowCount(0);
        });
    }

    /**
     * Updates the table with the given packet map
     *
     * @param table   The table to update
     * @param packets The packet map to update the table with
     */
    private void updateTable(JTable table, Map<String, Pair<Integer, Integer>> packets) {
        //Updates the already existing rows with the new values
        for (int i = 0; i < table.getRowCount(); i++) {
            String value = (String) table.getValueAt(i, 0);
            Pair<Integer, Integer> packetData = packets.remove(value);
            if (packetData != null) {
                table.setValueAt(packetData.a, i, 1);
                table.setValueAt(packetData.b, i, 2);
            }
        }

        //Adds any new packets to the table
        for (Map.Entry<String, Pair<Integer, Integer>> entry : packets.entrySet()) {
            ((DefaultTableModel) table.getModel()).addRow(new Object[]{entry.getKey(), entry.getValue().a, entry.getValue().b});
        }
    }

    public boolean canClose() {
        CompanionApp.SERVER.getMessageProcessor().enqueueMessage(new PacketLoggerStateChangeMessage(false, false));
        CompanionApp.SERVER.getMessageProcessor().enqueueMessage(new ClearPacketsMessage());
        CompanionApp.SERVER.getMessageBus().unregister(IncomingPacketsMessage.class, this);
        CompanionApp.SERVER.getMessageBus().unregister(OutgoingPacketsMessage.class, this);
        return true;
    }
}
