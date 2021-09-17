package com.github.minecraft_ta.totalDebugCompanion.lsp;

import com.github.minecraft_ta.totalDebugCompanion.util.CodeUtils;
import com.github.minecraft_ta.totalDebugCompanion.util.FileUtils;
import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.LanguageServer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class JavaLanguageServer {

    public static final Path SRC_DIR = Paths.get(".", "workspace", "custom-project", "src");

    private LanguageServer server;
    private LSPServerProcess process;
    private final Map<String, Integer> fileVersionMap = new HashMap<>();

    public JavaLanguageServer() {
        Runtime.getRuntime().addShutdownHook(new Thread(this::stop));
    }

    public boolean isSetup() {
        return getLauncherJarPath() != null;
    }

    public void start() {
        if (this.server != null)
            throw new IllegalStateException();
        var launcherJarPath = getLauncherJarPath();
        if (launcherJarPath == null)
            throw new IllegalStateException("Launcher jar path is null");

        process = new LSPServerProcess();
        process.start(launcherJarPath);
        var client = new LSPJavaClient();

        Launcher<LanguageServer> launcher = Launcher.createLauncher(client, LanguageServer.class, process.getInputStream(), process.getOutputStream());
        this.server = launcher.getRemoteProxy();
        launcher.startListening();

        this.server.initialize(getInitParams()).thenApply(res -> {
            this.server.initialized(new InitializedParams());
            CodeUtils.setTokenLegend(res.getCapabilities().getSemanticTokensProvider().getLegend());
            return res;
        }).exceptionally((e) -> {
            e.printStackTrace();
            return null;
        });
    }

    public void stop() {
        this.server.shutdown().join();
        this.server.exit();
        var result = this.process.awaitTermination(1000, TimeUnit.SECONDS);
        if (!result)
            this.process.kill();
    }

    public void didOpen(DidOpenTextDocumentParams params) {
        var uri = params.getTextDocument().getUri();
        if (!Files.exists(Paths.get(FileUtils.toURI(uri))))
            throw new IllegalStateException();

        params.getTextDocument().setVersion(this.fileVersionMap.merge(uri, 1, Integer::sum));
        this.server.getTextDocumentService().didOpen(params);
    }

    public void didChange(DidChangeTextDocumentParams params) {
        params.getTextDocument().setVersion(this.fileVersionMap.merge(params.getTextDocument().getUri(), 1, Integer::sum));
        this.server.getTextDocumentService().didChange(params);
    }

    public void didClose(DidCloseTextDocumentParams params) {
        this.server.getTextDocumentService().didClose(params);
    }

    public CompletableFuture<Either<List<CompletionItem>, CompletionList>> completion(CompletionParams params) {
        return this.server.getTextDocumentService().completion(params);
    }

    public CompletableFuture<SemanticTokens> semanticsTokenFull(SemanticTokensParams params) {
        return this.server.getTextDocumentService().semanticTokensFull(params);
    }

    public CompletableFuture<List<? extends TextEdit>> formatting(DocumentFormattingParams params) {
        return this.server.getTextDocumentService().formatting(params);
    }

    public CompletableFuture<List<? extends TextEdit>> onTypeFormatting(DocumentOnTypeFormattingParams params) {
        return this.server.getTextDocumentService().onTypeFormatting(params);
    }

    private Path getLauncherJarPath() {
        var pluginDir = Paths.get(".", "jdt-language-server-latest", "plugins");
        if (!Files.exists(pluginDir) || !Files.isDirectory(pluginDir))
            return null;

        try {
            return Files.list(pluginDir).filter(p -> p.getFileName().toString().startsWith("org.eclipse.equinox.launcher_")).findFirst().orElse(null);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private InitializeParams getInitParams() {
        InitializeParams initParams = new InitializeParams();
        initParams.setWorkspaceFolders(null);
        WorkspaceClientCapabilities workspaceClientCapabilities = new WorkspaceClientCapabilities();
//        workspaceClientCapabilities.setSymbol(new SymbolCapabilities()); //Workspace search
        workspaceClientCapabilities.setWorkspaceFolders(true);
        workspaceClientCapabilities.setConfiguration(true);

        TextDocumentClientCapabilities textDocumentClientCapabilities = new TextDocumentClientCapabilities();
//        textDocumentClientCapabilities.setCodeAction(new CodeActionCapabilities());
        var completionItemCapabilities = new CompletionItemCapabilities(true);
        completionItemCapabilities.setInsertReplaceSupport(true);
        textDocumentClientCapabilities.setCompletion(new CompletionCapabilities(completionItemCapabilities));
//        textDocumentClientCapabilities.setDefinition(new DefinitionCapabilities());
//        textDocumentClientCapabilities.setDocumentHighlight(new DocumentHighlightCapabilities());
        textDocumentClientCapabilities.setFormatting(new FormattingCapabilities());
//        textDocumentClientCapabilities.setHover(new HoverCapabilities());
        textDocumentClientCapabilities.setOnTypeFormatting(new OnTypeFormattingCapabilities());
//        textDocumentClientCapabilities.setRangeFormatting(new RangeFormattingCapabilities());
//        textDocumentClientCapabilities.setReferences(new ReferencesCapabilities());
//        textDocumentClientCapabilities.setRename(new RenameCapabilities());
        textDocumentClientCapabilities.setSemanticTokens(new SemanticTokensCapabilities(false));
//        textDocumentClientCapabilities.setSignatureHelp(new SignatureHelpCapabilities());
        textDocumentClientCapabilities.setSynchronization(new SynchronizationCapabilities(false, false, false));
        initParams.setCapabilities(new ClientCapabilities(workspaceClientCapabilities, textDocumentClientCapabilities, null));
//        initParams.setInitializationOptions(null);
//        initParams.setInitializationOptions(
//                serverDefinition.getInitializationOptions(URI.create(initParams.getRootUri())));

        return initParams;
    }
}
