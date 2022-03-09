package com.github.minecraft_ta.totalDebugCompanion.jdt.impls;

import com.github.minecraft_ta.totalDebugCompanion.jdt.JDTHacks;
import com.github.minecraft_ta.totalDebugCompanion.jdt.JIndexResolvedBinaryType;
import com.github.minecraft_ta.totalDebugCompanion.ui.views.SearchEverywherePopup;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.internal.codeassist.impl.AssistSourceType;
import org.eclipse.jdt.internal.core.IJavaElementRequestor;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.jdt.internal.core.NameLookup;
import org.eclipse.jdt.internal.core.util.Util;

import java.lang.reflect.Modifier;
import java.util.Arrays;

public class NameLookupImpl extends NameLookup {

    public NameLookupImpl() {
        super(null, null, null, null, null);
    }

    @Override
    public boolean isPackage(String[] pkgName) {
        var packageName = pkgName.length == 1 ? "" : Util.concatWith(Arrays.copyOf(pkgName, pkgName.length - 1), '/');
        var className = pkgName[pkgName.length - 1];

        //If it's not a class, it's a package. For now.
        return SearchEverywherePopup.CLASS_INDEX.findClass(packageName, className) == null;
    }

    @Override
    public Answer findType(String typeName, String packageName, boolean partialMatch, int acceptFlags, boolean checkRestrictions, IPackageFragmentRoot[] moduleContext) {
//        System.out.println("findType -> " + "typeName = " + typeName + ", packageName = " + packageName + ", partialMatch = " + partialMatch + ", acceptFlags = " + acceptFlags + ", checkRestrictions = " + checkRestrictions + ", moduleContext = " + Arrays.deepToString(moduleContext));

        if (SearchEverywherePopup.CLASS_INDEX.findClass(packageName, typeName) != null) {
            return JDTHacks.createNameLookupAnswer(new JIndexResolvedBinaryType(packageName, typeName), null, null);
        }
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
                    return JDTHacks.DUMMY_JAVA_PROJECT;
                }

                @Override
                public int getFlags() {
                    return Modifier.PUBLIC;
                }

                @Override
                public IPackageFragment getPackageFragment() {
                    return JDTHacks.createPackageFragment("");
                }
            });
    }

    @Override
    public void seekPackageFragments(String name, boolean partialMatch, IJavaElementRequestor requestor) {
        System.out.println("seekPackageFragments -> name = " + name + ", partialMatch = " + partialMatch + ", requestor = " + requestor);
        //super.seekPackageFragments(name, partialMatch, requestor);
    }
}
