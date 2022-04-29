package com.github.minecraft_ta.totalDebugCompanion.jdt.impls;

import com.github.minecraft_ta.totalDebugCompanion.jdt.JDTHacks;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.core.JarPackageFragmentRoot;
import org.eclipse.jdt.internal.core.OpenableElementInfo;
import org.eclipse.jdt.internal.core.util.HashtableOfArrayToObject;

import java.util.ArrayList;

public class DummyJarPackageFragmentRoot extends JarPackageFragmentRoot {

    public DummyJarPackageFragmentRoot() {
        super(new RootResourceImpl(), new Path("dummy-jar-path"), JDTHacks.DUMMY_JAVA_PROJECT, null);
    }

    @Override
    protected Object createElementInfo() {
        var info = super.createElementInfo();
        var table = new HashtableOfArrayToObject();
        // Empty package
        table.put(CharOperation.NO_STRINGS, new ArrayList[]{EMPTY_LIST, EMPTY_LIST});
        JDTHacks.setField(info, "rawPackageInfo", table);
        return info;
    }

    @Override
    protected boolean computeChildren(OpenableElementInfo info, IResource underlyingResource) throws JavaModelException {
        return true;
    }
}
