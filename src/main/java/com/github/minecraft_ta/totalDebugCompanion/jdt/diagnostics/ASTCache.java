package com.github.minecraft_ta.totalDebugCompanion.jdt.diagnostics;

import com.github.minecraft_ta.totalDebugCompanion.jdt.impls.CompilationUnitImpl;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

public class ASTCache {

    private static final Map<String, Entry> CACHE = new HashMap<>();
    private static final Map<String, List<BiConsumer<CompilationUnit, Integer>>> LISTENERS = new ConcurrentHashMap<>();

    public static void update(String key, String contents) {
        int version = 0;
        synchronized (CACHE) {
            var existing = CACHE.get(key);
            if (existing != null)
                version = ++existing.version;
        }

        int finalVersion = version;
        CompletableFuture.runAsync(() -> {
            ASTParser parser = ASTParser.newParser(AST.JLS8);
            parser.setSource(new CompilationUnitImpl("Test", contents));
            parser.setResolveBindings(true);
            parser.setStatementsRecovery(true);
            parser.setKind(ASTParser.K_COMPILATION_UNIT);
            var ast = (CompilationUnit) parser.createAST(null);

            synchronized (CACHE) {
                var entry = CACHE.computeIfAbsent(key, (k) -> new Entry());
                //There's already something newer available
                if (entry.version > finalVersion)
                    return;

                entry.version = finalVersion;
                entry.unit = ast;

                notifyListeners(key, ast, finalVersion);
            }
        });
    }

    public static void addChangeListener(String key, BiConsumer<CompilationUnit, Integer> listener) {
        LISTENERS.computeIfAbsent(key, (k) -> new ArrayList<>()).add(listener);
        synchronized (CACHE) {
            var existing = CACHE.get(key);
            if (existing != null)
                listener.accept(existing.unit, existing.version);
        }
    }

    public static void removeFromCache(String key) {
        synchronized (CACHE) {
            CACHE.remove(key);
        }
        LISTENERS.remove(key);
    }

    public static CompilationUnit getFromCache(String key) {
        synchronized (CACHE) {
            return CACHE.get(key).unit;
        }
    }

    private static void notifyListeners(String key, CompilationUnit ast, int version) {
        for (var listener : LISTENERS.getOrDefault(key, Collections.emptyList())) {
            listener.accept(ast, version);
        }
    }

    public static class Entry {

        public int version;
        public CompilationUnit unit;
    }
}
