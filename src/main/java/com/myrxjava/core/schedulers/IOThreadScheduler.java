package com.myrxjava.core.schedulers;

import com.myrxjava.core.Scheduler;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/** Аналог Schedulers.io() — CachedThreadPool c читаемыми именами потоков. */
public final class IOThreadScheduler implements Scheduler {

    private static final AtomicInteger IDX = new AtomicInteger();

    private final ExecutorService executor = Executors.newCachedThreadPool(
            r -> new Thread(r, "IOThread-" + IDX.incrementAndGet())
    );

    @Override public void execute(Runnable task) { executor.execute(task); }

    /** Удобно в тестах / при завершении JVM. */
    public void shutdown() { executor.shutdownNow(); }
}
