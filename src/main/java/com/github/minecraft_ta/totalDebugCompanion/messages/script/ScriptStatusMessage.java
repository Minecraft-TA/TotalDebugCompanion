package com.github.minecraft_ta.totalDebugCompanion.messages.script;

import com.github.tth05.scnet.message.AbstractMessageIncoming;
import com.github.tth05.scnet.util.ByteBufferInputStream;
import com.github.tth05.scnet.util.ByteBufferOutputStream;

public class ScriptStatusMessage extends AbstractMessageIncoming {

    private int scriptId;
    private Type type;
    private String message;

    public ScriptStatusMessage() {
    }

    @Override
    public void read(ByteBufferInputStream messageStream) {
        this.scriptId = messageStream.readInt();
        this.type = Type.valueOf(messageStream.readString());
        this.message = messageStream.readString();
    }

    @Override
    public void write(ByteBufferOutputStream messageStream) {
        messageStream.writeInt(this.scriptId);
        messageStream.writeString(this.type.name());
        messageStream.writeString(this.message);
    }

    public static enum Type {
        COMPILATION_FAILED,
        RUN_EXCEPTION,
        RUN_COMPLETED;
    }

    public int getScriptId() {
        return scriptId;
    }

    public Type getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }
}
