package com.github.minecraft_ta.totalDebugCompanion;

import com.formdev.flatlaf.FlatDarculaLaf;
import com.github.minecraft_ta.totalDebugCompanion.lsp.JavaLanguageServer;
import com.github.minecraft_ta.totalDebugCompanion.messages.FocusWindowMessage;
import com.github.minecraft_ta.totalDebugCompanion.messages.ReadyMessage;
import com.github.minecraft_ta.totalDebugCompanion.messages.chunkGrid.ChunkGridDataMessage;
import com.github.minecraft_ta.totalDebugCompanion.messages.chunkGrid.ChunkGridRequestInfoUpdateMessage;
import com.github.minecraft_ta.totalDebugCompanion.messages.chunkGrid.ReceiveDataStateMessage;
import com.github.minecraft_ta.totalDebugCompanion.messages.chunkGrid.UpdateFollowPlayerStateMessage;
import com.github.minecraft_ta.totalDebugCompanion.messages.codeView.CodeViewClickMessage;
import com.github.minecraft_ta.totalDebugCompanion.messages.codeView.DecompileAndOpenRequestMessage;
import com.github.minecraft_ta.totalDebugCompanion.messages.codeView.OpenFileMessage;
import com.github.minecraft_ta.totalDebugCompanion.messages.packetLogger.IncomingPacketsMessage;
import com.github.minecraft_ta.totalDebugCompanion.messages.packetLogger.OutgoingPacketsMessage;
import com.github.minecraft_ta.totalDebugCompanion.messages.packetLogger.ClearPacketsMessage;
import com.github.minecraft_ta.totalDebugCompanion.messages.packetLogger.PacketLoggerStateChangeMessage;
import com.github.minecraft_ta.totalDebugCompanion.messages.script.ClassPathMessage;
import com.github.minecraft_ta.totalDebugCompanion.messages.script.RunScriptMessage;
import com.github.minecraft_ta.totalDebugCompanion.messages.script.ScriptStatusMessage;
import com.github.minecraft_ta.totalDebugCompanion.messages.script.StopScriptMessage;
import com.github.minecraft_ta.totalDebugCompanion.messages.search.OpenSearchResultsMessage;
import com.github.minecraft_ta.totalDebugCompanion.ui.components.global.SimpleMenuBarBorder;
import com.github.minecraft_ta.totalDebugCompanion.ui.views.DownloadProgressWindow;
import com.github.minecraft_ta.totalDebugCompanion.ui.views.MainWindow;
import com.github.minecraft_ta.totalDebugCompanion.util.FileUtils;
import com.github.minecraft_ta.totalDebugCompanion.util.UIUtils;
import com.github.tth05.scnet.Server;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;

