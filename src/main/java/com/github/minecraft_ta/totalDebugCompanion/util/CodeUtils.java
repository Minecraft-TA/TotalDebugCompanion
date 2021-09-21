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
    private static final SimpleAttributeSet SEPARATOR_ATTRIBUTES = new SimpleAttributeSet();
    private static final SimpleAttributeSet COMMENT_ATTRIBUTES = new SimpleAttributeSet();
    private static final SimpleAttributeSet METHOD_ATTRIBUTES = new SimpleAttributeSet();
    private static final SimpleAttributeSet TYPE_ATTRIBUTES = new SimpleAttributeSet();
    private static final SimpleAttributeSet PROPERTY_ATTRIBUTES = new SimpleAttributeSet();

    private static SemanticTokensLegend tokenLegend;

    static {
        StyleConstants.setForeground(KEYWORD_ATTRIBUTES, Color.decode("#C679DD"));
        StyleConstants.setForeground(LITERAL_ATTRIBUTES, Color.decode("#D19A66"));
        StyleConstants.setForeground(STRING_LITERAL_ATTRIBUTES, Color.decode("#98C379"));
        StyleConstants.setForeground(SEPARATOR_ATTRIBUTES, Color.decode("#778899"));
        StyleConstants.setForeground(COMMENT_ATTRIBUTES, Color.decode("#59626F"));
        StyleConstants.setForeground(METHOD_ATTRIBUTES, Color.decode("#61AEEF"));
        StyleConstants.setForeground(TYPE_ATTRIBUTES, Color.decode("#E5C17C"));
        StyleConstants.setForeground(PROPERTY_ATTRIBUTES, Color.decode("#E06C75"));
    }

    private static void highlightWithJavaParser(JTextPane component, boolean simple) {
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
            var attributes = simple ? getSimpleColorCode(javaToken) : getColorCode(javaToken);

            if (attributes != null) {
                var rootElement = component.getDocument().getDefaultRootElement();
                var element = rootElement.getElement(range.begin.line - 1);

                component.getStyledDocument().setCharacterAttributes(element.getStartOffset() + range.begin.column - 1, range.end.column - range.begin.column + 1, attributes, true);
            }
        }
    }

    public static void highlightJavaCodeSimple(JTextPane component) {
        highlightWithJavaParser(component, true);
    }

    public static void highlightJavaCodeAdvanced(JTextPane component) {
        highlightWithJavaParser(component, false);
    }

    public static void highlightJavaCodeSemanticTokens(List<Integer> data, JTextPane component) {
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
            var tokenModifiers = data.get(i + 4);
            /*var tokenModifierList = IntStream.range(0, 12)
                    .map(shift -> ((tokenModifiers >> shift) & 0b1) == 1 ? shift : -1)
                    .filter(shift -> shift != -1)
                    .mapToObj(index -> tokenLegend.getTokenModifiers().get(index)).toList();*/

            var colors = switch (tokenType) {
                case "method" -> METHOD_ATTRIBUTES;
                case "class" -> TYPE_ATTRIBUTES;
                case "property" -> PROPERTY_ATTRIBUTES;
                case "namespace", "modifier", "parameter" -> null;
                default -> {
                    System.out.println("Unknown highlight token" + tokenType);
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
        return switch (token.getCategory()) {
            case KEYWORD -> KEYWORD_ATTRIBUTES;
            case LITERAL -> {
                if (token.getKind() == JavaToken.Kind.STRING_LITERAL.getKind())
                    yield STRING_LITERAL_ATTRIBUTES;
                yield LITERAL_ATTRIBUTES;
            }
            case COMMENT -> COMMENT_ATTRIBUTES;
            case OPERATOR -> METHOD_ATTRIBUTES;
            case SEPARATOR -> SEPARATOR_ATTRIBUTES;
            default -> null;
        };
    }

    private static SimpleAttributeSet getSimpleColorCode(JavaToken token) {
        return switch (token.getCategory()) {
            case KEYWORD -> KEYWORD_ATTRIBUTES;
            case LITERAL -> {
                if (token.getKind() == JavaToken.Kind.STRING_LITERAL.getKind())
                    yield STRING_LITERAL_ATTRIBUTES;
                yield LITERAL_ATTRIBUTES;
            }
            case COMMENT -> COMMENT_ATTRIBUTES;
            default -> null;
        };
    }

    public static void setTokenLegend(SemanticTokensLegend legend) {
        tokenLegend = legend;
    }
}
