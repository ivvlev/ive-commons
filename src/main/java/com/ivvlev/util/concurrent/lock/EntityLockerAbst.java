package com.ivvlev.util.concurrent.lock;

import com.ivvlev.util.concurrent.TimeoutException;
import com.ivvlev.util.function.Procedure;

import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BiFunction;
import java.util.function.Supplier;

/**
 * Abstract implementation of EntityLocker interface. Implements common methods.
 */
abstract class EntityLockerAbst<K> implements EntityLocker<K> {
    private final Map<Long, Transaction<K>> threadTxMap_ = new ConcurrentHashMap<>();

    @Override
    public void lock(K key) {
        doLock(key, 0, TimeUnit.MILLISECONDS);
    }

    @Override
    public boolean tryLock(K key, long timeout, TimeUnit unit) {
        try {
            doLock(key, timeout, unit);
        } catch (TimeoutException e) {
            return false;
        }
        return true;
    }

    @Override
    public void unlock(K key) {
        Objects.requireNonNull(key, "key");
        final Entry<K> entry = findEntry(key);
        if (entry == null) {
            throw new EntityLockerException(String.format("Key '%s' doesn't locked", key));
        }
        final Transaction<K> tx = getTransaction();
        if (!tx.isHoldingEntry(entry)) {
            removeEmptyTransaction(tx);
            throw new EntityLockerException(String.format("Key '%s' doesn't locked by thread '%s'", key, tx.threadId));
        }
        doUnlock(tx, key, entry);
    }

    @Override
    public boolean isLockedByCurrentThread(K key) {
        Entry<K> entry = findEntry(key);
        return entry != null && entry.lock.isHeldByCurrentThread();
    }

    @Override
    public <R> R forLock(K key, Supplier<R> protectedMethod) {
        return doForLock(key, protectedMethod, 0, TimeUnit.MILLISECONDS);
    }

    @Override
    public void forLock(K key, Procedure protectedMethod) {
        forLock(key, () -> {
            protectedMethod.exec();
            return null;
        });
    }

    @Override
    public <R> R forLock(K key, Supplier<R> protectedMethod, long timeout, TimeUnit unit) {
        return doForLock(key, protectedMethod, timeout, unit);
    }

    @Override
    public void forLock(K key, Procedure protectedMethod, long timeout, TimeUnit unit) {
        forLock(key, () -> {
            protectedMethod.exec();
            return null;
        }, timeout, unit);
    }

    /**
     * Little faster than
     * <pre>
     *     entityLocker.lock("key");
     *     try {
     *         // do some work
     *     } finally {
     *         entityLocker.unlock("key");
     *     }
     * </pre>
     * because does not contain additional checks and accesses to synchronized resources.
     */
    private <R> R doForLock(K key, Supplier<R> protectedMethod, long timeout, TimeUnit unit) {
        Objects.requireNonNull(protectedMethod, "protectedMethod");
        final Entry<K> entry = doLock(key, timeout, unit);
        try {
            return protectedMethod.get();
        } finally {
            doUnlock(entry.getHoldTx(), key, entry);
        }
    }

    private Entry<K> doLock(K key, long timeout, TimeUnit unit) {
        Objects.requireNonNull(key, "key");
        final Transaction<K> tx = getTransaction();
        try {
            final Entry<K> entry = acquireEntry(key, tx, unit.toMillis(timeout));
            try {
                try {
                    tx.lock(entry, timeout, unit);
                    return entry;
                } catch (DeadlockException e) {
                    throw new DeadlockException(String.format("Deadlock detected for key '%s'", key), e);
                }
            } catch (Exception e) {
                releaseEntry(key);
                throw e;
            }
        } catch (Exception e) {
            removeEmptyTransaction(tx);
            throw e;
        }
    }

    private void doUnlock(Transaction<K> tx, K key, Entry<K> entry) {
        try {
            tx.unlock(entry);
            releaseEntry(key);
        } finally {
            removeEmptyTransaction(tx);
        }
    }

    protected final Transaction<K> getTransaction() {
        return threadTxMap_.computeIfAbsent(Thread.currentThread().getId(), Transaction::new);
    }

    protected final void removeEmptyTransaction(Transaction<K> tx) {
        if (tx.getHoldingEntryCount() == 0) {
            threadTxMap_.remove(tx.threadId);
        }
    }

    protected abstract Entry<K> acquireEntry(K key, Transaction<K> tx, long timeout);

    protected abstract Entry<K> findEntry(K key);

    protected abstract void releaseEntry(K key);

    protected abstract int getEntryCount();

    /**
     * Key entry. Wraps ReentrantLock and
     */
    protected static final class Entry<K> {
        /**
         * Counts a number of taking Entry-instance from key-map.
         * Field not volatile because changed only in {@link Map#compute(Object, BiFunction)} synchronized method.
         */
        private long usageCounter_ = 0;
        /**
         * Only one transaction can lock {@link Entry}.
         * One transaction can lock key multiple times.
         */
        private Transaction<K> holdTx_ = null;
        /**
         * Counts the number of locks.
         */
        private long holdCounter_ = 0;
        /**
         * ReentrantLock object
         */
        public final ReentrantLock lock = new ReentrantLock();

