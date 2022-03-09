package com.github.minecraft_ta.totalDebugCompanion.jdt;

import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.CompilationUnit;
import org.eclipse.jdt.internal.core.ResolvedBinaryType;

public class JIndexResolvedBinaryType extends ResolvedBinaryType {

    private final String packageName;

    public JIndexResolvedBinaryType(String packageName, String name) {
        super(new CompilationUnit(JDTHacks.createPackageFragment(packageName), name, null), name, "some unique key");
        this.packageName = packageName;
    }

    @Override
    public Object getElementInfo() throws JavaModelException {
        return new JIndexBinaryType(this.packageName, this.name);
    }
}
