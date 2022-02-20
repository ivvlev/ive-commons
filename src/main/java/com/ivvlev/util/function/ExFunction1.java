package com.ivvlev.util.function;

/**
 * В отличие от {@link java.util.function.Function} пробрасывает исключение
 */
@FunctionalInterface
public interface ExFunction1<T, R> {
    R exec(T a) throws Exception;
}
