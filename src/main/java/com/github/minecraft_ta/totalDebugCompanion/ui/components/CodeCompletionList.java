package com.github.minecraft_ta.totalDebugCompanion.ui.components;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import org.eclipse.lsp4j.CompletionItem;

import javax.swing.*;
import java.awt.*;

public class CodeCompletionList extends JList<CompletionItem> {

    public CodeCompletionList() {
        super(new DefaultListModel<>());
        setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                var item = (CompletionItem) value;
                var label = item.getLabel();
                var dividerIndex = label.indexOf('-');
                if (dividerIndex == -1)
                    dividerIndex = label.indexOf(':');

                var renderText = dividerIndex == -1 ? label : """
                        <html><span style='color: rgb(187, 187, 187)'>%s</span>  <span style='color: rgb(150, 150, 150)'>%s</span></html>
                        """.formatted(label.substring(0, dividerIndex - 1), label.substring(dividerIndex + 2));
                var component = super.getListCellRendererComponent(list, renderText, index, isSelected, cellHasFocus);

                setIcon(switch (item.getKind()) {
                    case Method -> new FlatSVGIcon("icons/method.svg");
                    case Class -> new FlatSVGIcon("icons/class.svg");
                    case Constant -> new FlatSVGIcon("icons/constant.svg");
                    case Field -> new FlatSVGIcon("icons/property.svg");
                    case Variable -> new FlatSVGIcon("icons/variable.svg");
                    case Interface -> new FlatSVGIcon("icons/interface.svg");
                    case Enum -> new FlatSVGIcon("icons/enum.svg");
                    case Constructor -> new FlatSVGIcon("icons/constructor.svg");
                    default -> null;
                });

                return component;
            }
        });
    }
}
