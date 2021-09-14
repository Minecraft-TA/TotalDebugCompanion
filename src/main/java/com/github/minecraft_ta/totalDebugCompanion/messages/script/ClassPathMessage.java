package com.github.minecraft_ta.totalDebugCompanion.messages.script;

import com.github.tth05.scnet.message.AbstractMessage;
import com.github.tth05.scnet.util.ByteBufferInputStream;
import com.github.tth05.scnet.util.ByteBufferOutputStream;

public class ClassPathMessage extends AbstractMessage {

    private String classPath;

    @Override
    public void read(ByteBufferInputStream messageStream) {
        this.classPath = messageStream.readString();
    }

    @Override
    public void write(ByteBufferOutputStream messageStream) {
        //Acts as a request for this message when sent
    }

    public String getClassPath() {
        return classPath;
    }
}
