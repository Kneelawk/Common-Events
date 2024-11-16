package com.kneelawk.commonevents.impl.event;

import java.util.Objects;

import org.jetbrains.annotations.Nullable;

public sealed abstract class KeyHolder permits StrongKey, WeakKey {
    public abstract @Nullable Object get();

    @Override
    public abstract int hashCode();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof KeyHolder holder)) return false;

        // in the case of Events, we don't care whether we've lost our ref, because when removing an empty reference,
        // we might as well remove everything else with an empty reference too
        Object o1 = get();
        Object o2 = holder.get();
        return Objects.equals(o1, o2);
    }
}
