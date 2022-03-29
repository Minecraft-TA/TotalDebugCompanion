package com.github.minecraft_ta.totalDebugCompanion.jdt;

import com.github.minecraft_ta.totalDebugCompanion.jdt.stubs.IBinaryTypeStub;
import com.github.minecraft_ta.totalDebugCompanion.ui.views.SearchEverywherePopup;
import com.github.tth05.jindex.IndexedClass;
import org.eclipse.jdt.internal.compiler.env.*;
import org.eclipse.jdt.internal.compiler.lookup.BinaryTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.LookupEnvironment;

import java.util.Arrays;

public class JIndexBinaryType implements IBinaryTypeStub {

    private final IndexedClass indexedClass;

    public JIndexBinaryType(IndexedClass indexedClass) {
        this.indexedClass = indexedClass;
    }

    @Override
    public IBinaryAnnotation[] getAnnotations() {
        return ITypeAnnotationWalker.NO_ANNOTATIONS;
    }

    @Override
    public char[] getName() {
        return this.indexedClass.getNameWithPackage().toCharArray();
    }

    @Override
    public char[] getEnclosingTypeName() {
        //TODO: Return enclosing type name
        return null;
    }

    @Override
    public char[] getSuperclassName() {
        var superClass = this.indexedClass.getSuperClass();
        if (superClass == null) {
            return null;
        }

        return superClass.getNameWithPackage().toCharArray();
    }

    @Override
    public char[][] getInterfaceNames() {
        var interfaces = this.indexedClass.getInterfaces();
        if (interfaces == null)
            return null;

        var array = new char[interfaces.length][];
        for (int i = 0; i < interfaces.length; i++) {
            array[i] = interfaces[i].getNameWithPackage().toCharArray();
        }

        return array;
    }

    @Override
    public char[] getSourceName() {
        var name = this.indexedClass.getName();
        var dollarIndex = name.indexOf('$');

        return name.substring(dollarIndex == -1 ? 0 : dollarIndex + 1).toCharArray();
    }

    @Override
    public char[] getGenericSignature() {
        var str = this.indexedClass.getGenericSignatureString();
        if (str == null)
            return null;
        return str.toCharArray();
    }

    @Override
    public IBinaryField[] getFields() {
        var fields = this.indexedClass.getFields();
        var array = new JIndexBinaryField[fields.length];
        for (int i = 0; i < fields.length; i++) {
            array[i] = new JIndexBinaryField(fields[i]);
        }
        return array;
    }

    @Override
    public IBinaryMethod[] getMethods() {
        var methods = this.indexedClass.getMethods();
        var array = new JIndexBinaryMethod[methods.length];
        for (int i = 0; i < methods.length; i++) {
            array[i] = new JIndexBinaryMethod(methods[i]);
        }
        return array;
    }

    @Override
    public IBinaryNestedType[] getMemberTypes() {
        //TODO: Maybe implement this in JIndex instead?
        return Arrays.stream(SearchEverywherePopup.CLASS_INDEX.findClasses(this.indexedClass.getName(), 500))
                .filter(c -> c.getName().contains("$"))
                .filter(c -> c.getName().substring(0, c.getName().indexOf('$')).equals(this.indexedClass.getName()))
                .map(c -> new IBinaryNestedType() {
                    @Override
                    public char[] getEnclosingTypeName() {
                        return JIndexBinaryType.this.getName();
                    }

                    @Override
                    public int getModifiers() {
                        return c.getAccessFlags();
                    }

                    @Override
                    public char[] getName() {
                        return c.getNameWithPackage().toCharArray();
                    }
                }).toArray(IBinaryNestedType[]::new);
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
        return this.indexedClass.getAccessFlags();
    }

    @Override
    public boolean isBinaryType() {
        return true;
    }
}
