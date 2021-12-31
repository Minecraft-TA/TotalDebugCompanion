package com.github.minecraft_ta.totalDebugCompanion.messages.packetLogger;

import com.github.tth05.scnet.message.AbstractMessageOutgoing;
import com.github.tth05.scnet.util.ByteBufferOutputStream;

public class PacketLoggerStateChangeMessage extends AbstractMessageOutgoing {

    private final boolean logIncoming;
    private final boolean logOutgoing;

    public PacketLoggerStateChangeMessage(boolean logIncoming, boolean logOutgoing) {
        this.logIncoming = logIncoming;
        this.logOutgoing = logOutgoing;
    }

    @Override
    public void write(ByteBufferOutputStream messageStream) {
        messageStream.writeBoolean(logIncoming);
        messageStream.writeBoolean(logOutgoing);
    }
}
