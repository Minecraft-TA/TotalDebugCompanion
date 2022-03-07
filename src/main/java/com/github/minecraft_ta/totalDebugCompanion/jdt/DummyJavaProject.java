package com.github.minecraft_ta.totalDebugCompanion.jdt;

import org.eclipse.core.internal.resources.Workspace;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jdt.core.*;
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
    public IClasspathEntry getClasspathEntryFor(IPath path) throws JavaModelException {
        return new IClasspathEntry() {
            @Override
            public boolean combineAccessRules() {
                return false;
            }

            @Override
            public IAccessRule[] getAccessRules() {
                return new IAccessRule[0];
            }

            @Override
            public int getContentKind() {
                return 0;
            }

            @Override
            public int getEntryKind() {
                return 0;
            }

            @Override
            public IPath[] getExclusionPatterns() {
                return new IPath[0];
            }

            @Override
            public IClasspathAttribute[] getExtraAttributes() {
                return new IClasspathAttribute[0];
            }

            @Override
            public IPath[] getInclusionPatterns() {
                return new IPath[0];
            }

            @Override
            public IPath getOutputLocation() {
                return null;
            }

            @Override
            public IPath getPath() {
                return null;
            }

            @Override
            public IPath getSourceAttachmentPath() {
                return null;
            }

            @Override
            public IPath getSourceAttachmentRootPath() {
                return null;
            }

            @Override
            public IClasspathEntry getReferencingEntry() {
                return null;
            }

            @Override
            public boolean isExported() {
                return false;
            }

            @Override
            public IClasspathEntry getResolvedEntry() {
                return null;
            }
        };
    }

    @Override
    public IEclipsePreferences getEclipsePreferences() {
        return null;
    }
}
