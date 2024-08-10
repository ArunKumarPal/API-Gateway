package com.arun.sample.gateway.model;

public record Pair<T1, T2>(T1 t1, T2 t2) {
    public static <I1, I2> Pair<I1, I2> of(I1 t1, I2 t2) {
        return new Pair<>(t1, t2);
    }
}
