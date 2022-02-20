package com.ivvlev.util.function;

/**
 * Процедура c тремя аргументами.
 */
@FunctionalInterface
public interface Procedure3<T1, T2, T3> {
    void exec(T1 a1, T2 a2, T3 a3);

}
