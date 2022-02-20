package com.ivvlev.util.event;

import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Источник событий.
 * Потокобезопасный. Возможна взаимная блокировка потоков, если поток, отправивший событие, начнёт ждать поток,
 * пытающийся добавить листенер в данный источник событий.
 */
public class ConcurrentEventSourceImpl<E extends BaseEvent> extends EventSourceAbst<E> {

    public ConcurrentEventSourceImpl() {
        super(new CopyOnWriteArrayList<>());
    }

    @Override
    protected void doFireEvent(EventListener<E>[] eventListenerArray, E event) {
        for (EventListener<E> eventListener : eventListenerArray) {
            eventListener.handleEvent(event);
        }
    }
}
