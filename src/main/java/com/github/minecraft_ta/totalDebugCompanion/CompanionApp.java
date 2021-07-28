package com.github.minecraft_ta.totalDebugCompanion;

import com.formdev.flatlaf.FlatDarculaLaf;
import com.github.minecraft_ta.totalDebugCompanion.messages.chunkGrid.ChunkGridDataMessage;
import com.github.minecraft_ta.totalDebugCompanion.messages.chunkGrid.ChunkGridRequestInfoUpdateMessage;
import com.github.minecraft_ta.totalDebugCompanion.messages.chunkGrid.ReceiveDataStateMessage;
import com.github.minecraft_ta.totalDebugCompanion.messages.chunkGrid.UpdateFollowPlayerStateMessage;
import com.github.minecraft_ta.totalDebugCompanion.messages.codeView.CodeViewClickMessage;
import com.github.minecraft_ta.totalDebugCompanion.messages.codeView.DecompileAndOpenRequestMessage;
import com.github.minecraft_ta.totalDebugCompanion.messages.codeView.OpenFileMessage;
import com.github.minecraft_ta.totalDebugCompanion.messages.search.OpenSearchResultsMessage;
import com.github.minecraft_ta.totalDebugCompanion.ui.views.MainWindow;
import com.github.minecraft_ta.totalDebugCompanion.util.UIUtils;
import com.github.tth05.scnet.Server;

import javax.swing.*;
import javax.swing.plaf.BorderUIResource;
import javax.swing.plaf.ColorUIResource;
import java.awt.*;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class CompanionApp {

    public static final Server SERVER = new Server();
    private static Path ROOT_PATH;

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Missing argument");
            return;
        }

        ROOT_PATH = Paths.get(args[0]);
        if (!Files.exists(ROOT_PATH)) {
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

        var mainWindow = new MainWindow();
        mainWindow.setSize(1280, 720);
        mainWindow.setVisible(true);

        SERVER.bind(new InetSocketAddress(25570));
        SERVER.getMessageProcessor().registerMessage((short) 1, OpenFileMessage.class);
        SERVER.getMessageProcessor().registerMessage((short) 2, OpenSearchResultsMessage.class);
        SERVER.getMessageProcessor().registerMessage((short) 3, DecompileAndOpenRequestMessage.class);
        SERVER.getMessageProcessor().registerMessage((short) 4, CodeViewClickMessage.class);
        SERVER.getMessageProcessor().registerMessage((short) 5, ReceiveDataStateMessage.class);
        SERVER.getMessageProcessor().registerMessage((short) 6, ChunkGridDataMessage.class);
        SERVER.getMessageProcessor().registerMessage((short) 7, ChunkGridRequestInfoUpdateMessage.class);
        SERVER.getMessageProcessor().registerMessage((short) 8, UpdateFollowPlayerStateMessage.class);

        SERVER.getMessageBus().listenAlways(OpenFileMessage.class, (m) -> OpenFileMessage.handle(m, mainWindow));
        SERVER.getMessageBus().listenAlways(OpenSearchResultsMessage.class, (m) -> OpenSearchResultsMessage.handle(m, mainWindow));

        UIUtils.centerJFrame(mainWindow);
    }

    public static Path getRootPath() {
        return ROOT_PATH;
    }
}
