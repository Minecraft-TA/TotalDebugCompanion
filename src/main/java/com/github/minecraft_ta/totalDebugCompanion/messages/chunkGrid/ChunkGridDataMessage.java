package com.github.minecraft_ta.totalDebugCompanion.messages.chunkGrid;

import com.github.minecraft_ta.totalDebugCompanion.util.ChunkGridRequestInfo;
import com.github.tth05.scnet.message.AbstractMessageIncoming;
import com.github.tth05.scnet.util.ByteBufferInputStream;

import java.util.HashMap;
import java.util.Map;

public class ChunkGridDataMessage extends AbstractMessageIncoming {

    private ChunkGridRequestInfo requestInfo;
    private Map<Integer, Map<Integer, Byte>> stateMap;

    @Override
    public void read(ByteBufferInputStream messageStream) {
        this.requestInfo = ChunkGridRequestInfo.fromBytes(messageStream);
        int count = messageStream.readInt();

        var sizeEstimate = (int) Math.sqrt(count);
        this.stateMap = new HashMap<>(sizeEstimate);
        for (int i = 0; i < count; i++) {
            var encodedPos = messageStream.readLong();
            int x = (int) (encodedPos >> 32);
            int z = (int) (encodedPos);
            var state = messageStream.readByte();
            this.stateMap.computeIfAbsent(x, (k) -> new HashMap<>(sizeEstimate)).putIfAbsent(z, state);
        }
    }

    public Map<Integer, Map<Integer, Byte>> getStateMap() {
        return this.stateMap;
    }
}
