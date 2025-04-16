package com.myrxjava.core;

import java.util.ArrayList;
import java.util.List;

public class CompositeDisposable implements Disposable {
    private volatile boolean disposed;
    private List<Disposable> disposables = new ArrayList<>();

    public void add(Disposable disposable) {
        if (!disposed) {
            disposables.add(disposable);
        }
    }

    @Override
    public void dispose() {
        disposed = true;
        disposables.forEach(Disposable::dispose);
    }

    @Override
    public boolean isDisposed() {
        return disposed;
    }
}