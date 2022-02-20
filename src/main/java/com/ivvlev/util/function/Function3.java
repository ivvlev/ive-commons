package com.ivvlev.util.function;

@FunctionalInterface
public interface Function3<T1, T2, T3, R> {
    R exec(T1 a1, T2 a2, T3 a3);
}
