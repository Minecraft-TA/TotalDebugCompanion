package com.github.minecraft_ta.totalDebugCompanion.messages;

import com.github.tth05.scnet.message.AbstractMessageOutgoing;
import com.github.tth05.scnet.util.ByteBufferOutputStream;

public class DecompileAndOpenRequestMessage extends AbstractMessageOutgoing {

    private final String className;

    public DecompileAndOpenRequestMessage(String className) {
        this.className = className;
    }

    @Override
    public void write(ByteBufferOutputStream messageStream) {
        messageStream.writeString(this.className);
    }
}
