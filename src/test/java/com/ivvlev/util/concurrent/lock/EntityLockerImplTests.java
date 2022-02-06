package com.ivvlev.util.concurrent.lock;

import com.ivvlev.util.concurrent.TimeoutException;
import com.ivvlev.util.function.ExProcedure1;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

public class EntityLockerImplTests {
    protected final Logger logger_ = Logger.getLogger(getClass().getCanonicalName());

    protected final String KEY1 = "key1";
    protected final String KEY2 = "key2";
    private EntityLocker<String> entityLocker_;
    private ExecutorService executorService_;

    @BeforeEach
    protected void beforeEach() {
        entityLocker_ = newEntityLocker();
        executorService_ = Executors.newFixedThreadPool(2);
    }

    @AfterEach
    protected void afterEach() throws InterruptedException {
        entityLocker_ = null;
        executorService_.shutdown();
        if (!executorService_.awaitTermination(5, TimeUnit.SECONDS)) {
            throw new RuntimeException("ExecutorService not shutdown during 5 seconds.");
        }
        executorService_ = null;
    }

    protected EntityLocker<String> newEntityLocker() {
        return new EntityLockerImpl<>();
    }

    public EntityLocker<String> getEntityLocker() {
        return entityLocker_;
    }

    public ExecutorService getExecutorService() {
        return executorService_;
    }

