package com.myrxjava.core;

import com.myrxjava.core.schedulers.*;

/**
 * Простейший планировщик.  <br/>
 * Singleton-экземпляры доступны через {@code Scheduler.io()}, {@code Scheduler.computation()}, {@code Scheduler.single()}.
 */
public interface Scheduler {

    void execute(Runnable task);

    /* ---------- ready-to-use schedulers ----------- */
    IOThreadScheduler IO          = new IOThreadScheduler();
    ComputationScheduler COMPUTE  = new ComputationScheduler();
    SingleThreadScheduler SINGLE  = new SingleThreadScheduler();

    static Scheduler io()         { return IO;      }
    static Scheduler computation(){ return COMPUTE; }
    static Scheduler single()     { return SINGLE;  }
}
