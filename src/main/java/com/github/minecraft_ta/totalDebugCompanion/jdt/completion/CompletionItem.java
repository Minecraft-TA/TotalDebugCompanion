package com.github.minecraft_ta.totalDebugCompanion.jdt.completion;

import java.util.ArrayList;
import java.util.List;

public class CompletionItem {

    private String label;
    private CompletionItemKind kind;
    private boolean isSnippet;
    private int relevance;
    private final List<CustomTextEdit> textEdits = new ArrayList<>();

    private final CustomCompletionRequestor requestor;

    public CompletionItem(CustomCompletionRequestor requestor) {
        this.requestor = requestor;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public void setKind(CompletionItemKind kind) {
        this.kind = kind;
    }

    public void setRelevance(int relevance) {
        this.relevance = relevance;
    }

    public String getLabel() {
        return label;
    }

    public CompletionItemKind getKind() {
        return kind;
    }

    public int getRelevance() {
        return relevance;
    }

    public boolean isSnippet() {
        return isSnippet;
    }

    public void setSnippet(boolean snippet) {
        isSnippet = snippet;
    }

    public void addTextEdit(CustomTextEdit customTextEdit) {
        this.textEdits.add(customTextEdit);
    }

    public List<CustomTextEdit> getTextEdits() {
        return textEdits;
    }

    public CustomCompletionRequestor getRequestor() {
        return requestor;
    }
}
