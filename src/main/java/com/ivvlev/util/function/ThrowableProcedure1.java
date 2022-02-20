package com.ivvlev.util.function;

/**
 * Процедура с одним аргументом, пробрасываюшая все исключения, унаследованные от Throwable.
 */
@FunctionalInterface
public interface ThrowableProcedure1<T> {
    void exec(T a) throws Throwable;
}
