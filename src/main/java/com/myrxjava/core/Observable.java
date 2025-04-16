package com.myrxjava.core;

import com.myrxjava.core.operators.FilterOperator;
import com.myrxjava.core.operators.FlatMapOperator;
import com.myrxjava.core.operators.MapOperator;
import com.myrxjava.functions.*;
import java.util.concurrent.atomic.AtomicReference;

public class Observable<T> {
    private final ObservableOnSubscribe<T> source;

    public interface ObservableOnSubscribe<T> {
        void subscribe(Observer<? super T> observer);
    }

    public Observable(ObservableOnSubscribe<T> source) {
        this.source = source;
    }

    public static <T> Observable<T> create(ObservableOnSubscribe<T> source) {
        return new Observable<>(source);
    }

    // Основные методы подписки
    public void subscribe(Observer<? super T> observer) {
        source.subscribe(observer);
    }

    public void subscribe(
            Consumer<? super T> onNext,
            Consumer<? super Throwable> onError,
            Action onComplete
    ) {
        subscribe(new Observer<T>() {
            private final AtomicReference<Disposable> disposableRef = new AtomicReference<>();

            @Override
            public void onSubscribe(Disposable d) {
                disposableRef.set(d);
            }

            @Override
            public void onNext(T item) {
                try {
                    onNext.accept(item);
                } catch (Exception e) {
                    handleError(e);
                }
            }

            @Override
            public void onError(Throwable t) {
                try {
                    onError.accept(t);
                } catch (Exception e) {
                    // Логирование ошибки обработчика
                } finally {
                    disposeIfNeeded();
                }
            }

            @Override
            public void onComplete() {
                try {
                    onComplete.run();
                } catch (Exception e) {
                    handleError(e);
                } finally {
                    disposeIfNeeded();
                }
            }

            private void handleError(Exception e) {
                try {
                    onError.accept(e);
                } catch (Exception ex) {
                    // Логирование вторичной ошибки
                }
                disposeIfNeeded();
            }

            private void disposeIfNeeded() {
                Disposable d = disposableRef.get();
                if (d != null && !d.isDisposed()) {
                    d.dispose();
                }
            }
        });
    }

    // Операторы преобразования
    public <R> Observable<R> map(Function<? super T, ? extends R> mapper) {
        return new Observable<>(new MapOperator<>(this, mapper));
    }

    public Observable<T> filter(Predicate<? super T> predicate) {
        return new Observable<>(new FilterOperator<>(this, predicate));
    }

    public <R> Observable<R> flatMap(Function<? super T, ? extends Observable<R>> mapper) {
        return new Observable<>(new FlatMapOperator<>(this, mapper));
    }

    // Управление потоками
    public Observable<T> subscribeOn(Scheduler scheduler) {
        return new Observable<>(observer ->
                scheduler.execute(() -> source.subscribe(observer))
        );
    }

    public Observable<T> observeOn(Scheduler scheduler) {
        return new Observable<>(observer ->
                source.subscribe(new Observer<T>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        observer.onSubscribe(d);
                    }

                    @Override
                    public void onNext(T item) {
                        scheduler.execute(() -> observer.onNext(item));
                    }

                    @Override
                    public void onError(Throwable t) {
                        scheduler.execute(() -> observer.onError(t));
                    }

                    @Override
                    public void onComplete() {
                        scheduler.execute(observer::onComplete);
                    }
                })
        );
    }
}