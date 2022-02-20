package com.ivvlev.util.function;

/**
 * Процедура c одним аргументом, прорабсывающая все исключения.
 */
@FunctionalInterface
public interface ExProcedure2<T1,T2> {
    void exec(T1 a1, T2 a2) throws Exception;

}
