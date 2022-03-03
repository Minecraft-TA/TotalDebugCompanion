package com.github.minecraft_ta.totalDebugCompanion.jdt;

import org.eclipse.core.internal.resources.Workspace;
import org.eclipse.core.internal.runtime.DataArea;
import org.eclipse.core.internal.runtime.InternalPlatform;
import org.eclipse.core.internal.runtime.MetaDataKeeper;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.content.IContentTypeManager;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.internal.codeassist.CompletionEngine;
import org.eclipse.jdt.internal.core.*;
import org.eclipse.jdt.internal.core.search.indexing.IndexManager;
import org.osgi.util.tracker.ServiceTracker;

public class Test {

    public static void main(String[] args) throws Throwable {
        /*Document document = new Document("public class Test {public static void test() {String.jo}}");
        ASTParser parser = ASTParser.newParser(AST.getJLSLatest());

        parser.setSource(document.get().toCharArray());

        parser.setResolveBindings(true);
        parser.setStatementsRecovery(true);
        parser.setKind(ASTParser.K_COMPILATION_UNIT);
        var ast = (CompilationUnit) parser.createAST(null);*/

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
        System.out.println(Platform.getContentTypeManager());

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


        var DUMMY_JAVA_PROJECT = new DummyJavaProject();
        var unit = new CompilationUnit(null, "Test", DefaultWorkingCopyOwner.PRIMARY) {

            String s = "public class Test {public static void test() {Hal.t}}";
            @Override
            public IResource resource(PackageFragmentRoot root) {
                return new DummyFile(s);
            }

            @Override
            public IBuffer getBuffer() throws JavaModelException {
                //Overwrite for performance
                return new IBuffer() {

                    @Override
                    public void addBufferChangedListener(IBufferChangedListener listener) {

                    }

                    @Override
                    public void append(char[] text) {

                    }

                    @Override
                    public void append(String text) {

                    }

                    @Override
                    public void close() {

                    }

                    @Override
                    public char getChar(int position) {
                        return getCharacters()[position];
                    }

                    @Override
                    public char[] getCharacters() {
                        return getContents().toCharArray();
                    }

                    @Override
                    public String getContents() {
                        return s;
                    }

                    @Override
                    public int getLength() {
                        return s.length();
                    }

                    @Override
                    public IOpenable getOwner() {
                        return null;
                    }

                    @Override
                    public String getText(int offset, int length) throws IndexOutOfBoundsException {
                        return null;
                    }

                    @Override
                    public IResource getUnderlyingResource() {
                        return null;
                    }

                    @Override
                    public boolean hasUnsavedChanges() {
                        return false;
                    }

                    @Override
                    public boolean isClosed() {
                        return false;
                    }

                    @Override
                    public boolean isReadOnly() {
                        return false;
                    }

                    @Override
                    public void removeBufferChangedListener(IBufferChangedListener listener) {

                    }

                    @Override
                    public void replace(int position, int length, char[] text) {

                    }

                    @Override
                    public void replace(int position, int length, String text) {

                    }

                    @Override
                    public void save(IProgressMonitor progress, boolean force) throws JavaModelException {

                    }

                    @Override
                    public void setContents(char[] contents) {

                    }

                    @Override
                    public void setContents(String contents) {

                    }
                };
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
            public JavaProject getJavaProject() {
                return DUMMY_JAVA_PROJECT;
            }
        };

//        var result = new CompilationResult(unit, 1, 1, 5);
//        var dietParse = parser.dietParse(unit, result, 54);
        CompletionEngine.DEBUG = true;
//        var c = new JavaContentAssistInvocationContext(unit);
        //IContentAssistProcessor.computeCompletionProposals(fViewer, invocationOffset)
        var time = System.nanoTime();

        //Code completion
        unit.codeComplete(51, new CompletionRequestor() {
            @Override
            public void accept(CompletionProposal proposal) {
                System.out.println(((System.nanoTime() - time) / 1_000_000.0) + "Proposal -> " + proposal);
            }
        });

        //Formatting
        //This returns a list of TextEdits to apply
        //ToolFactory.createCodeFormatter(null).format(CodeFormatter.K_COMPILATION_UNIT, "The source", 0, sourceLength, 0, "\n").;

        //Semantic tokes
        //Basically use a DOM compilation unit, then visit it while resolving types and collect tokens
        //https://github.dev/eclipse/eclipse.jdt.ls/blob/3811ab7fe6d6fdaf54de6ec58a2a5158c98c1609/org.eclipse.jdt.ls.core/src/org/eclipse/jdt/ls/core/internal/handlers/SemanticTokensHandler.java

//        unit.codeComplete(5, requestor);
        Thread.sleep(1000);
        System.out.println();
    }

}
