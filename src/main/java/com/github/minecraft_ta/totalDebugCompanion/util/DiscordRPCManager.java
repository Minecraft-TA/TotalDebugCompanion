package com.github.minecraft_ta.totalDebugCompanion.util;


import de.jcm.discordgamesdk.Core;
import de.jcm.discordgamesdk.CreateParams;
import de.jcm.discordgamesdk.activity.Activity;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.time.Instant;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class DiscordRPCManager {
    private Core core;
    private Activity activity;
    private String lastActivity = "";
    private boolean enabled = true;

    public DiscordRPCManager() {
        try {
            File discordLibrary = downloadDiscordLibrary();
            Core.init(discordLibrary);
        } catch (IOException e) {
            this.enabled = false;
            System.err.println("Failed to download Discord Game SDK: ");
            e.printStackTrace();
            return;
        }

        try (CreateParams params = new CreateParams()) {
            params.setClientID(1116710018369204286L);
            params.setFlags(CreateParams.getDefaultFlags());
            this.core = new Core(params);
        }

        this.activity = new Activity();
        this.activity.timestamps().setStart(Instant.now());
        this.activity.assets().setLargeImage("default");
        updateActivity();

        Thread callbackThread = new Thread(() -> {
            while (true) {
                core.runCallbacks();
                try {
                    Thread.sleep(16);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        callbackThread.setDaemon(true);
        callbackThread.start();
    }

    private static File downloadDiscordLibrary() throws IOException {
        File tempDir = new File(System.getProperty("java.io.tmpdir"), "total_debug_discord_game_sdk");

        // Find out which name Discord's library has (.dll for Windows, .so for Linux)
        String name = "discord_game_sdk";
        String suffix;

        String osName = System.getProperty("os.name").toLowerCase(Locale.ROOT);
        String arch = System.getProperty("os.arch").toLowerCase(Locale.ROOT);

        if (osName.contains("windows")) {
            suffix = ".dll";
        } else if (osName.contains("linux")) {
            suffix = ".so";
        } else if (osName.contains("mac os")) {
            suffix = ".dylib";
        } else {
            throw new RuntimeException("cannot determine OS type: " + osName);
        }

		/*
		Some systems report "amd64" (e.g. Windows and Linux), some "x86_64" (e.g. Mac OS).
		At this point we need the "x86_64" version, as this one is used in the ZIP.
		 */
        if (arch.equals("amd64"))
            arch = "x86_64";

        // Path of Discord's library inside the ZIP
        String zipPath = "lib/" + arch + "/" + name + suffix;

        // Return the file if it already exists
        File file = new File(tempDir, name + suffix);
        if (file.exists())
            return file;

        // Open the URL as a ZipInputStream
        URL downloadUrl = new URL("https://dl-game-sdk.discordapp.net/2.5.6/discord_game_sdk.zip");
        ZipInputStream zin = new ZipInputStream(downloadUrl.openStream());

        // Search for the right file inside the ZIP
        ZipEntry entry;
        while ((entry = zin.getNextEntry()) != null) {
            if (entry.getName().equals(zipPath)) {
                // Create a new temporary directory
                // We need to do this, because we may not change the filename on Windows
                if (!tempDir.mkdir())
                    throw new IOException("Cannot create temporary directory");

                File temp = new File(tempDir, name + suffix);

                Files.copy(zin, temp.toPath());

                zin.close();

                return temp;
            }
            zin.closeEntry();
        }
        zin.close();
        return null;
    }

    public void setDetails(String details) {
        if (!enabled) return;
        activity.setDetails(details);
        updateActivity();
    }

    public void setState(String state, boolean saveLastState) {
        if (!enabled) return;
        if (saveLastState) this.lastActivity = state;
        activity.setState(state);
        updateActivity();
    }

    public void loadLastState() {
        if (!enabled) return;
        activity.setState(lastActivity);
        updateActivity();
    }

    private void updateActivity() {
        if (!enabled) return;
        core.activityManager().updateActivity(activity);
    }
}
