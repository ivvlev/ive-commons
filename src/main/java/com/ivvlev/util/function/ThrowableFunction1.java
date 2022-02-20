package com.ivvlev.util.function;

/**
 * Функция c одним аргументом, пробрасываюшая все исключения, унаследованные от Throwable.
 */
@FunctionalInterface
public interface ThrowableFunction1<T, R> {
    R exec(T a) throws Throwable;
}
