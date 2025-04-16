package com.myrxjava;

import com.myrxjava.core.Disposable;
import com.myrxjava.core.Observable;
import com.myrxjava.core.Observer;
import com.myrxjava.core.Scheduler;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class ObservableTest {
    private static final int TEST_TIMEOUT = 2;

    @Test
    void shouldEmitItemsAndComplete() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(3);
        List<Integer> results = new ArrayList<>();

        Observable.create((Observer<Integer> observer) -> {
            observer.onNext(1);
            observer.onNext(2);
            observer.onComplete();
        }).subscribe(new Observer<Integer>() {
            @Override
            public void onSubscribe(Disposable d) {}

            @Override
            public void onNext(Integer item) {
                results.add(item);
                latch.countDown();
            }

            @Override
            public void onError(Throwable t) {
                fail("Unexpected error");
            }

            @Override
            public void onComplete() {
                latch.countDown();
            }
        });

        assertTrue(latch.await(TEST_TIMEOUT, TimeUnit.SECONDS));
        assertEquals(Arrays.asList(1, 2), results);
    }

    @Test
    void mapOperatorShouldTransformItems() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(3);
        List<Integer> results = new ArrayList<>();

        Observable.create((Observer<Integer> observer) -> {
                    observer.onNext(1);
                    observer.onNext(2);
                    observer.onComplete();
                })
                .map(x -> x * 10)
                .subscribe(
                        new Observer<Integer>() {
                            @Override
                            public void onSubscribe(Disposable d) {}

                            @Override
                            public void onNext(Integer item) {
                                results.add(item);
                            }

                            @Override
                            public void onError(Throwable t) {
                                fail("Unexpected error");
                            }

                            @Override
                            public void onComplete() {
                                latch.countDown();
                            }
                        }
                );

        assertTrue(latch.await(TEST_TIMEOUT, TimeUnit.SECONDS));
        assertEquals(Arrays.asList(10, 20), results);
    }

    @Test
    void filterOperatorShouldSkipItems() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(2);
        List<Integer> results = new ArrayList<>();

        Observable.create((Observer<Integer> observer) -> {
                    observer.onNext(10);
                    observer.onNext(5);
                    observer.onNext(20);
                    observer.onComplete();
                })
                .filter(x -> x > 10)
                .subscribe(
                        new Observer<Integer>() {
                            @Override
                            public void onSubscribe(Disposable d) {}

                            @Override
                            public void onNext(Integer item) {
                                results.add(item);
                            }

                            @Override
                            public void onError(Throwable t) {
                                fail("Unexpected error");
                            }

                            @Override
                            public void onComplete() {
                                latch.countDown();
                            }
                        }
                );

        assertTrue(latch.await(TEST_TIMEOUT, TimeUnit.SECONDS));
        assertEquals(Arrays.asList(20), results);
    }

    @Test
    void subscribeOnShouldChangeThread() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicBoolean isIOThread = new AtomicBoolean(false);

        Observable.create((Observer<String> observer) -> {
                    isIOThread.set(Thread.currentThread().getName().startsWith("IOThread"));
                    observer.onComplete();
                })
                .subscribeOn(Scheduler.io())
                .subscribe(
                        new Observer<String>() {
                            @Override
                            public void onSubscribe(Disposable d) {}

                            @Override
                            public void onNext(String item) {}

                            @Override
                            public void onError(Throwable t) {
                                fail("Unexpected error");
                            }

                            @Override
                            public void onComplete() {
                                latch.countDown();
                            }
                        }
                );

        assertTrue(latch.await(TEST_TIMEOUT, TimeUnit.SECONDS));
        assertTrue(isIOThread.get());
    }

    @Test
    void flatMapShouldMergeObservables() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(5);
        List<Integer> results = new ArrayList<>();

        Observable.create((Observer<Integer> observer) -> {
                    observer.onNext(1);
                    observer.onNext(2);
                    observer.onComplete();
                })
                .flatMap(x -> Observable.create((Observer<Integer> obs) -> {
                    obs.onNext(x * 10);
                    obs.onNext(x * 20);
                    obs.onComplete();
                }))
                .subscribe(
                        new Observer<Integer>() {
                            @Override
                            public void onSubscribe(Disposable d) {}

                            @Override
                            public void onNext(Integer item) {
                                results.add(item);
                            }

                            @Override
                            public void onError(Throwable t) {
                                fail("Unexpected error");
                            }

                            @Override
                            public void onComplete() {
                                latch.countDown();
                            }
                        }
                );

        assertTrue(latch.await(TEST_TIMEOUT, TimeUnit.SECONDS));
        assertTrue(results.containsAll(Arrays.asList(10, 20, 20, 40)));
    }

    @Test
    void shouldPropagateErrors() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicBoolean errorReceived = new AtomicBoolean(false);

        Observable.create((Observer<Integer> observer) -> {
                    observer.onNext(1);
                    observer.onNext(0);
                    observer.onComplete();
                })
                .map(x -> 10 / x)
                .subscribe(
                        new Observer<Integer>() {
                            @Override
                            public void onSubscribe(Disposable d) {}

                            @Override
                            public void onNext(Integer item) {}

                            @Override
                            public void onError(Throwable t) {
                                errorReceived.set(true);
                                latch.countDown();
                            }

                            @Override
                            public void onComplete() {
                                fail("Should not complete");
                            }
                        }
                );

        assertTrue(latch.await(TEST_TIMEOUT, TimeUnit.SECONDS));
        assertTrue(errorReceived.get());
    }

    @Test
    void disposableShouldStopEmission() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicInteger receivedItems = new AtomicInteger(0);
        AtomicBoolean isDisposed = new AtomicBoolean(false);

        Disposable[] disposable = new Disposable[1];

        Observable.create((Observer<Integer> observer) -> {
                    disposable[0] = new Disposable() {
                        private volatile boolean disposed;

                        @Override
                        public void dispose() {
                            disposed = true;
                        }

                        @Override
                        public boolean isDisposed() {
                            return disposed;
                        }
                    };

                    for (int i = 0; i < 100; i++) {
                        if (disposable[0].isDisposed()) break;
                        observer.onNext(i);
                    }
                    observer.onComplete();
                })
                .subscribe(
                        new Observer<Integer>() {
                            @Override
                            public void onSubscribe(Disposable d) {}

                            @Override
                            public void onNext(Integer item) {
                                receivedItems.incrementAndGet();
                                if (receivedItems.get() == 5) {
                                    disposable[0].dispose();
                                    isDisposed.set(true);
                                    latch.countDown();
                                }
                            }

                            @Override
                            public void onError(Throwable t) {
                                fail("Unexpected error");
                            }

                            @Override
                            public void onComplete() {
                                fail("Should not complete");
                            }
                        }
                );

        assertTrue(latch.await(TEST_TIMEOUT, TimeUnit.SECONDS));
        assertTrue(isDisposed.get());
        assertEquals(5, receivedItems.get());
    }
}