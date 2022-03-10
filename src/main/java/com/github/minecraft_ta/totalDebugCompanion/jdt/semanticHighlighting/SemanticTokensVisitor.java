package com.github.minecraft_ta.totalDebugCompanion.jdt.semanticHighlighting;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.SimpleName;
import org.fife.ui.rsyntaxtextarea.Token;
import org.fife.ui.rsyntaxtextarea.TokenImpl;

import java.util.List;

public class SemanticTokensVisitor extends ASTVisitor {

    private final List<Token> tokens;

    public SemanticTokensVisitor(List<Token> tokens) {
        this.tokens = tokens;
    }

    @Override
    public boolean visit(SimpleName node) {
        var binding = node.resolveBinding();
        if (binding == null)
            return false;

        var type = getTokenType(binding);
        if (type != -1) {
            addToken(node, type);
        }

        return false;
    }

    private int getTokenType(IBinding binding) {
        return switch (binding.getKind()) {
            case IBinding.TYPE -> ShadowedTokenTypes.TYPE;
            default -> -1;
        };
    }

    private void addToken(ASTNode node, int type) {
        var t = new TokenImpl();
        t.textOffset = node.getStartPosition();
        t.textCount = node.getLength();
        t.setType(type);
        this.tokens.add(t);
    }
}