    protected void forExecutorService(int nTreads, ExProcedure1<ExecutorService> testProc) throws Exception {
        ExecutorService executorService = Executors.newFixedThreadPool(nTreads);
        try {
            testProc.exec(executorService);
        } finally {
            executorService.shutdown();
        }
        if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
            throw new RuntimeException("ExecutorService not shutdown during 5 seconds.");
        }
    }

    @Test
    public void lock_DoReentrantLock_NoExceptions() {
        entityLocker_.lock(KEY1);
        entityLocker_.lock(KEY1);
        entityLocker_.unlock(KEY1);
        entityLocker_.unlock(KEY1);
    }

    @Test
    public void unlock_WithoutPriorLock_ExceptionThrown() {
        Assertions.assertThrows(EntityLockerException.class, () -> entityLocker_.unlock(KEY1));
    }

    @Test
    public void isLockedByCurrentThread_BeforeLock_False() {
        Assertions.assertFalse(entityLocker_.isLockedByCurrentThread(KEY1));
    }

    @Test
    public void isLockedByCurrentThread_AfterLock_True() {
        entityLocker_.lock(KEY1);
        Assertions.assertTrue(entityLocker_.isLockedByCurrentThread(KEY1));
    }

    @Test
    public void isLockedByCurrentThread_AfterUnlock_False() {
        entityLocker_.lock(KEY1);
        entityLocker_.unlock(KEY1);
        Assertions.assertFalse(entityLocker_.isLockedByCurrentThread(KEY1));
    }

    @Test
    public void forLock_MultipleThreadsWritingToVariable_NoExceptions() throws Exception {
        final AtomicLong counter = new AtomicLong(0);
        final long threadCount = 20;
        final long iterationsCount = 100;
        final List<Future<?>> futureList = new ArrayList<>();
        forExecutorService(20, executorService20 -> {
            for (int i = 0; i < threadCount; i++) {
                futureList.add(executorService20.submit(() -> {
                    for (int j = 0; j < iterationsCount; j++) {
                        entityLocker_.forLock(KEY1, () -> {
                            long n = counter.get();
                            sleep(1);
                            if (!counter.compareAndSet(n, n + 1)) {
                                throw new ConcurrentModificationException();
                            }
                        });
                    }
                }));
            }
            futureList.forEach(x -> {
                try {
                    x.get();
                } catch (Exception e) {
                    futureList.forEach(f -> f.cancel(true));
                    throw new RuntimeException(e);
                }
            });
        });
        Assertions.assertEquals(threadCount * iterationsCount, counter.get());
    }

    @Test
    public void lock_UnlockedWritingToVariable_ConcurrentModificationExceptionThrown() {
        final AtomicLong counter = new AtomicLong(0);
        final long iterationsCount = 10000;
        Future<?> future1 = (getExecutorService().submit(() -> {
            for (int j = 0; j < iterationsCount; j++) {
                entityLocker_.lock(KEY1);
                try {
                    long n = counter.get();
                    sleep(1);
                    if (!counter.compareAndSet(n, n + 1)) {
                        throw new ConcurrentModificationException();
                    }
                } finally {
                    entityLocker_.unlock(KEY1);
                }
            }
        }));
        // Do writing without lock
        for (int i = 0; i < iterationsCount; i++) {
            counter.incrementAndGet();
            sleep(1);
            if (future1.isDone()) {
                break;
            }
        }
        ExecutionException executionException = Assertions.assertThrows(ExecutionException.class, future1::get);
        Assertions.assertTrue(executionException.getCause() instanceof ConcurrentModificationException);
    }

    @Test
    public void tryLock_KeyIsLockedByAnotherThread_ReturnFalse() throws Exception {
        entityLocker_.lock(KEY1);
        try {
            Future<Boolean> future1 = getExecutorService().submit(() -> {
                return entityLocker_.tryLock(KEY1, 1000, TimeUnit.MILLISECONDS);
            });
            Assertions.assertFalse(future1.get());
        } finally {
            entityLocker_.unlock(KEY1);
        }
    }

    @Test
    public void unlock_KeyNotLocked_EntityLockerExceptionThrown() {
        Assertions.assertThrows(EntityLockerException.class, () -> entityLocker_.unlock(KEY1));
    }

    @Test
    public void lock_DeadlockPresent_DeadlockExceptionThrown() throws Exception {
        Future<?> future1 = getExecutorService().submit(() -> {
            entityLocker_.lock(KEY1);
            try {
                sleep(200);
                entityLocker_.lock(KEY2);
                try {
                    sleep(1000);
                } finally {
                    entityLocker_.unlock(KEY2);
                }
            } finally {
                entityLocker_.unlock(KEY1);
            }
        });
        Future<?> future2 = getExecutorService().submit(() -> {
            entityLocker_.lock(KEY2);
            try {
                sleep(500);
                entityLocker_.lock(KEY1);
            } finally {
                entityLocker_.unlock(KEY2);
            }
        });
        future1.get();
        ExecutionException executionException = Assertions.assertThrows(ExecutionException.class, future2::get);
        Assertions.assertTrue(executionException.getCause() instanceof DeadlockException);
    }


    @Test
    public void forLock_ReentryLock_NoExceptions() {
        entityLocker_.forLock(KEY1, () -> {
            doStuff(KEY1);
            sleep(200);
            entityLocker_.forLock(KEY1, () -> {
                doStuff(KEY1);
                sleep(1000);
            });
        });
    }

    @Test
    public void forLock_LockTwoDifferKeys_NoExceptions() {
        entityLocker_.forLock(KEY1, () -> {
            doStuff(KEY1);
            sleep(200);
            entityLocker_.forLock(KEY2, () -> {
                doStuff(KEY2);
                sleep(1000);
            });
        });
    }

    @Test
    public void forLock_DeadlockPresent_DeadlockExceptionThrown() throws Exception {
        Future<?> future1 = getExecutorService().submit(() -> {
            entityLocker_.forLock(KEY1, () -> {
                doStuff(KEY1);
                sleep(200);
                entityLocker_.forLock(KEY2, () -> {
                    doStuff(KEY2);
                    sleep(1000);
                });
            });
        });
        Future<?> future2 = getExecutorService().submit(() -> {
            entityLocker_.forLock(KEY2, () -> {
                doStuff(KEY2);
                sleep(500);
                entityLocker_.forLock(KEY1, () -> {
                    doStuff(KEY1);
                });
            });
        });
        future1.get();
        ExecutionException executionException = Assertions.assertThrows(ExecutionException.class, future2::get);
        Assertions.assertTrue(executionException.getCause() instanceof DeadlockException);
    }

    @Test
    public void forLock_WithTimeout_TimeoutExceptionThrown() throws Exception {
        Future<?> future1 = getExecutorService().submit(() -> {
            entityLocker_.forLock(KEY1, () -> {
                doStuff(KEY1);
                sleep(2000);
            });
        });
        Future<?> future2 = getExecutorService().submit(() -> {
            sleep(500);
            entityLocker_.forLock(KEY1, () -> doStuff(KEY1), 200, TimeUnit.MILLISECONDS);
        });
        future1.get();
        ExecutionException executionException = Assertions.assertThrows(ExecutionException.class, future2::get);
        Assertions.assertThrows(TimeoutException.class, () -> {
            throw executionException.getCause();
        });
    }

    protected void sleep(long mills) {
        try {
            Thread.sleep(mills);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    protected void doStuff(String key) {
        //logger_.info(String.format("Thread %s work with entity key %s", Thread.currentThread().getId(), key));
        int n = 0;
        for (int i = 0; i < 1000; i++) {
            n++;
        }
    }
}
