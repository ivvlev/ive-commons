package com.ivvlev.util.function;

/**
 * В отличие от {@link java.util.function.Function} пробрасывает исключение
 */
@FunctionalInterface
public interface ExFunction3<T1, T2, T3, R> {
    R exec(T1 a1, T2 a2, T3 a3) throws Exception;
}
