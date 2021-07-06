package com.github.minecraft_ta.totalDebugCompanion.messages.codeView;

import com.github.tth05.scnet.message.AbstractMessageOutgoing;
import com.github.tth05.scnet.util.ByteBufferOutputStream;

public class CodeViewClickMessage extends AbstractMessageOutgoing {

    private final String fileName;
    private final int row;
    private final int column;

    public CodeViewClickMessage(String fileName, int row, int column) {
        this.fileName = fileName;
        this.row = row;
        this.column = column;
    }

    @Override
    public void write(ByteBufferOutputStream messageStream) {
        messageStream.writeString(this.fileName);
        messageStream.writeInt(this.row);
        messageStream.writeInt(this.column);
    }
}
