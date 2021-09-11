package com.github.minecraft_ta.totalDebugCompanion.ui.components.editors;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.github.minecraft_ta.totalDebugCompanion.CompanionApp;
import com.github.minecraft_ta.totalDebugCompanion.messages.script.RunScriptMessage;
import com.github.minecraft_ta.totalDebugCompanion.messages.script.ScriptStatusMessage;
import com.github.minecraft_ta.totalDebugCompanion.model.ScriptView;
import com.github.minecraft_ta.totalDebugCompanion.ui.components.FlatIconButton;
import com.github.minecraft_ta.totalDebugCompanion.util.CodeUtils;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.function.Consumer;

public class ScriptPanel extends AbstractCodeViewPanel {

    private static int SCRIPT_ID = 0;
    private int scriptId = SCRIPT_ID++;

    public ScriptPanel(ScriptView scriptView) {
        super();

        CompanionApp.SERVER.getMessageBus().listenAlways(ScriptStatusMessage.class, (m) -> {
            if (m.getScriptId() != this.scriptId)
                return;

            System.out.println(m.getMessage());
        });

        var headerBar = Box.createHorizontalBox();
        headerBar.setBackground(Color.GRAY);
        headerBar.setBorder(new CompoundBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.GRAY), BorderFactory.createEmptyBorder(5, 0, 5, 0)));

        var runExecutor = (Consumer<Boolean>) (server) -> {
            if (!CompanionApp.SERVER.isClientConnected()) {
                this.bottomInformationBar.setInfoText("Not connected to game client!", new Color(255, 103, 103));
                return;
            }

            this.bottomInformationBar.clearInfoText();
            CompanionApp.SERVER.getMessageProcessor().enqueueMessage(new RunScriptMessage(this.scriptId++, this.editorPane.getText(), server));
        };
        var runButton = new FlatIconButton(new FlatSVGIcon("icons/run.svg"), false);
        runButton.addActionListener(e -> runExecutor.accept(false));
        runButton.setToolTipText("Run on client");

        var runServerButton = new FlatIconButton(new FlatSVGIcon("icons/runServer.svg"), false);
        runServerButton.addActionListener(e -> runExecutor.accept(true));
        runServerButton.setToolTipText("Run on server");

        headerBar.add(runButton);
        headerBar.add(runServerButton);
        setHeaderComponent(headerBar);

        var textArea = this.editorPane;

        textArea.setParagraphAttributes(
                StyleContext.getDefaultStyleContext().addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.TabSet,
                        new TabSet(new TabStop[]{
                                new TabStop(30, TabStop.ALIGN_LEFT, TabStop.LEAD_NONE),
                                new TabStop(60, TabStop.ALIGN_LEFT, TabStop.LEAD_NONE),
                                new TabStop(90, TabStop.ALIGN_LEFT, TabStop.LEAD_NONE),
                                new TabStop(120, TabStop.ALIGN_LEFT, TabStop.LEAD_NONE)
                        })
                ),
                false
        );

        textArea.setBackground(new Color(60, 63, 65));
        textArea.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                SwingUtilities.invokeLater(() -> {
                    var styledDocument = textArea.getStyledDocument();
                    styledDocument.setCharacterAttributes(0, styledDocument.getLength(), new SimpleAttributeSet(), true);

                    CodeUtils.highlightJavaCode(textArea.getText().replace("\r", ""), textArea);
                    updateLineNumbers();
                });
            }
        });

        updateLineNumbers();
    }
}
