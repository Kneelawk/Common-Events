package com.kneelawk.commonevents.impl.event;

import org.jetbrains.annotations.Nullable;

public sealed abstract class KeyHolder permits StrongKey, WeakKey {
    public abstract @Nullable Object get();

    @Override
    public abstract int hashCode();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof KeyHolder holder)) return false;

        Object o1 = get();
        // null means we lost this ref, so we compare hashCodes because that's all we have left
        if (o1 == null) {
            return this instanceof WeakKey && o instanceof WeakKey && hashCode() == o.hashCode();
        }

        Object o2 = holder.get();
        return o1.equals(o2);
    }
}
