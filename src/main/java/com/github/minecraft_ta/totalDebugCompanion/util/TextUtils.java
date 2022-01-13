package com.github.minecraft_ta.totalDebugCompanion.util;

public class TextUtils {

    public static int asIntOrDefault(String text, int defaultValue) {
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public static int countNewLines(String s) {
        int count = 0;

        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == '\n')
                count++;
        }

        return count;
    }

    public static String htmlHighlightString(String primary, String divider, String secondary) {
        return "<html><span style='color: rgb(187, 187, 187)'>%s</span>%s<span style='color: rgb(150, 150, 150)'>%s</span></html>".formatted(primary, divider, secondary);
    }
}
