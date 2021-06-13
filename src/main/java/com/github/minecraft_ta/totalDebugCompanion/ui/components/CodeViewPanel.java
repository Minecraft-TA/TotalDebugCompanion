package com.github.minecraft_ta.totalDebugCompanion.ui.components;

import com.github.minecraft_ta.totalDebugCompanion.util.UIUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;

public class CodeViewPanel extends JScrollPane {

    private static Font JETBRAINS_MONO_FONT = null;
    static {
        try {
            JETBRAINS_MONO_FONT = Font.createFont(Font.TRUETYPE_FONT, CodeViewPanel.class.getResourceAsStream("/font/jetbrainsmono_regular.ttf"));
        } catch (FontFormatException | IOException e) {
            e.printStackTrace();
        }
    }

    private final JTextPane editorPane = new JTextPane();
    private final JTextArea lineNumbers = new JTextArea();

    public CodeViewPanel() {
        super();

        editorPane.setEditable(false);
        editorPane.setFont(JETBRAINS_MONO_FONT.deriveFont(14f));
        editorPane.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int offset = editorPane.viewToModel2D(e.getPoint());
                int line = editorPane.getDocument().getDefaultRootElement().getElementIndex(offset);
                int column = (offset - editorPane.getDocument().getDefaultRootElement().getElement(line).getStartOffset());
                System.out.println(line + ":" + column);
            }

            @Override
            public void mousePressed(MouseEvent e) {

            }

            @Override
            public void mouseReleased(MouseEvent e) {

            }

            @Override
            public void mouseEntered(MouseEvent e) {

            }

            @Override
            public void mouseExited(MouseEvent e) {

            }
        });

        lineNumbers.setFont(editorPane.getFont());
        lineNumbers.setEditable(false);
        lineNumbers.setForeground(Color.GRAY);
        lineNumbers.setHighlighter(null);

        setViewportView(UIUtils.horizontalLayout(lineNumbers, editorPane));
        setBorder(BorderFactory.createEmptyBorder());

        initScrolling();
    }

    public void setCode(String code) {
        editorPane.setText(code);

        int lineCount = code.split("\n").length;
        int lineNumberLength = (lineCount + "").length();

        var lineNumberTextBuilder = new StringBuilder();
        for (int i = 0; i < lineCount; i++) {
            lineNumberTextBuilder.append(String.format("%" + lineNumberLength + "d", i + 1));

            if (i != lineCount - 1)
                lineNumberTextBuilder.append("\n");
        }

        lineNumbers.setText(lineNumberTextBuilder.toString());
        //Adjust max width, otherwise it will take up too much space
        int charsWidth = lineNumbers.getFontMetrics(lineNumbers.getFont())
                .charsWidth("9".repeat(lineNumberLength).toCharArray(), 0, lineNumberLength);
        lineNumbers.setColumns(lineNumberLength);
        lineNumbers.setMaximumSize(new Dimension(charsWidth, 10000));
    }

    public JTextPane getEditorPane() {
        return editorPane;
    }

    private void initScrolling() {
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
}
