package com.ivvlev.util.function;

import java.io.IOException;

/**
 * Функция, прорабсывающая исключения вводв/вывода.
 */
@FunctionalInterface
public interface IOFunction<R> {
    R exec() throws IOException;
}
