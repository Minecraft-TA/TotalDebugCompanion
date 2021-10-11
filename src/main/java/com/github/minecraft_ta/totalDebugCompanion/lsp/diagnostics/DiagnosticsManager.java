package com.github.minecraft_ta.totalDebugCompanion.lsp.diagnostics;

import com.github.minecraft_ta.totalDebugCompanion.util.UIUtils;
import org.eclipse.lsp4j.Diagnostic;

import javax.swing.text.*;
import java.awt.*;
import java.awt.geom.GeneralPath;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DiagnosticsManager {

    private static final DefaultHighlighter.DefaultHighlightPainter ERROR_PAINTER = new UnderlineHighlightPainter(Color.RED);
    private static final DefaultHighlighter.DefaultHighlightPainter WARNING_PAINTER = new UnderlineHighlightPainter(Color.ORANGE.darker());

    private final Map<String, JTextComponent> editors = new HashMap<>();
    private final Map<String, List<Object>> currentHighlights = new HashMap<>();

    public void publishDiagnostics(String uri, List<Diagnostic> diagnostics) {
        var editor = this.editors.get(uri);
        if (editor == null)
            return;

        clearAllHighlights(uri);

        var highlights = this.currentHighlights.computeIfAbsent(uri, (key) -> new ArrayList<>());
        for (Diagnostic diagnostic : diagnostics) {
            var range = diagnostic.getRange();
            try {
                highlights.add(editor.getHighlighter().addHighlight(
                        UIUtils.posToOffset(editor, range.getStart()),
                        UIUtils.posToOffset(editor, range.getEnd()),
                        switch (diagnostic.getSeverity()) {
                            case Error -> ERROR_PAINTER;
                            case Warning -> WARNING_PAINTER;
                            default -> {
                                System.out.println("Not painter for: " + diagnostic);
                                yield ERROR_PAINTER;
                            }
                        }
                ));
            } catch (BadLocationException e) {
                System.err.println("Diagnostic at invalid location");
                e.printStackTrace();
            }
        }
    }

    public void clearAllHighlights(String uri) {
        var editor = this.editors.get(uri);
        if (editor == null)
            return;

        var highlights = this.currentHighlights.computeIfAbsent(uri, (key) -> new ArrayList<>());
        for (Object highlight : highlights) {
            editor.getHighlighter().removeHighlight(highlight);
        }
        highlights.clear();
    }

    public void register(String uri, JTextComponent component) {
        this.editors.put(uri, component);
    }

    public void unregister(String uri) {
        this.editors.remove(uri);
        this.currentHighlights.remove(uri);
    }

    private static final class UnderlineHighlightPainter extends DefaultHighlighter.DefaultHighlightPainter {

        public UnderlineHighlightPainter(Color c) {
            super(c);
        }

        @Override
        public Shape paintLayer(Graphics g, int offs0, int offs1, Shape bounds, JTextComponent c, View view) {
            Color color = getColor();
            g.setColor(color == null ? c.getSelectionColor() : color);

            Rectangle r;
            if (offs0 == view.getStartOffset() && offs1 == view.getEndOffset()) {
                if (bounds instanceof Rectangle) {
                    r = (Rectangle) bounds;
                } else {
                    r = bounds.getBounds();
                }
            } else {
                try {
                    Shape shape = view.modelToView(offs0, Position.Bias.Forward, offs1, Position.Bias.Backward, bounds);
                    r = (shape instanceof Rectangle) ? (Rectangle) shape : shape.getBounds();
                } catch (BadLocationException e) {
                    r = null;
                }
            }

            if (r != null) {
                var clip = g.getClip();
                g.setClip(r);
                r.width = Math.max(r.width, 1);

                var path = new GeneralPath();

                for (int x = ((r.x / 5) * 5); x < r.x + r.width; x += 5) {
                    path.moveTo(x, r.y + r.height - 1);
                    path.lineTo(x + 2, r.y + r.height - 3);
                    path.moveTo(x + 2, r.y + r.height - 3);
                    path.lineTo(x + 2 + 2, r.y + r.height - 1);
                }
                ((Graphics2D) g).draw(path);
                g.setClip(clip);
            }

            return r;
        }
    }
}
