package com.ivvlev.util.function;

/**
 * Процедура, прорабсывающая все исключения.
 */
@FunctionalInterface
public interface ExProcedure {
    void exec() throws Exception;

}
