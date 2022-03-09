package com.github.minecraft_ta.totalDebugCompanion.jdt.impls;

import com.github.minecraft_ta.totalDebugCompanion.jdt.stubs.IClassPathEntryStub;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.WorkingCopyOwner;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.jdt.internal.core.NameLookup;
import org.eclipse.jdt.internal.core.SearchableEnvironment;

public class JavaProjectImpl extends JavaProject {

    public JavaProjectImpl() {
        super(new ProjectImpl(), null);
    }

    @Override
    public NameLookup newNameLookup(ICompilationUnit[] workingCopies, boolean excludeTestCode) throws JavaModelException {
        return new NameLookupImpl();
    }

    @Override
    public SearchableEnvironment newSearchableNameEnvironment(WorkingCopyOwner owner, boolean excludeTestCode) throws JavaModelException {
        return new SearchableEnvironment(this, (WorkingCopyOwner) null, true);
    }

    @Override
    public IClasspathEntry getClasspathEntryFor(IPath path) throws JavaModelException {
        return new IClassPathEntryStub() {};
    }

    @Override
    public IEclipsePreferences getEclipsePreferences() {
        return null;
    }
}
