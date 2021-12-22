package com.github.minecraft_ta.totalDebugCompanion.messages.packetLogger;

import com.github.tth05.scnet.message.AbstractMessageIncoming;
import com.github.tth05.scnet.util.ByteBufferInputStream;

import java.util.HashMap;
import java.util.Map;

public class OutgoingPacketsMessage extends AbstractMessageIncoming {

    private final Map<String, Integer> outgoingPackets = new HashMap<>();

    @Override
    public void read(ByteBufferInputStream messageStream) {
        int count = messageStream.readInt();
        for (int i = 0; i < count; i++) {
            outgoingPackets.put(messageStream.readString(), messageStream.readInt());
        }
    }

    public Map<String, Integer> getOutgoingPackets() {
        return outgoingPackets;
    }
}
