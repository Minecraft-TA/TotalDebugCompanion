package com.github.minecraft_ta.totalDebugCompanion.ui.components.editors;

import com.github.minecraft_ta.totalDebugCompanion.CompanionApp;
import com.github.minecraft_ta.totalDebugCompanion.GlobalConfig;
import com.github.minecraft_ta.totalDebugCompanion.messages.CodeViewClickMessage;
import com.github.minecraft_ta.totalDebugCompanion.model.CodeView;
import com.github.minecraft_ta.totalDebugCompanion.search.SearchManager;
import com.github.minecraft_ta.totalDebugCompanion.ui.components.FontSizeSliderBar;
import com.github.minecraft_ta.totalDebugCompanion.ui.components.SearchHeaderBar;
import com.github.minecraft_ta.totalDebugCompanion.util.UIUtils;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Utilities;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.Objects;

public class CodeViewPanel extends Box {

    public static Font JETBRAINS_MONO_FONT = null;
    static {
        try {
            JETBRAINS_MONO_FONT = Font.createFont(Font.TRUETYPE_FONT, Objects.requireNonNull(CodeViewPanel.class.getResourceAsStream("/font/jetbrainsmono_regular.ttf"), "Unable to load font"));
        } catch (FontFormatException | IOException e) {
            e.printStackTrace();
        }
    }

    private final JScrollPane scrollPane = new JScrollPane();
    private final JTextPane editorPane = new JTextPane();
    private final JTextArea lineNumbers = new JTextArea();

    private final SearchManager searchManager = new SearchManager(editorPane);

    private JComponent headerComponent;
    private int lineCount;

    public CodeViewPanel(CodeView codeView) {
        super(BoxLayout.Y_AXIS);

        this.editorPane.setEditable(false);
        this.editorPane.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int offset = editorPane.viewToModel2D(e.getPoint());
                Rectangle2D modelView;
                try {
                    modelView = editorPane.modelToView2D(offset);
                    //Did we click to the right of a line, and the cursor got adjusted to the left?
                    if (modelView.getX() < e.getX() && offset == Utilities.getRowEnd(editorPane, offset))
                        return;
                } catch (BadLocationException ex) {
                    throw new RuntimeException("Offset not in view", ex);
                }

                int line = editorPane.getDocument().getDefaultRootElement().getElementIndex(offset);
                int column = (offset - editorPane.getDocument().getDefaultRootElement().getElement(line).getStartOffset());

                CompanionApp.SERVER.getMessageProcessor().enqueueMessage(
                        new CodeViewClickMessage(codeView.getPath().getFileName().toString(), line, column)
                );
            }

