package com.github.minecraft_ta.totalDebugCompanion;

import com.formdev.flatlaf.FlatDarculaLaf;
import com.github.minecraft_ta.totalDebugCompanion.jdt.BaseScript;
import com.github.minecraft_ta.totalDebugCompanion.messages.FocusWindowMessage;
import com.github.minecraft_ta.totalDebugCompanion.messages.ReadyMessage;
import com.github.minecraft_ta.totalDebugCompanion.messages.chunkGrid.ChunkGridDataMessage;
import com.github.minecraft_ta.totalDebugCompanion.messages.chunkGrid.ChunkGridRequestInfoUpdateMessage;
import com.github.minecraft_ta.totalDebugCompanion.messages.chunkGrid.ReceiveDataStateMessage;
import com.github.minecraft_ta.totalDebugCompanion.messages.chunkGrid.UpdateFollowPlayerStateMessage;
import com.github.minecraft_ta.totalDebugCompanion.messages.codeView.CodeViewClickMessage;
import com.github.minecraft_ta.totalDebugCompanion.messages.codeView.DecompileAndOpenRequestMessage;
import com.github.minecraft_ta.totalDebugCompanion.messages.codeView.OpenFileMessage;
import com.github.minecraft_ta.totalDebugCompanion.messages.packetLogger.*;
import com.github.minecraft_ta.totalDebugCompanion.messages.script.RunScriptMessage;
import com.github.minecraft_ta.totalDebugCompanion.messages.script.ScriptStatusMessage;
import com.github.minecraft_ta.totalDebugCompanion.messages.script.StopScriptMessage;
import com.github.minecraft_ta.totalDebugCompanion.messages.search.OpenSearchResultsMessage;
import com.github.minecraft_ta.totalDebugCompanion.ui.components.global.SimpleMenuBarBorder;
import com.github.minecraft_ta.totalDebugCompanion.ui.views.MainWindow;
import com.github.minecraft_ta.totalDebugCompanion.util.FileUtils;
import com.github.minecraft_ta.totalDebugCompanion.util.UIUtils;
import com.github.tth05.scnet.Server;
import org.fife.ui.rsyntaxtextarea.AbstractTokenMakerFactory;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.TokenMakerFactory;

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

        ROOT_PATH = Paths.get(args[0]).toAbsolutePath().normalize();
        if (!Files.exists(ROOT_PATH)) {
            System.out.println("Path does not exist");
            return;
        }

        int id = 1;
        SERVER.getMessageProcessor().registerMessage((short) id++, ReadyMessage.class);
        SERVER.getMessageProcessor().registerMessage((short) id++, OpenFileMessage.class);
        SERVER.getMessageProcessor().registerMessage((short) id++, OpenSearchResultsMessage.class);
        SERVER.getMessageProcessor().registerMessage((short) id++, DecompileAndOpenRequestMessage.class);
        SERVER.getMessageProcessor().registerMessage((short) id++, CodeViewClickMessage.class);
        SERVER.getMessageProcessor().registerMessage((short) id++, ReceiveDataStateMessage.class);
        SERVER.getMessageProcessor().registerMessage((short) id++, ChunkGridDataMessage.class);
        SERVER.getMessageProcessor().registerMessage((short) id++, ChunkGridRequestInfoUpdateMessage.class);
        SERVER.getMessageProcessor().registerMessage((short) id++, UpdateFollowPlayerStateMessage.class);
        SERVER.getMessageProcessor().registerMessage((short) id++, RunScriptMessage.class);
        SERVER.getMessageProcessor().registerMessage((short) id++, ScriptStatusMessage.class);
        SERVER.getMessageProcessor().registerMessage((short) id++, StopScriptMessage.class);
        SERVER.getMessageProcessor().registerMessage((short) id++, FocusWindowMessage.class);
        SERVER.getMessageProcessor().registerMessage((short) id++, PacketLoggerStateChangeMessage.class);
        SERVER.getMessageProcessor().registerMessage((short) id++, IncomingPacketsMessage.class);
        SERVER.getMessageProcessor().registerMessage((short) id++, OutgoingPacketsMessage.class);
        SERVER.getMessageProcessor().registerMessage((short) id++, ClearPacketsMessage.class);
        SERVER.getMessageProcessor().registerMessage((short) id++, ChannelListMessage.class);
        SERVER.getMessageProcessor().registerMessage((short) id++, SetChannelMessage.class);
        SERVER.getMessageProcessor().registerMessage((short) id++, PacketContentMessage.class);
        SERVER.getMessageProcessor().registerMessage((short) id++, CapturePacketMessage.class);
        SERVER.getMessageProcessor().registerMessage((short) id++, BlockPacketMessage.class);
        SERVER.bind(new InetSocketAddress(25570));

        FlatDarculaLaf.setup();
        TokenMakerFactory.setDefaultInstance(new AbstractTokenMakerFactory() {
            @Override
            protected void initTokenMakerMap() {
                putMapping(RSyntaxTextArea.SYNTAX_STYLE_JAVA, "com.github.minecraft_ta.totalDebugCompanion.jdt.semanticHighlighting.JavaTokenMaker");
            }
        });

        setupScripts();
        startUI();
    }

    private static void startUI() {
        UIManager.put("SplitPaneDivider.style", "plain");
        UIManager.put("Component.focusColor", new ColorUIResource(new Color(0, 0, 0, 0)));
        UIManager.put("TabbedPane.tabInsets", new Insets(0, 10, 0, 10));
        UIManager.put("TabbedPane.tabHeight", 25);
        UIManager.put("Slider.focusedColor", new ColorUIResource(new Color(0, 0, 0, 0)));
        UIManager.put("Table.focusSelectedCellHighlightBorder", new BorderUIResource(BorderFactory.createEmptyBorder(0, 5, 0, 0)));
        UIManager.put("Table.focusCellHighlightBorder", new BorderUIResource(BorderFactory.createEmptyBorder(0, 3, 0, 0)));
        UIManager.put("Tree.selectionBackground", new ColorUIResource(new Color(5 / 255f, 127 / 255f, 242 / 255f, 0.5f)));
        UIManager.put("List.selectionBackground", new ColorUIResource(new Color(5 / 255f, 127 / 255f, 242 / 255f, 0.5f)));
        UIManager.put("TitlePane.unifiedBackground", false);
        UIManager.put("MenuBar.border", new SimpleMenuBarBorder());

        SERVER.getMessageBus().listenAlways(OpenFileMessage.class, OpenFileMessage::handle);
        SERVER.getMessageBus().listenAlways(OpenSearchResultsMessage.class, OpenSearchResultsMessage::handle);
        SERVER.getMessageBus().listenAlways(FocusWindowMessage.class, (m) -> UIUtils.focusWindow(MainWindow.INSTANCE));
        SERVER.addOnConnectionListener(() -> SERVER.getMessageProcessor().enqueueMessage(new ReadyMessage()));

        SERVER.getMessageProcessor().enqueueMessage(new ReadyMessage());

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            SERVER.getMessageProcessor().enqueueMessage(new StopScriptMessage(-1));
            SERVER.getMessageProcessor().enqueueMessage(new PacketLoggerStateChangeMessage(false, false));
            SERVER.getMessageProcessor().enqueueMessage(new ClearPacketsMessage());
            SERVER.getMessageProcessor().enqueueMessage(new SetChannelMessage("All channels"));
        }));

        SwingUtilities.invokeLater(() -> {
            MainWindow.INSTANCE.setSize(1280, 720);
            MainWindow.INSTANCE.setVisible(true);
            UIUtils.centerJFrame(MainWindow.INSTANCE);
        });
    }

    private static void setupScripts() {
        FileUtils.createIfNotExists(ROOT_PATH.resolve("scripts"), true);
        BaseScript.writeToFileIfNotExists();
    }

    public static Path getRootPath() {
        return ROOT_PATH;
    }
}
