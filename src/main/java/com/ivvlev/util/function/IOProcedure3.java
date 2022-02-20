package com.ivvlev.util.function;

import java.io.IOException;

/**
 * Процедура, прорабсывающая исключения вводв/вывода.
 */
@FunctionalInterface
public interface IOProcedure3<T1, T2, T3> {
    void exec(T1 a1, T2 a2, T3 a3) throws IOException;

}
