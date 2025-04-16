package com.myrxjava.core.operators;

import com.myrxjava.core.Observable;
import com.myrxjava.core.Observer;
import com.myrxjava.core.Disposable;
import com.myrxjava.functions.Function;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class FlatMapOperator<T, R> implements Observable.ObservableOnSubscribe<R> {
    private final Observable<T> source;
    private final Function<? super T, ? extends Observable<R>> mapper;
    private final AtomicInteger activeObservables = new AtomicInteger(1);

    public FlatMapOperator(Observable<T> source, Function<? super T, ? extends Observable<R>> mapper) {
        this.source = source;
        this.mapper = mapper;
    }

    @Override
    public void subscribe(Observer<? super R> observer) {
        source.subscribe(new Observer<T>() {
            private volatile boolean done;
            private CompositeDisposable composite = new CompositeDisposable();

            @Override
            public void onSubscribe(Disposable d) {
                composite.add(d);
                observer.onSubscribe(composite);
            }

            @Override
            public void onNext(T item) {
                activeObservables.incrementAndGet();
                try {
                    Observable<R> observable = mapper.apply(item);
                    observable.subscribe(
                            new Observer<R>() {
                                @Override
                                public void onSubscribe(Disposable d) {
                                    composite.add(d);
                                }

                                @Override
                                public void onNext(R rItem) {
                                    observer.onNext(rItem);
                                }

                                @Override
                                public void onError(Throwable t) {
                                    observer.onError(t);
                                }

                                @Override
                                public void onComplete() {
                                    if (activeObservables.decrementAndGet() == 0 && done) {
                                        observer.onComplete();
                                    }
                                }
                            }
                    );
                } catch (Exception e) {
                    observer.onError(e);
                }
            }

            @Override
            public void onError(Throwable t) {
                done = true;
                observer.onError(t);
            }

            @Override
            public void onComplete() {
                done = true;
                if (activeObservables.decrementAndGet() == 0) {
                    observer.onComplete();
                }
            }
        });
    }

    static class CompositeDisposable implements Disposable {
        private volatile boolean disposed;
        private List<Disposable> disposables = new ArrayList<>();

        void add(Disposable d) {
            if (!disposed) {
                disposables.add(d);
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
}