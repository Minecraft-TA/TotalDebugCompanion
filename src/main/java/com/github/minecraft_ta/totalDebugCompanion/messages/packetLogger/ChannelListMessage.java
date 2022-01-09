package com.github.minecraft_ta.totalDebugCompanion.messages.packetLogger;

import com.github.tth05.scnet.message.AbstractMessageIncoming;
import com.github.tth05.scnet.util.ByteBufferInputStream;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ChannelListMessage extends AbstractMessageIncoming {

    private final List<String> channels;

    public ChannelListMessage() {
        this.channels = new ArrayList<>();
    }

    @Override
    public void read(ByteBufferInputStream messageStream) {
        int size = messageStream.readInt();
        for (int i = 0; i < size; i++) {
            channels.add(messageStream.readString());
        }
        channels.add("minecraft");
        channels.sort(Comparator.comparing(String::toLowerCase));
    }

    public List<String> getChannels() {
        return channels;
    }
}
