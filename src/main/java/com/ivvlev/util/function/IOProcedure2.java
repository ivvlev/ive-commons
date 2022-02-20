package com.ivvlev.util.function;

import java.io.IOException;

/**
 * Процедура, прорабсывающая исключения вводв/вывода.
 */
@FunctionalInterface
public interface IOProcedure2<T1, T2> {
    void exec(T1 a1, T2 a2) throws IOException;

}
