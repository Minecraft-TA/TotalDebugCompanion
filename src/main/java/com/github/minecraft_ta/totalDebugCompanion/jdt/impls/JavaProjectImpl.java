package com.github.minecraft_ta.totalDebugCompanion.jdt.impls;

import com.github.minecraft_ta.totalDebugCompanion.jdt.stubs.IClassPathEntryStub;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.jdt.internal.core.NameLookup;
import org.eclipse.jdt.internal.core.SearchableEnvironment;

import java.util.Map;

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
    public IModuleDescription getModuleDescription() throws JavaModelException {
        return null;
    }

    @Override
    public Map<String, String> getOptions(boolean inheritJavaCoreOptions) {
        //TODO: Load these from a file
        var options = JavaCore.getOptions();
        options.put("org.eclipse.jdt.core.compiler.compliance", "1.8");
        options.put("org.eclipse.jdt.core.compiler.source", "1.8");
        options.put("org.eclipse.jdt.core.compiler.codegen.targetPlatform", "1.8");
        return options;
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
