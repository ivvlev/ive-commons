package com.ivvlev.util.event;

public class DataEventImpl<T> extends BaseEventAbst implements DataEvent<T> {

    private final T data_;

    @Override
    public T getData() {
        return data_;
    }

    public DataEventImpl(Object sourceObj, T data) {
        super(sourceObj);
        data_ = data;
    }
}
