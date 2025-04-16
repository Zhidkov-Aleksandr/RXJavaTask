package com.myrxjava.functions;

@FunctionalInterface
public interface Consumer<T> {
    void accept(T t) throws Exception;
}
