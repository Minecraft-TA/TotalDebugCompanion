package com.github.minecraft_ta.totalDebugCompanion.messages.packetLogger;

import com.github.tth05.scnet.message.AbstractMessageOutgoing;
import com.github.tth05.scnet.util.ByteBufferOutputStream;

public class BlockPacketMessage extends AbstractMessageOutgoing {

    private final String packet;

    public BlockPacketMessage(String packet) {
        this.packet = packet;
    }

    @Override
    public void write(ByteBufferOutputStream messageStream) {
        messageStream.writeString(packet);
    }
}
