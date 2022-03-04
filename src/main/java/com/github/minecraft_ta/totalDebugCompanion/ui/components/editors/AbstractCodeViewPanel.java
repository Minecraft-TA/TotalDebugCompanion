package com.github.minecraft_ta.totalDebugCompanion.ui.components.editors;

import com.github.minecraft_ta.totalDebugCompanion.GlobalConfig;
import com.github.minecraft_ta.totalDebugCompanion.jdt.semanticHighlighting.JavaTokenMaker;
import com.github.minecraft_ta.totalDebugCompanion.ui.components.global.BottomInformationBar;
import com.github.minecraft_ta.totalDebugCompanion.util.DocumentChangeListener;
import com.github.minecraft_ta.totalDebugCompanion.util.UIUtils;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rtextarea.Gutter;
import org.fife.ui.rtextarea.RTextScrollPane;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.text.BadLocationException;
import java.awt.*;
import java.awt.event.HierarchyEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.Objects;

public class AbstractCodeViewPanel extends JPanel {

    public static Font JETBRAINS_MONO_FONT = null;
    static {
        try {
            JETBRAINS_MONO_FONT = Font.createFont(Font.TRUETYPE_FONT, Objects.requireNonNull(CodeViewPanel.class.getResourceAsStream("/font/jetbrainsmono_regular.ttf"), "Unable to load font"));
            GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(JETBRAINS_MONO_FONT);
        } catch (FontFormatException | IOException e) {
            e.printStackTrace();
        }
    }


    protected final RSyntaxTextArea editorPane = new RSyntaxTextArea();
    protected final RTextScrollPane editorScrollPane = new RTextScrollPane(editorPane);

    protected final BottomInformationBar bottomInformationBar = new BottomInformationBar();

    protected JComponent headerComponent;

    public AbstractCodeViewPanel() {
        super(new BorderLayout());

        this.editorScrollPane.getGutter().setBorder(new Gutter.GutterBorder(0, 5, 0, 0));
        this.editorScrollPane.getGutter().setForeground(Color.GRAY);
        this.editorScrollPane.getGutter().setBackground(UIManager.getColor("TextPane.background"));
        this.editorScrollPane.setBorder(BorderFactory.createEmptyBorder());

        this.editorPane.setAnimateBracketMatching(false);
        this.editorPane.setPaintMatchedBracketPair(true);
        this.editorPane.setMatchedBracketBGColor(Color.GRAY);
        this.editorPane.setMatchedBracketBorderColor(null);
        this.editorPane.setCurrentLineHighlightColor(Color.decode("#4e5052"));
        this.editorPane.setBackground(UIManager.getColor("TextPane.background"));
        this.editorPane.setForeground(UIManager.getColor("EditorPane.foreground"));
        this.editorPane.setSelectionColor(UIManager.getColor("EditorPane.selectionBackground"));
        this.editorPane.getDocument().addDocumentListener((DocumentChangeListener) e -> {
            if (e.getType() == DocumentEvent.EventType.CHANGE)
                return;

            try {
                var document = this.editorPane.getDocument();
                var field = document.getClass().getDeclaredField("tokenMaker");
                field.setAccessible(true);
                ((JavaTokenMaker) field.get(document)).reset(document.getText(0, document.getLength()));
            } catch (Throwable ex) {
                throw new RuntimeException(ex);
            }
        });

        add(this.editorScrollPane, BorderLayout.CENTER);
        add(this.bottomInformationBar, BorderLayout.SOUTH);

        updateFonts();

        PropertyChangeListener fontSizeListener = event -> updateFonts();
        GlobalConfig.getInstance().addPropertyChangeListener("fontSize", fontSizeListener);
        addHierarchyListener(e -> {
            if (e.getChangeFlags() == HierarchyEvent.PARENT_CHANGED && getParent() == null) {
                GlobalConfig.getInstance().removePropertyChangeListener("fontSize", fontSizeListener);
            }
        });
    }

    public void focusLine(int line) {
        SwingUtilities.invokeLater(() -> {
            var verticalScrollBar = this.editorScrollPane.getVerticalScrollBar();
            verticalScrollBar.setValue((int) ((line - 1) * ((double) verticalScrollBar.getMaximum() / this.editorPane.getDocument().getDefaultRootElement().getElementCount())));
        });
    }

    public void focusRange(int offsetStart, int offsetEnd) {
        try {
            var rect = this.editorPane.modelToView2D(offsetStart);
            var viewport = this.editorScrollPane.getViewport();

            var viewSize = viewport.getViewSize();
            var extentSize = viewport.getExtentSize();

            int rangeWidth = UIUtils.getFontWidth(this.editorPane, "9".repeat(offsetEnd - offsetStart));
            int x = (int) Math.max(0, rect.getX() - ((extentSize.width - rangeWidth) / 2f));
            x = Math.min(x, viewSize.width - extentSize.width);
            int y = (int) Math.max(0, rect.getY() - ((extentSize.height - rect.getHeight()) / 2f));
            y = Math.min(y, viewSize.height - extentSize.height);

            viewport.setViewPosition(new Point(x, y));
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    public void setHeaderComponent(JComponent component) {
        removeHeaderComponent();
        this.headerComponent = component;
        add(component, BorderLayout.NORTH);
        revalidate();
        repaint();
        SwingUtilities.invokeLater(() -> {
            //Adjust scroll bar to keep it in place
            var verticalScrollBar = editorScrollPane.getVerticalScrollBar();
            verticalScrollBar.setValue((int) (verticalScrollBar.getValue() + component.getPreferredSize().getHeight()));
        });
    }

    public void removeHeaderComponent() {
        if (this.headerComponent == null)
            return;

        synchronized (getTreeLock()) {
            var i = 0;
            for (; i < getComponentCount(); i++) {
                var component = getComponent(i);
                if (component == this.headerComponent) {
                    remove(i);
                    revalidate();
                    repaint();
                    //Adjust scroll bar to keep it in place
                    var verticalScrollBar = editorScrollPane.getVerticalScrollBar();
                    verticalScrollBar.setValue(verticalScrollBar.getValue() - component.getHeight());
                    break;
                }
            }
        }

        this.headerComponent = null;
    }

    protected void updateFonts() {
        var newFont = JETBRAINS_MONO_FONT.deriveFont(GlobalConfig.getInstance().<Float>getValue("fontSize"));
        this.editorPane.setFractionalFontMetricsEnabled(true);
        this.editorPane.setFont(newFont);

        this.editorScrollPane.getGutter().setLineNumberFont(newFont);
    }
}
