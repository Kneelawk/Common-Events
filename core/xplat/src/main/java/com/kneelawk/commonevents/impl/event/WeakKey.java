package com.kneelawk.commonevents.impl.event;

import java.lang.ref.WeakReference;

import org.jetbrains.annotations.Nullable;

public final class WeakKey extends KeyHolder {
    private final WeakReference<Object> ref;
    private final int hashCode;

    public WeakKey(Object obj) {
        this.ref = new WeakReference<>(obj);
        this.hashCode = obj.hashCode();
    }

    @Override
    public @Nullable Object get() {
        return ref.get();
    }

    @Override
    public int hashCode() {
        return hashCode;
    }
}
