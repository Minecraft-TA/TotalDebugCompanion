package com.github.minecraft_ta.totalDebugCompanion.jdt;

import org.eclipse.core.internal.resources.Workspace;
import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.internal.codeassist.impl.AssistSourceType;
import org.eclipse.jdt.internal.core.IJavaElementRequestor;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.jdt.internal.core.NameLookup;
import org.eclipse.jdt.internal.core.PackageFragment;

import java.lang.reflect.Modifier;
import java.util.Arrays;

class SimpleNameLookup extends NameLookup {

    private final DummyJavaProject dummyJavaProject;

    public SimpleNameLookup(DummyJavaProject dummyJavaProject) {
        super(null, null, null, null, null);
        this.dummyJavaProject = dummyJavaProject;
    }

    @Override
    public boolean isPackage(String[] pkgName) {
        System.out.println("isPackage -> pkgName = " + Arrays.deepToString(pkgName));
        return true;
    }

    @Override
    public Answer findType(String typeName, String packageName, boolean partialMatch, int acceptFlags, boolean checkRestrictions, IPackageFragmentRoot[] moduleContext) {
        System.out.println("findType -> " + "typeName = " + typeName + ", packageName = " + packageName + ", partialMatch = " + partialMatch + ", acceptFlags = " + acceptFlags + ", checkRestrictions = " + checkRestrictions + ", moduleContext = " + Arrays.deepToString(moduleContext));
        return null;
    }

    @Override
    public void seekTypes(String name, IPackageFragment pkg, boolean partialMatch, int acceptFlags, IJavaElementRequestor requestor, boolean considerSecondaryTypes) {
        System.out.println("seekTypes -> name = " + name + ", pkg = " + pkg + ", partialMatch = " + partialMatch + ", acceptFlags = " + acceptFlags + ", requestor = " + requestor + ", considerSecondaryTypes = " + considerSecondaryTypes);
        //super.seekTypes(name, pkg, partialMatch, acceptFlags, requestor, considerSecondaryTypes);
        if ("Hallo".startsWith(name))
            requestor.acceptType(new AssistSourceType(null, "Hallo", null, null) {
                @Override
                public JavaProject getJavaProject() {
                    return dummyJavaProject;
                }

                @Override
                public int getFlags() {
                    return Modifier.PUBLIC;
                }

                @Override
                public IPackageFragment getPackageFragment() {
                    return (PackageFragment) dummyJavaProject.getPackageFragmentRoot(new Workspace().newResource(new org.eclipse.core.runtime.Path("dummy/Package"), IResource.FILE)).getPackageFragment("");
                }
            });
    }

    @Override
    public void seekPackageFragments(String name, boolean partialMatch, IJavaElementRequestor requestor) {
        System.out.println("seekPackageFragments -> name = " + name + ", partialMatch = " + partialMatch + ", requestor = " + requestor);
        //super.seekPackageFragments(name, partialMatch, requestor);
    }
}
