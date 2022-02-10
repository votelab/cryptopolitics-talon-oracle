package io.inblocks.civicpower.cryptopolitics;

import java.util.List;

public class ListUtils {
    private ListUtils() {}

    public static <T> List<T> subst(List<T> list, T target, T replacement) {
      return list.stream().map(item -> item == target ? replacement : item).toList();
    }
}
