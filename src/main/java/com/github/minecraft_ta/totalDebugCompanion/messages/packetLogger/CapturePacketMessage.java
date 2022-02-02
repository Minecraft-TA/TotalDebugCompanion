package com.github.minecraft_ta.totalDebugCompanion.messages.packetLogger;

import com.github.tth05.scnet.message.AbstractMessageOutgoing;
import com.github.tth05.scnet.util.ByteBufferOutputStream;

public class CapturePacketMessage extends AbstractMessageOutgoing {

    private final String packet;
    private final boolean remove;

    public CapturePacketMessage(String packet, boolean remove) {
        this.packet = packet;
        this.remove = remove;
    }

    @Override
    public void write(ByteBufferOutputStream messageStream) {
        messageStream.writeString(packet);
        messageStream.writeBoolean(remove);
    }
}
