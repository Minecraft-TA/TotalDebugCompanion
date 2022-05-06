package com.github.minecraft_ta.totalDebugCompanion.jdt.impls;

import com.github.minecraft_ta.totalDebugCompanion.jdt.JDTHacks;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.BufferChangedEvent;
import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.CompilationUnit;
import org.eclipse.jdt.internal.core.DefaultWorkingCopyOwner;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.jdt.internal.core.PackageFragmentRoot;

public class CompilationUnitImpl extends CompilationUnit {

    private final IBuffer buffer;

    public CompilationUnitImpl(String name, String contents) {
        //TODO: is the name relevant?
        super(JDTHacks.createPackageFragment(extractPackageName(contents)), name, DefaultWorkingCopyOwner.PRIMARY);
        this.buffer = new BufferImpl(contents);
        //Force some model updates
        bufferChanged(new BufferChangedEvent(this.buffer, 0, 0, ""));
        try {
            makeConsistent(new NullProgressMonitor());
        } catch (JavaModelException e) {
            e.printStackTrace();
        }
    }

    @Override
    public IResource resource(PackageFragmentRoot root) {
        return new FileImpl(this.buffer);
    }

    @Override
    public char[] getContents() {
        return this.buffer.getCharacters();
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

    private static String extractPackageName(String contents) {
        for (int i = 0; i < Math.min(100, contents.length()); i++) {
            char c = contents.charAt(i);
            if (c == '\n') {
                String firstLine = contents.substring(0, i);
                if (!firstLine.startsWith("package ")) {
                    return "";
                } else {
                    return firstLine.substring("package ".length(), i - 1 /* semi-colon */);
                }
            }
        }

        return "";
    }
}
