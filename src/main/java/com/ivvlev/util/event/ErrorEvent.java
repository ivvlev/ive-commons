package com.ivvlev.util.event;

public interface ErrorEvent<T extends Exception> extends BaseEvent {

    T getException();

}
