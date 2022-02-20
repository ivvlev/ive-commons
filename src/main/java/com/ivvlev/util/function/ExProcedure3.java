package com.ivvlev.util.function;

/**
 * Процедура c одним аргументом, прорабсывающая все исключения.
 */
@FunctionalInterface
public interface ExProcedure3<T1, T2, T3> {
    void exec(T1 a1, T2 a2, T3 a3) throws Exception;

}
