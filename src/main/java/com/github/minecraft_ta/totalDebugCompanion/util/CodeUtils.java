package com.github.minecraft_ta.totalDebugCompanion.util;

import com.github.javaparser.JavaParser;
import com.github.javaparser.JavaToken;
import com.github.javaparser.Range;
import com.github.javaparser.TokenRange;

import javax.swing.*;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import java.awt.*;

public class CodeUtils {

    private static final SimpleAttributeSet KEYWORD_ATTRIBUTES = new SimpleAttributeSet();
    private static final SimpleAttributeSet LITERAL_ATTRIBUTES = new SimpleAttributeSet();
    private static final SimpleAttributeSet STRING_LITERAL_ATTRIBUTES = new SimpleAttributeSet();
    private static final SimpleAttributeSet OPERATOR_ATTRIBUTES = new SimpleAttributeSet();
    private static final SimpleAttributeSet SEPARATOR_ATTRIBUTES = new SimpleAttributeSet();
    private static final SimpleAttributeSet COMMENT_ATTRIBUTES = new SimpleAttributeSet();
    static {
        StyleConstants.setForeground(KEYWORD_ATTRIBUTES, Color.decode("#CC7832"));
        StyleConstants.setForeground(LITERAL_ATTRIBUTES, Color.decode("#6897BB"));
        StyleConstants.setForeground(STRING_LITERAL_ATTRIBUTES, Color.decode("#98C379"));
        StyleConstants.setForeground(OPERATOR_ATTRIBUTES, Color.decode("#A1C17E"));
        StyleConstants.setForeground(SEPARATOR_ATTRIBUTES, Color.decode("#778899"));
        StyleConstants.setForeground(COMMENT_ATTRIBUTES, Color.decode("#59626F"));
    }

    public static void highlightJavaCode(String code, JTextPane component) {
        TokenRange globalTokenRange;
        try {
            var javaParser = new JavaParser();
            globalTokenRange = javaParser.parse(code).getResult().get().getTokenRange().get();
        } catch (Throwable t) {
            return;
        }

        int offset = 0;

        for (JavaToken javaToken : globalTokenRange) {
            Range range = javaToken.getRange().get();
            var attributes = getColorCode(javaToken);

            int from = range.begin.column - 1;
            int to = range.end.column;

            if (attributes != null) {
                component.getStyledDocument().setCharacterAttributes(offset, to - from, attributes, true);
            }

            offset += to - from;
        }
    }

    private static SimpleAttributeSet getColorCode(JavaToken token) {
        switch (token.getCategory()) {
            case KEYWORD:
                return KEYWORD_ATTRIBUTES;
            case LITERAL:
                if (token.getKind() == JavaToken.Kind.STRING_LITERAL.getKind())
                    return STRING_LITERAL_ATTRIBUTES;
                return LITERAL_ATTRIBUTES;
            case OPERATOR:
                return OPERATOR_ATTRIBUTES;
            case SEPARATOR:
                return SEPARATOR_ATTRIBUTES;
            case COMMENT:
                return COMMENT_ATTRIBUTES;
        }

        return null;
    }
}
