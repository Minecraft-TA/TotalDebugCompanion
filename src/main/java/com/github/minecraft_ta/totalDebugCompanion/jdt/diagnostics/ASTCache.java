package com.github.minecraft_ta.totalDebugCompanion.jdt.diagnostics;

import com.github.javaparser.utils.Pair;
import com.github.minecraft_ta.totalDebugCompanion.jdt.impls.CompilationUnitImpl;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class ASTCache {

    private static final Map<String, Pair<Integer, CompilationUnit>> CACHE = new ConcurrentHashMap<>();
    private static final Map<String, List<Consumer<CompilationUnit>>> LISTENERS = new ConcurrentHashMap<>();

    public static void update(String key, String contents) {
        var existing = CACHE.get(key);
        if (existing != null && existing.a == contents.hashCode())
            return;

        CompletableFuture.runAsync(() -> {
            ASTParser parser = ASTParser.newParser(AST.JLS8);
            parser.setSource(new CompilationUnitImpl("Test", contents));
            parser.setResolveBindings(true);
            parser.setStatementsRecovery(true);
            parser.setKind(ASTParser.K_COMPILATION_UNIT);
            var ast = (CompilationUnit) parser.createAST(null);

            CACHE.put(key, new Pair<>(contents.hashCode(), ast));

            notifyListeners(key, ast);
        });
    }

    public static void addChangeListener(String key, Consumer<CompilationUnit> listener) {
        LISTENERS.computeIfAbsent(key, (k) -> new ArrayList<>()).add(listener);
        var existing = CACHE.get(key);
        if (existing != null)
            listener.accept(existing.b);
    }

    public static void removeFromCache(String key) {
        CACHE.remove(key);
        LISTENERS.remove(key);
    }

    public static CompilationUnit getFromCache(String key) {
        return CACHE.get(key).b;
    }

    private static void notifyListeners(String key, CompilationUnit ast) {
        for (var listener : LISTENERS.getOrDefault(key, Collections.emptyList())) {
            listener.accept(ast);
        }
    }
}
