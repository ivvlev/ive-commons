package com.ivvlev.util.function;

/**
 * Процедура c двумя аргументом, прорабсывающая все исключения, унаследованные от Throwable.
 */
@FunctionalInterface
public interface ThrowableProcedure2<T1,T2> {
    void exec(T1 a1, T2 a2) throws Throwable;
}
