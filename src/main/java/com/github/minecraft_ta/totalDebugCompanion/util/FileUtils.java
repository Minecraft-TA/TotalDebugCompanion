package com.github.minecraft_ta.totalDebugCompanion.util;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;

public class FileUtils {

    public static void startNewDirectoryWatcher(Path directory, Runnable onChange) {
        try {
            var watchService = FileSystems.getDefault().newWatchService();
            directory.register(watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE);

            new Thread(() -> {
                while (true) {
                    try {
                        var key = watchService.take();

                        if (key.pollEvents().stream()
                                .anyMatch(e -> e.kind() != StandardWatchEventKinds.OVERFLOW &&
                                               e.kind() != StandardWatchEventKinds.ENTRY_MODIFY)) {
                            onChange.run();
                        }

                        var valid = key.reset();

                        if (!valid) {
                            System.err.println("Directory no longer accessible " + directory);
                            System.exit(0);
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean isSubPathOf(Path base, Path other) {
        var it1 = other.normalize().iterator();

        for (Path part : base.normalize()) {
            if (!it1.hasNext() || !part.equals(it1.next()))
                return false;
        }

        return it1.hasNext();
    }

    public static URI toURI(String s) {
        try {
            return new URI(s);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
