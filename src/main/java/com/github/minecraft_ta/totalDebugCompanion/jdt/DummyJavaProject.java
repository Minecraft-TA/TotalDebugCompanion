package com.github.minecraft_ta.totalDebugCompanion.jdt;

import org.eclipse.core.internal.resources.Workspace;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.WorkingCopyOwner;
import org.eclipse.jdt.internal.core.*;

class DummyJavaProject extends JavaProject {

    public DummyJavaProject() {
        super(new DummyIProject(), null);
    }

    @Override
    public NameLookup newNameLookup(ICompilationUnit[] workingCopies, boolean excludeTestCode) throws JavaModelException {
        return new SimpleNameLookup();
    }

    @Override
    public SearchableEnvironment newSearchableNameEnvironment(WorkingCopyOwner owner, boolean excludeTestCode) throws JavaModelException {
        return new SearchableEnvironment(this, new ICompilationUnit[]{new CompilationUnit((PackageFragment) DummyJavaProject.this.getPackageFragmentRoot(new Workspace().newResource(new org.eclipse.core.runtime.Path("dummy/Package"), IResource.FILE)).getPackageFragment("Package"),
                "HalloDude", DefaultWorkingCopyOwner.PRIMARY) {
            @Override
            public IResource resource(PackageFragmentRoot root) {
                return new DummyFile(new StringBufferImpl("public class HalloDude {public static void test() {}}"));
            }

            @Override
            protected IStatus validateCompilationUnit(IResource resource) {
                return Status.OK_STATUS;
            }

            @Override
            public boolean isWorkingCopy() {
                return false;
            }

            @Override
            public JavaElement getParent() {
                return super.getParent();
            }
        }}, true);
    }

    @Override
    public IEclipsePreferences getEclipsePreferences() {
        return null;
    }
}
