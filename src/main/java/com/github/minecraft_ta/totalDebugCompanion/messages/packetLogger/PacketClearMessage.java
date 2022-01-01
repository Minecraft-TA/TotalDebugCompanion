package com.github.minecraft_ta.totalDebugCompanion.messages.packetLogger;

import com.github.tth05.scnet.message.AbstractMessageOutgoing;
import com.github.tth05.scnet.util.ByteBufferOutputStream;

public class PacketClearMessage extends AbstractMessageOutgoing {

    @Override
    public void write(ByteBufferOutputStream messageStream) {
        // Nothing to write
    }
}
