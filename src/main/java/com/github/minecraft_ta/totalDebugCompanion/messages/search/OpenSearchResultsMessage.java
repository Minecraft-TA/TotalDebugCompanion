package com.github.minecraft_ta.totalDebugCompanion.messages.search;

import com.github.minecraft_ta.totalDebugCompanion.model.SearchResultView;
import com.github.minecraft_ta.totalDebugCompanion.ui.views.MainWindow;
import com.github.minecraft_ta.totalDebugCompanion.util.UIUtils;
import com.github.tth05.scnet.message.AbstractMessageIncoming;
import com.github.tth05.scnet.util.ByteBufferInputStream;

import java.util.List;
import java.util.stream.IntStream;

public class OpenSearchResultsMessage extends AbstractMessageIncoming {

    private String query;
    private List<String> results;
    private boolean methodSearch;
    private int classesCount;
    private int time;

    @Override
    public void read(ByteBufferInputStream messageStream) {
        this.query = messageStream.readString();

        var resultCount = messageStream.readInt();
        this.results = IntStream.range(0, resultCount).mapToObj(i -> messageStream.readString()).toList();
        this.methodSearch = messageStream.readBoolean();
        this.classesCount = messageStream.readInt();
        this.time = messageStream.readInt();
    }

    public static void handle(OpenSearchResultsMessage message) {
        var window = MainWindow.INSTANCE;
        var editorTabs = window.getEditorTabs();
        editorTabs.openEditorTab(new SearchResultView(
                message.query, message.results, message.methodSearch, message.classesCount, message.time
        )).join();

        UIUtils.focusWindow(window);
    }
}
