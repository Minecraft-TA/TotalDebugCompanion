package com.github.minecraft_ta.totalDebugCompanion.util;

import com.github.minecraft_ta.totalDebugCompanion.jdt.semanticHighlighting.ShadowedTokenTypes;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxScheme;
import org.fife.ui.rsyntaxtextarea.TokenTypes;

import javax.swing.*;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class CodeUtils {

    private static final SimpleAttributeSet KEYWORD_ATTRIBUTES = new SimpleAttributeSet();
    private static final SimpleAttributeSet LITERAL_ATTRIBUTES = new SimpleAttributeSet();
    private static final SimpleAttributeSet STRING_LITERAL_ATTRIBUTES = new SimpleAttributeSet();
    private static final SimpleAttributeSet SEPARATOR_ATTRIBUTES = new SimpleAttributeSet();
    private static final SimpleAttributeSet COMMENT_ATTRIBUTES = new SimpleAttributeSet();
    private static final SimpleAttributeSet METHOD_ATTRIBUTES = new SimpleAttributeSet();
    private static final SimpleAttributeSet TYPE_ATTRIBUTES = new SimpleAttributeSet();
    private static final SimpleAttributeSet ITALIC_TYPE_ATTRIBUTES = new SimpleAttributeSet();
    private static final SimpleAttributeSet PROPERTY_ATTRIBUTES = new SimpleAttributeSet();

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
        StyleConstants.setForeground(ITALIC_TYPE_ATTRIBUTES, StyleConstants.getForeground(TYPE_ATTRIBUTES));
        StyleConstants.setItalic(ITALIC_TYPE_ATTRIBUTES, true);
        StyleConstants.setForeground(PROPERTY_ATTRIBUTES, Color.decode("#E06C75"));
    }

    public static void initJavaColors(SyntaxScheme scheme) {
        scheme.getStyle(TokenTypes.RESERVED_WORD).foreground = Color.decode("#C679DD");
        scheme.getStyle(TokenTypes.RESERVED_WORD_2).foreground = Color.decode("#C679DD");
        scheme.getStyle(TokenTypes.DATA_TYPE).foreground = Color.decode("#C679DD");
        scheme.getStyle(TokenTypes.VARIABLE).foreground = Color.decode("#C67900");

        scheme.getStyle(TokenTypes.LITERAL_BOOLEAN).foreground = Color.decode("#D19A66");
        scheme.getStyle(TokenTypes.LITERAL_NUMBER_DECIMAL_INT).foreground = Color.decode("#D19A66");
        scheme.getStyle(TokenTypes.LITERAL_NUMBER_FLOAT).foreground = Color.decode("#D19A66");
        scheme.getStyle(TokenTypes.LITERAL_NUMBER_HEXADECIMAL).foreground = Color.decode("#D19A66");

        scheme.getStyle(TokenTypes.LITERAL_STRING_DOUBLE_QUOTE).foreground = Color.decode("#98C379");
        scheme.getStyle(TokenTypes.LITERAL_CHAR).foreground = Color.decode("#98C379");

        scheme.getStyle(TokenTypes.COMMENT_MULTILINE).foreground = Color.decode("#6e7f7f");
        scheme.getStyle(TokenTypes.COMMENT_MARKUP).foreground = Color.decode("#6e7f7f");
        scheme.getStyle(TokenTypes.COMMENT_DOCUMENTATION).foreground = Color.decode("#6e7f7f");
        scheme.getStyle(TokenTypes.COMMENT_EOL).foreground = Color.decode("#6e7f7f");

        scheme.getStyle(TokenTypes.SEPARATOR).foreground = UIManager.getColor("EditorPane.foreground");
        scheme.getStyle(TokenTypes.OPERATOR).foreground = UIManager.getColor("EditorPane.foreground");

        scheme.getStyle(TokenTypes.ANNOTATION).foreground = Color.decode("#E5C17C");
        scheme.getStyle(ShadowedTokenTypes.TYPE).foreground = Color.decode("#E5C17C");

        scheme.getStyle(TokenTypes.FUNCTION).foreground = Color.decode("#61AEEF");
        scheme.getStyle(ShadowedTokenTypes.FIELD).foreground = Color.decode("#E06C75");
    }

    public static void initSyntaxScheme(RSyntaxTextArea component) {
        component.setSyntaxEditingStyle(RSyntaxTextArea.SYNTAX_STYLE_JAVA);
        CodeUtils.initJavaColors(component.getSyntaxScheme());
    }
}
