package com.github.minecraft_ta.totalDebugCompanion.messages.packetLogger;

import com.github.tth05.scnet.message.AbstractMessageIncoming;
import com.github.tth05.scnet.util.ByteBufferInputStream;

public class PacketContentMessage extends AbstractMessageIncoming {

    private String packetName;
    private String channel;

    private String packetData;

    @Override
    public void read(ByteBufferInputStream messageStream) {
        packetName = messageStream.readString();
        channel = messageStream.readString();
        packetData = messageStream.readString();
    }

    public String getPacketName() {
        return packetName;
    }

    public String getChannel() {
        return channel;
    }

    public String getPacketData() {
        return packetData;
    }
}
