package com.github.minecraft_ta.totalDebugCompanion.jdt;

import com.github.minecraft_ta.totalDebugCompanion.jdt.stubs.IBinaryMethodStub;
import com.github.tth05.jindex.IndexedMethod;
import com.github.tth05.jindex.IndexedSignature;
import org.eclipse.jdt.internal.compiler.classfmt.JavaBinaryNames;

import java.util.Arrays;
import java.util.stream.Collectors;

public class JIndexBinaryMethod implements IBinaryMethodStub {

    private final IndexedMethod indexedMethod;

    public JIndexBinaryMethod(IndexedMethod indexedMethod) {
        this.indexedMethod = indexedMethod;
    }

    @Override
    public char[] getSelector() {
        return this.indexedMethod.getName().toCharArray();
    }

    @Override
    public char[] getGenericSignature() {
        return null;
    }

    @Override
    public int getModifiers() {
        return this.indexedMethod.getAccessFlags();
    }

    @Override
    public char[] getMethodDescriptor() {
        return (
                "(" + Arrays.stream(this.indexedMethod.getParameterTypeSignatures())
                        .map(IndexedSignature::toSignatureString)
                        .collect(Collectors.joining("")) +
                ")" + this.indexedMethod.getReturnTypeSignature().toSignatureString()
        ).toCharArray();
    }

    @Override
    public boolean isClinit() {
        return JavaBinaryNames.isClinit(getSelector());
    }

    @Override
    public boolean isConstructor() {
        return JavaBinaryNames.isConstructor(getSelector());
    }
}