        /**
         * Increment a usage counter.
         */
        public Entry<K> acquire() {
            usageCounter_++;
            return this;
        }

        /**
         * Decrement a usage counter.
         *
         * @return this, if counter great then zero. null, if counter equal zero.
         * This made for optimization of using in {@link Map#compute(Object, BiFunction)} method.
         */
        public Entry<K> release() {
            return --usageCounter_ > 0 ? this : null;
        }

        /**
         * Method set reference to transaction which lock the key.
         *
         * @param tx Transaction
         */
        public synchronized void hold(Transaction<K> tx) {
            if (holdTx_ != null && holdTx_ != tx) {
                throw new EntityLockerException("Entry has locked by thread" + holdTx_.threadId);
            }
            holdTx_ = tx;
            ++holdCounter_;
        }

        /**
         * Method unset reference to transaction which lock the key.
         */
        public synchronized boolean unhold() {
            long n = --holdCounter_;
            if (n == 0) {
                holdTx_ = null;
            }
            return n == 0;
        }

        /**
         * Method return the transaction witch has lock the key.
         *
         * @return Transaction, has locked the key.
         */
        public synchronized Transaction<K> getHoldTx() {
            return holdTx_;
        }
    }

    /**
     * One thread can own one transaction.
     */
    protected static final class Transaction<K> {
        /**
         * Map store keys and how many times key was locked by transaction.
         */
        private final Set<Entry<K>> lockedEntries_ = new HashSet<>();
        /**
         * Thread identifier
         */
        public final long threadId;
        /**
         * The key that the thread is waiting to lock. Need for deadlock detection.
         */
        public volatile Entry<K> waitingEntry = null;

        public Transaction(long threadId) {
            this.threadId = threadId;
        }

        /**
         * Method locks the key.
         *
         * @param entry Key entry.
         */
        public void lock(Entry<K> entry, long timeout, TimeUnit unit) {
            this.waitingEntry = entry;
            try {
                this.doLock(entry, timeout, unit);
                synchronized (lockedEntries_) {
                    entry.hold(this);
                    lockedEntries_.add(entry);
                }
            } finally {
                this.waitingEntry = null;
            }
        }

        /**
         * Method unlocks the key.
         *
         * @param entry Key entry
         */
        public void unlock(Entry<K> entry) {
            synchronized (lockedEntries_) {
                if (entry.unhold()) {
                    lockedEntries_.remove(entry);
                }
            }
            entry.lock.unlock();
        }

        /**
         * Blocking method with waiting timeout.
         *
         * @param entry   Locking entry
         * @param timeout the time to wait for the lock
         * @param unit    the time unit of the timeout argument
         */
        private void doLock(Entry<K> entry, long timeout, TimeUnit unit) {
            //Are sticking an optimistic strategy of locking
            try {
                if (!entry.lock.tryLock()) {
                    //Trying to detect deadlock if lock is not success.
                    detectDeadlock(this, entry);
                    if (timeout == 0) {
                        entry.lock.lock();
                    } else if (!entry.lock.tryLock(timeout, unit)) {
                        throw new TimeoutException();
                    }
                }
            } catch (InterruptedException e) {
                throw new EntityLockerException(e);
            }
        }

        /**
         * Method detect possible deadlock of two threads.
         * Deadlock it is:<br>
         * T1: K1 -> K2(wait)<br>
         * T2: K2 -> K1(deadlock)<br>
         * Transaction T1 lock key K1.<br>
         * Transaction T2 lock key K2.<br>
         * Transaction T1 try lock key K2 and enter to wait.<br>
         * If transaction T2 try lock key K1, it will enter to wait also.<br>
         * This state of two threads named deadlock.
         *
         * @param tx    Transaction, which will try to lock key entry.
         * @param entry Key entry which will locked.
         */
        private void detectDeadlock(Transaction<K> tx, Entry<K> entry) {
            final Transaction<K> holdTx = entry.getHoldTx();
            if (holdTx != null && holdTx != tx && holdTx.isHoldingEntry(entry) && tx.isHoldingEntry(holdTx.waitingEntry)) {
                throw new DeadlockException(String.format("Thread %s try to lock key which locked by thread %s",
                        tx.threadId, holdTx.threadId));
            }
        }

        /**
         * Method check is key was locked by transaction/tread.
         *
         * @param entry Key entry.
         * @return true, if key was locked.
         */
        public boolean isHoldingEntry(Entry<K> entry) {
            if (entry == null) {
                return false;
            }
            synchronized (lockedEntries_) {
                return lockedEntries_.contains(entry);
            }
        }

        /**
         * Method return locked key count
         *
         * @return locked key count
         */
        public int getHoldingEntryCount() {
            synchronized (lockedEntries_) {
                return lockedEntries_.size();
            }
        }
    }
}