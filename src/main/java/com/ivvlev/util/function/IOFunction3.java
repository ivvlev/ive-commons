package com.ivvlev.util.function;

import java.io.IOException;

/**
 * Функция, прорабсывающая исключения вводв/вывода.
 */
@FunctionalInterface
public interface IOFunction3<T1, T2, T3, R> {
    R exec(T1 a1, T2 a2, T3 a3) throws IOException;
}
