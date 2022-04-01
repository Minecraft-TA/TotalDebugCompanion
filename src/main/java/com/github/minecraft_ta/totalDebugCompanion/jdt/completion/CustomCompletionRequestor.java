package com.github.minecraft_ta.totalDebugCompanion.jdt.completion;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class CustomCompletionRequestor extends CompletionRequestor implements IProgressMonitor {


    private final Consumer<List<CompletionItem>> completionCallback;

    private List<CompletionProposal> proposals = new ArrayList<>();
//    private final ICompilationUnit unit;
    private CompletionContext context;
    private CompletionProposalDescriptionProvider descriptionProvider;

    private boolean cancelled;
    private long startTime;

    public CustomCompletionRequestor(/*ICompilationUnit unit,*/ Consumer<List<CompletionItem>> completionCallback) {
//        this.unit = unit;
        this.completionCallback = completionCallback;
        this.setRequireExtendedContext(true);
    }

    @Override
    public void accept(CompletionProposal proposal) {
        if (this.cancelled)
            return;

        if (isFiltered(proposal))
            return;
        if (!isIgnored(proposal.getKind())) {
            if (proposal.getKind() != CompletionProposal.POTENTIAL_METHOD_DECLARATION) {
                proposals.add(proposal);
            }
        }
    }

    private List<CompletionItem> convertProposals() {
        return proposals.stream().map(this::toCompletionItem).sorted(new CompletionItemComparator()).collect(Collectors.toList());
    }

    public CompletionItem toCompletionItem(CompletionProposal proposal) {
        final CompletionItem item = new CompletionItem();
        //TODO: Range?
        item.setRange(proposal.getReplaceStart(), proposal.getReplaceEnd());
        item.setRelevance(mapRelevance(proposal));
        item.setKind(mapKind(proposal));
        /*if (Flags.isDeprecated(proposal.getFlags())) {
            if (preferenceManager.getClientPreferences().isCompletionItemTagSupported()) {
                $.setTags(List.of(CompletionItemTag.Deprecated));
            } else {
                $.setDeprecated(true);
            }
        }*/

        this.descriptionProvider.updateDescription(proposal, item);
//        proposalProvider.updateReplacement(proposal, $, '\0');
        return item;
    }

    public int mapRelevance(CompletionProposal proposal) {
        int baseRelevance = proposal.getRelevance();
        return switch (proposal.getKind()) {
            case CompletionProposal.LABEL_REF -> baseRelevance + 1;
            case CompletionProposal.KEYWORD -> baseRelevance + 2;
            case CompletionProposal.TYPE_REF, CompletionProposal.ANONYMOUS_CLASS_DECLARATION, CompletionProposal.ANONYMOUS_CLASS_CONSTRUCTOR_INVOCATION -> baseRelevance + 3;
            case CompletionProposal.METHOD_REF, CompletionProposal.CONSTRUCTOR_INVOCATION, CompletionProposal.METHOD_NAME_REFERENCE, CompletionProposal.METHOD_DECLARATION, CompletionProposal.ANNOTATION_ATTRIBUTE_REF, CompletionProposal.POTENTIAL_METHOD_DECLARATION -> baseRelevance + 4;
            case CompletionProposal.FIELD_REF -> baseRelevance + 5;
            case CompletionProposal.LOCAL_VARIABLE_REF, CompletionProposal.VARIABLE_DECLARATION -> baseRelevance + 6;
            default -> baseRelevance;
        };
    }

    private CompletionItemKind mapKind(CompletionProposal proposal) {
        int kind = proposal.getKind();
        int flags = proposal.getFlags();
        switch (kind) {
            case CompletionProposal.ANONYMOUS_CLASS_CONSTRUCTOR_INVOCATION:
            case CompletionProposal.CONSTRUCTOR_INVOCATION:
                return CompletionItemKind.CONSTRUCTOR;
            case CompletionProposal.ANONYMOUS_CLASS_DECLARATION:
            case CompletionProposal.TYPE_REF:
                if (Flags.isInterface(flags)) {
                    return CompletionItemKind.INTERFACE;
                } else if (Flags.isEnum(flags)) {
                    return CompletionItemKind.ENUM;
                }
                return CompletionItemKind.CLASS;
            case CompletionProposal.FIELD_IMPORT:
            case CompletionProposal.METHOD_IMPORT:
            case CompletionProposal.PACKAGE_REF:
            case CompletionProposal.TYPE_IMPORT:
                return CompletionItemKind.IMPORT;
            case CompletionProposal.FIELD_REF:
                if (Flags.isEnum(flags))
                    return CompletionItemKind.ENUM_MEMBER;
                if (Flags.isStatic(flags) && Flags.isFinal(flags))
                    return CompletionItemKind.CONSTANT;
                return CompletionItemKind.FIELD;
            case CompletionProposal.FIELD_REF_WITH_CASTED_RECEIVER:
                return CompletionItemKind.FIELD;
            case CompletionProposal.KEYWORD:
                return CompletionItemKind.KEYWORD;
            case CompletionProposal.LABEL_REF:
                return CompletionItemKind.LABEL;
            case CompletionProposal.LOCAL_VARIABLE_REF:
            case CompletionProposal.VARIABLE_DECLARATION:
                return CompletionItemKind.VARIABLE;
            case CompletionProposal.METHOD_DECLARATION:
            case CompletionProposal.METHOD_REF:
            case CompletionProposal.METHOD_REF_WITH_CASTED_RECEIVER:
            case CompletionProposal.METHOD_NAME_REFERENCE:
            case CompletionProposal.POTENTIAL_METHOD_DECLARATION:
            case CompletionProposal.LAMBDA_EXPRESSION:
                return CompletionItemKind.METHOD;
            case CompletionProposal.ANNOTATION_ATTRIBUTE_REF:
            case CompletionProposal.JAVADOC_BLOCK_TAG:
            case CompletionProposal.JAVADOC_FIELD_REF:
            case CompletionProposal.JAVADOC_INLINE_TAG:
            case CompletionProposal.JAVADOC_METHOD_REF:
            case CompletionProposal.JAVADOC_PARAM_REF:
            case CompletionProposal.JAVADOC_TYPE_REF:
            case CompletionProposal.JAVADOC_VALUE_REF:
            default:
                return CompletionItemKind.TEXT;
        }
    }

    @Override
    public void acceptContext(CompletionContext context) {
        super.acceptContext(context);
        this.context = context;
        this.descriptionProvider = new CompletionProposalDescriptionProvider(context);
//        this.proposalProvider = new CompletionProposalReplacementProvider(unit, context, response.getOffset(), preferenceManager.getPreferences(), preferenceManager.getClientPreferences());
    }

    @Override
    public void setIgnored(int completionProposalKind, boolean ignore) {
        super.setIgnored(completionProposalKind, ignore);
        if (completionProposalKind == CompletionProposal.METHOD_DECLARATION && !ignore) {
            setRequireExtendedContext(true);
        }
    }

    @Override
    public boolean isTestCodeExcluded() {
        return true;
    }

    /**
     * copied from
     * org.eclipse.jdt.ui.text.java.CompletionProposalCollector.isFiltered(CompletionProposal)
     */
    protected boolean isFiltered(CompletionProposal proposal) {
        if (isIgnored(proposal.getKind())) {
            return true;
        }
        // Only filter types and constructors from completion.
        switch (proposal.getKind()) {
            case CompletionProposal.CONSTRUCTOR_INVOCATION:
            case CompletionProposal.ANONYMOUS_CLASS_CONSTRUCTOR_INVOCATION:
            case CompletionProposal.JAVADOC_TYPE_REF:
            case CompletionProposal.PACKAGE_REF:
            case CompletionProposal.TYPE_REF:
                return isTypeFiltered(proposal);
            case CompletionProposal.METHOD_REF:
                // Methods from already imported types and packages can still be proposed.
                // Whether the expected type is resolved or not can be told from the required proposal.
                // When the type is missing, an additional proposal could be found.
                if (proposal.getRequiredProposals() != null) {
                    return isTypeFiltered(proposal);
                }
        }
        return false;
    }

    protected boolean isTypeFiltered(CompletionProposal proposal) {
        char[] declaringType = getDeclaringType(proposal);
        //TODO: Type filter?
        return declaringType != null && /*TypeFilter.isFiltered(declaringType)*/false;
    }

    /**
     * copied from
     * org.eclipse.jdt.ui.text.java.CompletionProposalCollector.getDeclaringType(CompletionProposal)
     */
    protected final char[] getDeclaringType(CompletionProposal proposal) {
        switch (proposal.getKind()) {
            case CompletionProposal.METHOD_DECLARATION:
            case CompletionProposal.METHOD_NAME_REFERENCE:
            case CompletionProposal.JAVADOC_METHOD_REF:
            case CompletionProposal.METHOD_REF:
            case CompletionProposal.CONSTRUCTOR_INVOCATION:
            case CompletionProposal.ANONYMOUS_CLASS_CONSTRUCTOR_INVOCATION:
            case CompletionProposal.METHOD_REF_WITH_CASTED_RECEIVER:
            case CompletionProposal.ANNOTATION_ATTRIBUTE_REF:
            case CompletionProposal.POTENTIAL_METHOD_DECLARATION:
            case CompletionProposal.ANONYMOUS_CLASS_DECLARATION:
            case CompletionProposal.FIELD_REF:
            case CompletionProposal.FIELD_REF_WITH_CASTED_RECEIVER:
            case CompletionProposal.JAVADOC_FIELD_REF:
            case CompletionProposal.JAVADOC_VALUE_REF:
                char[] declaration = proposal.getDeclarationSignature();
                // special methods may not have a declaring type: methods defined on arrays etc.
                // Currently known: class literals don't have a declaring type - use Object
                if (declaration == null) {
                    return "java.lang.Object".toCharArray(); //$NON-NLS-1$
                }
                return Signature.toCharArray(declaration);
            case CompletionProposal.PACKAGE_REF:
                return proposal.getDeclarationSignature();
            case CompletionProposal.JAVADOC_TYPE_REF:
            case CompletionProposal.TYPE_REF:
                return Signature.toCharArray(proposal.getSignature());
            case CompletionProposal.LOCAL_VARIABLE_REF:
            case CompletionProposal.VARIABLE_DECLARATION:
            case CompletionProposal.KEYWORD:
            case CompletionProposal.LABEL_REF:
            case CompletionProposal.JAVADOC_BLOCK_TAG:
            case CompletionProposal.JAVADOC_INLINE_TAG:
            case CompletionProposal.JAVADOC_PARAM_REF:
                return null;
            default:
                Assert.isTrue(false);
                return null;
        }
    }

    @Override
    public void beginTask(String name, int totalWork) {
        this.startTime = System.nanoTime();
    }

    @Override
    public void done() {
        if (this.cancelled)
            return;

        this.completionCallback.accept(convertProposals());
    }

    @Override
    public void internalWorked(double work) {

    }

    @Override
    public boolean isCanceled() {
        return this.cancelled || (System.nanoTime() - startTime) > 1_000_000_000L;
    }

    @Override
    public void setCanceled(boolean value) {
        this.cancelled = value;
    }

    @Override
    public void setTaskName(String name) {

    }

    @Override
    public void subTask(String name) {

    }

    @Override
    public void worked(int work) {

    }

    @Override
    public boolean isAllowingRequiredProposals(int proposalKind, int requiredProposalKind) {
        return true;
    }
}
