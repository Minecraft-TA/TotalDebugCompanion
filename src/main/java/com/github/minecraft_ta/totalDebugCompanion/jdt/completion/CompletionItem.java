package com.github.minecraft_ta.totalDebugCompanion.jdt.completion;

public class CompletionItem {

    private String label;
    private String replacement;
    private CompletionItemKind kind;
    private int relevance;
    private int replaceStart;
    private int replaceEnd;

    public void setLabel(String label) {
        this.label = label;
    }

    public void setReplacement(String replacement) {
        this.replacement = replacement;
    }

    public void setKind(CompletionItemKind kind) {
        this.kind = kind;
    }

    public void setRelevance(int relevance) {
        this.relevance = relevance;
    }

    public void setRange(int replaceStart, int replaceEnd) {
        this.replaceStart = replaceStart;
        this.replaceEnd = replaceEnd;
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

    public int getReplaceStart() {
        return replaceStart;
    }

    public int getReplaceEnd() {
        return replaceEnd;
    }

    public String getReplacement() {
        if (this.replacement == null)
            return this.label;
        return replacement;
    }
}
