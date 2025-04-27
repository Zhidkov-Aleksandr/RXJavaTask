package com.myrxjava.core;

import com.myrxjava.functions.*;
import com.myrxjava.core.operators.*;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/** Базовый класс реактивного потока. */
public final class Observable<T> {

    /* ----------  создание  ---------- */

    public interface ObservableOnSubscribe<T> { void subscribe(Observer<? super T> observer); }

    private final ObservableOnSubscribe<T> source;
    private Observable(ObservableOnSubscribe<T> src) { this.source = src; }

    public static <T> Observable<T> create(ObservableOnSubscribe<T> src) { return new Observable<>(src); }

    /* ----------  подписка  ---------- */

    public void subscribe(Observer<? super T> downstream) {
        SafeObserver<T> safe = new SafeObserver<>(downstream);
        try {
            source.subscribe(safe);
        } catch (Throwable ex) {
            safe.onError(ex);
        }
    }

    /* удобный overload  */
    public void subscribe(Consumer<? super T> onNext,
                          Consumer<? super Throwable> onError,
                          Action onComplete) {

        subscribe(new Observer<T>() {
            final AtomicReference<Disposable> ref = new AtomicReference<>();

            @Override public void onSubscribe(Disposable d) { ref.set(d); }
            @Override public void onNext(T item)       { try { onNext.accept(item); } catch (Exception e) { onError(e); } }
            @Override public void onError(Throwable t) { try { onError.accept(t); } catch (Exception ignore) { } finally { dispose(); } }
            @Override public void onComplete()         { try { onComplete.run();  } catch (Exception ignore) { } finally { dispose(); } }

            private void dispose() {
                Disposable d = ref.getAndSet(null);
                if (d != null) d.dispose();
            }
        });
    }

    /* ----------  операторы ---------- */

    public <R> Observable<R> map(Function<? super T, ? extends R> mapper) {
        return new Observable<>(new MapOperator<>(this, mapper));
    }

    public Observable<T> filter(Predicate<? super T> p) {
        return new Observable<>(new FilterOperator<>(this, p));
    }

    public <R> Observable<R> flatMap(Function<? super T, ? extends Observable<R>> mapper) {
        return new Observable<>(new FlatMapOperator<>(this, mapper));
    }

    /* ----------  смена потоков ---------- */

    public Observable<T> subscribeOn(Scheduler scheduler) {
        return new Observable<>(observer -> scheduler.execute(() -> Observable.this.subscribe(observer)));
    }

    public Observable<T> observeOn(Scheduler scheduler) {
        return new Observable<>(observer ->
                Observable.this.subscribe(new Observer<T>() {

                    @Override public void onSubscribe(Disposable d) { observer.onSubscribe(d); }

                    @Override public void onNext(T item)   { scheduler.execute(() -> observer.onNext(item)); }
                    @Override public void onError(Throwable t) { scheduler.execute(() -> observer.onError(t)); }
                    @Override public void onComplete()     { scheduler.execute(observer::onComplete); }
                }));
    }

    /* ----------  Safe-Observer  ---------- */

    private static final class SafeObserver<T> implements Observer<T>, Disposable {

        private final Observer<? super T> actual;
        private final AtomicBoolean disposed = new AtomicBoolean(false);
        private Disposable upstream;

        SafeObserver(Observer<? super T> actual) { this.actual = actual; }

        @Override public void onSubscribe(Disposable d) {
            if (upstream != null) { d.dispose(); }              // двойная подписка
            else {
                upstream = d;
                actual.onSubscribe(this);
            }
        }

        @Override public void onNext(T item) {
            if (!isDisposed()) actual.onNext(item);
        }

        @Override public void onError(Throwable t) {
            if (disposed.compareAndSet(false, true)) {
                actual.onError(t);
                if (upstream != null) upstream.dispose();
            }
        }

        @Override public void onComplete() {
            if (disposed.compareAndSet(false, true)) {
                actual.onComplete();
                if (upstream != null) upstream.dispose();
            }
        }

        @Override public void dispose() {
            if (disposed.compareAndSet(false, true) && upstream != null) upstream.dispose();
        }

        @Override public boolean isDisposed() { return disposed.get(); }
    }
}
