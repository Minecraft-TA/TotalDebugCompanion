package com.github.minecraft_ta.totalDebugCompanion.ui.components.editors;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.formdev.flatlaf.extras.components.FlatComboBox;
import com.github.javaparser.utils.Pair;
import com.github.minecraft_ta.totalDebugCompanion.CompanionApp;
import com.github.minecraft_ta.totalDebugCompanion.messages.packetLogger.*;
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

    private static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("mm:ss:SSS");
    private long startTime = -1;

    /**
     * Creates a JTable with three columns: Packet Name, amount of times it was received and total size of the packets in bytes
     * Updates the table every time a new packet is received using the companion app's message bus
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

        //Adds a combo box to filter the packets by the channel they were sent on
        FlatComboBox<String> channelSelector = new FlatComboBox<>();
        channelSelector.addItem("All channels");
        channelSelector.setEditable(false);
        channelSelector.setMaximumSize(new Dimension(200, (int) channelSelector.getPreferredSize().getHeight()));

        //Sends a message to the game to request the channel list
        CompanionApp.SERVER.getMessageProcessor().enqueueMessage(new ChannelListMessage());

        //Adds a timer at the rop right corner of the panel to display how long the packet logger has been running
        JLabel timeLabel = new JLabel("00:00:00");
        timeLabel.setIcon(new FlatSVGIcon("icons/clock.svg"));
        timeLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        Timer timer = new Timer(50, e -> {
            if (startTime == -1) {
                startTime = System.currentTimeMillis();
            }
            long time = System.currentTimeMillis() - startTime;
            timeLabel.setText(SIMPLE_DATE_FORMAT.format(time));
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
        header.add(channelSelector);
        header.add(Box.createHorizontalGlue());
        header.add(timeLabel);
        header.add(Box.createHorizontalStrut(5));

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
            SwingUtilities.invokeLater(() -> {
                if (packetSelector.getSelectedIndex() == 0) {
                    updateTable(table, incomingPacketsMessage.getIncomingPackets());
                }
            });
        });

        CompanionApp.SERVER.getMessageBus().listenAlways(OutgoingPacketsMessage.class, this, outgoingPacketsMessage -> {
            SwingUtilities.invokeLater(() -> {
                if (packetSelector.getSelectedIndex() == 1) {
                    updateTable(table, outgoingPacketsMessage.getOutgoingPackets());
                }
            });
        });

        CompanionApp.SERVER.getMessageBus().listenAlways(ChannelListMessage.class, this, channelListMessage -> {
            SwingUtilities.invokeLater(() -> channelListMessage.getChannels().forEach(channelSelector::addItem));
        });

        //Add a listener to the run button to send a message to the game to start or stop logging packets
        runButton.addToggleListener(b -> {
            if (b) {
                timer.start();
                try {
                    Date date = SIMPLE_DATE_FORMAT.parse(timeLabel.getText());
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

        //Add a listener to the channel selector to send a message to the game to change the channel of the packets also clears the table
        channelSelector.addActionListener(e -> {
            CompanionApp.SERVER.getMessageProcessor().enqueueMessage(new SetChannelMessage((String) channelSelector.getSelectedItem()));
            CompanionApp.SERVER.getMessageProcessor().enqueueMessage(new ClearPacketsMessage());
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
        CompanionApp.SERVER.getMessageProcessor().enqueueMessage(new SetChannelMessage("All channels"));
        CompanionApp.SERVER.getMessageBus().unregister(IncomingPacketsMessage.class, this);
        CompanionApp.SERVER.getMessageBus().unregister(OutgoingPacketsMessage.class, this);
        CompanionApp.SERVER.getMessageBus().unregister(ChannelListMessage.class, this);
        return true;
    }
}
