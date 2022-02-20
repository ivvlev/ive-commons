package com.ivvlev.util.function;

/**
 * Процедура c тремя аргументом, прорабсывающая все исключения, унаследованные от Throwable.
 */
@FunctionalInterface
public interface ThrowableProcedure3<T1, T2, T3> {
    void exec(T1 a1, T2 a2, T3 a3) throws Throwable;
}
