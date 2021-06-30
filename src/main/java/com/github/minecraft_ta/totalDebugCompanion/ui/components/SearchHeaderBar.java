package com.github.minecraft_ta.totalDebugCompanion.ui.components;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.github.minecraft_ta.totalDebugCompanion.search.SearchManager;
import com.github.minecraft_ta.totalDebugCompanion.util.DocumentChangeListener;

import javax.swing.*;
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
        textField.setBorder(BorderFactory.createEmptyBorder(5, 25, 5, 5));
        textField.setPreferredSize(new Dimension(250, (int) textField.getPreferredSize().getHeight()));
        textField.setMaximumSize(textField.getPreferredSize());
        textField.getDocument().addDocumentListener((DocumentChangeListener) e -> {
            searchManager.setQuery(textField.getText());
        });
        textField.addActionListener(e -> searchManager.focusNextMatch());
        add(textField);
        add(createSeparator());

        add(createToggleableFlatButton("Match case", new FlatSVGIcon("icons/matchCase.svg"), (b) -> {
            searchManager.setMatchCase(b);
            //Force refresh
            searchManager.setQuery(textField.getText());
        }));
        JButton regexButton = createToggleableFlatButton("Regex", new FlatSVGIcon("icons/regex.svg"), (b) -> {
            searchManager.setUseRegex(b);
            searchManager.setQuery(textField.getText());
        });
        add(regexButton);
        add(createSeparator());
        add(createFlatButton("Previous", new FlatSVGIcon("icons/previousOccurence.svg"), (e) -> searchManager.focusPreviousMatch()));
        add(createFlatButton("Next", new FlatSVGIcon("icons/nextOccurence.svg"), (e) -> searchManager.focusNextMatch()));

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

    private JSeparator createSeparator() {
        JSeparator separator = new JSeparator(JSeparator.VERTICAL);
        separator.setForeground(Color.GRAY);
        separator.setMaximumSize(new Dimension(1, 100));
        return separator;
    }

    private JButton createToggleableFlatButton(String tooltip, Icon icon, Consumer<Boolean> toggleListener) {
        var button = new JButton(icon);
        button.setToolTipText(tooltip);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.addMouseListener(new MouseAdapter() {

            private final Color HOVER_COLOR = Color.GRAY.darker();
            private final Color TOGGLED_COLOR = new Color(90, 90, 90);
            private boolean state;

            @Override
            public void mouseClicked(MouseEvent e) {
                this.state = !this.state;
                toggleListener.accept(this.state);

                if (this.state) {
                    button.setContentAreaFilled(true);
                    button.setBackground(HOVER_COLOR);
                    ((FlatSVGIcon) button.getIcon()).setColorFilter(new FlatSVGIcon.ColorFilter((c) -> new Color(74, 136, 199)));
                } else {
                    button.setBackground(HOVER_COLOR);
                    ((FlatSVGIcon) button.getIcon()).setColorFilter(null);
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                button.setContentAreaFilled(true);
                button.setBackground(HOVER_COLOR);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (this.state)
                    button.setBackground(TOGGLED_COLOR);
                else
                    button.setContentAreaFilled(false);
            }
        });

        return button;
    }

    private JButton createFlatButton(String tooltip, Icon icon, Consumer<MouseEvent> clickListener) {
        var button = new JButton(icon);
        button.setToolTipText(tooltip);
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