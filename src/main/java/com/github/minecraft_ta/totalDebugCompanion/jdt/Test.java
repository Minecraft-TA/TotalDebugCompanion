package com.github.minecraft_ta.totalDebugCompanion.jdt;

import com.github.minecraft_ta.totalDebugCompanion.jdt.impls.CompilationUnitImpl;
import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.CompletionRequestor;

public class Test {

    public static void main(String[] args) throws Throwable {
        /*Document document = new Document("public class Test {public static void test() {String.jo}}");
        ASTParser parser = ASTParser.newParser(AST.getJLSLatest());

        parser.setSource(document.get().toCharArray());

        parser.setResolveBindings(true);
        parser.setStatementsRecovery(true);
        parser.setKind(ASTParser.K_COMPILATION_UNIT);
        var ast = (CompilationUnit) parser.createAST(null);*/

        var unit = new CompilationUnitImpl("Test", "public class Test {public static void test() {Hallo}}");

        var time = System.nanoTime();

        //Code completion
        unit.codeComplete(51, new CompletionRequestor() {
            @Override
            public void accept(CompletionProposal proposal) {
                System.out.println(((System.nanoTime() - time) / 1_000_000.0) + "Proposal -> " + proposal);
            }
        });

        //Formatting
        //This returns a list of TextEdits to apply
        //ToolFactory.createCodeFormatter(null).format(CodeFormatter.K_COMPILATION_UNIT, "The source", 0, sourceLength, 0, "\n").;

        //Semantic tokes
        //Basically use a DOM compilation unit, then visit it while resolving types and collect tokens
        //https://github.dev/eclipse/eclipse.jdt.ls/blob/3811ab7fe6d6fdaf54de6ec58a2a5158c98c1609/org.eclipse.jdt.ls.core/src/org/eclipse/jdt/ls/core/internal/handlers/SemanticTokensHandler.java

//        unit.codeComplete(5, requestor);
        Thread.sleep(1000);
    }

}
