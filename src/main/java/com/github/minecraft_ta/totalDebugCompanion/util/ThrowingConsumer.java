package com.github.minecraft_ta.totalDebugCompanion.util;

@FunctionalInterface
public interface ThrowingConsumer<T> {

    void accept(T t) throws Throwable;
}
