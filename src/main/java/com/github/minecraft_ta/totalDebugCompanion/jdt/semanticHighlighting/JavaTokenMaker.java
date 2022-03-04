package com.github.minecraft_ta.totalDebugCompanion.jdt.semanticHighlighting;

import com.github.minecraft_ta.totalDebugCompanion.jdt.InternalCompilationUnit;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.fife.ui.rsyntaxtextarea.Token;
import org.fife.ui.rsyntaxtextarea.TokenImpl;
import org.fife.ui.rsyntaxtextarea.TokenTypes;

import javax.swing.text.Segment;
import java.util.ArrayList;
import java.util.List;

public class JavaTokenMaker extends org.fife.ui.rsyntaxtextarea.modes.JavaTokenMaker {

    public void reset(String text) {
        System.out.println("RESET with text");
        ASTParser parser = ASTParser.newParser(AST.getJLSLatest());
        parser.setSource(new InternalCompilationUnit("Test", text));
        parser.setResolveBindings(true);
        parser.setStatementsRecovery(true);
        parser.setKind(ASTParser.K_COMPILATION_UNIT);
        var ast = (CompilationUnit) parser.createAST(null);

        List<Token> tokens = new ArrayList<>();
        ast.accept(new SemanticTokensVisitor(tokens));
        System.out.println(tokens);
    }

    @Override
    public Token getTokenList(Segment text, int initialTokenType, int startOffset) {
        var tokenList = super.getTokenList(text, initialTokenType, startOffset);
        if (tokenList.getType() == TokenTypes.RESERVED_WORD) {
            ((TokenImpl) tokenList).setType(TokenTypes.ANNOTATION);
        }
        return tokenList;
    }
}
