package edu.neu.ccs.prl.zeugma.internal.heritability;

import java.util.Objects;

public final class Pair<U, V> {
    private final U first;
    private final V second;

    public Pair(U first, V second) {
        this.first = first;
        this.second = second;
    }

    public U getFirst() {
        return first;
    }

    public V getSecond() {
        return second;
    }

    @Override
    public int hashCode() {
        int result = first != null ? first.hashCode() : 0;
        result = 31 * result + (second != null ? second.hashCode() : 0);
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof Pair)) {
            return false;
        }
        Pair<?, ?> pair = (Pair<?, ?>) o;
        if (!Objects.equals(first, pair.first)) {
            return false;
        }
        return Objects.equals(second, pair.second);
    }

    @Override
    public String toString() {
        return "(" + first + ", " + second + ")";
    }
}