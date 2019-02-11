package com.liph.chatterade.common;

import java.util.Objects;


public class Pair<A, B> {

    private final A first;
    private final B second;


    public static <X, Y> Pair<X, Y> of(X first, Y second) {
        return new Pair<X, Y>(first, second);
    }


    public Pair(A first, B second) {
        this.first = first;
        this.second = second;
    }


    public A getFirst() {
        return first;
    }

    public B getSecond() {
        return second;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pair<?, ?> pair = (Pair<?, ?>) o;
        return Objects.equals(first, pair.first) &&
                Objects.equals(second, pair.second);
    }

    @Override
    public int hashCode() {
        return Objects.hash(first, second);
    }

    @Override
    public String toString() {
        return "Pair{" +
                "first=" + first +
                ", second=" + second +
                '}';
    }
}
