package com.myrxjava.core.schedulers;

import com.myrxjava.core.Scheduler;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/** Аналог Schedulers.io() — кэширующий пул потоков. */
public final class IOThreadScheduler implements Scheduler {

    private static final AtomicInteger IDX = new AtomicInteger();

    /** Потоки будут называться IOThread-N, что удовлетворяет junit-тесту. */
    private final ExecutorService pool = Executors.newCachedThreadPool(
            r -> new Thread(r, "IOThread-" + IDX.incrementAndGet())
    );

    @Override public void execute(Runnable task) { pool.execute(task); }

    /** Метод удобен в юнит-тестах для корректного завершения пула. */
    public void shutdown() { pool.shutdownNow(); }
}
