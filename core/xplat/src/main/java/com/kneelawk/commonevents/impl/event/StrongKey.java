package com.kneelawk.commonevents.impl.event;

import org.jetbrains.annotations.Nullable;

public final class StrongKey extends KeyHolder {
    private final Object ref;

    public StrongKey(Object ref) {this.ref = ref;}

    @Override
    public @Nullable Object get() {
        return ref;
    }

    @Override
    public int hashCode() {
        return ref.hashCode();
    }
}
