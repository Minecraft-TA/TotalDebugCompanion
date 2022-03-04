package com.github.minecraft_ta.totalDebugCompanion.jdt;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.IBufferChangedListener;
import org.eclipse.jdt.core.IOpenable;
import org.eclipse.jdt.core.JavaModelException;

public class StringBufferImpl implements IBuffer {

    private final String s;

    public StringBufferImpl(String s) {
        this.s = s;
    }

    @Override
    public void addBufferChangedListener(IBufferChangedListener listener) {

    }

    @Override
    public void append(char[] text) {

    }

    @Override
    public void append(String text) {

    }

    @Override
    public void close() {

    }

    @Override
    public char getChar(int position) {
        return getCharacters()[position];
    }

    @Override
    public char[] getCharacters() {
        System.out.println("Called toCharacters");
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
    public IOpenable getOwner() {
        return null;
    }

    @Override
    public String getText(int offset, int length) throws IndexOutOfBoundsException {
        return null;
    }

    @Override
    public IResource getUnderlyingResource() {
        return null;
    }

    @Override
    public boolean hasUnsavedChanges() {
        return false;
    }

    @Override
    public boolean isClosed() {
        return false;
    }

    @Override
    public boolean isReadOnly() {
        return false;
    }

    @Override
    public void removeBufferChangedListener(IBufferChangedListener listener) {

    }

    @Override
    public void replace(int position, int length, char[] text) {

    }

    @Override
    public void replace(int position, int length, String text) {

    }

    @Override
    public void save(IProgressMonitor progress, boolean force) throws JavaModelException {

    }

    @Override
    public void setContents(char[] contents) {

    }

    @Override
    public void setContents(String contents) {

    }
}
