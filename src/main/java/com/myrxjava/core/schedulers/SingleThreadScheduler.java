package com.myrxjava.core.schedulers;

import com.myrxjava.core.Scheduler;

import java.util.concurrent.*;

public final class SingleThreadScheduler implements Scheduler {

    private final ExecutorService pool = Executors.newSingleThreadExecutor(
            r -> new Thread(r, "SingleThread")
    );

    @Override public void execute(Runnable task) { pool.execute(task); }

    public void shutdown() { pool.shutdownNow(); }
}
