package com.myrxjava.core.operators;

import com.myrxjava.core.*;
import com.myrxjava.functions.Function;

/** Реализация оператора map. */
public final class MapOperator<T, R> implements Observable.ObservableOnSubscribe<R> {

    private final Observable<T> source;
    private final Function<? super T, ? extends R> mapper;

    public MapOperator(Observable<T> src, Function<? super T, ? extends R> mapper) {
        this.source  = src;
        this.mapper  = mapper;
    }

    @Override public void subscribe(Observer<? super R> observer) {
        source.subscribe(new Observer<T>() {
            @Override public void onSubscribe(Disposable d) { observer.onSubscribe(d); }
            @Override public void onNext(T item) {
                try { observer.onNext(mapper.apply(item)); }
                catch (Exception ex) { observer.onError(ex); }
            }
            @Override public void onError(Throwable t)   { observer.onError(t); }
            @Override public void onComplete()           { observer.onComplete(); }
        });
    }
}
