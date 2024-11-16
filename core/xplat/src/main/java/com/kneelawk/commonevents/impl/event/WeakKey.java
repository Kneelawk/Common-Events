package com.kneelawk.commonevents.impl.event;

import java.lang.ref.WeakReference;

public final class WeakKey {
    private final WeakReference<Object> ref;
    private final int hashCode;

    public WeakKey(Object obj) {
        this.ref = new WeakReference<>(obj);
        this.hashCode = obj.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        // hash-code/equals magic that makes it so that a weak-key hash-code/equals the same as the object it's holding
        if (this == o) return true;
        if (o == null) return false;

        Object o1 = ref.get();
        // null means we lost this ref, so we compare hashCodes because that's all we have left
        if (o1 == null) return o instanceof WeakKey weakKey && hashCode == weakKey.hashCode;

        if (o instanceof WeakKey weakKey) {
            Object o2 = weakKey.ref.get();
            return o1.equals(o2);
        } else {
            return o1.equals(o);
        }
    }

    @Override
    public int hashCode() {
        return hashCode;
    }
}
