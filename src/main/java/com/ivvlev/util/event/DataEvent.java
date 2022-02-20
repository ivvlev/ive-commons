package com.ivvlev.util.event;

public interface DataEvent<T> extends BaseEvent {

    T getData();

}
