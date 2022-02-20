package com.ivvlev.util.event;

import java.util.function.BiConsumer;

/**
 * Источник событий
 */
public interface EventSource<E extends BaseEvent> {
    /**
     * Добавить слушателя в конец
     *
     * @param eventListener слушатель событий
     */
    void addEventListener(EventListener<E> eventListener);

//    EventListener<E> addEventListener(final Predicate<E> proc);

    void addEventListener(BiConsumer<EventListener<E>, E> proc);

    /**
     * Добавить слушателя в начало
     *
     * @param eventListener слушатель событий
     */
    void addEventListenerFirst(EventListener<E> eventListener);

    /**
     * Добавить слушателя перед существующим
     *
     * @param currentListener существующий слушатель
     * @param eventListener   добавляемый слушатель
     */
    void addEventListenerBefore(EventListener<E> currentListener, EventListener<E> eventListener);

    /**
     * Удалить слушателя
     *
     * @param eventListener слушатель событий
     */
    void removeEventListener(EventListener<E> eventListener);

    /**
     * Удалить всех слушателей
     */
    void removeAll();

    /**
     * Геттер
     *
     * @return количество слушателей
     */
    int getListenerCount();

    /**
     * Послать событие
     *
     * @param event событие
     */
    void fireEvent(E event);

    public static class Factory {
        public static <E extends BaseEvent> EventSource<E> newInstance() {
            return new EventSourceImpl<E>();
        }
    }
}
