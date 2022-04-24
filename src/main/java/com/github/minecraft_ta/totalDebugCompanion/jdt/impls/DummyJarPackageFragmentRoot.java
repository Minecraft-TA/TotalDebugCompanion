package com.github.minecraft_ta.totalDebugCompanion.jdt.impls;

import com.github.minecraft_ta.totalDebugCompanion.jdt.JDTHacks;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.JarPackageFragmentRoot;
import org.eclipse.jdt.internal.core.OpenableElementInfo;

public class DummyJarPackageFragmentRoot extends JarPackageFragmentRoot {

    public DummyJarPackageFragmentRoot() {
        super(new RootResourceImpl(), new Path("dummy-jar-path"), JDTHacks.DUMMY_JAVA_PROJECT, null);
    }

    @Override
    protected boolean computeChildren(OpenableElementInfo info, IResource underlyingResource) throws JavaModelException {
        return true;
    }
}
