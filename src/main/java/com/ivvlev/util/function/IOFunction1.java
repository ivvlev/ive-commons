package com.ivvlev.util.function;

import java.io.IOException;

/**
 * Функция, прорабсывающая исключения вводв/вывода.
 */
@FunctionalInterface
public interface IOFunction1<T,R> {
    R exec(T a) throws IOException;
}
