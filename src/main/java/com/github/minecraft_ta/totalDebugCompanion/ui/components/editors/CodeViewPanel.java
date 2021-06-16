package com.github.minecraft_ta.totalDebugCompanion.ui.components.editors;

import com.github.minecraft_ta.totalDebugCompanion.GlobalConfig;
import com.github.minecraft_ta.totalDebugCompanion.model.CodeView;
import com.github.minecraft_ta.totalDebugCompanion.server.CompanionAppServer;
import com.github.minecraft_ta.totalDebugCompanion.util.UIUtils;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Utilities;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Rectangle2D;
import java.io.IOException;

public class CodeViewPanel extends JScrollPane {

    public static Font JETBRAINS_MONO_FONT = null;
    static {
        try {
            JETBRAINS_MONO_FONT = Font.createFont(Font.TRUETYPE_FONT, CodeViewPanel.class.getResourceAsStream("/font/jetbrainsmono_regular.ttf"));
        } catch (FontFormatException | IOException e) {
            e.printStackTrace();
        }
    }

    private final JTextPane editorPane = new JTextPane();
    private final JTextArea lineNumbers = new JTextArea();

    private int lineCount;

    public CodeViewPanel(CodeView codeView) {
        super();

        editorPane.setEditable(false);
        editorPane.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int offset = editorPane.viewToModel2D(e.getPoint());
                Rectangle2D modelView;
                try {
                    modelView = editorPane.modelToView2D(offset);
                    //Did we click to the right of a line, and the cursor got adjusted to the left?
                    if (modelView.getX() < e.getX() && offset == Utilities.getRowEnd(editorPane, offset))
                        return;
                } catch (BadLocationException ex) {
                    throw new RuntimeException("Offset not in view", ex);
                }

                int line = editorPane.getDocument().getDefaultRootElement().getElementIndex(offset);
                int column = (offset - editorPane.getDocument().getDefaultRootElement().getElement(line).getStartOffset());

                CompanionAppServer.getInstance().writeBatch(out -> {
                    out.write(2);
                    out.writeUTF(codeView.getPath().getFileName().toString());
                    out.writeInt(line);
                    out.writeInt(column);
                });
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

        lineNumbers.setEditable(false);
        lineNumbers.setForeground(Color.GRAY);
        lineNumbers.setHighlighter(null);

        setViewportView(UIUtils.horizontalLayout(lineNumbers, editorPane));
        setBorder(BorderFactory.createEmptyBorder());

        initFonts();
        initScrolling();
        GlobalConfig.getInstance().addPropertyChangeListener("scrollMul", event -> initScrolling());
        GlobalConfig.getInstance().addPropertyChangeListener("fontSize", event -> initFonts());
    }

    public void setCode(String code) {
        editorPane.setText(code);

        this.lineCount = code.split("\n").length;
        int lineNumberLength = (this.lineCount + "").length();

        var lineNumberTextBuilder = new StringBuilder();
        for (int i = 0; i < this.lineCount; i++) {
            lineNumberTextBuilder.append(String.format("%" + lineNumberLength + "d", i + 1));

            if (i != this.lineCount - 1)
                lineNumberTextBuilder.append("\n");
        }

        lineNumbers.setText(lineNumberTextBuilder.toString());
        //Adjust max width, otherwise it will take up too much space
        int charsWidth = lineNumbers.getFontMetrics(lineNumbers.getFont())
                .charsWidth("9".repeat(lineNumberLength).toCharArray(), 0, lineNumberLength);
        lineNumbers.setColumns(lineNumberLength);
        lineNumbers.setMaximumSize(new Dimension(charsWidth, Integer.MAX_VALUE));
    }

    public void focusLine(int line) {
        this.getVerticalScrollBar().setValue((line - 1) * (this.getVerticalScrollBar().getMaximum() / this.lineCount));
    }

    private void initFonts() {
        editorPane.setFont(JETBRAINS_MONO_FONT.deriveFont(GlobalConfig.getInstance().<Float>getValue("fontSize")));
        lineNumbers.setFont(editorPane.getFont());
    }

    private void initScrolling() {
        FontMetrics metrics = getFontMetrics(editorPane.getFont());
        int lineHeight = metrics.getHeight();
        int charWidth = metrics.getMaxAdvance();

        JScrollBar systemVBar = new JScrollBar(JScrollBar.VERTICAL);
        JScrollBar systemHBar = new JScrollBar(JScrollBar.HORIZONTAL);
        int verticalIncrement = systemVBar.getUnitIncrement();
        int horizontalIncrement = systemHBar.getUnitIncrement();

        float mul = GlobalConfig.getInstance().<Float>getValue("scrollMul");

        getVerticalScrollBar().setUnitIncrement((int) (lineHeight * verticalIncrement * mul));
        getHorizontalScrollBar().setUnitIncrement((int) (charWidth * horizontalIncrement * mul));
    }

    public JTextPane getEditorPane() {
        return editorPane;
    }
}
