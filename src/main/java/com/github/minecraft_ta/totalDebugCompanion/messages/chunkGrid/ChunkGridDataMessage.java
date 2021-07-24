package com.github.minecraft_ta.totalDebugCompanion.messages.chunkGrid;

import com.github.minecraft_ta.totalDebugCompanion.util.ChunkGridRequestInfo;
import com.github.tth05.scnet.message.AbstractMessageIncoming;
import com.github.tth05.scnet.util.ByteBufferInputStream;

import java.util.HashMap;
import java.util.Map;

public class ChunkGridDataMessage extends AbstractMessageIncoming {

    private ChunkGridRequestInfo requestInfo;
    private Map<Long, Byte> stateMap;

    @Override
    public void read(ByteBufferInputStream messageStream) {
        this.requestInfo = ChunkGridRequestInfo.fromBytes(messageStream);
        int count = messageStream.readInt();

        this.stateMap = new HashMap<>();
        for (int i = 0; i < count; i++) {
            this.stateMap.put(messageStream.readLong(), messageStream.readByte());
        }
    }

    public Map<Long, Byte> getStateMap() {
        return this.stateMap;
    }
}
