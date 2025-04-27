package com.myrxjava.core;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

/** Группа Disposable-ов с потокобезопасным shutdown. */
public final class CompositeDisposable implements Disposable {

    private final CopyOnWriteArrayList<Disposable> list = new CopyOnWriteArrayList<>();
    private final AtomicBoolean disposed = new AtomicBoolean(false);

    public void add(Disposable d) {
        if (d == null) return;
        if (!disposed.get()) {
            list.add(d);
            if (disposed.get()) {           // double-check после добавления
                d.dispose();
            }
        } else {
            d.dispose();
        }
    }

    @Override public void dispose() {
        if (disposed.compareAndSet(false, true)) {
            list.forEach(Disposable::dispose);
            list.clear();
        }
    }

    @Override public boolean isDisposed() {
        return disposed.get();
    }
}
