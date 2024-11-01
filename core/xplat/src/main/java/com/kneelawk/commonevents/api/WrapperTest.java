package com.kneelawk.commonevents.api;

import java.lang.ref.WeakReference;
import java.util.function.Consumer;

import com.kneelawk.commonevents.impl.event.CleanerHolder;

public class WrapperTest implements Runnable {
    private final WeakReference<Runnable> wrapped;

    public WrapperTest(Runnable wrapped, Consumer<Object> cleaner) {
        this.wrapped = new WeakReference<>(wrapped);
        CleanerHolder.CLEANER.register(wrapped, new CleanerHolder(cleaner, this));
    }

    @Override
    public void run() {
        Runnable x = wrapped.get();
        if (x != null) {
            x.run();
        }
    }
}
