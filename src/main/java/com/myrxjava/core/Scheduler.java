package com.myrxjava.core;

import com.myrxjava.core.schedulers.ComputationScheduler;
import com.myrxjava.core.schedulers.IOThreadScheduler;
import com.myrxjava.core.schedulers.SingleThreadScheduler;

import java.util.concurrent.Executor;

public interface Scheduler {
    void execute(Runnable task);

    static Scheduler io() {
        return (Scheduler) new IOThreadScheduler();
    }

    static Scheduler computation() {
        return (Scheduler) new ComputationScheduler();
    }

    static Scheduler single() {
        return (Scheduler) new SingleThreadScheduler();
    }
}
