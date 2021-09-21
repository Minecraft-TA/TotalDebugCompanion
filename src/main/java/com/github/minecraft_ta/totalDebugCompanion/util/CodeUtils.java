package com.github.minecraft_ta.totalDebugCompanion.util;

import com.github.javaparser.JavaParser;
import com.github.javaparser.JavaToken;
import com.github.javaparser.Range;
import com.github.javaparser.TokenRange;
import org.eclipse.lsp4j.SemanticTokensLegend;

import javax.swing.*;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private static final Map<Integer, MutableAttributeSet> colorToAttributeSetMap = new HashMap<>();

    static {
        colorToAttributeSetMap.put(21, KEYWORD_ATTRIBUTES);
        colorToAttributeSetMap.put(25, TYPE_ATTRIBUTES);
        colorToAttributeSetMap.put(28, STRING_LITERAL_ATTRIBUTES);
//        colorToAttributeSetMap.put(32, PACKAGE_ATTRIBUTES);
        colorToAttributeSetMap.put(136, PROPERTY_ATTRIBUTES);
        colorToAttributeSetMap.put(162, METHOD_ATTRIBUTES);
        colorToAttributeSetMap.put(166, TYPE_ATTRIBUTES);
        colorToAttributeSetMap.put(197, LITERAL_ATTRIBUTES);
//        colorToAttributeSetMap.put(242, SEPARATOR_ATTRIBUTES);
//        colorToAttributeSetMap.put(249, LABEL_ATTRIBUTES);

        StyleConstants.setForeground(KEYWORD_ATTRIBUTES, Color.decode("#C679DD"));
        StyleConstants.setForeground(LITERAL_ATTRIBUTES, Color.decode("#D19A66"));
        StyleConstants.setForeground(STRING_LITERAL_ATTRIBUTES, Color.decode("#98C379"));
        StyleConstants.setForeground(SEPARATOR_ATTRIBUTES, Color.decode("#778899"));
        StyleConstants.setForeground(COMMENT_ATTRIBUTES, Color.decode("#59626F"));
        StyleConstants.setForeground(METHOD_ATTRIBUTES, Color.decode("#61AEEF"));
        StyleConstants.setForeground(TYPE_ATTRIBUTES, Color.decode("#E5C17C"));
        StyleConstants.setForeground(PROPERTY_ATTRIBUTES, Color.decode("#E06C75"));
    }

    public static void highlightAndSetJavaCodeAnsi(JTextPane component, String ansiText) {
        var newString = new StringBuilder((int) (ansiText.length() * 0.75));
        var highlights = new ArrayList<HighlightData>();

        int currentBlockStartOffset = 0;
        boolean inBlock = false;
        MutableAttributeSet currentColor = null;
        //Parse ansi escape codes
        for (int i = 0; i < ansiText.length(); ) {
            char c = ansiText.charAt(i);

            if (!inBlock && c == '\u001b') { //Start
                // i + 1 == '[' //Prefix
                // i + 2 == '0' //Style
                // i + 3.. == (';38;5;[number]m' || 'm')

                if (ansiText.charAt(i + 3) == 'm') {
                    i += 4;
                } else {
                    i += 9; //Skip '[0;38;5'
                    var colorString = new StringBuilder();
                    for (int j = i; j < i + 3; j++) { //Color is max 3 chars long
                        char colorChar = ansiText.charAt(j);
                        if (colorChar == 'm')
                            break;
                        colorString.append(colorChar);
                    }

                    var colorId = Integer.parseInt(colorString.toString());
                    currentColor = colorToAttributeSetMap.get(colorId);

                    i += (colorString.length() - 1) + 2; //Jump after 'm'
                }

                currentBlockStartOffset = newString.length();
                inBlock = true;
            } else if (inBlock && c == '\u001b') { //End
                i += 3; //Skip '[m'

                if (currentColor != null)
                    highlights.add(new HighlightData(currentBlockStartOffset, newString.length(), currentColor));
                currentColor = null;
                inBlock = false;
            } else {
                newString.append(c);
                i++;
            }
        }

        component.setText(newString.toString());
        for (HighlightData highlight : highlights) {
            component.getStyledDocument().setCharacterAttributes(highlight.offsetStart, highlight.offsetEnd - highlight.offsetStart, highlight.attributeSet, true);
        }
    }

    public static void highlightJavaCodeJavaParser(JTextPane component) {
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
            default -> null;
        };
    }

    public static void setTokenLegend(SemanticTokensLegend legend) {
        tokenLegend = legend;
    }

    record HighlightData(int offsetStart, int offsetEnd, MutableAttributeSet attributeSet) {}
}
