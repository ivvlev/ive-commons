package com.ivvlev.util.event;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Обычная реализация
 */
abstract class EventSourceAbst<E extends BaseEvent> implements EventSource<E> {
    /**
     * Множество слушателей
     */
    protected final List<EventListener<E>> eventListenerList_;

    protected EventSourceAbst(List<EventListener<E>> listenerList) {
        this.eventListenerList_ = listenerList;
    }

    /**
     * Добавить слушателя
     *
     * @param eventListener слушатель событий
     */
    @Override
    public void addEventListener(final EventListener<E> eventListener) {
        eventListenerList_.add(eventListener);
    }

    public EventListener<E> addEventListener(final Consumer<E> proc) {
        EventListenerAbst<E> listener = new EventListenerAbst<E>() {
            @Override
            public void handleEvent(E event) {
                proc.accept(event);
            }
        };
        addEventListener(listener);
        return listener;
    }

    public void addEventListener(final BiConsumer<EventListener<E>, E> proc) {
        addEventListener(new EventListenerAbst<E>() {
            @Override
            public void handleEvent(E event) {
                proc.accept(this, event);
            }
        });
    }

    @Override
    public void addEventListenerBefore(EventListener<E> currentListener, EventListener<E> eventListener) {
        int index = eventListenerList_.indexOf(currentListener);
        eventListenerList_.add(index, eventListener);
    }

    @Override
    public void addEventListenerFirst(EventListener<E> eventListener) {
        eventListenerList_.add(0, eventListener);
    }

    /**
     * Удалить слушателя
     *
     * @param eventListener слушатель событий
     */
    @Override
    public void removeEventListener(EventListener<E> eventListener) {
        boolean contains = eventListenerList_.contains(eventListener);
        if (contains) {
            eventListenerList_.remove(eventListener);
        }
    }

    /**
     * Удалить всех
     */
    @Override
    public void removeAll() {
        eventListenerList_.clear();
    }

    /**
     * Количество слушателей
     *
     * @return число слушателей
     */
    @Override
    public int getListenerCount() {
        return eventListenerList_.size();
    }

    @SuppressWarnings("unchecked")
    protected EventListener<E>[] getEventListenerArray() {
        return eventListenerList_.toArray(new EventListener[0]);
    }

    /**
     * Послать событие
     *
     * @param event событие
     */
    @Override
    public void fireEvent(E event) {
        doFireEvent(getEventListenerArray(), event);
    }

    protected abstract void doFireEvent(final EventListener<E>[] eventListenerArray, final E event);


}
