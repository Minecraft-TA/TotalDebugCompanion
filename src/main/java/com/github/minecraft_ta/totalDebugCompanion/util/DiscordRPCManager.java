package com.github.minecraft_ta.totalDebugCompanion.util;


import de.jcm.discordgamesdk.Core;
import de.jcm.discordgamesdk.CreateParams;
import de.jcm.discordgamesdk.activity.Activity;

import java.io.File;
import java.time.Instant;
import java.util.Locale;

public class DiscordRPCManager {
    private final Core core;
    private final Activity activity;

    public DiscordRPCManager() {
        File discordLibrary = loadDiscordGameSDK();

        Core.init(discordLibrary);

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
        callbackThread.start();
    }

    private File loadDiscordGameSDK() {
        String osName = System.getProperty("os.name").toLowerCase(Locale.ROOT);
        String arch = System.getProperty("os.arch").toLowerCase(Locale.ROOT);

        String suffix;
        if (osName.contains("windows")) {
            suffix = ".dll";
        } else if (osName.contains("linux")) {
            suffix = ".so";
        } else if (osName.contains("mac os")) {
            suffix = ".dylib";
        } else {
            throw new RuntimeException("cannot determine OS type: " + osName);
        }

        if (arch.equals("amd64")) {
            arch = "x86_64";
        }

        String path = "src/main/resources/discord/lib/" + arch + "/discord_game_sdk" + suffix;
        return new File(path);
    }

    public void setDetails(String details) {
        activity.setDetails(details);
        updateActivity();
    }

    public void setState(String state) {
        activity.setState(state);
        updateActivity();
    }

    private void updateActivity() {
        core.activityManager().updateActivity(activity);
    }
}
