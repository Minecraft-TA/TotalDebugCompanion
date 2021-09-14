package com.github.minecraft_ta.totalDebugCompanion.lsp;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

public class LSPServerProcess {

    private Process process;

    public void start(Path launcherPath) {
        try {
            this.process = new ProcessBuilder(
                    "\"" + System.getProperty("java.home") + File.separator + "bin" + File.separator + "java.exe\"",
                    "-Declipse.application=org.eclipse.jdt.ls.core.id1",
                    "-Dosgi.bundles.defaultStartLevel=4",
                    "-Declipse.product=org.eclipse.jdt.ls.core.product",
                    "-Dlog.level=ALL",
                    "-noverify",
                    "-jar",
                    launcherPath.toAbsolutePath().normalize().toString(),
                    "-configuration",
                    launcherPath.getParent().getParent().resolve("config_win").toAbsolutePath().normalize().toString(),
                    "-data").redirectError(ProcessBuilder.Redirect.INHERIT).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void kill() {
        this.process.destroy();
    }

    public boolean awaitTermination(long timeout, TimeUnit unit) {
        try {
            return this.process.waitFor(timeout, unit);
        } catch (InterruptedException e) {
            return !this.process.isAlive();
        }
    }

    public InputStream getInputStream() {
        return this.process.getInputStream();
    }

    public OutputStream getOutputStream() {
        return this.process.getOutputStream();
    }
}
