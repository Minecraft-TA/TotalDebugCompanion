package com.github.minecraft_ta.totalDebugCompanion.messages.packetLogger;

import com.github.javaparser.utils.Pair;
import com.github.tth05.scnet.message.AbstractMessageIncoming;
import com.github.tth05.scnet.util.ByteBufferInputStream;

import java.util.HashMap;
import java.util.Map;

public class IncomingPacketsMessage extends AbstractMessageIncoming {

    private final Map<String, Pair<Integer, Integer>> incomingPackets = new HashMap<>();

    @Override
    public void read(ByteBufferInputStream messageStream) {
        int count = messageStream.readInt();
        for (int i = 0; i < count; i++) {
            incomingPackets.put(messageStream.readString(), new Pair<>(messageStream.readInt(), messageStream.readInt()));
        }
    }

    public Map<String, Pair<Integer, Integer>> getIncomingPackets() {
        return incomingPackets;
    }
}
