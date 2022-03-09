package com.github.minecraft_ta.totalDebugCompanion.jdt.impls;

import com.github.minecraft_ta.totalDebugCompanion.jdt.stubs.IFileStub;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IBuffer;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class FileImpl implements IFileStub {

    private final IBuffer contents;

    public FileImpl(IBuffer contents) {
        this.contents = contents;
    }

    @Override
    public InputStream getContents() throws CoreException {
        return getContents(false);
    }

    @Override
    public InputStream getContents(boolean force) {
        return new ByteArrayInputStream(this.contents.getContents().getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public boolean exists() {
        return true;
    }
}
