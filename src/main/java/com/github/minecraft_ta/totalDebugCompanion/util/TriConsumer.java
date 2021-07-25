package com.github.minecraft_ta.totalDebugCompanion.util;

@FunctionalInterface
public interface TriConsumer<T, U, V> {

    void accept(T t, U u, V v);
}
