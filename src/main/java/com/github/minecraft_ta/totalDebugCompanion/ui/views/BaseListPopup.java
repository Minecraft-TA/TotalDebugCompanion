package com.github.minecraft_ta.totalDebugCompanion.ui.views;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class BaseListPopup<ITEM extends BaseListPopup.ListItem> extends BasePopup {

    private final List<Consumer<ITEM>> enterKeyListeners = new ArrayList<>();
    private final Listener listener = new Listener();
    private final JScrollPane scrollPane = new JScrollPane();
    {
        scrollPane.setPreferredSize(new Dimension(450, 200));
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
    }

    private JList<ITEM> list;
    private Component invoker;
    private int minimumListWidth = 200;
    private int boundXPos = -1;

    public BaseListPopup(Window owner) {
        super(owner);
        add(this.scrollPane, BorderLayout.CENTER);
    }

    public void show(JTextComponent invoker) {
        show(invoker, Alignment.BOTTOM_RIGHT);
    }

    public void show(JTextComponent invoker, Alignment alignment) {
        try {
            var cursorRect = invoker.modelToView2D(invoker.getCaretPosition());
            show(invoker, (int) cursorRect.getX(), (int) (cursorRect.getY() + cursorRect.getHeight()), alignment);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void show(Component invoker, int x, int y, Alignment alignment) {
        if (!(invoker instanceof JTextComponent))
            throw new IllegalArgumentException("invoker must be a JTextComponent");
        if (this.list.getModel().getSize() == 0)
            return;

        super.show(invoker, x, y, alignment);
        removeKeyListener();
        this.boundXPos = x;
        this.invoker = invoker;
        this.invoker.addKeyListener(this.listener);
    }

    @Override
    public void setVisible(boolean b) {
        super.setVisible(b);
        if (!b) {
            removeKeyListener();
            this.boundXPos = -1;
        }
    }

    private void removeKeyListener() {
        if (this.invoker != null)
            this.invoker.removeKeyListener(this.listener);
    }

    public void setList(JList<ITEM> list) {
        this.list = list;

        list.setSelectionBackground(UIManager.getColor("List.selectionBackground"));
        list.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    setVisible(false);
                } else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    runEnterKeyListeners();
                }
            }
        });
        list.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() != 2)
                    return;

                runEnterKeyListeners();
            }
        });

        this.scrollPane.setViewportView(list);
        pack();
    }

    public void setItems(List<? extends ITEM> items) {
        var prevWidth = getWidth();

        var model = ((DefaultListModel<ITEM>) this.list.getModel());
        model.removeAllElements();
        model.addAll(items);
        this.list.setSelectedIndex(0);
        this.scrollPane.getVerticalScrollBar().setValue(0);

        var longestItemLength = items.isEmpty() ? 0 : this.list.getFontMetrics(this.list.getFont()).stringWidth(
                "9".repeat(items.stream().mapToInt(ListItem::getLabelLength).max().getAsInt())
        );
        var preferredSize = new Dimension(longestItemLength + 35, Math.min(this.minimumListWidth, this.list.getPreferredSize().height));
        this.scrollPane.setPreferredSize(preferredSize);
        setMinimumSize(new Dimension(this.minimumListWidth, 20));
        pack();

        if(this.boundXPos != -1) {
            setLocation(getX() - (getWidth() - prevWidth)  / 2, getY());
        }
    }

    @Override
    public void setFont(Font f) {
        this.list.setFont(f);
    }

    public void setMinimumListWidth(int minimumListWidth) {
        this.minimumListWidth = minimumListWidth;
    }

    public void addKeyEnterListener(Consumer<ITEM> r) {
        this.enterKeyListeners.add(r);
    }

    private void runEnterKeyListeners() {
        if (this.list.getSelectedIndex() == -1)
            return;

        var val = this.list.getSelectedValue();
        this.enterKeyListeners.forEach(r -> r.accept(val));
    }

    public interface ListItem {

        int getLabelLength();
    }

    private class Listener extends KeyAdapter {

        @Override
        public void keyPressed(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                runEnterKeyListeners();
                e.consume();
            } else if (e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_DOWN) {
                var selectedIndex = list.getSelectedIndex() + (e.getKeyCode() == KeyEvent.VK_UP ? -1 : 1);
                if (selectedIndex > list.getModel().getSize() - 1)
                    selectedIndex = 0;
                else if (selectedIndex < 0)
                    selectedIndex = list.getModel().getSize() - 1;

                list.setSelectedIndex(selectedIndex);
                list.scrollRectToVisible(list.getCellBounds(selectedIndex, selectedIndex));
                e.consume();
            } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                setVisible(false);
                e.consume();
            }
        }
    }
}
