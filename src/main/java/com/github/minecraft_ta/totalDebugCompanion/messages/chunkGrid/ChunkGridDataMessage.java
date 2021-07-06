package com.github.minecraft_ta.totalDebugCompanion.messages.chunkGrid;

import com.github.minecraft_ta.totalDebugCompanion.util.ChunkGridRequestInfo;
import com.github.tth05.scnet.message.AbstractMessageIncoming;
import com.github.tth05.scnet.util.ByteBufferInputStream;

public class ChunkGridDataMessage extends AbstractMessageIncoming {

    private ChunkGridRequestInfo requestInfo;
    private byte[][] stateArray;

    @Override
    public void read(ByteBufferInputStream messageStream) {
        this.requestInfo = ChunkGridRequestInfo.fromBytes(messageStream);
        short width = messageStream.readShort();
        short height = messageStream.readShort();

        this.stateArray = new byte[width][height];
        for (int i = 0; i < width; i++) {
            messageStream.readByteArray(this.stateArray[i], 0, height);
        }
    }

    public byte[][] getStateArray() {
        return this.stateArray;
    }
}
