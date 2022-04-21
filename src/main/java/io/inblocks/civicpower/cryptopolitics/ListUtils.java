package io.inblocks.civicpower.cryptopolitics;

import java.util.Arrays;
import java.util.List;

public class ListUtils {
    private ListUtils() {}

    public static <T> List<T> subst(List<T> list, T target, T replacement) {
      return list.stream().map(item -> item == target ? replacement : item).toList();
    }

    public static <T> List<T> concat(List<T>... lists) {
        return Arrays.stream(lists).flatMap(List::stream).toList();
    }
}
