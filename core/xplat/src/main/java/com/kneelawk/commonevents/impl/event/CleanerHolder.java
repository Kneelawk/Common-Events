package com.kneelawk.commonevents.impl.event;

import java.lang.ref.Cleaner;
import java.util.function.Consumer;

public class CleanerHolder implements Runnable {
    public static final Cleaner CLEANER = Cleaner.create();

    private final Consumer<Object> cleaner;
    private final Object toClean;

    public CleanerHolder(Consumer<Object> cleaner, Object toClean) {
        this.cleaner = cleaner;
        this.toClean = toClean;
    }

    @Override
    public void run() {
        cleaner.accept(toClean);
    }
}
