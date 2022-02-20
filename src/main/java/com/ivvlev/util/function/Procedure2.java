package com.ivvlev.util.function;

/**
 * Процедура c двумя аргументами.
 */
@FunctionalInterface
public interface Procedure2<T1, T2> {
    void exec(T1 a1, T2 a2);

}