import javax.swing.*;
import javax.swing.plaf.BorderUIResource;
import javax.swing.plaf.ColorUIResource;
import java.awt.*;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class CompanionApp {

    public static final Server SERVER = new Server();
    public static JavaLanguageServer LSP;
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

        LSP = new JavaLanguageServer();

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
        SERVER.getMessageProcessor().registerMessage((short) id++, ClassPathMessage.class);
        SERVER.getMessageProcessor().registerMessage((short) id++, StopScriptMessage.class);
        SERVER.getMessageProcessor().registerMessage((short) id++, FocusWindowMessage.class);
        SERVER.getMessageProcessor().registerMessage((short) id++, PacketLoggerStateChangeMessage.class);
        SERVER.getMessageProcessor().registerMessage((short) id++, IncomingPacketsMessage.class);
        SERVER.getMessageProcessor().registerMessage((short) id++, OutgoingPacketsMessage.class);
        SERVER.getMessageProcessor().registerMessage((short) id++, ClearPacketsMessage.class);
        SERVER.bind(new InetSocketAddress(25570));

        FlatDarculaLaf.setup();

        CompletableFuture<Void> future;
        if (!LSP.isSetup()) {
            var basePath = ROOT_PATH.resolve("jdt-language-server-latest");
            FileUtils.createIfNotExists(basePath, true);

            future = HttpClient.newHttpClient().sendAsync(
                    HttpRequest.newBuilder()
                            .uri(URI.create("https://download.eclipse.org/jdtls/snapshots/jdt-language-server-latest.tar.gz"))
                            .build(),
                    HttpResponse.BodyHandlers.ofInputStream()
            ).thenAccept((res) -> {
                var downloadProgressWindow = new DownloadProgressWindow();
                downloadProgressWindow.setText("Downloading JDT Language Server (0MB)");
                downloadProgressWindow.setVisible(true);
                downloadProgressWindow.setLinkText(res.request().uri().toASCIIString());

                int writtenBytes = 0;
                try (var stream = new TarArchiveInputStream(new GzipCompressorInputStream(Channels.newInputStream(Channels.newChannel(res.body()))))) {
                    for (ArchiveEntry entry = stream.getNextEntry(); entry != null; entry = stream.getNextEntry()) {
                        Path toPath = basePath.resolve(entry.getName());
                        if (entry.isDirectory()) { //create directory
                            FileUtils.createIfNotExists(toPath, true);
                        } else { //transfer file to file system
                            try (FileChannel fileChannel = FileChannel.open(toPath, StandardOpenOption.WRITE, StandardOpenOption.CREATE)) {
                                fileChannel.transferFrom(Channels.newChannel(stream), 0, Long.MAX_VALUE);
                                writtenBytes += entry.getSize();
                            }

                            downloadProgressWindow.setText("Downloading JDT Language Server (%.2fMB)".formatted(writtenBytes / 1024d / 1024d));
                        }
                    }
                } catch (IOException e) {
                    throw new CompletionException(e);
                }

                downloadProgressWindow.setVisible(false);
                downloadProgressWindow.dispose();
            }).exceptionally(e -> {
                if (e == null)
                    return null;

                System.err.println("Unable to download latest JDT version");
                e.printStackTrace();
                System.exit(1);
                return null;
            });
        } else {
            future = CompletableFuture.completedFuture(null);
        }

        future.thenRun(() -> {
            setupEclipseProject();

            //Start application
            LSP.start();
            startUI();
        }).join();
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
        }));

        MainWindow.INSTANCE.setSize(1280, 720);
        MainWindow.INSTANCE.setVisible(true);
        UIUtils.centerJFrame(MainWindow.INSTANCE);
    }

    private static void setupEclipseProject() {
        var projectDir = ROOT_PATH.resolve("workspace").resolve("custom-project");
        FileUtils.createIfNotExists(projectDir, true);

        //Add classpath entries
        var classPathFile = projectDir.resolve(".classpath");

        var lock = new CompletableFuture<String>();
        SERVER.getMessageBus().listenOnce(ClassPathMessage.class, (m) -> {
            lock.complete(m.getClassPath());
        });

        var listener = (Runnable) () -> SERVER.getMessageProcessor().enqueueMessage(new ClassPathMessage());
        if (SERVER.isClientConnected())
            listener.run();
        else
            SERVER.addOnConnectionListener(listener);

        var classPathString = lock.orTimeout(5, TimeUnit.SECONDS).join();

        SERVER.removeOnConnectionListener(listener);
        try {
            //language=XML
            Files.writeString(classPathFile, """
                    <?xml version="1.0" encoding="UTF-8"?>
                    <classpath>
                        <classpathentry kind="src" path="src"/>
                        <classpathentry kind="con" path="org.eclipse.jdt.launching.JRE_CONTAINER"/>
                        <classpathentry kind="output" path="bin"/>
                        %s
                    </classpath>
                    """
                    //language=None
                    .formatted(Arrays.stream(classPathString.split(";"))
                            .filter(s -> !s.contains("scala")) //Filter this trash
                            .map(s -> "<classpathentry kind=\"lib\" path=\"" + Paths.get(s) + "\"/>")
                            .distinct()
                            .collect(Collectors.joining("\n")))
            );

            //Create .project file
            Files.writeString(projectDir.resolve(".project"), """
                    <?xml version="1.0" encoding="UTF-8"?>
                    <projectDescription>
                        <name>custom-project</name>
                        <comment></comment>
                        <projects>
                        </projects>
                        <buildSpec>
                            <buildCommand>
                                <name>org.eclipse.jdt.core.javabuilder</name>
                                <arguments>
                                </arguments>
                            </buildCommand>
                        </buildSpec>
                        <natures>
                            <nature>org.eclipse.jdt.core.javanature</nature>
                        </natures>
                    </projectDescription>
                    """);

            //Create .settings file
            FileUtils.createIfNotExists(projectDir.resolve(".settings"), true);
            Files.writeString(projectDir.resolve(".settings").resolve("org.eclipse.jdt.core.prefs"), """
                    eclipse.preferences.version=1
                    org.eclipse.jdt.core.compiler.codegen.inlineJsrBytecode=enabled
                    org.eclipse.jdt.core.compiler.codegen.targetPlatform=8
                    org.eclipse.jdt.core.compiler.codegen.unusedLocal=preserve
                    org.eclipse.jdt.core.compiler.compliance=8
                    org.eclipse.jdt.core.compiler.problem.assertIdentifier=error
                    org.eclipse.jdt.core.compiler.problem.enumIdentifier=error
                    org.eclipse.jdt.core.compiler.source=8
                    """);

            //Create src dir
            FileUtils.createIfNotExists(projectDir.resolve("src"), true);

            CompanionApp.LSP.getBaseScript().writeToFileIfNotExists();
        } catch (IOException e) {
            throw new CompletionException(e);
        }
    }

    public static Path getRootPath() {
        return ROOT_PATH;
    }
}
