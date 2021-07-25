package com.github.minecraft_ta.totalDebugCompanion.messages.chunkGrid;

import com.github.minecraft_ta.totalDebugCompanion.util.ChunkGridRequestInfo;
import com.github.tth05.scnet.message.AbstractMessage;
import com.github.tth05.scnet.util.ByteBufferInputStream;
import com.github.tth05.scnet.util.ByteBufferOutputStream;

public class ChunkGridRequestInfoUpdateMessage extends AbstractMessage {

    private ChunkGridRequestInfo chunkGridRequestInfo;

    public ChunkGridRequestInfoUpdateMessage() {
    }

    public ChunkGridRequestInfoUpdateMessage(ChunkGridRequestInfo chunkGridRequestInfo) {
        this.chunkGridRequestInfo = chunkGridRequestInfo;
    }

    @Override
    public void read(ByteBufferInputStream messageStream) {
        this.chunkGridRequestInfo = ChunkGridRequestInfo.fromBytes(messageStream);
    }

    @Override
    public void write(ByteBufferOutputStream messageStream) {
        this.chunkGridRequestInfo.toBytes(messageStream);
    }

    public ChunkGridRequestInfo getChunkGridRequestInfo() {
        return chunkGridRequestInfo;
    }
}
