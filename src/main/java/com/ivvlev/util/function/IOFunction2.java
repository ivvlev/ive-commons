package com.ivvlev.util.function;

import java.io.IOException;

/**
 * Функция, прорабсывающая исключения вводв/вывода.
 */
@FunctionalInterface
public interface IOFunction2<T1, T2, R> {
    R exec(T1 a1, T2 a2) throws IOException;
}
