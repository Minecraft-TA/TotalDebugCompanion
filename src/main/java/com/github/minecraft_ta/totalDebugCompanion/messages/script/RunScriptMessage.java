package com.github.minecraft_ta.totalDebugCompanion.messages.script;

import com.github.tth05.scnet.message.AbstractMessageOutgoing;
import com.github.tth05.scnet.util.ByteBufferOutputStream;

public class RunScriptMessage extends AbstractMessageOutgoing {

    private final int scriptId;
    private final String scriptText;
    private final boolean serverSide;

    public RunScriptMessage(int scriptId, String scriptText, boolean serverSide) {
        this.scriptId = scriptId;
        this.scriptText = scriptText;
        this.serverSide = serverSide;
    }

    @Override
    public void write(ByteBufferOutputStream messageStream) {
        messageStream.writeInt(this.scriptId);
        messageStream.writeString(this.scriptText);
        messageStream.writeBoolean(this.serverSide);
    }
}
