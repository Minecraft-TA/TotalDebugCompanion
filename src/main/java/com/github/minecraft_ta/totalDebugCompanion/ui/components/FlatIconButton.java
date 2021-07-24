package com.github.minecraft_ta.totalDebugCompanion.ui.components;

import com.formdev.flatlaf.extras.FlatSVGIcon;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class FlatIconButton extends JButton {

    private final Color HOVER_COLOR = Color.GRAY.darker();
    private final Color TOGGLED_COLOR = new Color(90, 90, 90);
    private boolean state;

    private final List<Consumer<Boolean>> toggleListeners = new ArrayList<>();

    public FlatIconButton(FlatSVGIcon icon, boolean toggleable) {
        super(icon);
        setBorderPainted(false);
        setFocusPainted(false);
        setContentAreaFilled(false);

        addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                if (!toggleable)
                    return;

                state = !state;
                toggleListeners.forEach(l -> l.accept(state));

                if (state) {
                    setContentAreaFilled(true);
                    setBackground(HOVER_COLOR);
                    icon.setColorFilter(new FlatSVGIcon.ColorFilter((c) -> new Color(74, 136, 199)));
                } else {
                    setBackground(HOVER_COLOR);
                    icon.setColorFilter(null);
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                setContentAreaFilled(true);
                setBackground(!toggleable ? Color.GRAY.darker() : HOVER_COLOR);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (toggleable && state)
                    setBackground(TOGGLED_COLOR);
                setContentAreaFilled(false);
            }
        });
    }

    public void addToggleListener(Consumer<Boolean> listener) {
        this.toggleListeners.add(listener);
    }
}
