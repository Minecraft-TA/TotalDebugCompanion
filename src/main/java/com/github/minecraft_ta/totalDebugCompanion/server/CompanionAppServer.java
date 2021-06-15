package com.github.minecraft_ta.totalDebugCompanion.server;

import com.github.minecraft_ta.totalDebugCompanion.model.CodeView;
import com.github.minecraft_ta.totalDebugCompanion.model.SearchResultView;
import com.github.minecraft_ta.totalDebugCompanion.ui.MainWindow;
import com.github.minecraft_ta.totalDebugCompanion.ui.components.EditorTabs;
import com.github.minecraft_ta.totalDebugCompanion.util.FileUtils;
import com.github.minecraft_ta.totalDebugCompanion.util.ThrowingConsumer;
import com.github.minecraft_ta.totalDebugCompanion.util.UIUtils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class CompanionAppServer {

    private static CompanionAppServer INSTANCE = null;

    private DataOutputStream outputStream;

    public void writeBatch(ThrowingConsumer<DataOutputStream> block) {
        if (outputStream == null)
            return;

        synchronized (this.outputStream) {
            try {
                block.accept(this.outputStream);
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }

    public void run(int port, MainWindow mainWindow) {
        ServerSocket serverSocket;
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        Thread thread = new Thread(() -> {
            while (true) {
                try {
                    var socket = serverSocket.accept();

                    this.outputStream = new DataOutputStream(socket.getOutputStream());
                    var inputStream = new DataInputStream(socket.getInputStream());

                    while (true) {
                        System.out.println("loop start");
                        switch (inputStream.readUnsignedByte()) {
                            case 1:
                                var path = Paths.get(inputStream.readUTF()).toAbsolutePath();
                                var row = inputStream.readInt();
                                System.out.println(row);
                                System.out.println(path);

                                if (!Files.exists(path) || !FileUtils.isSubPathOf(mainWindow.getRootPath(), path))
                                    break;

                                EditorTabs editorTabs = mainWindow.getEditorTabs();
                                editorTabs.getEditors().stream()
                                        .filter(e -> e instanceof CodeView)
                                        .map(e -> (CodeView) e)
                                        .filter(e -> e.getIdentifier().equals(path.toString()))
                                        .findFirst().ifPresentOrElse(c -> {
                                            editorTabs.setSelectedIndex(editorTabs.getEditors().indexOf(c));
                                        }, () -> {
                                            editorTabs.openEditorTab(new CodeView(path));
                                        });

                                UIUtils.focusWindow(mainWindow);
                                break;
                            case 2:
                                var query = inputStream.readUTF();
                                var resultCount = inputStream.readInt();
                                var results =
                                        IntStream.range(0, resultCount).mapToObj(i -> {
                                            try {
                                                return inputStream.readUTF();
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                                return "";
                                            }
                                        }).collect(Collectors.toList());
                                var methodSearch = inputStream.readBoolean();
                                var classesCount = inputStream.readInt();
                                var time = inputStream.readInt();

                                mainWindow.getEditorTabs().openEditorTab(new SearchResultView(
                                        query, results, methodSearch, classesCount, time
                                ));

                                UIUtils.focusWindow(mainWindow);
                                break;
                            default:
                                System.out.println("Received unknown packet id");
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    this.outputStream = null;
                }
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    public static CompanionAppServer getInstance() {
        if (INSTANCE == null)
            INSTANCE = new CompanionAppServer();

        return INSTANCE;
    }
}
