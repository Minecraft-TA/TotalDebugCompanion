package com.github.minecraft_ta.totalDebugCompanion.jdt.diagnostics;

import org.eclipse.jdt.core.JavaModelException;
import org.fife.ui.rsyntaxtextarea.LinkGenerator;
import org.fife.ui.rsyntaxtextarea.LinkGeneratorResult;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;

import javax.swing.event.HyperlinkEvent;
import java.util.Arrays;

public class CustomJavaLinkGenerator implements LinkGenerator {

    private final String identifier;

    public CustomJavaLinkGenerator(String identifier) {
        this.identifier = identifier;
    }

    @Override
    public LinkGeneratorResult isLinkAtOffset(RSyntaxTextArea textArea, int offs) {
        try {
            var elements = ASTCache.getFromCache(this.identifier).getTypeRoot().codeSelect(offs, 0);
            System.out.println(Arrays.toString(elements));
            if(elements == null || elements.length == 0)
                return null;
            if (elements.length > 1) {
                System.err.println("Multiple elements found at offset " + offs + ": " + Arrays.toString(elements));
                return null;
            }

            var el = elements[0];
            return new LinkGeneratorResult() {
                @Override
                public HyperlinkEvent execute() {
                    System.out.println("Clicked on " + el.toString());
                    return null;
                }

                @Override
                public int getSourceOffset() {
                    return offs;
                }
            };
        } catch (JavaModelException e) {
            e.printStackTrace();
            return null;
        }
    }
}
