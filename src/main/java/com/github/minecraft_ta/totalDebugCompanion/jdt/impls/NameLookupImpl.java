package com.github.minecraft_ta.totalDebugCompanion.jdt.impls;

import com.github.minecraft_ta.totalDebugCompanion.jdt.BaseScript;
import com.github.minecraft_ta.totalDebugCompanion.jdt.JDTHacks;
import com.github.minecraft_ta.totalDebugCompanion.jdt.JIndexResolvedBinaryType;
import com.github.minecraft_ta.totalDebugCompanion.ui.views.SearchEverywherePopup;
import com.github.tth05.jindex.IndexedClass;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.internal.core.IJavaElementRequestor;
import org.eclipse.jdt.internal.core.NameLookup;
import org.eclipse.jdt.internal.core.util.Util;

import java.util.Arrays;
import java.util.HashMap;

public class NameLookupImpl extends NameLookup {

    public NameLookupImpl() {
        super(null, null, null, null, new HashMap<>());
    }

    @Override
    public boolean isPackage(String[] pkgName) {
        //Hack, because JIndex has no actual isPackage function, causing unresolved types to be seen as packages
        // sometimes
        if (Character.isUpperCase(pkgName[0].charAt(0)))
            return false;

        var packageName = pkgName.length == 1 ? "" : Util.concatWith(Arrays.copyOf(pkgName, pkgName.length - 1), '/');
        var className = pkgName[pkgName.length - 1];

        //If it's not a class, it's a package. For now.
        return SearchEverywherePopup.CLASS_INDEX.findClass(packageName, className) == null;
    }

    @Override
    public Answer findType(String typeName, String packageName, boolean partialMatch, int acceptFlags, boolean considerSecondaryTypes, boolean waitForIndexes, boolean checkRestrictions, IProgressMonitor monitor, IPackageFragmentRoot[] moduleContext) {
        //Special case for BaseScript resolution
        if (packageName.equals("") && typeName.equals("BaseScript"))
            return JDTHacks.createNameLookupAnswer(new CompilationUnitImpl("BaseScript", BaseScript.getText()).getType("BaseScript"), null, null);

        var foundClass = SearchEverywherePopup.CLASS_INDEX.findClass(packageName, typeName);
        if (foundClass != null) {
            return JDTHacks.createNameLookupAnswer(new JIndexResolvedBinaryType(foundClass), null, null);
        }
        return null;
    }

    @Override
    public void seekTypes(String name, IPackageFragment pkg, boolean partialMatch, int acceptFlags, IJavaElementRequestor requestor, boolean considerSecondaryTypes) {
        System.out.println("seekTypes -> name = " + name + ", pkg = " + pkg + ", partialMatch = " + partialMatch + ", acceptFlags = " + acceptFlags + ", requestor = " + requestor + ", considerSecondaryTypes = " + considerSecondaryTypes);
        //super.seekTypes(name, pkg, partialMatch, acceptFlags, requestor, considerSecondaryTypes);
        for (IndexedClass foundClass : SearchEverywherePopup.CLASS_INDEX.findClasses(name, 300)) {
            if (foundClass.getName().lastIndexOf('$') != -1)
                continue;

            requestor.acceptType(new JIndexResolvedBinaryType(foundClass));
        }
    }

    @Override
    public void seekPackageFragments(String name, boolean partialMatch, IJavaElementRequestor requestor) {
        System.out.println("seekPackageFragments -> name = " + name + ", partialMatch = " + partialMatch + ", requestor = " + requestor);
        //super.seekPackageFragments(name, partialMatch, requestor);
    }
}