            //@formatter:off
            @Override
            public void mousePressed(MouseEvent e) {}
            @Override
            public void mouseReleased(MouseEvent e) {}
            @Override
            public void mouseEntered(MouseEvent e) {}
            @Override
            public void mouseExited(MouseEvent e) {}
            //@formatter:on
        });

        this.lineNumbers.setEditable(false);
        this.lineNumbers.setForeground(Color.GRAY);
        this.lineNumbers.setHighlighter(null);

        this.scrollPane.setViewportView(UIUtils.horizontalLayout(this.lineNumbers, this.editorPane));
        this.scrollPane.setBorder(BorderFactory.createEmptyBorder());
        add(this.scrollPane);
        add(new FontSizeSliderBar());

        //Scrolling and fonts
        initFonts();
        initScrolling();
        GlobalConfig.getInstance().addPropertyChangeListener("scrollMul", event -> initScrolling());
        GlobalConfig.getInstance().addPropertyChangeListener("fontSize", event -> initFonts());

        //Ctrl+F keybind for search
        this.editorPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("ctrl pressed F"), "openSearchPopup");
        this.editorPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("ESCAPE"), "closeSearchPopup");
        this.editorPane.getActionMap().put("closeSearchPopup", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                removeHeaderComponent();
                searchManager.hideHighlights();
            }
        });
        this.editorPane.getActionMap().put("openSearchPopup", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setHeaderComponent(new SearchHeaderBar(searchManager));
            }
        });

        //Stop search thread if the tab is closed
        addHierarchyListener(e -> {
            if (e.getChangeFlags() == HierarchyEvent.PARENT_CHANGED && getParent() == null) {
                this.searchManager.stopThread();
            }
        });

        //Scroll to focused search position
        this.searchManager.addFocusedIndexChangedListener(i -> {
            if (this.searchManager.getMatchCount() == 0)
                return;

            SwingUtilities.invokeLater(() -> focusRange(this.searchManager.getFocusedRangeStart(), this.searchManager.getFocusedRangeEnd()));
        });
    }

    public void setCode(String code) {
        this.editorPane.setText(code);

        this.lineCount = code.split("\n").length;
        int lineNumberLength = (this.lineCount + "").length();

        var lineNumberTextBuilder = new StringBuilder();
        for (int i = 0; i < this.lineCount; i++) {
            lineNumberTextBuilder.append(String.format("%" + lineNumberLength + "d", i + 1));

            if (i != this.lineCount - 1)
                lineNumberTextBuilder.append("\n");
        }

        this.lineNumbers.setText(lineNumberTextBuilder.toString());
        //Adjust max width, otherwise it will take up too much space
        int charsWidth = UIUtils.getFontWidth(this.lineNumbers, "9".repeat(lineNumberLength));
        this.lineNumbers.setColumns(lineNumberLength);
        this.lineNumbers.setMaximumSize(new Dimension(charsWidth, Integer.MAX_VALUE));
    }

    public void focusLine(int line) {
        var verticalScrollBar = this.scrollPane.getVerticalScrollBar();
        verticalScrollBar.setValue((line - 1) * (verticalScrollBar.getMaximum() / this.lineCount));
    }

    public void focusRange(int offsetStart, int offsetEnd) {
        try {
            var rect = this.editorPane.modelToView2D(offsetStart);
            var viewport = this.scrollPane.getViewport();

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
        CodeViewPanel.this.add(component, 0);
        CodeViewPanel.this.revalidate();
        CodeViewPanel.this.repaint();
        SwingUtilities.invokeLater(() -> {
            //Adjust scroll bar to keep it in place
            var verticalScrollBar = scrollPane.getVerticalScrollBar();
            verticalScrollBar.setValue((int) (verticalScrollBar.getValue() + component.getPreferredSize().getHeight()));
        });
    }

    public void removeHeaderComponent() {
        if (this.headerComponent == null)
            return;

        synchronized (getTreeLock()) {
            Component component = getComponent(0);
            if (this.headerComponent == component) {
                CodeViewPanel.this.remove(0);
                CodeViewPanel.this.revalidate();
                CodeViewPanel.this.repaint();
                //Adjust scroll bar to keep it in place
                var verticalScrollBar = scrollPane.getVerticalScrollBar();
                verticalScrollBar.setValue(verticalScrollBar.getValue() - component.getHeight());
            }
        }

        this.headerComponent = null;
    }

    private void initFonts() {
        this.editorPane.setFont(JETBRAINS_MONO_FONT.deriveFont(GlobalConfig.getInstance().<Float>getValue("fontSize")));
        this.lineNumbers.setFont(this.editorPane.getFont());
    }

    private void initScrolling() {
        FontMetrics metrics = getFontMetrics(this.editorPane.getFont());
        int lineHeight = metrics.getHeight();
        int charWidth = metrics.getMaxAdvance();

        JScrollBar systemVBar = new JScrollBar(JScrollBar.VERTICAL);
        JScrollBar systemHBar = new JScrollBar(JScrollBar.HORIZONTAL);
        int verticalIncrement = systemVBar.getUnitIncrement();
        int horizontalIncrement = systemHBar.getUnitIncrement();

        float mul = GlobalConfig.getInstance().<Float>getValue("scrollMul");

        this.scrollPane.getVerticalScrollBar().setUnitIncrement((int) (lineHeight * verticalIncrement * mul));
        this.scrollPane.getHorizontalScrollBar().setUnitIncrement((int) (charWidth * horizontalIncrement * mul));
    }

    public JTextPane getEditorPane() {
        return this.editorPane;
    }
}
