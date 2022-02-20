package com.ivvlev.util.function;

@FunctionalInterface
public interface Function2<T1, T2, R> {
    R exec(T1 a1, T2 a2);
}
