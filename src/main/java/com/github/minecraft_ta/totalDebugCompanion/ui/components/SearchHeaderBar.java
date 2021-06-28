package com.github.minecraft_ta.totalDebugCompanion.ui.components;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.github.minecraft_ta.totalDebugCompanion.search.SearchManager;
import com.github.minecraft_ta.totalDebugCompanion.util.DocumentChangeListener;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;

public class SearchHeaderBar extends JPanel {

    public SearchHeaderBar(SearchManager searchManager) {
        super();
        setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));

        var textField = new JTextField() {

            private final Icon searchIcon = new FlatSVGIcon("icons/search.svg");

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                var graphics = g.create();
                var insets = getBorder().getBorderInsets(this);
                searchIcon.paintIcon(this, graphics, 5, insets.top);
                graphics.dispose();
            }
        };
        textField.setBorder(new CompoundBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Color.GRAY), BorderFactory.createEmptyBorder(5, 25, 5, 5)));
        textField.setPreferredSize(new Dimension(400, (int) textField.getPreferredSize().getHeight()));
        textField.setMaximumSize(textField.getPreferredSize());
        textField.getDocument().addDocumentListener((DocumentChangeListener) e -> {
            searchManager.setQuery(textField.getText());
        });
        textField.addActionListener(e -> searchManager.focusNextMatch());
        add(textField);

        add(createFlatButton(new FlatSVGIcon("icons/previousOccurence.svg"), (e) -> searchManager.focusPreviousMatch()));
        add(createFlatButton(new FlatSVGIcon("icons/nextOccurence.svg"), (e) -> searchManager.focusNextMatch()));
        JLabel indexLabel = new JLabel("");
        searchManager.addFocusedIndexChangedListener(i -> {
            if (searchManager.getMatchCount() == 0) {
                indexLabel.setText("");
                return;
            }
            indexLabel.setText((i + 1) + "/" + searchManager.getMatchCount());
        });
        add(indexLabel);
        add(Box.createHorizontalGlue());

        setMaximumSize(new Dimension(10000, (int) getPreferredSize().getHeight()));
        setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.GRAY));

        SwingUtilities.invokeLater(textField::requestFocus);
    }

    private JButton createFlatButton(Icon icon, Consumer<MouseEvent> clickListener) {
        var button = new JButton(icon);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                clickListener.accept(e);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                button.setContentAreaFilled(true);
                button.setBackground(Color.GRAY.darker());
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setContentAreaFilled(false);
            }
        });

        return button;
    }
}
