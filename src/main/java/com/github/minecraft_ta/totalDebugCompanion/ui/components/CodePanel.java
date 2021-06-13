package com.github.minecraft_ta.totalDebugCompanion.ui.components;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class CodePanel extends JScrollPane {

    private static Font JETBRAINS_MONO_FONT = null;
    static {
        try {
            JETBRAINS_MONO_FONT = Font.createFont(Font.TRUETYPE_FONT, CodePanel.class.getResourceAsStream("/font/jetbrainsmono_regular.ttf"));
        } catch (FontFormatException | IOException e) {
            e.printStackTrace();
        }
    }

    private final JTextPane editorPane = new JTextPane();

    public CodePanel() {
        super();
        editorPane.setEditable(false);
        editorPane.setText(randomLines());
        editorPane.setFont(JETBRAINS_MONO_FONT.deriveFont(14f));

        var lineNumbers = new JTextArea();
        lineNumbers.setFont(editorPane.getFont());
        lineNumbers.setEditable(false);
        lineNumbers.setForeground(Color.GRAY);
        lineNumbers.setText(Stream.of(editorPane.getText().split("\n")).map(s -> "1").collect(Collectors.joining("\n")));
        lineNumbers.setHighlighter(null);
        lineNumbers.setColumns(1);

        var pane = new JPanel();
        pane.setLayout(new BoxLayout(pane, BoxLayout.X_AXIS));
        pane.add(lineNumbers);
        pane.add(editorPane);
        setViewportView(pane);
        setBorder(BorderFactory.createEmptyBorder());

        fixScrolling();
    }

    public void fixScrolling() {
        FontMetrics metrics = getFontMetrics(editorPane.getFont());
        int lineHeight = metrics.getHeight();
        int charWidth = metrics.getMaxAdvance();

        JScrollBar systemVBar = new JScrollBar(JScrollBar.VERTICAL);
        JScrollBar systemHBar = new JScrollBar(JScrollBar.HORIZONTAL);
        int verticalIncrement = systemVBar.getUnitIncrement();
        int horizontalIncrement = systemHBar.getUnitIncrement();

        getVerticalScrollBar().setUnitIncrement(lineHeight * verticalIncrement);
        getHorizontalScrollBar().setUnitIncrement(charWidth * horizontalIncrement);
    }

    public String randomLines() {
        return IntStream.range(0, (int) (Math.random() * 100)).mapToObj(i -> ThreadLocalRandom.current().ints(500, 0, 9).mapToObj(i2 -> i2 + "").collect(Collectors.joining())).collect(Collectors.joining("\n"));
    }
}
