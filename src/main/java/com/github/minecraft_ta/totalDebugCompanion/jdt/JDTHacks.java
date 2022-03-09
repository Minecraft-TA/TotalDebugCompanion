package com.github.minecraft_ta.totalDebugCompanion.jdt;

import com.github.minecraft_ta.totalDebugCompanion.jdt.impls.BundleContextImpl;
import com.github.minecraft_ta.totalDebugCompanion.jdt.impls.ContentTypeManagerImpl;
import com.github.minecraft_ta.totalDebugCompanion.jdt.impls.JavaProjectImpl;
import org.eclipse.core.internal.resources.Folder;
import org.eclipse.core.internal.resources.Workspace;
import org.eclipse.core.internal.runtime.DataArea;
import org.eclipse.core.internal.runtime.InternalPlatform;
import org.eclipse.core.internal.runtime.MetaDataKeeper;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.content.IContentTypeManager;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.codeassist.CompletionEngine;
import org.eclipse.jdt.internal.compiler.env.AccessRestriction;
import org.eclipse.jdt.internal.core.*;
import org.eclipse.jdt.internal.core.search.indexing.IndexManager;
import org.eclipse.jdt.internal.core.util.Util;
import org.osgi.util.tracker.ServiceTracker;
import sun.misc.Unsafe;

import java.net.URI;
import java.util.Map;

public class JDTHacks {

    public static final JavaProject DUMMY_JAVA_PROJECT;
    private static final PackageFragmentRoot PACKAGE_FRAGMENT_ROOT;
    private static final Unsafe UNSAFE;
    static {
        try {
            long t = System.nanoTime();
            System.out.println("Starting init hack");
            init();
            System.out.println("Init hack too: " + (System.nanoTime() - t) / 1_000_000.0);

            var theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
            theUnsafe.setAccessible(true);
            UNSAFE = (Unsafe) theUnsafe.get(null);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }

        DUMMY_JAVA_PROJECT = new JavaProjectImpl();
        PACKAGE_FRAGMENT_ROOT = createPackageFragmentRoot();
    }

