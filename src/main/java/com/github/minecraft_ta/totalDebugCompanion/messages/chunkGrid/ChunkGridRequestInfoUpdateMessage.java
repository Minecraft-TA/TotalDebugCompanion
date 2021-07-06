package com.github.minecraft_ta.totalDebugCompanion.messages.chunkGrid;

import com.github.minecraft_ta.totalDebugCompanion.util.ChunkGridRequestInfo;
import com.github.tth05.scnet.message.AbstractMessageOutgoing;
import com.github.tth05.scnet.util.ByteBufferOutputStream;

public class ChunkGridRequestInfoUpdateMessage extends AbstractMessageOutgoing {

    private ChunkGridRequestInfo chunkGridRequestInfo;

    public ChunkGridRequestInfoUpdateMessage() {
    }

    public ChunkGridRequestInfoUpdateMessage(ChunkGridRequestInfo chunkGridRequestInfo) {
        this.chunkGridRequestInfo = chunkGridRequestInfo;
    }

    @Override
    public void write(ByteBufferOutputStream messageStream) {
        this.chunkGridRequestInfo.toBytes(messageStream);
    }
}
