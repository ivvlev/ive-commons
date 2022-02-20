package com.ivvlev.util.function;

/**
 * В отличие от {@link java.util.function.Function} пробрасывает исключение, унаследованные от Throwable.
 */
@FunctionalInterface
public interface ThrowableFunction2<T1, T2, R> {
    R exec(T1 a1, T2 a2) throws Throwable;
}
