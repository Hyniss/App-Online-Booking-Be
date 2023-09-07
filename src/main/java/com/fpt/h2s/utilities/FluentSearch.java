package com.fpt.h2s.utilities;


import lombok.AllArgsConstructor;

import java.util.function.Function;
import java.util.function.Supplier;

@AllArgsConstructor
public class FluentSearch<T> {

    private final T value;

    public static <T> FluentSearch<T> start(Supplier<T> supplier) {
        return new FluentSearch<>(supplier.get());
    }

    public <R> FluentSearch<R> then(Function<T, R> function) {
        return new FluentSearch<>(function.apply(this.value));
    }

    public T get() {
        return this.value;
    }
}