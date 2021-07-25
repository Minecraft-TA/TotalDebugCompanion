package com.github.minecraft_ta.totalDebugCompanion.util;

public class TextUtils {

    public static int asIntOrDefault(String text, int defaultValue) {
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
