package com.github.minecraft_ta.totalDebugCompanion.model;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.github.minecraft_ta.totalDebugCompanion.ui.components.editors.SearchResultViewPanel;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class SearchResultView implements IEditorPanel {

    private static final Icon SEARCH_ICON = new FlatSVGIcon("icons/search.svg");

    private final String query;
    private final List<String> results;
    private final boolean methodSearch;
    private final int classesCount;
    private final int time;

    public SearchResultView(String query, List<String> results, boolean methodSearch, int classesCount, int time) {
        this.query = query;
        this.results = results;
        this.methodSearch = methodSearch;
        this.classesCount = classesCount;
        this.time = time;
    }

    @Override
    public String getTitle() {
        return this.query;
    }

    @Override
    public String getTooltip() {
        return this.query;
    }

    @Override
    public Icon getIcon() {
        return SEARCH_ICON;
    }

    @Override
    public Component getComponent() {
        return new SearchResultViewPanel(this);
    }

    public String getQuery() {
        return query;
    }

    public List<String> getResults() {
        return results;
    }

    public boolean isMethodSearch() {
        return methodSearch;
    }

    public int getClassesCount() {
        return classesCount;
    }

    public int getTime() {
        return time;
    }
}
