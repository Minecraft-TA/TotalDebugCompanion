package com.github.minecraft_ta.totalDebugCompanion.messages.chunkGrid;

import com.github.tth05.scnet.message.AbstractMessageOutgoing;
import com.github.tth05.scnet.util.ByteBufferOutputStream;

public class UpdateFollowPlayerStateMessage extends AbstractMessageOutgoing {

    public static final byte STATE_NONE = 0;
    public static final byte STATE_ONCE = 1;
    public static final byte STATE_FOLLOW = 2;

    private final int state;

    public UpdateFollowPlayerStateMessage(int state) {
        this.state = state;
    }

    @Override
    public void write(ByteBufferOutputStream messageStream) {
        messageStream.writeByte(this.state);
    }
}
