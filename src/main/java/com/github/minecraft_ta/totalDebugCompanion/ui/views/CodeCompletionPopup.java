package com.github.minecraft_ta.totalDebugCompanion.ui.views;

import com.github.minecraft_ta.totalDebugCompanion.Icons;
import com.github.minecraft_ta.totalDebugCompanion.jdt.completion.CompletionItem;
import com.github.minecraft_ta.totalDebugCompanion.util.TextUtils;

import javax.swing.*;
import java.awt.*;

public class CodeCompletionPopup extends BaseListPopup<CompletionItem> {

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

                var renderText = dividerIndex == -1 ? label :
                        TextUtils.htmlPrimarySecondaryString(label.substring(0, dividerIndex - 1), "  ", label.substring(dividerIndex + 2));
                renderText = item.getLabel();
                var component = super.getListCellRendererComponent(list, renderText, index, isSelected, cellHasFocus);

                setIcon(switch (item.getKind()) {
                    case METHOD -> Icons.JAVA_METHOD;
                    case CLASS -> Icons.JAVA_CLASS;
                    case ENUM -> Icons.JAVA_ENUM;
                    case INTERFACE -> Icons.JAVA_INTERFACE;
                    case CONSTANT -> Icons.JAVA_CONSTANT;
                    case ENUM_MEMBER, FIELD -> Icons.JAVA_PROPERTY;
                    case VARIABLE -> Icons.JAVA_VARIABLE;
                    case CONSTRUCTOR -> Icons.JAVA_CONSTRUCTOR;
                    case KEYWORD, IMPORT, LABEL, TEXT -> null;
                });

                return component;
            }
        });
    }


    public CodeCompletionPopup(Window owner) {
        super(owner);
        setList(this.completionItemList);
    }
}
