package com.myrxjava.core;

/** Контракт отмены подписки. */
public interface Disposable {
    void dispose();
    boolean isDisposed();
}