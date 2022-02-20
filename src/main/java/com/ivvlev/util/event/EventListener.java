package com.ivvlev.util.event;


/**
 * Слушатель событий
 * Потоко безопасный
 */
public interface EventListener<E extends BaseEvent> {
    /**
     * Обработать событие
     *
     * @param event событие
     */
    void handleEvent(E event);
}
