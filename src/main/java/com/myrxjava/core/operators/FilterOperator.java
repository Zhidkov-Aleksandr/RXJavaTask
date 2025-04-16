package com.myrxjava.core.operators;

import com.myrxjava.core.Observable;
import com.myrxjava.core.Observer;
import com.myrxjava.core.Disposable;
import com.myrxjava.functions.Predicate;

public class FilterOperator<T> implements Observable.ObservableOnSubscribe<T> {
    private final Observable<T> source;
    private final Predicate<? super T> predicate;

    public FilterOperator(Observable<T> source, Predicate<? super T> predicate) {
        this.source = source;
        this.predicate = predicate;
    }

    @Override
    public void subscribe(Observer<? super T> observer) {
        source.subscribe(new Observer<T>() {
            @Override
            public void onSubscribe(Disposable d) {
                observer.onSubscribe(d);
            }

            @Override
            public void onNext(T item) {
                try {
                    if (predicate.test(item)) {
                        observer.onNext(item);
                    }
                } catch (Exception e) {
                    observer.onError(e);
                }
            }

            @Override
            public void onError(Throwable t) {
                observer.onError(t);
            }

            @Override
            public void onComplete() {
                observer.onComplete();
            }
        });
    }
}
