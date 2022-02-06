package com.ivvlev.util.concurrent.lock;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

@Disabled
public class EntityLockerBenchmarks {
    private final Logger logger_ = Logger.getLogger(getClass().getCanonicalName());

    private static final ExecutorService executorService_1 = Executors.newFixedThreadPool(20);
    private static final ExecutorService executorService_2 = Executors.newFixedThreadPool(20);
    private static final ExecutorService executorService_3 = Executors.newFixedThreadPool(20);
    private final EntityLocker<Integer> entityLocker = new EntityLockerImpl<>();
    private final EntityLocker<Integer> entityLocker2 = new EntityLockerImpl<>();
    private final GlobalEntityLocker<Integer> globalEntityLocker = new GlobalEntityLockerImpl<>();

    @AfterAll
    private static void afterAll() {
        executorService_1.shutdown();
        executorService_2.shutdown();
    }

    /**
     * Test run 20 threads for EntityLocker and 20 threads for GlobalEntityLocker.
     * Each thread do 1 million iterations during which performs random lock/unlock of key in interval [0..10].
     * In result shown summary time of all lock/unlock cycles.
     */
    @Test
    public void benchmark() {
        final AtomicLong counter1 = new AtomicLong(0);
        final AtomicLong counter2 = new AtomicLong(0);
        final AtomicLong counter3 = new AtomicLong(0);

        final Random random = new Random(1);

        final long threadCount = 20;
        final long iterationsCount = 2000000;

        List<Future<?>> futureList = new ArrayList<>();
        for (int i = 0; i < threadCount; i++) {
            futureList.add(executorService_1.submit(() -> {
                for (int j = 0; j < iterationsCount; j++) {
                    int key = random.nextInt(10);
                    final long beginNanos = System.nanoTime();
                    entityLocker.lock(key);
                    try {
                        doStuff();
                    } finally {
                        entityLocker.unlock(key);
                    }
                    final long endNanos = System.nanoTime();
                    long n = 0;
                    do {
                        n = counter1.get();
                    } while (!counter1.compareAndSet(n, n + (endNanos - beginNanos)));
                }
            }));
            futureList.add(executorService_2.submit(() -> {
                for (int j = 0; j < iterationsCount; j++) {
                    int key = random.nextInt(10);
                    final long beginNanos = System.nanoTime();
                    entityLocker2.forLock(key, () -> {
                        doStuff();
                    });
                    final long endNanos = System.nanoTime();
                    long n = 0;
                    do {
                        n = counter2.get();
                    } while (!counter2.compareAndSet(n, n + (endNanos - beginNanos)));
                }
            }));
            futureList.add(executorService_3.submit(() -> {
                for (int j = 0; j < iterationsCount; j++) {
                    int key = random.nextInt(10);
                    final long beginNanos = System.nanoTime();
                    globalEntityLocker.lock(key);
                    try {
                        doStuff();
                    } finally {
                        globalEntityLocker.unlock(key);
                    }
                    final long endNanos = System.nanoTime();
                    long n = 0;
                    do {
                        n = counter3.get();
                    } while (!counter3.compareAndSet(n, n + (endNanos - beginNanos)));
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

        logger_.info("EntityLocker.lock/unlock:        " + counter1.get() / 1000000 + " ms");
        logger_.info("EntityLocker.forLock:            " + counter2.get() / 1000000 + " ms");
        logger_.info("GlobalEntityLocker.lock/unlock:  " + counter3.get() / 1000000 + " ms");
    }

    private void doStuff() {
        int n = 0;
        for (int i = 0; i < 1000000; i++) {
            n++;
        }
    }

}
