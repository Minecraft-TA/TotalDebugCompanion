package com.github.minecraft_ta.totalDebugCompanion.jdt;

import org.eclipse.core.internal.resources.Folder;
import org.eclipse.core.internal.resources.Workspace;
import org.eclipse.core.internal.runtime.DataArea;
import org.eclipse.core.internal.runtime.InternalPlatform;
import org.eclipse.core.internal.runtime.MetaDataKeeper;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.content.IContentTypeManager;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.codeassist.CompletionEngine;
import org.eclipse.jdt.internal.core.*;
import org.eclipse.jdt.internal.core.search.indexing.IndexManager;
import org.eclipse.jdt.internal.core.util.Util;
import org.osgi.util.tracker.ServiceTracker;
import sun.misc.Unsafe;

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

        DUMMY_JAVA_PROJECT = new DummyJavaProject();
        PACKAGE_FRAGMENT_ROOT = createPackageFragmentRoot();
    }

    public static IPackageFragment createPackageFragment(String name) {
        try {
            var instance = (PackageFragment) UNSAFE.allocateInstance(PackageFragment.class);
            instance.names = Util.getTrimmedSimpleNames(name);
            return instance;
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    private static Folder createFolder(String path) {
        try {
            var ctor = Folder.class.getDeclaredConstructor(IPath.class, Workspace.class);
            ctor.setAccessible(true);
            return ctor.newInstance(new Path(path), null);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    private static PackageFragmentRoot createPackageFragmentRoot() {
        try {
            var ctor = PackageFragmentRoot.class.getDeclaredConstructor(IResource.class, JavaProject.class);
            ctor.setAccessible(true);
            return ctor.newInstance(createFolder("dummy-folder"), DUMMY_JAVA_PROJECT);
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
        field.set(InternalPlatform.getDefault(), new DummyBundleContext());

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
        var value = new ServiceTracker<IContentTypeManager, IContentTypeManager>(InternalPlatform.getDefault().getBundleContext(), DummyContentManager.class.getName(), null);
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
