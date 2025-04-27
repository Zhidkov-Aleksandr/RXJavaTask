package com.myrxjava.core.operators;

import com.myrxjava.core.*;
import com.myrxjava.functions.Predicate;

/** Реализация оператора filter. */
public final class FilterOperator<T> implements Observable.ObservableOnSubscribe<T> {

    private final Observable<T> source;
    private final Predicate<? super T> predicate;

    public FilterOperator(Observable<T> src, Predicate<? super T> p) {
        this.source = src;
        this.predicate = p;
    }

    @Override public void subscribe(Observer<? super T> observer) {
        source.subscribe(new Observer<T>() {
            @Override public void onSubscribe(Disposable d) { observer.onSubscribe(d); }
            @Override public void onNext(T item) {
                try { if (predicate.test(item)) observer.onNext(item); }
                catch (Exception ex) { observer.onError(ex); }
            }
            @Override public void onError(Throwable t) { observer.onError(t); }
            @Override public void onComplete()         { observer.onComplete(); }
        });
    }
}
