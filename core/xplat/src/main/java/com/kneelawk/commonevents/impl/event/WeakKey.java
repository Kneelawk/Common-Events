package com.kneelawk.commonevents.impl.event;

import java.lang.ref.WeakReference;

public class WeakKey {
    private final WeakReference<Object> ref;
    private final int hashCode;

    public WeakKey(Object obj) {
        this.ref = new WeakReference<>(obj);
        this.hashCode = obj.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        WeakKey weakKey = (WeakKey) o;

        Object o1 = ref.get();
        // null means we lost this ref, so we compare hashCodes because that's all we have left
        if (o1 == null) return hashCode == weakKey.hashCode;

        Object o2 = weakKey.ref.get();
        return o1.equals(o2);
    }

    @Override
    public int hashCode() {
        return hashCode;
    }
}
