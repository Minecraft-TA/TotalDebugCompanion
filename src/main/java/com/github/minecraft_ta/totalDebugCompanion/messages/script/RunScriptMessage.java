package com.github.minecraft_ta.totalDebugCompanion.messages.script;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.github.tth05.scnet.message.AbstractMessageOutgoing;
import com.github.tth05.scnet.util.ByteBufferOutputStream;

import javax.swing.*;

public class RunScriptMessage extends AbstractMessageOutgoing {

    private final int scriptId;
    private final String scriptText;
    private final boolean serverSide;
    private final ExecutionEnvironment executionEnvironment;

    public RunScriptMessage(int scriptId, String scriptText, boolean serverSide, ExecutionEnvironment executionEnvironment) {
        this.scriptId = scriptId;
        this.scriptText = scriptText;
        this.serverSide = serverSide;
        this.executionEnvironment = executionEnvironment;
    }

    @Override
    public void write(ByteBufferOutputStream messageStream) {
        messageStream.writeInt(this.scriptId);
        messageStream.writeString(this.scriptText);
        messageStream.writeBoolean(this.serverSide);
        messageStream.writeString(executionEnvironment.name());
    }

    public enum ExecutionEnvironment {
        THREAD(html("Thread", "Run on a separate thread"), null),
        PRE_TICK(html("Pre Tick", "Run on main thread, pre game loop tick"), new FlatSVGIcon("icons/warning.svg")),
        POST_TICK(html("Post Tick", "Run on main thread, post game loop tick"), new FlatSVGIcon("icons/warning.svg"));

        private final String label;
        private final Icon icon;

        ExecutionEnvironment(String label, Icon icon) {
            this.label = label;
            this.icon = icon;
        }

        public String getLabel() {
            return label;
        }

        public Icon getIcon() {
            return this.icon;
        }

        private static String html(String label, String desc) {
            return "<html><span style='color: rgb(187, 187, 187)'>%s</span> - <span style='color: rgb(150, 150, 150)'>%s</span></html>".formatted(label, desc);
        }
    }
}
