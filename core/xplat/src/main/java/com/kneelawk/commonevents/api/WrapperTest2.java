package com.kneelawk.commonevents.api;

import java.lang.ref.WeakReference;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

import com.kneelawk.commonevents.impl.event.CleanerHolder;

public class WrapperTest2 implements UnaryOperator<String> {
    private final WeakReference<UnaryOperator<String>> wrapped;
    private final String defaultReturn;
    
    public WrapperTest2(UnaryOperator<String> wrapped, Consumer<Object> cleaner, String defaultReturn) {
        this.wrapped = new WeakReference<>(wrapped);
        CleanerHolder.CLEANER.register(wrapped, new CleanerHolder(cleaner, this));
        this.defaultReturn = defaultReturn;
    }

    @Override
    public String apply(String arg0) {
        UnaryOperator<String> x = wrapped.get();
        if (x != null) {
            return x.apply(arg0);
        } else {
            return defaultReturn;
        }
    }
}
