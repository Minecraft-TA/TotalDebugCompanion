package com.github.minecraft_ta.totalDebugCompanion.jdt;

import org.eclipse.jdt.internal.compiler.env.*;
import org.eclipse.jdt.internal.compiler.lookup.BinaryTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.LookupEnvironment;
import org.eclipse.jdt.internal.core.util.Util;

import java.lang.reflect.Modifier;

public class DummyIBinaryType implements IBinaryType {

    private String packageName;
    private String name;

    public DummyIBinaryType(String packageName, String name) {
        this.packageName = packageName;
        this.name = name;
    }

    @Override
    public IBinaryField[] getFields() {
        return null;
    }

    @Override
    public IBinaryMethod[] getMethods() {
        return null;
    }

    @Override
    public char[] getSuperclassName() {
        return null;
    }

    @Override
    public IBinaryAnnotation[] getAnnotations() {
        return ITypeAnnotationWalker.NO_ANNOTATIONS;
    }

    @Override
    public IBinaryTypeAnnotation[] getTypeAnnotations() {
        return null;
    }

    @Override
    public char[] getEnclosingMethod() {
        return null;
    }

    @Override
    public char[] getEnclosingTypeName() {
        return null;
    }

    @Override
    public IRecordComponent[] getRecordComponents() {
        return null;
    }

    @Override
    public char[] getModule() {
        return null;
    }

    @Override
    public char[] getGenericSignature() {
        return null;
    }

    @Override
    public char[][] getInterfaceNames() {
        return null;
    }

    @Override
    public IBinaryNestedType[] getMemberTypes() {
        return null;
    }

    @Override
    public char[][][] getMissingTypeNames() {
        return null;
    }

    @Override
    public char[] getName() {
        return (this.packageName + "/" + this.name).replace('.', '/').toCharArray();
    }

    @Override
    public char[] getSourceName() {
        return this.name.toCharArray();
    }

    @Override
    public long getTagBits() {
        return 0;
    }

    @Override
    public boolean isAnonymous() {
        return false;
    }

    @Override
    public boolean isLocal() {
        return false;
    }

    @Override
    public boolean isRecord() {
        return false;
    }

    @Override
    public boolean isMember() {
        return false;
    }

    @Override
    public char[] sourceFileName() {
        return null;
    }

    @Override
    public ITypeAnnotationWalker enrichWithExternalAnnotationsFor(ITypeAnnotationWalker walker, Object member, LookupEnvironment environment) {
        return ITypeAnnotationWalker.EMPTY_ANNOTATION_WALKER;
    }

    @Override
    public BinaryTypeBinding.ExternalAnnotationStatus getExternalAnnotationStatus() {
        return BinaryTypeBinding.ExternalAnnotationStatus.NO_EEA_FILE;
    }

    @Override
    public int getModifiers() {
        return Modifier.PUBLIC;
    }

    @Override
    public boolean isBinaryType() {
        return true;
    }

    @Override
    public char[] getFileName() {
        return Util.concat(getName(), ".class".toCharArray());
    }
}
