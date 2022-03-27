package com.github.minecraft_ta.totalDebugCompanion.jdt.semanticHighlighting;

import com.github.minecraft_ta.totalDebugCompanion.jdt.impls.CompilationUnitImpl;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.fife.ui.rsyntaxtextarea.Token;
import org.fife.ui.rsyntaxtextarea.TokenTypes;

import javax.swing.text.Segment;
import java.util.HashMap;
import java.util.Map;

public class JavaTokenMaker extends org.fife.ui.rsyntaxtextarea.modes.JavaTokenMaker {

    private final Map<Integer, Integer> overwrittenTokenTypes = new HashMap<>();

    public void reset(String text) {
        long t = System.nanoTime();
        ASTParser parser = ASTParser.newParser(AST.JLS8);
        parser.setSource(new CompilationUnitImpl("TODO: Pass correct name here?", text));
        parser.setResolveBindings(true);
        parser.setStatementsRecovery(true);
        parser.setKind(ASTParser.K_COMPILATION_UNIT);
        var ast = (CompilationUnit) parser.createAST(null);

        overwrittenTokenTypes.clear();
        ast.accept(new SemanticTokensVisitor(overwrittenTokenTypes));
        System.out.println("Finding special tokens took " + (System.nanoTime() - t) / 1_000_000.0);
    }

    @Override
    public Token getTokenList(Segment text, int initialTokenType, int startOffset) {
        var firstToken = super.getTokenList(text, initialTokenType, startOffset);
        var currentToken = firstToken;

        while (currentToken != null) {
            if (currentToken.getType() != TokenTypes.NULL) {
                var token = this.overwrittenTokenTypes.get(currentToken.getOffset());
                if (token != null)
                    currentToken.setType(token);
            }

            currentToken = currentToken.getNextToken();
        }

        return firstToken;
    }
}
