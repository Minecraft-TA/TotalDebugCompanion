package com.github.minecraft_ta.totalDebugCompanion.messages.packetLogger;

import com.github.tth05.scnet.message.AbstractMessageIncoming;
import com.github.tth05.scnet.util.ByteBufferInputStream;

import java.util.HashMap;
import java.util.Map;

public class IncomingPacketsMessage extends AbstractMessageIncoming {

    private final Map<String, Integer> incomingPackets = new HashMap<>();

    @Override
    public void read(ByteBufferInputStream messageStream) {
        int count = messageStream.readInt();
        for (int i = 0; i < count; i++) {
            incomingPackets.put(messageStream.readString(), messageStream.readInt());
        }
    }

    public Map<String, Integer> getIncomingPackets() {
        return incomingPackets;
    }
}
