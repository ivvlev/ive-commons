package com.ivvlev.util.event;

/**
 * Источник событий. Потокобезопасный.
 * Реализация, устойчивая к изменению списка листенеров в процессе рассылки событий лисненерам. Если в процессе
 * рассылки событий удалить листенер из источника, этому листенеру событие отослано не будет.
 */
public class MutableConcurrentEventSourceImpl<E extends BaseEvent> extends ConcurrentEventSourceImpl<E> {

    @Override
    protected void doFireEvent(final EventListener<E>[] eventListenerArray, final E event) {
        for (EventListener<E> eventListener : eventListenerArray) {
            // Проверка потокобезопасна.
            // Метод contains(), по факту, выполняет 'eventListener.linkedList_ == eventListenerList_'
            // Где ссылки: final eventListenerList_ и volatile linkedList_.
            if (eventListenerList_.contains(eventListener)) {
                eventListener.handleEvent(event);
            }
        }
    }
}
