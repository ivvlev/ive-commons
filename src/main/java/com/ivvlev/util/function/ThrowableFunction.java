package com.ivvlev.util.function;

/**
 * Функция, пробрасываюшая все исключения, унаследованные от Throwable
 */
@FunctionalInterface
public interface ThrowableFunction <R> {
    R exec() throws Throwable;
}
