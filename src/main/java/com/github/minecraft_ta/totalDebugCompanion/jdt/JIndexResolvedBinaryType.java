package com.github.minecraft_ta.totalDebugCompanion.jdt;

import com.github.tth05.jindex.IndexedClass;
import org.eclipse.jdt.internal.core.CompilationUnit;
import org.eclipse.jdt.internal.core.ResolvedBinaryType;

public class JIndexResolvedBinaryType extends ResolvedBinaryType {

    private final JIndexBinaryType binaryType;

    public JIndexResolvedBinaryType(IndexedClass indexedClass) {
        super(new CompilationUnit(JDTHacks.createPackageFragment(indexedClass.getPackage()), indexedClass.getName(), null), indexedClass.getName(), "some unique key");
        this.binaryType = new JIndexBinaryType(indexedClass);
    }

    @Override
    public Object getElementInfo() {
        return this.binaryType;
    }
}
