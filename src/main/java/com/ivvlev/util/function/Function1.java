package com.ivvlev.util.function;

@FunctionalInterface
public interface Function1<T,R> {
    R exec(T a);
}
