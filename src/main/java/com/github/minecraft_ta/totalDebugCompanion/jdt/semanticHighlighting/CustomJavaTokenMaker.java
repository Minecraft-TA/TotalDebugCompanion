package com.github.minecraft_ta.totalDebugCompanion.jdt.semanticHighlighting;

import com.github.minecraft_ta.totalDebugCompanion.jdt.diagnostics.ASTCache;
import org.fife.ui.rsyntaxtextarea.RSyntaxDocument;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.Token;
import org.fife.ui.rsyntaxtextarea.TokenTypes;
import org.fife.ui.rsyntaxtextarea.modes.JavaTokenMaker;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Segment;
import javax.swing.text.TextAction;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.Map;

public class CustomJavaTokenMaker extends JavaTokenMaker {

    private final Object tokenTypeLock = new Object();
    private Map<Integer, Integer> overwrittenTokenTypes;

    private volatile int lastVisitedVersion;

    public void setASTKey(String identifier, JComponent textComponent) {
        ASTCache.addChangeListener(identifier, (ast, version) -> {
            lastVisitedVersion = version;

            var tokenTypes = new HashMap<Integer, Integer>();
            ast.accept(new SemanticTokensVisitor(tokenTypes));

            if (version != lastVisitedVersion)
                return;

            synchronized (this.tokenTypeLock) {
                this.overwrittenTokenTypes = tokenTypes;
                SwingUtilities.invokeLater(textComponent::repaint);
            }
        });
    }

    @Override
    public Token getTokenList(Segment text, int initialTokenType, int startOffset) {
        var firstToken = super.getTokenList(text, initialTokenType, startOffset);
        var currentToken = firstToken;

        if (this.overwrittenTokenTypes == null)
            return firstToken;

        synchronized (this.tokenTypeLock) {
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

    @Override
    public Action getInsertBreakAction() {
        return new TextAction("customInsertBreak") {

            @Override
            public void actionPerformed(ActionEvent e) {
                var area = (RSyntaxTextArea) getTextComponent(e);
                var document = (RSyntaxDocument) area.getDocument();

                var caretPos = area.getCaretPosition();
                var root = document.getDefaultRootElement();
                var line = root.getElement(root.getElementIndex(caretPos));
                var start = line.getStartOffset();
                var len = line.getEndOffset() - 1 - start;
                try {
                    var lineText = document.getText(start, len);
                    var tabCount = getTabCount(lineText);
                    var builder = new StringBuilder("\n");
                    builder.append("\t".repeat(tabCount + (document.getText(caretPos - 1, 1).equals("{") ? 1 : 0)));

                    caretPos += builder.length();
                    if (getOpenBraceCount(document) > 0)
                        builder.append("\n").append("\t".repeat(tabCount)).append("}");

                    area.insert(builder.toString(), area.getCaretPosition());
                    area.setCaretPosition(caretPos);
                } catch (BadLocationException ex) {
                    ex.printStackTrace();
                }
            }

            private static int getTabCount(String t) {
                var c = 0;
                for (var i = 0; i < t.length(); i++) {
                    if (t.charAt(i) == '\t')
                        c++;
                    else
                        return c;
                }

                return c;
            }

            private static int getOpenBraceCount(RSyntaxDocument doc) {
                int openCount = 0;
                for (Token t : doc) {
                    if (t.getType() == Token.SEPARATOR && t.length() == 1) {
                        char ch = t.charAt(0);
                        if (ch == '{')
                            openCount++;
                        else if (ch == '}')
                            openCount--;
                    }
                }

                return openCount;
            }
        };
    }
}
