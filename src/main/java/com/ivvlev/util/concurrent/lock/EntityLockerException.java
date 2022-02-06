package com.ivvlev.util.concurrent.lock;

public class EntityLockerException extends RuntimeException {
    public EntityLockerException() {
    }

    public EntityLockerException(String message) {
        super(message);
    }

    public EntityLockerException(String message, Throwable cause) {
        super(message, cause);
    }

    public EntityLockerException(Throwable cause) {
        super(cause);
    }

    public EntityLockerException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
