package com.github.minecraft_ta.totalDebugCompanion.util;

import com.github.javaparser.JavaParser;
import com.github.javaparser.JavaToken;
import com.github.javaparser.Range;
import com.github.javaparser.TokenRange;
import org.eclipse.lsp4j.SemanticTokensLegend;

import javax.swing.*;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import java.awt.*;
import java.util.List;

public class CodeUtils {

    private static final SimpleAttributeSet KEYWORD_ATTRIBUTES = new SimpleAttributeSet();
    private static final SimpleAttributeSet LITERAL_ATTRIBUTES = new SimpleAttributeSet();
    private static final SimpleAttributeSet STRING_LITERAL_ATTRIBUTES = new SimpleAttributeSet();
    private static final SimpleAttributeSet OPERATOR_ATTRIBUTES = new SimpleAttributeSet();
    private static final SimpleAttributeSet SEPARATOR_ATTRIBUTES = new SimpleAttributeSet();
    private static final SimpleAttributeSet COMMENT_ATTRIBUTES = new SimpleAttributeSet();
    private static final SimpleAttributeSet METHOD_ATTRIBUTES = new SimpleAttributeSet();
    private static final SimpleAttributeSet TYPE_ATTRIBUTES = new SimpleAttributeSet();

    private static SemanticTokensLegend tokenLegend;

    static {
        StyleConstants.setForeground(KEYWORD_ATTRIBUTES, Color.decode("#CC7832"));
        StyleConstants.setForeground(LITERAL_ATTRIBUTES, Color.decode("#6897BB"));
        StyleConstants.setForeground(STRING_LITERAL_ATTRIBUTES, Color.decode("#98C379"));
        StyleConstants.setForeground(OPERATOR_ATTRIBUTES, Color.decode("#A1C17E"));
        StyleConstants.setForeground(SEPARATOR_ATTRIBUTES, Color.decode("#778899"));
        StyleConstants.setForeground(COMMENT_ATTRIBUTES, Color.decode("#59626F"));
        StyleConstants.setForeground(METHOD_ATTRIBUTES, new Color(150, 130, 200));
        StyleConstants.setForeground(TYPE_ATTRIBUTES, new Color(194, 163, 101));
    }

    public static void highlightJavaCode(JTextPane component) {
        String code = UIUtils.getText(component);

        TokenRange globalTokenRange;
        try {
            var javaParser = new JavaParser();
            globalTokenRange = javaParser.parse(code).getResult().get().getTokenRange().get();
        } catch (Throwable t) {
            return;
        }

        for (JavaToken javaToken : globalTokenRange) {
            Range range = javaToken.getRange().get();
            var attributes = getColorCode(javaToken);

            if (attributes != null) {
                var rootElement = component.getDocument().getDefaultRootElement();
                var element = rootElement.getElement(range.begin.line - 1);

                component.getStyledDocument().setCharacterAttributes(element.getStartOffset() + range.begin.column - 1, range.end.column - range.begin.column + 1, attributes, true);
            }
        }
    }

    public static void highlightSemanticJavaCode(List<Integer> data, JTextPane component) {
        int line = 0;
        int column = 0;
        for (int i = 0; i < data.size(); i += 5) {
            var relativeLine = data.get(i);
            if (relativeLine != 0)
                column = 0;
            line += relativeLine;
            column += data.get(i + 1);

            var tokenLength = data.get(i + 2);
            var tokenType = tokenLegend.getTokenTypes().get(data.get(i + 3));
            //Token modifiers are ignored for now

            var colors = switch (tokenType) {
                case "method" -> METHOD_ATTRIBUTES;
                case "class" -> TYPE_ATTRIBUTES;
//                case "namespace" -> KEYWORD_ATTRIBUTES;
                default -> {
                    System.out.println("unknown " + tokenType);
                    yield null;
                }
            };

            if (colors != null) {
                var rootElement = component.getDocument().getDefaultRootElement();
                var element = rootElement.getElement(line);

                component.getStyledDocument().setCharacterAttributes(element.getStartOffset() + column, tokenLength, colors, true);
            }
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

    public static void setTokenLegend(SemanticTokensLegend legend) {
        tokenLegend = legend;
    }
}
