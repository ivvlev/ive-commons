package com.ivvlev.util;


import com.ivvlev.util.event.BaseEvent;
import com.ivvlev.util.event.BaseEventImpl;
import com.ivvlev.util.event.EventSource;
import com.ivvlev.util.event.EventSourceImpl;
import com.ivvlev.util.function.ExFunction;
import com.ivvlev.util.function.ExProcedure;

import java.io.Closeable;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Класс реализует фунциональность логической блокировки основанной на счётчике блокировок.
 * Реализация не является потокобезопасной и не предназначена для использования в многопоточном окружении.
 */
public class LockObject implements Closeable {

    private final AtomicInteger lockCount_ = new AtomicInteger();
    private final EventSource<BaseEvent> unlockedEventSource_ = new EventSourceImpl<>();

    public LockObject lock() {
        lockCount_.incrementAndGet();
        return this;
    }

    public void unlock() {
        int c = lockCount_.decrementAndGet();
        if (c < 0)
            throw new RuntimeException("Not locked before.");
        else if (c == 0) {
            unlockedEventSource_.fireEvent(new BaseEventImpl(this));
        }
    }

    public boolean isLocked() {
        return lockCount_.get() != 0;
    }

    public boolean notLocked() {
        return !isLocked();
    }

    /**
     * Метод реализован для использования блокировки в конструкции  try (LockObject ignored = getCWALock().lock()) {}
     */
    @Override
    public void close() {
        unlock();
    }

    /**
     * Метод выполняет, переданное лямбда выражение, если не установлена блокировка.
     * Перед выполнением устанавливается блокировка.
     * Таким образом, можно гарантировать однократное выполнение лямбда выражения.
     *
     * @param p Исполняемое лямбда выражение
     */
    public void ifNotLockedLockAndCall(ExProcedure p) throws Exception {
        if (notLocked()) {
            try (LockObject ignore = this.lock()) {
                p.exec();
            }
        }
    }

    /**
     * Увеличивает счётчик блокировок и выполняет анонимный метод
     */
    public void lockAndCall(ExProcedure p) throws Exception {
        try (LockObject ignore = this.lock()) {
            p.exec();
        }
    }

    /**
     * Увеличивает счётчик блокировок и выполняет анонимный метод
     */
    public <R> R lockAndCall(ExFunction<R> p) throws Exception {
        try (LockObject ignore = this.lock()) {
            return p.exec();
        }
    }

    /**
     * Источник события разблокировки объекта. Событие отправляется после обнуления счётчика блокировок.
     */
    public EventSource<BaseEvent> getUnlockedEventSource() {
        return unlockedEventSource_;
    }
}
