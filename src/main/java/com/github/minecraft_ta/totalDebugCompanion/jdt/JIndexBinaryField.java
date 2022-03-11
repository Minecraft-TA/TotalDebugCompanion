package com.github.minecraft_ta.totalDebugCompanion.jdt;

import com.github.tth05.jindex.IndexedField;
import org.eclipse.jdt.internal.compiler.env.IBinaryAnnotation;
import org.eclipse.jdt.internal.compiler.env.IBinaryField;
import org.eclipse.jdt.internal.compiler.env.IBinaryTypeAnnotation;
import org.eclipse.jdt.internal.compiler.impl.Constant;

public class JIndexBinaryField implements IBinaryField {

    private final IndexedField indexedField;

    public JIndexBinaryField(IndexedField indexedField) {
        this.indexedField = indexedField;
    }

    @Override
    public IBinaryAnnotation[] getAnnotations() {
        return null;
    }

    @Override
    public IBinaryTypeAnnotation[] getTypeAnnotations() {
        return null;
    }

    @Override
    public Constant getConstant() {
        return Constant.NotAConstant;
    }

    @Override
    public char[] getGenericSignature() {
        throw new UnsupportedOperationException();
    }

    @Override
    public char[] getName() {
        return this.indexedField.getName().toCharArray();
    }

    @Override
    public long getTagBits() {
        return 0;
    }

    @Override
    public char[] getTypeName() {
        return this.indexedField.getTypeSignature().toSignatureString().toCharArray();
    }

    @Override
    public int getModifiers() {
        return this.indexedField.getAccessFlags();
    }
}
