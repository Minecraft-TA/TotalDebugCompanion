package com.github.minecraft_ta.totalDebugCompanion.ui.components.global;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.github.minecraft_ta.totalDebugCompanion.search.SearchManager;
import com.github.minecraft_ta.totalDebugCompanion.ui.components.FlatIconButton;
import com.github.minecraft_ta.totalDebugCompanion.ui.components.FlatIconTextField;
import com.github.minecraft_ta.totalDebugCompanion.util.DocumentChangeListener;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

public class SearchHeaderBar extends JPanel {

    public SearchHeaderBar(SearchManager searchManager) {
        super();
        setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));

        var textField = new FlatIconTextField(new FlatSVGIcon("icons/search.svg"));
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
        add(createFlatButton("Previous", new FlatSVGIcon("icons/previousOccurence.svg"), searchManager::focusPreviousMatch));
        add(createFlatButton("Next", new FlatSVGIcon("icons/nextOccurence.svg"), searchManager::focusNextMatch));

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

    private JButton createToggleableFlatButton(String tooltip, FlatSVGIcon icon, Consumer<Boolean> toggleListener) {
        var button = new FlatIconButton(icon, true);
        button.setToolTipText(tooltip);
        button.addToggleListener(toggleListener);
        return button;
    }

    private JButton createFlatButton(String tooltip, FlatSVGIcon icon, Runnable clickListener) {
        var button = new FlatIconButton(icon, false);
        button.setToolTipText(tooltip);
        button.addActionListener((e) -> clickListener.run());
        return button;
    }
}
