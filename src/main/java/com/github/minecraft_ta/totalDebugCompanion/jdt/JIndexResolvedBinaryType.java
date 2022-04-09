package com.github.minecraft_ta.totalDebugCompanion.jdt;

import com.github.tth05.jindex.IndexedClass;
import org.eclipse.jdt.core.ITypeParameter;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.core.*;

public class JIndexResolvedBinaryType extends ResolvedBinaryType {

    private final JIndexBinaryType binaryType;

    public JIndexResolvedBinaryType(IndexedClass indexedClass) {
        super(new CompilationUnit(JDTHacks.createPackageFragment(indexedClass.getPackage().getNameWithParentsDot()), indexedClass.getName(), null), indexedClass.getName(), "some unique key");
        this.binaryType = new JIndexBinaryType(indexedClass);
    }

    @Override
    public ITypeParameter[] getTypeParameters() {
        var signature = this.binaryType.getGenericSignature();
        if (signature == null)
            return new ITypeParameter[0];

        char[][] typeParameterSignatures = Signature.getTypeParameters(signature);
        var typeParameters = new ITypeParameter[typeParameterSignatures.length];

        for (int i = 0; i < typeParameterSignatures.length; i++) {
            char[] typeParameterSignature = typeParameterSignatures[i];
            CharOperation.replace(typeParameterSignature, '/', '.');
            char[][] typeParameterBoundSignatures = Signature.getTypeParameterBounds(typeParameterSignature);

            int boundLength = typeParameterBoundSignatures.length;
            char[][] typeParameterBounds = new char[boundLength][];
            for (int j = 0; j < boundLength; j++) {
                typeParameterBounds[j] = Signature.toCharArray(typeParameterBoundSignatures[j]);
            }

            TypeParameterElementInfo info = new TypeParameterElementInfo();
            info.bounds = typeParameterBounds;
            info.boundsSignatures = typeParameterBoundSignatures;
            TypeParameter typeParameter = new TypeParameter(this, new String(Signature.getTypeVariable(typeParameterSignature))) {
                @Override
                public Object getElementInfo() {
                    return info;
                }
            };
            typeParameters[i] = typeParameter;
        }

        return typeParameters;
    }

    @Override
    public JavaElement getParent() {
        return super.getParent();
    }

    @Override
    public Object getElementInfo() {
        return this.binaryType;
    }

   /* @Override
    public IType getDeclaringType() {
        //TODO: Declaring type
        return null;
    }*/
}
