package com.myrxjava.core.operators;

import com.myrxjava.core.*;
import com.myrxjava.functions.Function;

import java.util.concurrent.atomic.AtomicInteger;

/** Реализация flatMap без back-pressure, но с корректным dispose. */
public final class FlatMapOperator<T, R> implements Observable.ObservableOnSubscribe<R> {

    private final Observable<T> source;
    private final Function<? super T, ? extends Observable<R>> mapper;

    public FlatMapOperator(Observable<T> src, Function<? super T, ? extends Observable<R>> mapper) {
        this.source = src;
        this.mapper = mapper;
    }

    @Override public void subscribe(Observer<? super R> downstream) {

        CompositeDisposable composite = new CompositeDisposable();
        AtomicInteger wip = new AtomicInteger(1);          // «active» счётчик

        downstream.onSubscribe(composite);

        source.subscribe(new Observer<T>() {

            @Override public void onSubscribe(Disposable d) { composite.add(d); }

            @Override public void onNext(T item) {
                if (composite.isDisposed()) return;
                Observable<R> inner;
                try { inner = mapper.apply(item); }
                catch (Exception ex) { downstream.onError(ex); return; }

                wip.incrementAndGet();
                inner.subscribe(new Observer<R>() {
                    @Override public void onSubscribe(Disposable d) { composite.add(d); }

                    @Override public void onNext(R r) { downstream.onNext(r); }

                    @Override public void onError(Throwable t) {
                        composite.dispose();
                        downstream.onError(t);
                    }

                    @Override public void onComplete() {
                        if (wip.decrementAndGet() == 0 && !composite.isDisposed()) {
                            downstream.onComplete();
                        }
                    }
                });
            }

            @Override public void onError(Throwable t) {
                composite.dispose();
                downstream.onError(t);
            }

            @Override public void onComplete() {
                if (wip.decrementAndGet() == 0 && !composite.isDisposed()) {
                    downstream.onComplete();
                }
            }
        });
    }
}
