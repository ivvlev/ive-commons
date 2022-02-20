package com.ivvlev.util.function;

/**
 * Процедура c одним аргументом.
 */
@FunctionalInterface
public interface Procedure1<T> {
    void exec(T a);

}
