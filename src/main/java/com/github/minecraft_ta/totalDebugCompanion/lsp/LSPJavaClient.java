package com.github.minecraft_ta.totalDebugCompanion.lsp;

import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.services.LanguageClient;

import java.util.concurrent.CompletableFuture;

public class LSPJavaClient implements LanguageClient {

    @Override
    public void telemetryEvent(Object object) {
        System.out.println("telemetryEvent object = " + object);
    }

    @Override
    public void publishDiagnostics(PublishDiagnosticsParams diagnostics) {
        //diagnostics.getDiagnostics().stream().filter(d -> !d.getMessage().contains("non-project")).toList()
//        System.out.println("publishDiagnostics diagnostics = " + diagnostics); //TODO: show in editor
    }

    @Override
    public void showMessage(MessageParams messageParams) {
        System.out.println("showMessage messageParams = " + messageParams);
    }

    @Override
    public CompletableFuture<MessageActionItem> showMessageRequest(ShowMessageRequestParams requestParams) {
        System.out.println("showMessageRequest requestParams = " + requestParams);
        return null;
    }

    @Override
    public void logMessage(MessageParams message) {
//            System.out.println("logMessage message = " + message);
    }

    @Override
    public CompletableFuture<Void> registerCapability(RegistrationParams params) {
        System.out.println("registerCapability params = " + params);
        return CompletableFuture.runAsync(() -> {});
    }

    @Override
    public CompletableFuture<Void> unregisterCapability(UnregistrationParams params) {
        System.out.println("unregisterCapability params = " + params);
        return CompletableFuture.runAsync(() -> {});
    }
}
