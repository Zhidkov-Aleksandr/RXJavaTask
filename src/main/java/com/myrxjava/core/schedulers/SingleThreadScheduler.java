package com.myrxjava.core.schedulers;

import com.myrxjava.core.Scheduler;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SingleThreadScheduler implements Scheduler {
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    public void execute(Runnable task) {
        executor.execute(task);
    }
}
