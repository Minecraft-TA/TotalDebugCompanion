package com.github.minecraft_ta.totalDebugCompanion.jdt.semanticHighlighting;

import com.github.minecraft_ta.totalDebugCompanion.jdt.InternalCompilationUnit;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.fife.ui.rsyntaxtextarea.Token;
import org.fife.ui.rsyntaxtextarea.TokenTypes;

import javax.swing.text.Segment;
import java.util.ArrayList;
import java.util.List;

public class JavaTokenMaker extends org.fife.ui.rsyntaxtextarea.modes.JavaTokenMaker {

    private final List<Token> tokens = new ArrayList<>();

    public void reset(String text) {
        long t = System.nanoTime();
        ASTParser parser = ASTParser.newParser(AST.getJLSLatest());
        parser.setSource(new InternalCompilationUnit("Test", text));
        parser.setResolveBindings(true);
        parser.setStatementsRecovery(true);
        parser.setKind(ASTParser.K_COMPILATION_UNIT);
        var ast = (CompilationUnit) parser.createAST(null);

        tokens.clear();
        ast.accept(new SemanticTokensVisitor(tokens));
        System.out.println("Finding special tokens took " + (System.nanoTime() - t) / 1_000_000.0);
    }

    @Override
    public Token getTokenList(Segment text, int initialTokenType, int startOffset) {
        var firstToken = super.getTokenList(text, initialTokenType, startOffset);
        var currentToken = firstToken;

        while (currentToken != null) {
            //Yes, this is not efficient, but who cares for now ¯\_(ツ)_/¯
            for (Token token : this.tokens) {
                if (currentToken.getType() == TokenTypes.NULL)
                    continue;

                if (token.getTextOffset() == currentToken.getOffset()) {
                    currentToken.setType(token.getType());
                }
            }

            currentToken = currentToken.getNextToken();
        }

        return firstToken;
    }
}
