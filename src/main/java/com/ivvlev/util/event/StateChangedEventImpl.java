package com.ivvlev.util.event;

public class StateChangedEventImpl<T> extends BaseEventAbst implements StateChangedEvent<T> {

    private final T state_;

    @Override
    public T getState() {
        return state_;
    }

    public StateChangedEventImpl(Object sourceObj, T state) {
        super(sourceObj);
        state_ = state;
    }
}
