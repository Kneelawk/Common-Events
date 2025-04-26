package com.kneelawk.commonevents.api;

import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class EventTests {
    record Thing(int i) {
    }

    @Test
    void testDuplicateKeys() {
        Event<Runnable> event = Event.builderSimple(Runnable.class).scanned(false).build();

        Thing thing = new Thing(1);
        AtomicBoolean callback1 = new AtomicBoolean(false);
        AtomicBoolean callback2 = new AtomicBoolean(false);
        event.registerKeyed(thing, () -> callback1.lazySet(true));
        event.registerKeyed(thing, () -> callback2.lazySet(true));

        event.invoker().run();

        assertTrue(callback1.get());
        assertTrue(callback2.get());
    }

    @Test
    void testRemoveDuplicateKeys() {
        Event<Runnable> event = Event.builderSimple(Runnable.class).scanned(false).build();

        Thing thing = new Thing(1);
        AtomicBoolean callback1 = new AtomicBoolean(false);
        AtomicBoolean callback2 = new AtomicBoolean(false);
        AtomicBoolean callback3 = new AtomicBoolean(false);
        event.registerKeyed(thing, () -> callback1.lazySet(true));
        event.registerKeyed(thing, () -> callback2.lazySet(true));
        event.registerKeyed(new Thing(2), () -> callback3.lazySet(true));

        event.unregister(new Thing(1));

        event.invoker().run();

        assertFalse(callback1.get());
        assertFalse(callback2.get());
        assertTrue(callback3.get());
    }
}
