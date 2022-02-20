package com.ivvlev.util.function;

/**
 * Процедура c одним аргументом, прорабсывающая все исключения.
 */
@FunctionalInterface
public interface ExProcedure1<T> {
    void exec(T a) throws Exception;

}
