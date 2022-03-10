package com.github.minecraft_ta.totalDebugCompanion.ui.views;

import com.github.minecraft_ta.totalDebugCompanion.ui.components.editors.AbstractCodeViewPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

public class CodeCompletionPopup extends BasePopup {

    private final JList<Object> completionItemList = new JList<>(new DefaultListModel<>());
    {
        //TODO: Fix completion popup
        /*completionItemList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                var item = (CompletionItem) value;
                var label = item.getLabel().replace("<", "&#60;").replace(">", "&#62;");
                var dividerIndex = label.indexOf('-');
                if (dividerIndex == -1)
                    dividerIndex = label.indexOf(':');

                var renderText = dividerIndex == -1 ? label :
                        TextUtils.htmlHighlightString(label.substring(0, dividerIndex - 1), "  ", label.substring(dividerIndex + 2));
                var component = super.getListCellRendererComponent(list, renderText, index, isSelected, cellHasFocus);

                setIcon(switch (item.getKind()) {
                    case Method -> Icons.JAVA_METHOD;
                    case Class -> Icons.JAVA_CLASS;
                    case Constant -> Icons.JAVA_CONSTANT;
                    case Field -> Icons.JAVA_PROPERTY;
                    case Variable -> Icons.JAVA_VARIABLE;
                    case Interface -> Icons.JAVA_INTERFACE;
                    case Enum -> Icons.JAVA_ENUM;
                    case Constructor -> Icons.JAVA_CONSTRUCTOR;
                    default -> null;
                });

                return component;
            }
        });*/
        completionItemList.setFont(AbstractCodeViewPanel.JETBRAINS_MONO_FONT.deriveFont(14f));
        completionItemList.setSelectionBackground(UIManager.getColor("List.selectionBackground"));
        completionItemList.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    setVisible(false);
                } else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    enterKeyListeners.forEach(Runnable::run);
                }
            }
        });
    }

    private final JScrollPane scrollPane = new JScrollPane(completionItemList);
    {
        scrollPane.setPreferredSize(new Dimension(450, 200));
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
    }

    private final List<Runnable> enterKeyListeners = new ArrayList<>();

    public CodeCompletionPopup() {
        add(this.scrollPane, BorderLayout.CENTER);
        pack();
    }

    public void scrollRectToVisible(Rectangle cellBounds) {
        this.completionItemList.scrollRectToVisible(cellBounds);
    }

    public void setItems(List<?> items) {
        /*var model = ((DefaultListModel<CompletionItem>) this.completionItemList.getModel());
        model.removeAllElements();
        model.addAll(items);
        this.completionItemList.setSelectedIndex(0);
        this.scrollPane.getVerticalScrollBar().setValue(0);

        var longestItemLength = this.completionItemList.getFontMetrics(this.completionItemList.getFont()).stringWidth(
                items.stream().max(Comparator.comparingInt(i -> i.getLabel().length())).get().getLabel()
        );
        this.scrollPane.setPreferredSize(new Dimension(longestItemLength + 25, Math.min(200, this.completionItemList.getPreferredSize().height)));
        pack();*/
    }

    public void addKeyEnterListener(Runnable r) {
        this.enterKeyListeners.add(r);
    }

    @Override
    public void setFont(Font f) {
        this.completionItemList.setFont(f);
    }

    public void setSelectedIndex(int selectedIndex) {
        this.completionItemList.setSelectedIndex(selectedIndex);
    }

    public int getSelectedIndex() {
        return this.completionItemList.getSelectedIndex();
    }

   /* public CompletionItem getSelectedValue() {
        return this.completionItemList.getSelectedValue();
    }*/

    public Rectangle getCellBounds(int index1, int index2) {
        return this.completionItemList.getCellBounds(index1, index2);
    }

   /* public DefaultListModel<CompletionItem> getModel() {
        return ((DefaultListModel<CompletionItem>) this.completionItemList.getModel());
    }*/
}
