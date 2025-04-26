package com.kneelawk.commonevents.test.test;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.kneelawk.commonevents.api.Event;
import com.kneelawk.commonevents.api.Listen;
import com.kneelawk.commonevents.api.Scan;

@Scan
public class SimpleCommonEventsScanningTest {
    public interface MyCallback {
        void doSomething();
    }

    public static final Event<MyCallback> MY_EVENT = Event.createSimple(MyCallback.class);

    private static boolean event1;

    @BeforeEach
    public void init() {
        event1 = false;
    }

    @Test
    public void testScannedEvents() {
        Assertions.assertFalse(event1);
        MY_EVENT.invoker().doSomething();
        Assertions.assertTrue(event1);
    }

    @Listen(MyCallback.class)
    public static void onSomething() {
        event1 = true;
    }
}
