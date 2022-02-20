package com.ivvlev.util.event;

/**
 * Реализация интерфейса BaseEvent
 */
public class BaseEventImpl extends BaseEventAbst implements BaseEvent{
    public BaseEventImpl(Object sourceObj) {
        super(sourceObj);
    }
}
