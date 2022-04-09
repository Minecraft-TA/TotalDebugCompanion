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

import java.util.HashMap;

public class NameLookupImpl extends NameLookup {

    public NameLookupImpl() {
        super(null, null, null, null, new HashMap<>());
    }

    @Override
    public boolean isPackage(String[] pkgName) {
        return SearchEverywherePopup.CLASS_INDEX.findPackage(Util.concatWith(pkgName, '/')) != null;
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
        IndexedClass[] classes;
        if (name == null || name.isBlank()) {
            var packageName = pkg.getElementName();
            if (packageName.isBlank())
                return;

            var indexedPackage = SearchEverywherePopup.CLASS_INDEX.findPackage(packageName);
            if (indexedPackage == null)
                return;

            classes = indexedPackage.getClasses();
        } else {
            classes = SearchEverywherePopup.CLASS_INDEX.findClasses(name, 300);
        }

        for (IndexedClass foundClass : classes) {
            if (foundClass.getName().lastIndexOf('$') != -1)
                continue;

            requestor.acceptType(new JIndexResolvedBinaryType(foundClass));
        }
    }

    @Override
    public void seekPackageFragments(String name, boolean partialMatch, IJavaElementRequestor requestor) {
        System.out.println("seekPackageFragments -> name = " + name + ", partialMatch = " + partialMatch + ", requestor = " + requestor);
        //super.seekPackageFragments(name, partialMatch, requestor);
        requestor.acceptPackageFragment(JDTHacks.createPackageFragment("java.lang"));
    }
}
