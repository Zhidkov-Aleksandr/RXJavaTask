package com.myrxjava;

import com.myrxjava.core.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class Main {
    private static final CountDownLatch latch = new CountDownLatch(4);

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Basic Observable Demo ===");
        basicObservableDemo();

        System.out.println("\n=== Operators Demo ===");
        operatorsDemo();

        System.out.println("\n=== Multithreading Demo ===");
        multithreadingDemo();

        System.out.println("\n=== FlatMap Demo ===");
        flatMapDemo();

        System.out.println("\n=== Error Handling Demo ===");
        errorHandlingDemo();

        latch.await(5, TimeUnit.SECONDS);
        System.out.println("\nAll demos completed!");
    }

    private static void basicObservableDemo() {
        Observable.<String>create(observer -> {
            observer.onNext("Hello");
            observer.onNext("World");
            observer.onComplete();
        }).subscribe(new SimpleObserver<>("Basic"));
    }

    private static void operatorsDemo() {
        Observable.<Integer>create(observer -> {
                    for (int i = 1; i <= 5; i++) observer.onNext(i);
                    observer.onComplete();
                })
                .map(x -> x * 10)
                .filter(x -> x > 25)
                .subscribe(new SimpleObserver<>("Operators"));
    }

    private static void multithreadingDemo() {
        Observable.<Integer>create(observer -> {
                    printThread("Producer");
                    for (int i = 1; i <= 3; i++) observer.onNext(i);
                    observer.onComplete();
                })
                .subscribeOn(Scheduler.io())
                .observeOn(Scheduler.computation())
                .map(x -> {
                    printThread("Map");
                    return x * 100;
                })
                .subscribe(new SimpleObserver<>("Multithreading"));
    }

    private static void flatMapDemo() {
        Observable.<Integer>create(observer -> {
                    observer.onNext(1);
                    observer.onNext(2);
                    observer.onComplete();
                })
                .flatMap(x -> Observable.<Integer>create(obs -> {
                    obs.onNext(x * 10);
                    obs.onNext(x * 20);
                    obs.onComplete();
                }))
                .subscribe(new SimpleObserver<>("FlatMap"));
    }

    private static void errorHandlingDemo() {
        Observable.<Integer>create(observer -> {
                    observer.onNext(1);
                    observer.onNext(0); // Will cause error
                    observer.onNext(3);
                    observer.onComplete();
                })
                .map(x -> 10 / x)
                .subscribe(new Observer<Integer>() {
                    @Override public void onSubscribe(Disposable d) {}

                    @Override
                    public void onNext(Integer item) {
                        System.out.println("[ErrorDemo] Received: " + item);
                    }

                    @Override
                    public void onError(Throwable t) {
                        System.out.println("[ErrorDemo] Error: " + t.getMessage());
                        latch.countDown();
                    }

                    @Override
                    public void onComplete() {
                        System.out.println("[ErrorDemo] Completed");
                        latch.countDown();
                    }
                });
    }

    static class SimpleObserver<T> implements Observer<T> {
        private final String name;

        SimpleObserver(String name) {
            this.name = name;
        }

        @Override
        public void onSubscribe(Disposable d) {
            System.out.printf("[%s] Subscribed%n", name);
        }

        @Override
        public void onNext(T item) {
            printThread("Consumer");
            System.out.printf("[%s] Received: %s%n", name, item);
            latch.countDown();
        }

        @Override
        public void onError(Throwable t) {
            System.out.printf("[%s] Error: %s%n", name, t.getMessage());
            latch.countDown();
        }

        @Override
        public void onComplete() {
            System.out.printf("[%s] Completed%n", name);
            latch.countDown();
        }
    }

    private static void printThread(String operation) {
        System.out.printf("[%s] Thread: %s%n",
                operation, Thread.currentThread().getName());
    }
}