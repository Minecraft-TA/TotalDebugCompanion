package com.github.minecraft_ta.totalDebugCompanion.jdt.semanticHighlighting;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.fife.ui.rsyntaxtextarea.Token;

import java.util.List;

public class SemanticTokensVisitor extends ASTVisitor {

    private final List<Token> tokens;

    public SemanticTokensVisitor(List<Token> tokens) {
        this.tokens = tokens;
    }



    @Override
    public boolean visit(ImportDeclaration node) {
        node.resolveBinding();
        return false;
    }
}
