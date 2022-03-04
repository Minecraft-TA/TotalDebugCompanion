package com.github.minecraft_ta.totalDebugCompanion.jdt;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.internal.core.CompilationUnit;
import org.eclipse.jdt.internal.core.DefaultWorkingCopyOwner;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.jdt.internal.core.PackageFragmentRoot;

public class InternalCompilationUnit extends CompilationUnit {

    private final IBuffer buffer;

    public InternalCompilationUnit(String name, String contents) {
        //TODO: is the name relevant?
        super(null, name, DefaultWorkingCopyOwner.PRIMARY);
        this.buffer = new StringBufferImpl(contents);
    }

    @Override
    public IResource resource(PackageFragmentRoot root) {
        return new DummyFile(this.buffer);
    }

    @Override
    public IBuffer getBuffer() {
        return this.buffer;
    }

    @Override
    protected IStatus validateCompilationUnit(IResource resource) {
        return Status.OK_STATUS;
    }

    @Override
    public boolean isWorkingCopy() {
        return false;
    }

    @Override
    public JavaProject getJavaProject() {
        return JDTHacks.DUMMY_JAVA_PROJECT;
    }

}
