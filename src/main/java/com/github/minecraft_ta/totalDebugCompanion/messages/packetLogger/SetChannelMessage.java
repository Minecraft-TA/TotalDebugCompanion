package com.github.minecraft_ta.totalDebugCompanion.messages.packetLogger;

import com.github.tth05.scnet.message.AbstractMessageOutgoing;
import com.github.tth05.scnet.util.ByteBufferOutputStream;

public class SetChannelMessage extends AbstractMessageOutgoing {

    private final String channel;

    public SetChannelMessage(String channel) {
        this.channel = channel;
    }

    @Override
    public void write(ByteBufferOutputStream messageStream) {
        messageStream.writeString(channel);
    }
}
