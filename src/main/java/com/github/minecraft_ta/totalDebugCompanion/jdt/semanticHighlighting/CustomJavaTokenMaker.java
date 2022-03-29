package com.github.minecraft_ta.totalDebugCompanion.jdt.semanticHighlighting;

import com.github.minecraft_ta.totalDebugCompanion.jdt.diagnostics.ASTCache;
import org.fife.ui.rsyntaxtextarea.Token;
import org.fife.ui.rsyntaxtextarea.TokenTypes;
import org.fife.ui.rsyntaxtextarea.modes.JavaTokenMaker;

import javax.swing.*;
import javax.swing.text.Segment;
import java.util.HashMap;
import java.util.Map;

public class CustomJavaTokenMaker extends JavaTokenMaker {

    private final Map<Integer, Integer> overwrittenTokenTypes = new HashMap<>();

    public void setASTKey(String identifier, JComponent textComponent) {
        ASTCache.addChangeListener(identifier, (ast) -> {
            synchronized (this.overwrittenTokenTypes) {
                overwrittenTokenTypes.clear();
                ast.accept(new SemanticTokensVisitor(overwrittenTokenTypes));
            }

            SwingUtilities.invokeLater(textComponent::repaint);
        });
    }

    @Override
    public Token getTokenList(Segment text, int initialTokenType, int startOffset) {
        var firstToken = super.getTokenList(text, initialTokenType, startOffset);
        var currentToken = firstToken;

        synchronized (this.overwrittenTokenTypes) {
            while (currentToken != null) {
                if (currentToken.getType() != TokenTypes.NULL) {
                    var token = this.overwrittenTokenTypes.get(currentToken.getOffset());
                    if (token != null)
                        currentToken.setType(token);
                }

                currentToken = currentToken.getNextToken();
            }
        }

        return firstToken;
    }
}
