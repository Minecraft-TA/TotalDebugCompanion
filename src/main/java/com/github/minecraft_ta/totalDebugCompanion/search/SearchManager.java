package com.github.minecraft_ta.totalDebugCompanion.search;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.function.IntConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SearchManager {

    private static final DefaultHighlighter.DefaultHighlightPainter HIGHLIGHT_PAINTER = new DefaultHighlighter.DefaultHighlightPainter(new Color(255, 255, 0, 200));
    private static final DefaultHighlighter.DefaultHighlightPainter FOCUSED_HIGHLIGHT_PAINTER = new DefaultHighlighter.DefaultHighlightPainter(new Color(0, 0, 255, 100));

    private final JTextPane textPane;
    private final List<IntConsumer> focusedIndexChangeListeners = new ArrayList<>();

    private final List<HighlightInfo> highlights = new ArrayList<>();
    private final Deque<String> searchQueue = new ConcurrentLinkedDeque<>();
    private final Thread searchThread;

    private Object focusedMatchReference;
    private int focusedMatchIndex;

    private boolean matchCase;
    private boolean useRegex;

    public SearchManager(JTextPane textPane) {
        this.textPane = textPane;
        this.searchThread = new Thread(() -> {
            while (true) {
                synchronized (this.searchQueue) {
                    if (this.searchQueue.isEmpty()) {
                        try {
                            this.searchQueue.wait();
                        } catch (InterruptedException e) {
                            break;
                        }
                    }
                }

                var query = this.searchQueue.removeLast();
                this.searchQueue.clear();
                this.highlights.clear();

                var text = textPane.getText();

                Matcher matcher;
                try {
                    int flags = matchCase ? 0 : Pattern.CASE_INSENSITIVE;
                    matcher = Pattern.compile(this.useRegex ? query : Pattern.quote(query), flags).matcher(text);
                } catch (Throwable t) {continue;}

                while (matcher.find()) {
                    if (!this.searchQueue.isEmpty())
                        break;
                    this.highlights.add(new HighlightInfo(matcher.start(), matcher.end()));
                }

                //If we weren't interrupted, display all results
                if (matcher.hitEnd()) {
                    this.focusedMatchReference = null;
                    this.focusedIndexChangeListeners.forEach(i -> i.accept(0));
                    this.focusedMatchIndex = 0;
                    final var highlightsCopy = new ArrayList<>(this.highlights);
                    SwingUtilities.invokeLater(() -> {
                        showHighlights(highlightsCopy);
                    });
                }
            }
        });
        this.searchThread.setDaemon(true);
        this.searchThread.setName("Text search thread");
        this.searchThread.start();
    }

    public void showHighlights() {
        hideHighlights();
        final var highlightsCopy = new ArrayList<>(highlights);
        showHighlights(highlightsCopy);
    }

    public void hideHighlights() {
        this.textPane.getHighlighter().removeAllHighlights();
    }

    public void focusNextMatch() {
        focusMatchAtIndex(Math.min(getMatchCount() - 1, this.focusedMatchIndex + 1));
    }

    public void focusPreviousMatch() {
        focusMatchAtIndex(Math.max(0, this.focusedMatchIndex - 1));
    }

    public int getMatchCount() {
        return this.highlights.size();
    }

    public void stopThread() {
        this.searchThread.interrupt();
    }

    public void setQuery(String query) {
        if (query == null || query.isEmpty()) {
            this.textPane.getHighlighter().removeAllHighlights();
            this.highlights.clear();
            this.focusedIndexChangeListeners.forEach(i -> i.accept(0));
            this.focusedMatchIndex = 0;
            this.focusedMatchReference = null;
            return;
        }

        synchronized (this.searchQueue) {
            this.searchQueue.notify();
            this.searchQueue.add(query);
        }
    }

    public void setUseRegex(boolean useRegex) {
        this.useRegex = useRegex;
    }

    public int getFocusedMatchIndex() {
        return this.focusedMatchIndex;
    }

    public int getFocusedRangeStart() {
        return this.highlights.get(this.focusedMatchIndex).start;
    }

    public int getFocusedRangeEnd() {
        return this.highlights.get(this.focusedMatchIndex).end;
    }

    public void addFocusedIndexChangedListener(IntConsumer listener) {
        this.focusedIndexChangeListeners.add(listener);
    }

    private void focusMatchAtIndex(int index) {
        if (this.highlights.isEmpty())
            return;

        try {
            //Add back the normal highlighting
            var oldMatchPos = this.highlights.get(this.focusedMatchIndex);
            if (oldMatchPos.highlightReference != null)
                textPane.getHighlighter().removeHighlight(oldMatchPos.highlightReference);
            oldMatchPos.highlightReference = textPane.getHighlighter().addHighlight(oldMatchPos.start, oldMatchPos.end, HIGHLIGHT_PAINTER);

            //Remove the focus highlighting
            if (this.focusedMatchReference != null)
                textPane.getHighlighter().removeHighlight(this.focusedMatchReference);

            this.focusedIndexChangeListeners.forEach(i -> i.accept(index));
            this.focusedMatchIndex = index;

            //Remove normal highlighting at new position
            textPane.getHighlighter().removeHighlight(this.highlights.get(this.focusedMatchIndex).highlightReference);

            //Add focus highlighting at new position
            var matchPos = this.highlights.get(this.focusedMatchIndex);
            this.focusedMatchReference = textPane.getHighlighter().addHighlight(matchPos.start, matchPos.end, FOCUSED_HIGHLIGHT_PAINTER);
        } catch (BadLocationException ignored) {
            ignored.printStackTrace();
        }
    }

    private void showHighlights(List<HighlightInfo> highlights) {
        textPane.getHighlighter().removeAllHighlights();
        highlights.forEach(p -> {
            try {
                p.highlightReference = textPane.getHighlighter().addHighlight(p.start, p.end, HIGHLIGHT_PAINTER);
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
        });

        focusMatchAtIndex(0);
    }

    public void setMatchCase(boolean matchCase) {
        this.matchCase = matchCase;
    }

    private static final class HighlightInfo {

        private int start;
        private int end;

        private Object highlightReference;

        public HighlightInfo(int start, int end) {
            this.start = start;
            this.end = end;
        }
    }
}
