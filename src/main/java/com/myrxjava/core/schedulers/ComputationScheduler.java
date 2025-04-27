package com.myrxjava.core.schedulers;

import com.myrxjava.core.Scheduler;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/** Аналог Schedulers.computation() — FixedThreadPool по числу ядер. */
public final class ComputationScheduler implements Scheduler {

    private static final int SIZE = Runtime.getRuntime().availableProcessors();
    private static final AtomicInteger IDX = new AtomicInteger();

    private final ExecutorService pool = Executors.newFixedThreadPool(
            SIZE,
            r -> new Thread(r, "ComputationThread-" + IDX.incrementAndGet())
    );

    @Override public void execute(Runnable task) { pool.execute(task); }

    public void shutdown() { pool.shutdownNow(); }
}
