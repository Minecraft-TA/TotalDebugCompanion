package com.github.minecraft_ta.totalDebugCompanion.ui.views;

import com.github.minecraft_ta.totalDebugCompanion.CompanionApp;
import com.github.minecraft_ta.totalDebugCompanion.Icons;
import com.github.minecraft_ta.totalDebugCompanion.messages.codeView.DecompileOrOpenMessage;
import com.github.minecraft_ta.totalDebugCompanion.util.TextUtils;
import com.github.tth05.jindex.IndexedClass;
import com.github.tth05.jindex.IndexedMethod;
import org.eclipse.jdt.core.IJavaElement;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;

public class FindImplementationsPopup extends BaseListPopup<FindImplementationsPopup.InternalListItem> {

    private final JList<InternalListItem> implementationsList = new JList<>(new DefaultListModel<>());
    {
        implementationsList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                var component = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setIcon(((InternalListItem) value).getIcon());
                return component;
            }
        });
    }

    public FindImplementationsPopup(Window owner) {
        super(owner);
        setList(this.implementationsList);
        addKeyEnterListener(InternalListItem::onAction);
    }

    public void setItems(IndexedClass indexedClass) {
        setItems(Arrays.stream(indexedClass.findImplementations(false)).map(ClassListItem::new).toList());
    }

    public void setItems(IndexedMethod indexedMethod) {
        setItems(Arrays.stream(indexedMethod.findImplementations(true)).map(MethodListItem::new).toList());
    }

    protected static abstract class InternalListItem implements ListItem {

        public abstract Icon getIcon();

        public abstract void onAction();

        public abstract String toString();
    }

    private static class ClassListItem extends InternalListItem {

        private final IndexedClass indexedClass;

        private ClassListItem(IndexedClass indexedClass) {
            this.indexedClass = indexedClass;
        }

        @Override
        public void onAction() {
            CompanionApp.SERVER.getMessageProcessor().enqueueMessage(new DecompileOrOpenMessage(indexedClass.getNameWithPackageDot()));
        }

        @Override
        public Icon getIcon() {
            return Icons.JAVA_CLASS;
        }

        @Override
        public int getLabelLength() {
            return this.indexedClass.getNameWithPackageDot().length() + /* '/' */ 1 + /* ' ' */ 1;
        }

        @Override
        public String toString() {
            return TextUtils.htmlHighlightString(indexedClass.getName(), " ", indexedClass.getPackage().getNameWithParentsDot());
        }
    }

    private static class MethodListItem extends InternalListItem {

        private final IndexedMethod indexedMethod;

        private MethodListItem(IndexedMethod indexedMethod) {
            this.indexedMethod = indexedMethod;
        }

        @Override
        public void onAction() {
            CompanionApp.SERVER.getMessageProcessor().enqueueMessage(new DecompileOrOpenMessage(
                    indexedMethod.getDeclaringClass().getNameWithPackageDot(),
                    IJavaElement.METHOD,
                    indexedMethod.getName() + indexedMethod.getDescriptorString())
            );
        }

        @Override
        public Icon getIcon() {
            return Icons.JAVA_METHOD;
        }

        @Override
        public int getLabelLength() {
            return indexedMethod.getDeclaringClass().getNameWithPackageDot().length() + 1 + 1 + indexedMethod.getName().length();
        }

        @Override
        public String toString() {
            var indexedClass = this.indexedMethod.getDeclaringClass();
            return TextUtils.htmlHighlightString(indexedClass.getName() + "#" + this.indexedMethod.getName(), " ", indexedClass.getPackage().getNameWithParentsDot());
        }
    }
}
