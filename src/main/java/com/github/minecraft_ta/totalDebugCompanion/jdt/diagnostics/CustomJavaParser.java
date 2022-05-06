package com.github.minecraft_ta.totalDebugCompanion.jdt.diagnostics;

import org.eclipse.jdt.core.compiler.IProblem;
import org.fife.ui.rsyntaxtextarea.RSyntaxDocument;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rsyntaxtextarea.parser.*;

public class CustomJavaParser extends AbstractParser {

    private final String astKey;

    public CustomJavaParser(String astKey) {
        this.astKey = astKey;
    }

    @Override
    public ParseResult parse(RSyntaxDocument doc, String style) {
        var result = new DefaultParseResult(this);
        result.setParsedLines(0, doc.getDefaultRootElement().getElementCount() - 1);

        if (!SyntaxConstants.SYNTAX_STYLE_JAVA.equals(style))
            return result;

        var ast = ASTCache.getFromCache(this.astKey);
        if (ast == null)
            return result;

        for (IProblem problem : ast.getProblems()) {
            var notice = new DefaultParserNotice(this, problem.getMessage(), problem.getSourceLineNumber(), problem.getSourceStart(), problem.getSourceEnd() - problem.getSourceStart() + 1);
            notice.setLevel(problem.isInfo() ? ParserNotice.Level.INFO : problem.isWarning() ? ParserNotice.Level.WARNING : ParserNotice.Level.ERROR);
            result.addNotice(notice);
        }
        return result;
    }
}
