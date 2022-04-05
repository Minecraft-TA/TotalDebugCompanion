package com.github.minecraft_ta.totalDebugCompanion.jdt.impls;

import com.github.minecraft_ta.totalDebugCompanion.jdt.stubs.IBufferStub;

public class BufferImpl implements IBufferStub {

    private final String s;

    public BufferImpl(String s) {
        this.s = s;
    }

    @Override
    public char getChar(int position) {
        return getCharacters()[position];
    }

    @Override
    public char[] getCharacters() {
        return getContents().toCharArray();
    }

    @Override
    public String getContents() {
        return this.s;
    }

    @Override
    public int getLength() {
        return this.s.length();
    }

    @Override
    public String getText(int offset, int length) throws IndexOutOfBoundsException {
        return s.substring(offset, offset + length);
    }
}
