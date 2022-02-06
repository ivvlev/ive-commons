package com.ivvlev.util.concurrent.lock;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ConcurrentModificationException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class GlobalEntityLockerImplTests extends EntityLockerImplTests {

    protected EntityLocker<String> newEntityLocker() {
        return new GlobalEntityLockerImpl<>();
    }

    public GlobalEntityLocker<String> getEntityLocker() {
        return (GlobalEntityLocker<String>) super.getEntityLocker();
    }

    @Test
    public void isGlobalLocked_NoGlobalLock_False() {
        Assertions.assertFalse(getEntityLocker().isGlobalLocked());
    }

    @Test
    public void isGlobalLockedByCurrentThread_NoGlobalLock_False() {
        Assertions.assertFalse(getEntityLocker().isGlobalLockedByCurrentThread());
    }

    @Test
    public void isGlobalLocked_AfterLockGlobal_True() {
        getEntityLocker().lockGlobal();
        Assertions.assertTrue(getEntityLocker().isGlobalLocked());
    }

    @Test
    public void isGlobalLockedByCurrentThread_AfterLockGlobal_True() {
        getEntityLocker().lockGlobal();
        Assertions.assertTrue(getEntityLocker().isGlobalLockedByCurrentThread());
    }

    @Test
    public void isGlobalLocked_AfterUnlockGlobal_False() {
        getEntityLocker().lockGlobal();
        getEntityLocker().unlockGlobal();
        Assertions.assertFalse(getEntityLocker().isGlobalLocked());
    }

    @Test
    public void isGlobalLockedByCurrentThread_AfterUnlockGlobal_False() {
        getEntityLocker().lockGlobal();
        getEntityLocker().unlockGlobal();
        Assertions.assertFalse(getEntityLocker().isGlobalLockedByCurrentThread());
    }

    @Test
    public void isGlobalLockedByCurrentThread_PerformGlobalLockAfterLock_NoExceptions() {
        getEntityLocker().lock(KEY1);
        try {
            getEntityLocker().lockGlobal();
            try {
                Assertions.assertTrue(getEntityLocker().isGlobalLockedByCurrentThread());
            } finally {
                getEntityLocker().unlockGlobal();
            }
        } finally {
            getEntityLocker().unlock(KEY1);
        }
    }

    @Test
    public void tryLockGlobal_KeyIsLockedByAnotherThread_False() throws Exception {
        getEntityLocker().lock(KEY1);
        try {
            Future<Boolean> future1 = (getExecutorService().submit(() -> {
                return getEntityLocker().tryLockGlobal(1000, TimeUnit.MILLISECONDS);
            }));
            Assertions.assertFalse(future1.get());
        } finally {
            getEntityLocker().unlock(KEY1);
        }
    }

    @Test
    public void tryLockGlobal_GlobalLockSetByAnotherThread_False() throws Exception {
        getEntityLocker().lockGlobal();
        try {
            Future<Boolean> future1 = (getExecutorService().submit(() -> {
                return getEntityLocker().tryLockGlobal(1000, TimeUnit.MILLISECONDS);
            }));
            Assertions.assertFalse(future1.get());
        } finally {
            getEntityLocker().unlockGlobal();
        }
    }


    @Test
    public void lockGlobal_SecondThreadWaitUntilTestThreadDoGlobalUnlock_CounterChangeAfterUnlockGlobal() throws Exception {
        final AtomicLong counter = new AtomicLong(0);
        Future<?> future;
        getEntityLocker().lockGlobal();
        try {
            future = getExecutorService().submit(() -> {
                getEntityLocker().forLock(KEY1, () -> {
                    long n = counter.get();
                    if (!counter.compareAndSet(n, n + 1)) {
                        throw new ConcurrentModificationException();
                    }
                });
            });
            Assertions.assertEquals(0, counter.get());
            sleep(2000);
            Assertions.assertEquals(0, counter.get());
        } finally {
            getEntityLocker().unlockGlobal();
        }
        future.get();
        Assertions.assertEquals(1, counter.get());
    }

    @Test
    public void lockGlobal_TestThreadWaitUntilSecondThreadDoUnlock_CounterChangeAfterLockGlobal() throws Exception {
        final AtomicLong counter = new AtomicLong(0);
        Future<?> future = getExecutorService().submit(() -> {
            getEntityLocker().forLock(KEY1, () -> {
                sleep(2000);
                long n = counter.get();
                if (!counter.compareAndSet(n, n + 1)) {
                    throw new ConcurrentModificationException();
                }
            });
        });
        //wait for begin of forLock in second thread
        sleep(1000);
        Assertions.assertEquals(0, counter.get());
        //Test thread will wait hear.
        getEntityLocker().lockGlobal();
        Assertions.assertEquals(1, counter.get());
        getEntityLocker().unlockGlobal();
        future.get();
    }

    @Test
    public void lockGlobal_LockInDifferentThread_NoExceptions() throws Exception {
        final AtomicLong counter = new AtomicLong(0);
        final long iterationsCount = 10000;
        Future<?> future1 = getExecutorService().submit(() -> {
            for (int j = 0; j < iterationsCount; j++) {
                getEntityLocker().forLock(KEY1, () -> {
                    long n = counter.get();
                    if (!counter.compareAndSet(n, n + 1)) {
                        throw new ConcurrentModificationException();
                    }
                });
            }
        });
        Future<?> future2 = getExecutorService().submit(() -> {
            for (int j = 0; j < iterationsCount; j++) {
                getEntityLocker().lockGlobal();
                try {
                    long n = counter.get();
                    if (!counter.compareAndSet(n, n + 1)) {
                        throw new ConcurrentModificationException();
                    }
                } finally {
                    getEntityLocker().unlockGlobal();
                }
            }
        });
        future1.get();
        future2.get();
    }
}
