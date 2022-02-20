package com.ivvlev.util.function;

/**
 * В отличие от {@link java.util.function.Function} пробрасывает исключение
 */
@FunctionalInterface
public interface ExFunction2<T1, T2, R> {
    R exec(T1 a1, T2 a2) throws Exception;
}
