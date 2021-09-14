package com.github.minecraft_ta.totalDebugCompanion;

import com.formdev.flatlaf.FlatDarculaLaf;
import com.github.minecraft_ta.totalDebugCompanion.lsp.JavaLanguageServer;
import com.github.minecraft_ta.totalDebugCompanion.messages.chunkGrid.ChunkGridDataMessage;
import com.github.minecraft_ta.totalDebugCompanion.messages.chunkGrid.ChunkGridRequestInfoUpdateMessage;
import com.github.minecraft_ta.totalDebugCompanion.messages.chunkGrid.ReceiveDataStateMessage;
import com.github.minecraft_ta.totalDebugCompanion.messages.chunkGrid.UpdateFollowPlayerStateMessage;
import com.github.minecraft_ta.totalDebugCompanion.messages.codeView.CodeViewClickMessage;
import com.github.minecraft_ta.totalDebugCompanion.messages.codeView.DecompileAndOpenRequestMessage;
import com.github.minecraft_ta.totalDebugCompanion.messages.codeView.OpenFileMessage;
import com.github.minecraft_ta.totalDebugCompanion.messages.script.ClassPathMessage;
import com.github.minecraft_ta.totalDebugCompanion.messages.script.RunScriptMessage;
import com.github.minecraft_ta.totalDebugCompanion.messages.script.ScriptStatusMessage;
import com.github.minecraft_ta.totalDebugCompanion.messages.search.OpenSearchResultsMessage;
import com.github.minecraft_ta.totalDebugCompanion.ui.views.MainWindow;
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
    public static final JavaLanguageServer LSP = new JavaLanguageServer();
    private static Path ROOT_PATH;

    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.out.println("Missing argument");
            return;
        }

        ROOT_PATH = Paths.get(args[0]);
        if (!Files.exists(ROOT_PATH)) {
            System.out.println("Path does not exist");
            return;
        }

        SERVER.getMessageProcessor().registerMessage((short) 1, OpenFileMessage.class);
        SERVER.getMessageProcessor().registerMessage((short) 2, OpenSearchResultsMessage.class);
        SERVER.getMessageProcessor().registerMessage((short) 3, DecompileAndOpenRequestMessage.class);
        SERVER.getMessageProcessor().registerMessage((short) 4, CodeViewClickMessage.class);
        SERVER.getMessageProcessor().registerMessage((short) 5, ReceiveDataStateMessage.class);
        SERVER.getMessageProcessor().registerMessage((short) 6, ChunkGridDataMessage.class);
        SERVER.getMessageProcessor().registerMessage((short) 7, ChunkGridRequestInfoUpdateMessage.class);
        SERVER.getMessageProcessor().registerMessage((short) 8, UpdateFollowPlayerStateMessage.class);
        SERVER.getMessageProcessor().registerMessage((short) 9, RunScriptMessage.class);
        SERVER.getMessageProcessor().registerMessage((short) 10, ScriptStatusMessage.class);
        SERVER.getMessageProcessor().registerMessage((short) 11, ClassPathMessage.class);
        SERVER.bind(new InetSocketAddress(25570));

        CompletableFuture<Void> future;
        if (!LSP.isSetup()) {
            var basePath = Paths.get(".", "jdt-language-server-latest");
            if (!Files.exists(basePath))
                Files.createDirectory(basePath);

            future = HttpClient.newHttpClient().sendAsync(
                    HttpRequest.newBuilder()
                            .uri(URI.create("https://download.eclipse.org/jdtls/snapshots/jdt-language-server-latest.tar.gz"))
                            .build(),
                    HttpResponse.BodyHandlers.ofInputStream()
            ).thenAccept((res) -> {
                int writtenBytes = 0;
                try (var stream = new TarArchiveInputStream(new GzipCompressorInputStream(Channels.newInputStream(Channels.newChannel(res.body()))))) {
                    for (ArchiveEntry entry = stream.getNextEntry(); entry != null; entry = stream.getNextEntry()) {
                        Path toPath = basePath.resolve(entry.getName());
                        if (entry.isDirectory()) { //create directory
                            if (!Files.exists(toPath))
                                Files.createDirectory(toPath);
                        } else { //transfer file to file system
                            try (FileChannel fileChannel = FileChannel.open(toPath, StandardOpenOption.WRITE, StandardOpenOption.CREATE)) {
                                fileChannel.transferFrom(Channels.newChannel(stream), 0, Long.MAX_VALUE);
                                writtenBytes += entry.getSize();
                            }

                            //send progress message
                            System.out.println(writtenBytes);
                        }
                    }
                } catch (IOException e) {
                    throw new CompletionException(e);
                }
            }).exceptionally(e -> {
                if (e == null)
                    return null;

                System.err.println("Unable to download latest JDT version");
                e.printStackTrace();
                System.exit(1);
                return null;
            }).thenRun(() -> {
                //Generate workspace
                LSP.start();
                LSP.stop();
            });
        } else {
            future = CompletableFuture.completedFuture(null);
        }

        future.thenRun(() -> {
            //Add classpath entries
            var classPathFile = Paths.get(".", "workspace", "jdt.ls-java-project", ".classpath");

            var lock = new CompletableFuture<String>();
            SERVER.getMessageBus().listenOnce(ClassPathMessage.class, (m) -> {
                lock.complete(m.getClassPath());
            });

            var listener = (Runnable) () -> SERVER.getMessageProcessor().enqueueMessage(new ClassPathMessage());
            SERVER.addOnConnectionListener(listener);

            var classPathString = lock.orTimeout(3, TimeUnit.SECONDS).join();

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
                                .collect(Collectors.joining("\n")))
                );
            } catch (IOException e) {
                throw new CompletionException(e);
            }

            //Start application
            LSP.start();
            startUI();
        }).join();
    }

    private static void startUI() {
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

        SERVER.getMessageBus().listenAlways(OpenFileMessage.class, (m) -> OpenFileMessage.handle(m, mainWindow));
        SERVER.getMessageBus().listenAlways(OpenSearchResultsMessage.class, (m) -> OpenSearchResultsMessage.handle(m, mainWindow));

        UIUtils.centerJFrame(mainWindow);
    }

    public static Path getRootPath() {
        return ROOT_PATH;
    }
}
