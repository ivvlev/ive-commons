package com.ivvlev.util.event;

public class ErrorEventImpl<T extends Exception> extends BaseEventAbst implements ErrorEvent<T> {

    private final T exception_;

    @Override
    public T getException() {
        return exception_;
    }

    public ErrorEventImpl(Object sourceObj, T exception) {
        super(sourceObj);
        exception_ = exception;
    }
}
