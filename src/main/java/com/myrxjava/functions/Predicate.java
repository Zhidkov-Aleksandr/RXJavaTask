package com.myrxjava.functions;

@FunctionalInterface
public interface Predicate<T> { // Добавлен параметр типа <T>
    boolean test(T t) throws Exception;
}
