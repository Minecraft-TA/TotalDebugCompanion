package com.github.minecraft_ta.totalDebugCompanion.util;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.text.*;
import javax.swing.undo.UndoableEdit;
import java.awt.*;

public class UIUtils {

    public static void setTextAndKeepCaret(JTextComponent component, String text) {
        var caretPos = component.getCaretPosition();
        component.setText(text);
        if (caretPos <= text.length())
            component.setCaretPosition(caretPos);
    }

    public static void setIntegerTextFieldEnabled(JTextComponent component) {
        var document = component.getDocument();
        if (!(document instanceof AbstractDocument))
            throw new IllegalArgumentException();

        ((AbstractDocument) document).setDocumentFilter(new DocumentFilter() {
            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) {
                var newText = new StringBuffer(component.getText()).replace(offset, offset + length, text).toString();
                try {
                    //Allow empty field and starting with a minus
                    if (!newText.isBlank() && !newText.equals("-"))
                        Integer.parseInt(newText);
                    super.replace(fb, offset, length, text, attrs);
                } catch (Throwable ignored) {}
            }
        });
    }

    public static int getFontWidth(JComponent component, String s) {
        return component.getFontMetrics(component.getFont()).stringWidth(s);
    }

    public static <T extends JComponent> T withBorder(T c, Border border) {
        c.setBorder(border);
        return c;
    }

    public static Component topAndBottomStickyLayout(Component top, Component bottom) {
        return verticalLayout(top, Box.createVerticalGlue(), bottom);
    }

    public static JComponent verticalLayout(Component... component) {
        var box = Box.createVerticalBox();
        for (Component c : component) {
            box.add(c);
        }

        return box;
    }

    public static Component horizontalLayout(Component... component) {
        var box = Box.createHorizontalBox();
        for (Component c : component) {
            box.add(c, Box.LEFT_ALIGNMENT);
        }

        return box;
    }

    public static String getText(JTextComponent c) {
        try {
            return c.getDocument().getText(0, c.getDocument().getLength());
        } catch (BadLocationException e) {
            throw new RuntimeException(e);
        }
    }

    public static int posToOffset(JTextComponent c, org.eclipse.lsp4j.Position pos) {
        var offset = c.getDocument().getDefaultRootElement().getElement(pos.getLine()).getStartOffset();
        offset += pos.getCharacter();
        return offset;
    }

    public static org.eclipse.lsp4j.Position offsetToPosition(JTextComponent c, int offset) {
        var defaultRootElement = c.getDocument().getDefaultRootElement();
        var line = defaultRootElement.getElementIndex(offset);
        var lineElement = defaultRootElement.getElement(line);
        var column = (offset - lineElement.getStartOffset());
        return new org.eclipse.lsp4j.Position(line, column);
    }

    public static DocumentEvent.EventType getDocumentEventTypeFromEdit(UndoableEdit edit) {
        try {
            var field = edit.getClass().getSuperclass().getDeclaredField("type");
            field.setAccessible(true);
            return (DocumentEvent.EventType) field.get(edit);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static void focusWindow(JFrame frame) {
        frame.setVisible(true);
        int state = frame.getExtendedState();
        state &= ~JFrame.ICONIFIED;
        frame.setExtendedState(state);
        frame.setAlwaysOnTop(true);
        frame.toFront();
        frame.requestFocus();
        frame.setAlwaysOnTop(false);
    }

    public static void centerJFrame(JFrame frame) {
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setLocation(dim.width / 2 - frame.getSize().width / 2, dim.height / 2 - frame.getSize().height / 2);
    }
}
