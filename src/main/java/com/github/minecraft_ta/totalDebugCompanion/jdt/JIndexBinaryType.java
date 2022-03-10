package com.github.minecraft_ta.totalDebugCompanion.jdt;

import com.github.minecraft_ta.totalDebugCompanion.jdt.stubs.IBinaryTypeStub;
import com.github.minecraft_ta.totalDebugCompanion.ui.views.SearchEverywherePopup;
import com.github.tth05.jindex.IndexedClass;
import org.eclipse.jdt.internal.compiler.env.IBinaryAnnotation;
import org.eclipse.jdt.internal.compiler.env.IBinaryNestedType;
import org.eclipse.jdt.internal.compiler.env.ITypeAnnotationWalker;
import org.eclipse.jdt.internal.compiler.lookup.BinaryTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.LookupEnvironment;

import java.lang.reflect.Modifier;
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
        //TODO: Return super class name
        return null;
    }

    @Override
    public char[][] getInterfaceNames() {
        //TODO: Return interface names
        return null;
    }

    @Override
    public char[] getSourceName() {
        var name = this.indexedClass.getName();
        var dollarIndex = name.indexOf('$');

        return name.substring(dollarIndex == -1 ? 0 : dollarIndex + 1).toCharArray();
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
        return Modifier.PUBLIC;
    }

    @Override
    public boolean isBinaryType() {
        return true;
    }
}
