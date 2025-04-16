package com.myrxjava.core.operators;

import com.myrxjava.core.Observable;
import com.myrxjava.core.Observer;
import com.myrxjava.core.Disposable;
import com.myrxjava.functions.Function;

public class MapOperator<T, R> implements Observable.ObservableOnSubscribe<R> {
    private final Observable<T> source;
    private final Function<? super T, ? extends R> mapper;

    public MapOperator(Observable<T> source, Function<? super T, ? extends R> mapper) {
        this.source = source;
        this.mapper = mapper;
    }

    @Override
    public void subscribe(Observer<? super R> observer) {
        source.subscribe(new Observer<T>() {
            @Override
            public void onSubscribe(Disposable d) {
                observer.onSubscribe(d);
            }

            @Override
            public void onNext(T item) {
                try {
                    R result = mapper.apply(item);
                    observer.onNext(result);
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
