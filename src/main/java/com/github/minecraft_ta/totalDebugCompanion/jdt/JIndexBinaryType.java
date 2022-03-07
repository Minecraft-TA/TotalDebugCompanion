package com.github.minecraft_ta.totalDebugCompanion.jdt;

import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.CompilationUnit;
import org.eclipse.jdt.internal.core.JavaElement;
import org.eclipse.jdt.internal.core.ResolvedBinaryType;

public class JIndexBinaryType extends ResolvedBinaryType {

    private final String packageName;

    public JIndexBinaryType(String packageName, String name) {
        super(new CompilationUnit(JDTHacks.createPackageFragment(packageName), name, null), name, "some unique key");
        this.packageName = packageName;
    }

    @Override
    public Object getElementInfo() throws JavaModelException {
        return new DummyIBinaryType(this.packageName, this.name);
    }
}
