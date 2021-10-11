package com.github.minecraft_ta.totalDebugCompanion.lsp;

import com.github.minecraft_ta.totalDebugCompanion.CompanionApp;
import com.github.minecraft_ta.totalDebugCompanion.lsp.diagnostics.DiagnosticsManager;
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class JavaLanguageServer {

    public static final Path SRC_DIR = CompanionApp.getRootPath().resolve("workspace").resolve("custom-project").resolve("src");

    private LanguageServer server;
    private LSPServerProcess process;

    private final Map<String, Integer> fileVersionMap = new HashMap<>();
    private final BaseScript baseScript = new BaseScript(SRC_DIR.resolve("BaseScript.java"));
    private final DiagnosticsManager diagnosticsManager = new DiagnosticsManager();

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
        var client = new LSPJavaClient(this);

        Launcher<LanguageServer> launcher = Launcher.createLauncher(client, LanguageServer.class, process.getInputStream(), process.getOutputStream());
        this.server = launcher.getRemoteProxy();
        launcher.startListening();

        this.server.initialize(getInitParams()).thenApply(res -> {
            this.server.initialized(new InitializedParams());
            CodeUtils.setTokenLegend(res.getCapabilities().getSemanticTokensProvider().getLegend());
            System.out.println(res.getCapabilities().getExecuteCommandProvider());
            return res;
        }).exceptionally((e) -> {
            e.printStackTrace();
            return null;
        });
    }

    public void stop() {
        if (this.server != null) {
            this.server.shutdown().join();
            this.server.exit();
        }
        if (this.process != null) {
            var result = this.process.awaitTermination(1000, TimeUnit.SECONDS);
            if (!result)
                this.process.kill();
        }
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

    public CompletableFuture<Object> executeCommand(ExecuteCommandParams params) {
        return this.server.getWorkspaceService().executeCommand(params);
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

    public CompletableFuture<List<? extends SymbolInformation>> symbols(WorkspaceSymbolParams params) {
        return this.server.getWorkspaceService().symbol(params);
    }

    public CompletableFuture<SignatureHelp> signatureHelp(SignatureHelpParams params) {
        return this.server.getTextDocumentService().signatureHelp(params);
    }

    public BaseScript getBaseScript() {
        return this.baseScript;
    }

    public DiagnosticsManager getDiagnosticsManager() {
        return diagnosticsManager;
    }

    public int getDocumentVersion(String uri) {
        return this.fileVersionMap.getOrDefault(uri, -1);
    }

    private Path getLauncherJarPath() {
        var pluginDir = CompanionApp.getRootPath().resolve("jdt-language-server-latest").resolve("plugins");
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
        var extendedClientCapabilities = new HashMap<String, Object>();
        extendedClientCapabilities.put("classFileContentsSupport", true);
        var settings = new HashMap<String, Object>();
        settings.put("java.symbols.includeSourceMethodDeclarations", true);
        var initializationOptions = new HashMap<String, Object>();
        initializationOptions.put("extendedClientCapabilities", extendedClientCapabilities);
        initializationOptions.put("settings", settings);
        initParams.setInitializationOptions(initializationOptions);

        initParams.setRootUri(CompanionApp.getRootPath().resolve("workspace").toUri().toString());
        initParams.setWorkspaceFolders(List.of(new WorkspaceFolder(CompanionApp.getRootPath().resolve("workspace").resolve("custom-project").toUri().toString())));
        WorkspaceClientCapabilities workspaceClientCapabilities = new WorkspaceClientCapabilities();
        var symbolCapabilities = new SymbolCapabilities();
        symbolCapabilities.setSymbolKind(new SymbolKindCapabilities(List.of(SymbolKind.values())));
        workspaceClientCapabilities.setSymbol(symbolCapabilities); //Workspace search
        workspaceClientCapabilities.setExecuteCommand(new ExecuteCommandCapabilities());
        workspaceClientCapabilities.setWorkspaceFolders(true);
        workspaceClientCapabilities.setConfiguration(true);

        TextDocumentClientCapabilities textDocumentClientCapabilities = new TextDocumentClientCapabilities();
//        textDocumentClientCapabilities.setCodeAction(new CodeActionCapabilities());
        var completionItemCapabilities = new CompletionItemCapabilities(true);
        completionItemCapabilities.setInsertReplaceSupport(true);
        completionItemCapabilities.setInsertTextModeSupport(new CompletionItemInsertTextModeSupportCapabilities(Arrays.asList(InsertTextMode.values())));
        textDocumentClientCapabilities.setCompletion(new CompletionCapabilities(completionItemCapabilities));
//        textDocumentClientCapabilities.setDefinition(new DefinitionCapabilities());
//        textDocumentClientCapabilities.setDocumentHighlight(new DocumentHighlightCapabilities());
        textDocumentClientCapabilities.setFormatting(new FormattingCapabilities());
//        textDocumentClientCapabilities.setHover(new HoverCapabilities());
//        textDocumentClientCapabilities.setRangeFormatting(new RangeFormattingCapabilities());
//        textDocumentClientCapabilities.setReferences(new ReferencesCapabilities());
//        textDocumentClientCapabilities.setRename(new RenameCapabilities());
        textDocumentClientCapabilities.setSemanticTokens(new SemanticTokensCapabilities(false));
        textDocumentClientCapabilities.setSignatureHelp(new SignatureHelpCapabilities());
        textDocumentClientCapabilities.setPublishDiagnostics(new PublishDiagnosticsCapabilities(true, new DiagnosticsTagSupport(Arrays.asList(DiagnosticTag.values())), true));
        textDocumentClientCapabilities.setSynchronization(new SynchronizationCapabilities(false, false, false));
        initParams.setCapabilities(new ClientCapabilities(workspaceClientCapabilities, textDocumentClientCapabilities, null));

        return initParams;
    }
}
