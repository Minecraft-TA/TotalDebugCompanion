package com.github.minecraft_ta.totalDebugCompanion.util;

public class Triple<A, B, C> {

    private final A a;
    private final B b;
    private final C c;

    private Triple(A a, B b, C c) {
        this.a = a;
        this.b = b;
        this.c = c;
    }

    public static <A, B, C> Triple<A, B, C> of(A a, B b, C c) {
        return new Triple<>(a, b, c);
    }
}
