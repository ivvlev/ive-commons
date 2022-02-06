package com.ivvlev.util.concurrent.lock;

import java.util.concurrent.TimeUnit;

/**
 * Utility interface that provides synchronization mechanism similar to row-level DB locking.
 * Interface extents EntityLocker, adding Global Lock functionality.
 */
public interface GlobalEntityLocker<K> extends EntityLocker<K> {
    /**
     * Method perform global lock set. Global lock will set, immediately after releasing all locks currently set.
     * If global lock set by thread T1, no any other thread can lock keys.
     */
    void lockGlobal();

    /**
     * Method perform global lock set with timeout. Global lock will set, immediately after releasing all locks currently set.
     * If global lock set by thread T1, no any other thread can lock keys.
     *
     * @param timeout the time to wait for the lock
     * @param unit    the time unit of the timeout argument
     * @return {@code true} if success else {@code false}.
     * @throws EntityLockerException if the thread is interrupted or any other exception thrown
     */
    boolean tryLockGlobal(long timeout, TimeUnit unit);

    /**
     * Method perform global unlock.
     *
     * @throws IllegalMonitorStateException if global lock have not been set before
     */
    void unlockGlobal();

    /**
     * Queries if this lock is held by any thread. This method is
     * designed for use in monitoring of the system state,
     * not for synchronization control.
     *
     * @return {@code true} if any thread holds this lock and
     * {@code false} otherwise
     */
    boolean isGlobalLocked();

    /**
     * Check is global lock set by current thread.
     *
     * @return {@code true} if global lock set by current thread, else {@code false}.
     */
    boolean isGlobalLockedByCurrentThread();

}
