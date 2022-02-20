package com.ivvlev.util.function;

/**
 * Функция пробрасываюшая все исключения.
 */
@FunctionalInterface
public interface ExFunction<R> {
    R exec() throws Exception;
}
