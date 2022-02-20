package com.ivvlev.util.event;

import java.util.LinkedList;

/**
 * Обычная реализация
 */
public class EventSourceImpl<E extends BaseEvent> extends EventSourceAbst<E> {

    public EventSourceImpl() {
        super(new LinkedList<>());
    }

    protected void doFireEvent(EventListener<E>[] eventListenerArray, E event) {
        for (EventListener<E> eventListener : eventListenerArray) {
            eventListener.handleEvent(event);
        }
    }
}
