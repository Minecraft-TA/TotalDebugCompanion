package com.github.minecraft_ta.totalDebugCompanion.model;

import javax.swing.*;
import java.awt.*;

public interface IEditorPanel {

    String getTitle();

    String getIdentifier();

    String getTooltip();

    Icon getIcon();

    Component getComponent();
}
