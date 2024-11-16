package com.kneelawk.commonevents.impl;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;

public class ArrayUtils {
    private static final Comparator<Object> HASH_COMPARATOR = Comparator.comparingInt(Objects::hashCode);

    public static int search(Object[] a, Object key) {
        int len = a.length;
        for (int i = 0; i < len; i++) {
            if (Objects.equals(a[i], key)) return i;
        }
        return -1;
    }

    public static int binarySearch(Object[] a, Object key) {
        int index = Arrays.binarySearch(a, key, HASH_COMPARATOR);
        if (index < 0) return index;
        return refineBinarySearch(a, key, index);
    }

    public static int refineBinarySearch(Object[] a, Object key, int guess) {
        if (Objects.equals(a[guess], key)) return guess;

        int len = a.length;
        for (int i = 1; i < len; i++) {
            boolean pos = i + guess < len;
            if (pos && Objects.equals(a[guess + i], key)) return guess + i;
            boolean neg = i - guess >= 0;
            if (neg && Objects.equals(a[guess - i], key)) return guess - i;
            if ((!pos || Objects.hashCode(a[guess + i]) != Objects.hashCode(key)) &&
                (!neg || Objects.hashCode(a[guess - i]) != Objects.hashCode(key))) return -guess - 1;
        }

        return -guess - 1;
    }
}
