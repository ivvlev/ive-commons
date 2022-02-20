package com.ivvlev.util.event;

/**
 * Базовый интерфейс для всех событий
 * Неизменный
 */
public interface BaseEvent {
    /**
     * Гетер
     *
     * @return источник
     */
    Object getSourceObj();

}
