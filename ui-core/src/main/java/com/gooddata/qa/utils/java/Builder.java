package com.gooddata.qa.utils.java;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class Builder<T> {

    private Supplier<T> instantiator;
    private List<Consumer<T>> instanceModifiers = new ArrayList<>();

    public Builder(Supplier<T> instantiator) {
        this.instantiator = instantiator;
    }

    public static <T> Builder<T> of(Supplier<T> instantiator) {
        return new Builder<>(instantiator);
    }

    public Builder<T> with(Consumer<T> consumer) {
        instanceModifiers.add(consumer);
        return this;
    }

    public T build() {
        T instance = instantiator.get();
        instanceModifiers.forEach(consumer -> consumer.accept(instance));
        return instance;
    }
}
