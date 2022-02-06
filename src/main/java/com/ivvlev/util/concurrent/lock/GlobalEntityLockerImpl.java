package com.ivvlev.util.concurrent.lock;

import com.ivvlev.util.concurrent.TimeoutException;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * The class is supposed to be used by the components that are responsible for managing storage and caching of different
 * type of entities in the application. EntityLocker itself does not deal with the entities, only with the IDs (primary keys) of the entities.
 * <p>
 * Requirements:
 * <p>
 * 1. EntityLocker should support different types of entity IDs.
 * <p>
 * 2. EntityLocker’s interface should allow the caller to specify which entity does it want to work with (using entity ID),
 * and designate the boundaries of the code that should have exclusive access to the entity (called “protected code”).
 * <p>
 * 3. For any given entity, EntityLocker should guarantee that at most one thread executes protected code on that entity.
 * If there’s a concurrent request to lock the same entity, the other thread should wait until the entity becomes available.
 * <p>
 * 4. EntityLocker should allow concurrent execution of protected code on different entities.
 * *
 * 5. Allow reentrant locking.
 * <p>
 * 6. Allow the caller to specify timeout for locking an entity.
 * <p>
 * 7. Implement protection from deadlocks (but not taking into account possible locks outside EntityLocker).
 * <p>
 * 8. Implement global lock. Protected code that executes under a global lock must not execute concurrently with any other protected code.
 * <p>
 * Implementation of requirement #8 require different type of synchronisation. The result of this is low
 * performance of lock/unlock cycle compare to EntityLockerImpl.
 */
public class GlobalEntityLockerImpl<K> extends EntityLockerAbst<K> implements GlobalEntityLocker<K> {
    private final Map<K, Entry<K>> keyEntryMap_ = new HashMap<>();
    private long globalLockThreadId_ = -1;
    private long globalLockCount_ = 0;

    @Override
    public boolean isGlobalLockedByCurrentThread() {
        final long currentThreadId = Thread.currentThread().getId();
        synchronized (keyEntryMap_) {
            return globalLockThreadId_ == currentThreadId;
        }
    }

    @Override
    public boolean isGlobalLocked() {
        synchronized (keyEntryMap_) {
            return globalLockThreadId_ >= 0;
        }
    }

    @Override
    public void lockGlobal() {
        tryLockGlobal(0, TimeUnit.MILLISECONDS);
    }

    @Override
    public boolean tryLockGlobal(final long timeout, final TimeUnit unit) {
        Objects.requireNonNull(unit, "unit");
        final Transaction<K> tx = getTransaction();
        try {
            final long timeoutMills = timeout == 0 ? Long.MAX_VALUE : System.currentTimeMillis() + unit.toMillis(timeout);
            synchronized (keyEntryMap_) {
                //Set a global lock immediately, if no lock set by another thread before.
                //This is necessary to prevent other threads from blocking keys after lockGlobal call.
                if (globalLockThreadId_ < 0) {
                    globalLockThreadId_ = tx.threadId;
                }
                //Iterating until other thread:
                // 1. will unlock keys
                // 2. will clear global lock
                while ((globalLockThreadId_ == tx.threadId && getEntryCount() > tx.getHoldingEntryCount())
                        || (globalLockThreadId_ != tx.threadId && globalLockCount_ > 0)) {
                    if (System.currentTimeMillis() < timeoutMills) {
                        keyEntryMap_.wait(timeout);
                    } else {
                        return false;
                    }
                }
                globalLockThreadId_ = tx.threadId;
                globalLockCount_++;
            }
        } catch (InterruptedException e) {
            removeEmptyTransaction(tx);
            throw new EntityLockerException(e);
        }
        return true;
    }

    @Override
    public void unlockGlobal() {
        synchronized (keyEntryMap_) {
            if (globalLockThreadId_ < 0) {
                throw new EntityLockerException("The global lock not set before.");
            }
            if (globalLockThreadId_ != Thread.currentThread().getId()) {
                throw new EntityLockerException("The global lock was set by another thread.");
            }
            if (--globalLockCount_ == 0) {
                globalLockThreadId_ = -1;
            }
            keyEntryMap_.notifyAll();
        }
    }

    @Override
    protected Entry<K> acquireEntry(K key, Transaction<K> tx, long timeout) {
        try {
            final long timeoutMills = timeout == 0 ? Long.MAX_VALUE : System.currentTimeMillis() + timeout;
            synchronized (keyEntryMap_) {
                while (globalLockThreadId_ >= 0 && globalLockThreadId_ != tx.threadId) {
                    if (System.currentTimeMillis() >= timeoutMills) {
                        throw new TimeoutException();
                    }
                    keyEntryMap_.wait(timeout);
                }
                return keyEntryMap_.compute(key, (k, e) -> (e != null ? e : new Entry<K>()).acquire());
            }
        } catch (InterruptedException e) {
            throw new EntityLockerException(e);
        }
    }

    @Override
    protected Entry<K> findEntry(K key) {
        synchronized (keyEntryMap_) {
            return keyEntryMap_.get(key);
        }
    }

    @Override
    protected void releaseEntry(K key) {
        synchronized (keyEntryMap_) {
            keyEntryMap_.computeIfPresent(key, (k, e) -> e.release());
            keyEntryMap_.notifyAll();
        }
    }

    @Override
    protected int getEntryCount() {
        synchronized (keyEntryMap_) {
            return keyEntryMap_.size();
        }
    }

}