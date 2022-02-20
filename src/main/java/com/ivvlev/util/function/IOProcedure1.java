package com.ivvlev.util.function;

import java.io.IOException;

/**
 * Процедура, прорабсывающая исключения вводв/вывода.
 */
@FunctionalInterface
public interface IOProcedure1<T> {
    /**
     * @throws IOException Пробрасываемое исключение
     */
    void exec(T a) throws IOException;

}
