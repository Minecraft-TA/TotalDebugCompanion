package com.github.minecraft_ta.totalDebugCompanion.jdt;

import com.github.minecraft_ta.totalDebugCompanion.jdt.impls.ClassFileImpl;
import com.github.minecraft_ta.totalDebugCompanion.ui.views.SearchEverywherePopup;
import com.github.tth05.jindex.IndexedClass;
import org.eclipse.jdt.core.IOrdinaryClassFile;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeParameter;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.core.JavaElement;
import org.eclipse.jdt.internal.core.ResolvedBinaryType;
import org.eclipse.jdt.internal.core.TypeParameter;
import org.eclipse.jdt.internal.core.TypeParameterElementInfo;

public class JIndexResolvedBinaryType extends ResolvedBinaryType {

    private final JIndexBinaryType binaryType;

    public JIndexResolvedBinaryType(IndexedClass indexedClass) {
        super(null, indexedClass.getSourceName(), indexedClass.getNameWithPackage());
        this.binaryType = new JIndexBinaryType(indexedClass);
        setParent(new ClassFileImpl(this, indexedClass));
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

    @Override
    public boolean isBinary() {
        return true;
    }

    @Override
    public IType getDeclaringType() {
        var enclosingTypeName = this.binaryType.getEnclosingTypeName();
        if (enclosingTypeName == null)
            return null;
        var typeStr = new String(enclosingTypeName);
        var packageName = typeStr.substring(0, typeStr.lastIndexOf('/'));
        var typeName = typeStr.substring(packageName.length() + 1);
        return new JIndexResolvedBinaryType(SearchEverywherePopup.CLASS_INDEX.findClass(packageName, typeName));
    }

    @Override
    public IOrdinaryClassFile getClassFile() {
        return (IOrdinaryClassFile) getParent();
    }
}
