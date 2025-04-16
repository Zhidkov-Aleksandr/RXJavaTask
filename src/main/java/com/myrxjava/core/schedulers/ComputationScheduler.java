package com.myrxjava.core.schedulers;

import com.myrxjava.core.Scheduler;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ComputationScheduler implements Scheduler {
    private static final int THREAD_COUNT = Runtime.getRuntime().availableProcessors();
    private final ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);

    @Override
    public void execute(Runnable task) {
        executor.execute(task);
    }
}
