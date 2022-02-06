package com.ivvlev.util.concurrent.lock;

import com.ivvlev.util.concurrent.TimeoutException;
import com.ivvlev.util.function.Procedure;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Utility interface that provides synchronization mechanism similar to row-level DB locking.
 */
public interface EntityLocker<K> {

    /**
     * Method perform lock of key.
     *
     * @param key Entity key.
     * @throws DeadlockException     if deadlock occurs
     * @throws EntityLockerException if the thread is interrupted or any other exception thrown
     */
    void lock(K key);

    /**
     * Method perform attempt to lock of key with timeout.
     *
     * @param key     Entity key
     * @param timeout the time to wait for the lock
     * @param unit    the time unit of the timeout argument
     * @return {@code true} if success, else {@code false}.
     * @throws DeadlockException     if deadlock occurs
     * @throws EntityLockerException if the thread is interrupted or any other exception thrown
     */
    boolean tryLock(K key, long timeout, TimeUnit unit);

    /**
     * Method perform unlock of key.
     *
     * @param key Entity key.
     */
    void unlock(K key);

    /**
     * Check is key currently locked by current thread.
     *
     * @param key Entity key.
     * @return {@code true} if key locked by current thread, else {@code false}.
     */
    boolean isLockedByCurrentThread(K key);

    /**
     * The method locks the passed key for the duration of the anonymous method execution.
     *
     * @param key             Entity key
     * @param protectedMethod Anonymous method
     * @return The result of anonymous method
     * @throws DeadlockException     if deadlock occurs
     * @throws EntityLockerException if the thread is interrupted or any other exception thrown
     */
    <R> R forLock(K key, Supplier<R> protectedMethod);

    /**
     * The method locks the passed key for the duration of the anonymous method execution.
     *
     * @param key             Entity key
     * @param protectedMethod Anonymous method
     * @throws DeadlockException     if deadlock occurs
     * @throws EntityLockerException if the thread is interrupted or any other exception thrown
     */
    void forLock(K key, Procedure protectedMethod);

    /**
     * The method locks the passed key for the duration of the anonymous method execution.
     *
     * @param key             Entity key
     * @param protectedMethod Anonymous method
     * @param timeout         the time to wait for the lock
     * @param unit            the time unit of the timeout argument
     * @return The result of anonymous method
     * @throws TimeoutException      if timeout occurs
     * @throws DeadlockException     if deadlock occurs
     * @throws EntityLockerException if the thread is interrupted or any other exception thrown
     */
    <R> R forLock(K key, Supplier<R> protectedMethod, long timeout, TimeUnit unit);

    /**
     * The method locks the passed key for the duration of the anonymous method execution.
     *
     * @param key             Entity key
     * @param protectedMethod Anonymous method
     * @param timeout         the time to wait for the lock
     * @param unit            the time unit of the timeout argument
     * @throws TimeoutException      if timeout occurs
     * @throws DeadlockException     if deadlock occurs
     * @throws EntityLockerException if the thread is interrupted or any other exception thrown
     */
    void forLock(K key, Procedure protectedMethod, long timeout, TimeUnit unit);

}
