package com.github.minecraft_ta.totalDebugCompanion.model;

import com.github.minecraft_ta.totalDebugCompanion.ui.components.editors.BaseScriptPanel;

import java.awt.*;

public class BaseScriptView extends ScriptView {

    public BaseScriptView(String scriptName) {
        super(scriptName);
    }

    @Override
    public Component getComponent() {
        if (this.scriptPanel == null)
            this.scriptPanel = new BaseScriptPanel(this);
        return this.scriptPanel;
    }
}
