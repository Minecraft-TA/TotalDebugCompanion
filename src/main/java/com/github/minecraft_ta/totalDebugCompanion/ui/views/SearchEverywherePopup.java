package com.github.minecraft_ta.totalDebugCompanion.ui.views;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.github.minecraft_ta.totalDebugCompanion.CompanionApp;
import com.github.minecraft_ta.totalDebugCompanion.messages.codeView.DecompileAndOpenRequestMessage;
import com.github.minecraft_ta.totalDebugCompanion.ui.components.FlatIconTextField;
import com.github.minecraft_ta.totalDebugCompanion.util.DocumentChangeListener;
import com.github.minecraft_ta.totalDebugCompanion.util.UIUtils;
import com.github.tth05.jindex.ClassIndex;
import com.github.tth05.jindex.IndexedClass;

import javax.swing.*;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Arrays;

public class SearchEverywherePopup extends JFrame {

    private static final ClassIndex CLASS_INDEX = new ClassIndex(CompanionApp.getRootPath().resolve("index").toAbsolutePath().normalize().toString());

    private static final SearchEverywherePopup INSTANCE = new SearchEverywherePopup();

    private final JList<IndexedClass> resultList = new JList<>(new DefaultListModel<>());
    {
        resultList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                var indexedClass = (IndexedClass) value;
                var renderText = """
                        <html><span style='color: rgb(187, 187, 187)'>%s</span>  <span style='color: rgb(150, 150, 150)'>%s</span></html>
                        """.formatted(indexedClass.getName(), indexedClass.getNameWithPackage().substring(0, indexedClass.getNameWithPackage().lastIndexOf('.')));

                var component = super.getListCellRendererComponent(list, renderText, index, isSelected, cellHasFocus);
                setIcon(CodeCompletionPopup.CLASS_ICON);
                return component;
            }
        });
        resultList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() < 2 || !SwingUtilities.isLeftMouseButton(e))
                    return;

                var clickedIndex = resultList.locationToIndex(e.getPoint());
                if (clickedIndex == -1 || !resultList.getCellBounds(0, resultList.getLastVisibleIndex()).contains(e.getPoint()))
                    return;

                CompanionApp.SERVER.getMessageProcessor().enqueueMessage(new DecompileAndOpenRequestMessage(resultList.getModel().getElementAt(clickedIndex).getNameWithPackage()));
                setVisible(false);
            }
        });
    }

    private final JScrollPane resultListScrollPane = new JScrollPane(this.resultList);
    {
        resultListScrollPane.setPreferredSize(new Dimension(500, 500));
    }

    private final FlatIconTextField searchTextField = new FlatIconTextField(new FlatSVGIcon("icons/search.svg"));
    {
        searchTextField.getDocument().addDocumentListener((DocumentChangeListener) e -> {
            var query = searchTextField.getText();
            var model = (DefaultListModel<IndexedClass>) resultList.getModel();
            if (query == null || query.isBlank()) {
                model.clear();
                return;
            }

            var classes = CLASS_INDEX.findClasses(query, 40);
            model.clear();
            model.addAll(Arrays.asList(classes));
        });
        ((AbstractDocument) searchTextField.getDocument()).setDocumentFilter(new DocumentFilter() {
            @Override
            public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
                if (!string.chars().allMatch(i -> i < 128))
                    return;

                super.insertString(fb, offset, string, attr);
            }

            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
                if (!text.chars().allMatch(i -> i < 128))
                    return;

                super.replace(fb, offset, length, text, attrs);
            }
        });
    }

    public SearchEverywherePopup() {
        setLayout(new BorderLayout());

        add(this.searchTextField, BorderLayout.NORTH);
        add(this.resultListScrollPane, BorderLayout.CENTER);

        setUndecorated(true);
        pack();
        addWindowFocusListener(new WindowAdapter() {
            @Override
            public void windowLostFocus(WindowEvent e) {
                INSTANCE.setVisible(false);
            }
        });
    }

    public static void open() {
        if (INSTANCE.isVisible()) {
            INSTANCE.toFront();
            return;
        }

        INSTANCE.setVisible(true);
        UIUtils.centerJFrame(INSTANCE);

        INSTANCE.searchTextField.grabFocus();
        INSTANCE.searchTextField.selectAll();
    }
}
