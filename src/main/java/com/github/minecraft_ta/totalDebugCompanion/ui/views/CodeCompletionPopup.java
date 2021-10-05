package com.github.minecraft_ta.totalDebugCompanion.ui.views;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.github.minecraft_ta.totalDebugCompanion.ui.components.editors.AbstractCodeViewPanel;
import org.eclipse.lsp4j.CompletionItem;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

public class CodeCompletionPopup extends BasePopup {

    public static final FlatSVGIcon METHOD_ICON = new FlatSVGIcon("icons/method.svg");
    public static final FlatSVGIcon CLASS_ICON = new FlatSVGIcon("icons/class.svg");
    public static final FlatSVGIcon CONSTANT_ICON = new FlatSVGIcon("icons/constant.svg");
    public static final FlatSVGIcon PROPERTY_ICON = new FlatSVGIcon("icons/property.svg");
    public static final FlatSVGIcon VARIABLE_ICON = new FlatSVGIcon("icons/variable.svg");
    public static final FlatSVGIcon INTERFACE_ICON = new FlatSVGIcon("icons/interface.svg");
    public static final FlatSVGIcon ENUM_ICON = new FlatSVGIcon("icons/enum.svg");
    public static final FlatSVGIcon CONSTRUCTOR_ICON = new FlatSVGIcon("icons/constructor.svg");

    private final JList<CompletionItem> completionItemList = new JList<>(new DefaultListModel<>());
    {
        completionItemList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                var item = (CompletionItem) value;
                var label = item.getLabel().replace("<", "&#60;").replace(">", "&#62;");
                var dividerIndex = label.indexOf('-');
                if (dividerIndex == -1)
                    dividerIndex = label.indexOf(':');

                var renderText = dividerIndex == -1 ? label : """
                        <html><span style='color: rgb(187, 187, 187)'>%s</span>  <span style='color: rgb(150, 150, 150)'>%s</span></html>
                        """.formatted(label.substring(0, dividerIndex - 1), label.substring(dividerIndex + 2));
                var component = super.getListCellRendererComponent(list, renderText, index, isSelected, cellHasFocus);

                setIcon(switch (item.getKind()) {
                    case Method -> METHOD_ICON;
                    case Class -> CLASS_ICON;
                    case Constant -> CONSTANT_ICON;
                    case Field -> PROPERTY_ICON;
                    case Variable -> VARIABLE_ICON;
                    case Interface -> INTERFACE_ICON;
                    case Enum -> ENUM_ICON;
                    case Constructor -> CONSTRUCTOR_ICON;
                    default -> null;
                });

                return component;
            }
        });
        completionItemList.setFont(AbstractCodeViewPanel.JETBRAINS_MONO_FONT.deriveFont(14f));
        completionItemList.setSelectionBackground(new Color(5 / 255f, 127 / 255f, 242 / 255f, 0.5f));
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

    public void setItems(List<CompletionItem> items) {
        var model = ((DefaultListModel<CompletionItem>) this.completionItemList.getModel());
        model.removeAllElements();
        model.addAll(items);
        this.completionItemList.setSelectedIndex(0);
        this.scrollPane.getVerticalScrollBar().setValue(0);
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

    public CompletionItem getSelectedValue() {
        return this.completionItemList.getSelectedValue();
    }

    public Rectangle getCellBounds(int index1, int index2) {
        return this.completionItemList.getCellBounds(index1, index2);
    }

    public DefaultListModel<CompletionItem> getModel() {
        return ((DefaultListModel<CompletionItem>) this.completionItemList.getModel());
    }
}
