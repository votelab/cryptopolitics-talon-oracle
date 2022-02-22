package io.inblocks.civicpower.cryptopolitics.models;

import io.micronaut.core.annotation.Introspected;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.List;

@Introspected
public record Weighted<T>(@NotNull T thing, @Min(0) int weight) {

    static final long LONG_MASK = 0xffffffffL;

    public static <T> T select(List<Weighted<T>> list, long seed) {
        int total = 0;
        int[] rollingSumOfCount = new int[list.size()];
        for (int i = 0; i < list.size(); i++) {
            total += list.get(i).weight;
            rollingSumOfCount[i] = total;
        }
        if (total == 0)
            throw new IllegalArgumentException();
        int randomCardIndex = (int) ((seed & LONG_MASK) % total);
        int index = 0;
        while (randomCardIndex >= rollingSumOfCount[index]) {
            index++;
        }
        return list.get(index).thing;
    }
}
