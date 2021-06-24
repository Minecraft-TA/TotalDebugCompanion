package com.github.minecraft_ta.totalDebugCompanion;

import com.formdev.flatlaf.FlatDarculaLaf;
import com.github.minecraft_ta.totalDebugCompanion.messages.CodeViewClickMessage;
import com.github.minecraft_ta.totalDebugCompanion.messages.DecompileAndOpenRequestMessage;
import com.github.minecraft_ta.totalDebugCompanion.messages.OpenFileMessage;
import com.github.minecraft_ta.totalDebugCompanion.messages.OpenSearchResultsMessage;
import com.github.minecraft_ta.totalDebugCompanion.ui.MainWindow;
import com.github.tth05.scnet.Server;

import javax.swing.*;
import javax.swing.plaf.BorderUIResource;
import javax.swing.plaf.ColorUIResource;
import java.awt.*;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Paths;

public class CompanionApp {

    public static final Server SERVER = new Server();

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Missing argument");
            return;
        }

        var rootPath = Paths.get(args[0]);
        if (!Files.exists(rootPath)) {
            System.out.println("Path does not exist");
            return;
        }

        FlatDarculaLaf.setup();
        UIManager.put("Component.focusColor", new ColorUIResource(new Color(0, 0, 0, 0)));
        UIManager.put("TabbedPane.tabInsets", new Insets(0, 10, 0, 10));
        UIManager.put("TabbedPane.tabHeight", 25);
        UIManager.put("Slider.focusedColor", new ColorUIResource(new Color(0, 0, 0, 0)));
        UIManager.put("Table.focusSelectedCellHighlightBorder", new BorderUIResource(BorderFactory.createEmptyBorder(0, 5, 0, 0)));
        UIManager.put("Table.focusCellHighlightBorder", new BorderUIResource(BorderFactory.createEmptyBorder(0, 3, 0, 0)));

        var mainWindow = new MainWindow(rootPath);
        mainWindow.setSize(1280, 720);
        mainWindow.setVisible(true);

        SERVER.bind(new InetSocketAddress(25570));
        SERVER.getMessageProcessor().registerMessage((short) 1, OpenFileMessage.class);
        SERVER.getMessageProcessor().registerMessage((short) 2, OpenSearchResultsMessage.class);
        SERVER.getMessageProcessor().registerMessage((short) 3, DecompileAndOpenRequestMessage.class);
        SERVER.getMessageProcessor().registerMessage((short) 4, CodeViewClickMessage.class);
        SERVER.getMessageBus().listenAlways(OpenFileMessage.class, (m) -> OpenFileMessage.handle(m, mainWindow));
        SERVER.getMessageBus().listenAlways(OpenSearchResultsMessage.class, (m) -> OpenSearchResultsMessage.handle(m, mainWindow));

        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        mainWindow.setLocation(dim.width / 2 - mainWindow.getSize().width / 2, dim.height / 2 - mainWindow.getSize().height / 2);
    }
}
