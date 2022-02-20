package com.ivvlev.util.function;

import java.io.IOException;

/**
 * Процедура, прорабсывающая исключения вводв/вывода.
 */
@FunctionalInterface
public interface IOProcedure {
    void exec() throws IOException;

}
