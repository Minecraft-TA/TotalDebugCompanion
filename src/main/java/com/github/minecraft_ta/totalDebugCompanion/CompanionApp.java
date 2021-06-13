package com.github.minecraft_ta.totalDebugCompanion;

import com.formdev.flatlaf.FlatDarculaLaf;
import com.github.minecraft_ta.totalDebugCompanion.ui.MainWindow;

import java.nio.file.Files;
import java.nio.file.Paths;

public class CompanionApp {

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Missing argument");
            return;
        }

        var rootPath = Paths.get(args[0]);
        if (!Files.exists(rootPath)) {
            System.out.println("Path does not exist");
            return;
        }

        FlatDarculaLaf.setup();

        var mainWindow = new MainWindow(rootPath);
        mainWindow.setSize(1280, 720);
        mainWindow.setVisible(true);
    }
}
