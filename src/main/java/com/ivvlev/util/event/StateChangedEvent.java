package com.ivvlev.util.event;

public interface StateChangedEvent<T> extends BaseEvent {

    T getState();

}
