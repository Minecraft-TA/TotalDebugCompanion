package com.github.minecraft_ta.totalDebugCompanion.jdt.diagnostics;

import com.github.minecraft_ta.totalDebugCompanion.CompanionApp;
import com.github.minecraft_ta.totalDebugCompanion.messages.codeView.DecompileOrOpenMessage;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.*;
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
            var fromCache = ASTCache.getFromCache(this.identifier);
            if (fromCache == null)
                return null;

            var elements = fromCache.getTypeRoot().codeSelect(offs, 0);
            if (elements == null || elements.length == 0)
                return null;
            if (elements.length > 1) {
                System.err.println("Multiple elements found at offset " + offs + ": " + Arrays.toString(elements));
                return null;
            }

            return new LinkResult(elements[0], offs);
        } catch (JavaModelException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static class LinkResult implements LinkGeneratorResult {

        private final IJavaElement el;
        private final int offs;

        public LinkResult(IJavaElement el, int offs) {
            this.el = el;
            this.offs = offs;
        }

        @Override
        public HyperlinkEvent execute() {
            //TODO: Handle these differently
            if (el instanceof SourceMethod || el instanceof SourceField || el instanceof SourceType)
                return null;

            String className;
            int targetMemberType = -1;
            String targetMemberIdentifier = "";
            if (el instanceof ResolvedBinaryMethod method) {
                className = method.getDeclaringType().getFullyQualifiedName();
                targetMemberType = method.getElementType();
                targetMemberIdentifier = method.getKey();
            } else if (el instanceof ResolvedBinaryField field) {
                className = field.getDeclaringType().getFullyQualifiedName();
                targetMemberType = field.getElementType();
                targetMemberIdentifier = field.getElementName();
            } else if (el instanceof ResolvedBinaryType type) {
                className = type.getFullyQualifiedName();
            } else {
                return null;
            }

            CompanionApp.SERVER.getMessageProcessor().enqueueMessage(new DecompileOrOpenMessage(className, targetMemberType, targetMemberIdentifier));
            return null;
        }

        @Override
        public int getSourceOffset() {
            return offs;
        }
    }
}
