package com.github.minecraft_ta.totalDebugCompanion.messages.chunkGrid;

import com.github.tth05.scnet.message.AbstractMessageOutgoing;
import com.github.tth05.scnet.util.ByteBufferOutputStream;

/**
 * Message from the companion app which signals if it wants to start or stop receiving chunk view data.
 */
public class ReceiveDataStateMessage extends AbstractMessageOutgoing {

    private boolean state;

    public ReceiveDataStateMessage() {
    }

    public ReceiveDataStateMessage(boolean state) {
        this.state = state;
    }

    @Override
    public void write(ByteBufferOutputStream messageStream) {
        messageStream.writeBoolean(this.state);
    }
}