    public static PackageFragment createPackageFragment(String name) {
        try {
            var instance = (PackageFragment) UNSAFE.allocateInstance(PackageFragment.class);
            instance.names = Util.getTrimmedSimpleNames(name);
            setField(instance, "isValidPackageName", true);
            setField(JavaElement.class, instance, "parent", PACKAGE_FRAGMENT_ROOT);
            return instance;
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static NameLookup.Answer createNameLookupAnswer(IType type, AccessRestriction res, IClasspathEntry entry) {
        return createInstance(NameLookup.Answer.class, new Class[]{IType.class, AccessRestriction.class, IClasspathEntry.class}, type, res, entry);
    }

    private static Folder createFolder(String path) {
        return createInstance(Folder.class, new Class<?>[]{IPath.class, Workspace.class}, new Path(path), null);
    }

    private static PackageFragmentRoot createPackageFragmentRoot() {
        return createInstance(PackageFragmentRoot.class, new Class<?>[]{IResource.class, JavaProject.class}, new IResource() {
            @Override
            public void accept(IResourceProxyVisitor visitor, int memberFlags) throws CoreException {

            }

            @Override
            public void accept(IResourceProxyVisitor visitor, int depth, int memberFlags) throws CoreException {

            }

            @Override
            public void accept(IResourceVisitor visitor) throws CoreException {

            }

            @Override
            public void accept(IResourceVisitor visitor, int depth, boolean includePhantoms) throws CoreException {

            }

            @Override
            public void accept(IResourceVisitor visitor, int depth, int memberFlags) throws CoreException {

            }

            @Override
            public void clearHistory(IProgressMonitor monitor) throws CoreException {

            }

            @Override
            public void copy(IPath destination, boolean force, IProgressMonitor monitor) throws CoreException {

            }

            @Override
            public void copy(IPath destination, int updateFlags, IProgressMonitor monitor) throws CoreException {

            }

            @Override
            public void copy(IProjectDescription description, boolean force, IProgressMonitor monitor) throws CoreException {

            }

            @Override
            public void copy(IProjectDescription description, int updateFlags, IProgressMonitor monitor) throws CoreException {

            }

            @Override
            public IMarker createMarker(String type) throws CoreException {
                return null;
            }

            @Override
            public IResourceProxy createProxy() {
                return null;
            }

            @Override
            public void delete(boolean force, IProgressMonitor monitor) throws CoreException {

            }

            @Override
            public void delete(int updateFlags, IProgressMonitor monitor) throws CoreException {

            }

            @Override
            public void deleteMarkers(String type, boolean includeSubtypes, int depth) throws CoreException {

            }

            @Override
            public boolean exists() {
                return false;
            }

            @Override
            public IMarker findMarker(long id) throws CoreException {
                return null;
            }

            @Override
            public IMarker[] findMarkers(String type, boolean includeSubtypes, int depth) throws CoreException {
                return new IMarker[0];
            }

            @Override
            public int findMaxProblemSeverity(String type, boolean includeSubtypes, int depth) throws CoreException {
                return 0;
            }

            @Override
            public String getFileExtension() {
                return null;
            }

            @Override
            public IPath getFullPath() {
                return new Path("dummy-folder");
            }

            @Override
            public long getLocalTimeStamp() {
                return 0;
            }

            @Override
            public IPath getLocation() {
                return null;
            }

            @Override
            public URI getLocationURI() {
                return null;
            }

            @Override
            public IMarker getMarker(long id) {
                return null;
            }

            @Override
            public long getModificationStamp() {
                return 0;
            }

            @Override
            public String getName() {
                return null;
            }

            @Override
            public IPathVariableManager getPathVariableManager() {
                return null;
            }

            @Override
            public IContainer getParent() {
                return null;
            }

            @Override
            public Map<QualifiedName, String> getPersistentProperties() throws CoreException {
                return null;
            }

            @Override
            public String getPersistentProperty(QualifiedName key) throws CoreException {
                return null;
            }

            @Override
            public IProject getProject() {
                return null;
            }

            @Override
            public IPath getProjectRelativePath() {
                return null;
            }

            @Override
            public IPath getRawLocation() {
                return null;
            }

            @Override
            public URI getRawLocationURI() {
                return null;
            }

            @Override
            public ResourceAttributes getResourceAttributes() {
                return null;
            }

            @Override
            public Map<QualifiedName, Object> getSessionProperties() throws CoreException {
                return null;
            }

            @Override
            public Object getSessionProperty(QualifiedName key) throws CoreException {
                return null;
            }

            @Override
            public int getType() {
                return 0;
            }

            @Override
            public IWorkspace getWorkspace() {
                return null;
            }

            @Override
            public boolean isAccessible() {
                return false;
            }

            @Override
            public boolean isDerived() {
                return false;
            }

            @Override
            public boolean isDerived(int options) {
                return false;
            }

            @Override
            public boolean isHidden() {
                return false;
            }

            @Override
            public boolean isHidden(int options) {
                return false;
            }

            @Override
            public boolean isLinked() {
                return false;
            }

            @Override
            public boolean isVirtual() {
                return false;
            }

            @Override
            public boolean isLinked(int options) {
                return false;
            }

            @Override
            public boolean isLocal(int depth) {
                return false;
            }

            @Override
            public boolean isPhantom() {
                return false;
            }

            @Override
            public boolean isReadOnly() {
                return false;
            }

            @Override
            public boolean isSynchronized(int depth) {
                return false;
            }

            @Override
            public boolean isTeamPrivateMember() {
                return false;
            }

            @Override
            public boolean isTeamPrivateMember(int options) {
                return false;
            }

            @Override
            public void move(IPath destination, boolean force, IProgressMonitor monitor) throws CoreException {

            }

            @Override
            public void move(IPath destination, int updateFlags, IProgressMonitor monitor) throws CoreException {

            }

            @Override
            public void move(IProjectDescription description, boolean force, boolean keepHistory, IProgressMonitor monitor) throws CoreException {

            }

            @Override
            public void move(IProjectDescription description, int updateFlags, IProgressMonitor monitor) throws CoreException {

            }

            @Override
            public void refreshLocal(int depth, IProgressMonitor monitor) throws CoreException {

            }

            @Override
            public void revertModificationStamp(long value) throws CoreException {

            }

            @Override
            public void setDerived(boolean isDerived) throws CoreException {

            }

            @Override
            public void setDerived(boolean isDerived, IProgressMonitor monitor) throws CoreException {

            }

            @Override
            public void setHidden(boolean isHidden) throws CoreException {

            }

            @Override
            public void setLocal(boolean flag, int depth, IProgressMonitor monitor) throws CoreException {

            }

            @Override
            public long setLocalTimeStamp(long value) throws CoreException {
                return 0;
            }

            @Override
            public void setPersistentProperty(QualifiedName key, String value) throws CoreException {

            }

            @Override
            public void setReadOnly(boolean readOnly) {

            }

            @Override
            public void setResourceAttributes(ResourceAttributes attributes) throws CoreException {

            }

            @Override
            public void setSessionProperty(QualifiedName key, Object value) throws CoreException {

            }

            @Override
            public void setTeamPrivateMember(boolean isTeamPrivate) throws CoreException {

            }

            @Override
            public void touch(IProgressMonitor monitor) throws CoreException {

            }

            @Override
            public <T> T getAdapter(Class<T> adapter) {
                return null;
            }

            @Override
            public boolean contains(ISchedulingRule rule) {
                return false;
            }

            @Override
            public boolean isConflicting(ISchedulingRule rule) {
                return false;
            }
        }, DUMMY_JAVA_PROJECT);
    }

    private static <T> T createInstance(Class<T> clazz, Class<?>[] argClasses, Object... args) {
        try {
            var ctor = clazz.getDeclaredConstructor(argClasses);
            ctor.setAccessible(true);
            return ctor.newInstance(args);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    private static void setField(Object o, String fieldName, Object value) {
        setField(o.getClass(), o, fieldName, value);
    }

    private static void setField(Class<?> clazz, Object o, String fieldName, Object value) {
        try {
            var field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(o, value);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    private static void setStaticField(Class<?> c, String fieldName, Object value) {
        try {
            var field = c.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(null, value);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    private static void init() throws Throwable {
        //Set global instance
        new ResourcesPlugin();
        new JavaCore();

        //bundleContext
        var field = InternalPlatform.class.getDeclaredField("context");
        field.setAccessible(true);
        field.set(InternalPlatform.getDefault(), new BundleContextImpl());

        //bundle
        field = ResourcesPlugin.class.getSuperclass().getDeclaredField("bundle");
        field.setAccessible(true);
        field.set(ResourcesPlugin.getPlugin(), InternalPlatform.getDefault().getBundleContext().getBundle());

        //bundle
        field = JavaCore.class.getSuperclass().getDeclaredField("bundle");
        field.setAccessible(true);
        field.set(JavaCore.getPlugin(), InternalPlatform.getDefault().getBundleContext().getBundle());

        //contentTracker
        field = InternalPlatform.class.getDeclaredField("contentTracker");
        field.setAccessible(true);
        var value = new ServiceTracker<IContentTypeManager, IContentTypeManager>(InternalPlatform.getDefault().getBundleContext(), ContentTypeManagerImpl.class.getName(), null);
        value.open(true);
        field.set(InternalPlatform.getDefault(), value);

        //cachedInstanceLocation
        field = InternalPlatform.class.getDeclaredField("cachedInstanceLocation");
        field.setAccessible(true);
        field.set(InternalPlatform.getDefault(), new org.eclipse.core.runtime.Path(""));

        //initialized
        field = InternalPlatform.class.getDeclaredField("initialized");
        field.setAccessible(true);
        field.set(InternalPlatform.getDefault(), true);

        //cache
        field = JavaModelManager.class.getDeclaredField("cache");
        field.setAccessible(true);
        field.set(JavaModelManager.getJavaModelManager(), new JavaModelCache());

        //dataArea
        field = MetaDataKeeper.class.getDeclaredField("metaArea");
        field.setAccessible(true);
        field.set(null, new DataArea() {
            @Override
            protected synchronized void assertLocationInitialized() throws IllegalStateException {
            }

            @Override
            public IPath getMetadataLocation() throws IllegalStateException {
                return new org.eclipse.core.runtime.Path("");
            }
        });

        //indexManager
        field = JavaModelManager.class.getDeclaredField("indexManager");
        field.setAccessible(true);
        field.set(JavaModelManager.getJavaModelManager(), new IndexManager() {

        });

        //workspace
        field = ResourcesPlugin.class.getDeclaredField("workspace");
        field.setAccessible(true);
        field.set(null, new Workspace());

        CompletionEngine.DEBUG = true;
    }
}
