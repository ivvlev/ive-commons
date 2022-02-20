package com.ivvlev.util.function;

/**
 * Процедура, пробрасываюшая все исключения, унаследованные от Throwable.
 */
@FunctionalInterface
public interface ThrowableProcedure {
    void exec() throws Throwable;
}
