package com.github.minecraft_ta.totalDebugCompanion.ui.components.editors;

import com.github.minecraft_ta.totalDebugCompanion.CompanionApp;
import com.github.minecraft_ta.totalDebugCompanion.messages.script.RunScriptMessage;
import com.github.minecraft_ta.totalDebugCompanion.model.ScriptView;
import com.github.minecraft_ta.totalDebugCompanion.util.CodeUtils;
import com.github.minecraft_ta.totalDebugCompanion.util.DocumentChangeListener;

import javax.swing.*;
import javax.swing.text.SimpleAttributeSet;
import java.awt.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class ScriptPanel extends AbstractCodeViewPanel {

    public ScriptPanel(ScriptView scriptView) {
        super();

        var headerBar = Box.createHorizontalBox();
        var runButton = new JButton("Run");
        runButton.addActionListener(e -> {
            CompanionApp.SERVER.getMessageProcessor().enqueueMessage(new RunScriptMessage(this.editorPane.getText()));
        });

        headerBar.add(runButton);
        setHeaderComponent(headerBar);

        var running = new AtomicBoolean();
        var textArea = this.editorPane;
        textArea.setBackground(new Color(60, 63, 65));
        textArea.getDocument().addDocumentListener((DocumentChangeListener) e -> {
            if (running.get())
                return;

            running.set(true);
            SwingUtilities.invokeLater(() -> {
                var styledDocument = textArea.getStyledDocument();
                styledDocument.setCharacterAttributes(0, styledDocument.getLength(), new SimpleAttributeSet(), true);

                CodeUtils.highlightJavaCode(textArea.getText().replace("\r", ""), textArea);
                running.set(false);
            });
            this.updateLineNumbers();
        });

        updateLineNumbers();
    }
}
