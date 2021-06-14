package com.github.minecraft_ta.totalDebugCompanion.server;

import com.github.minecraft_ta.totalDebugCompanion.model.CodeView;
import com.github.minecraft_ta.totalDebugCompanion.ui.MainWindow;
import com.github.minecraft_ta.totalDebugCompanion.ui.components.EditorTabs;
import com.github.minecraft_ta.totalDebugCompanion.util.FileUtils;
import com.github.minecraft_ta.totalDebugCompanion.util.UIUtils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.function.Consumer;

public class CompanionAppServer {

    private final int port;
    private final MainWindow mainWindow;

    private DataOutputStream outputStream;

    public CompanionAppServer(int port, MainWindow mainWindow) {
        this.port = port;
        this.mainWindow = mainWindow;
    }

    public void writeBatch(Consumer<DataOutputStream> block) {
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

    public void run() {
        ServerSocket serverSocket;
        try {
            serverSocket = new ServerSocket(this.port);
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
                        switch (inputStream.readUnsignedByte()) {
                            case 1:
                                var path = Paths.get(inputStream.readUTF()).toAbsolutePath();
                                var row = inputStream.readInt();

                                if (!Files.exists(path) || !FileUtils.isSubPathOf(this.mainWindow.getRootPath(), path))
                                    break;

                                EditorTabs editorTabs = this.mainWindow.getEditorTabs();
                                editorTabs.getEditors().stream()
                                        .filter(e -> e instanceof CodeView)
                                        .map(e -> (CodeView) e)
                                        .filter(e -> e.getIdentifier().equals(path.toString()))
                                        .findFirst().ifPresentOrElse(c -> {
                                            editorTabs.setSelectedIndex(editorTabs.getEditors().indexOf(c));
                                        }, () -> {
                                            editorTabs.openEditorTab(new CodeView(path));
                                        });

                                UIUtils.focusWindow(this.mainWindow);
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
}
