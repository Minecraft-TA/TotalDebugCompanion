package com.github.minecraft_ta.totalDebugCompanion.messages.script;

import com.github.tth05.scnet.message.AbstractMessageOutgoing;
import com.github.tth05.scnet.util.ByteBufferOutputStream;

public class RunScriptMessage extends AbstractMessageOutgoing {

    private String scriptText;

    public RunScriptMessage(String scriptText) {
        this.scriptText = scriptText;
    }

    @Override
    public void write(ByteBufferOutputStream messageStream) {
        messageStream.writeString(this.scriptText);
    }
}
