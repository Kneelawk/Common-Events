package com.kneelawk.commonevents.impl.event;

import java.util.Objects;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class UnsortedEventPhaseDataTests {
    record Thing(int i) {}

    @Test
    void testInsertion() {
        UnsortedEventPhaseData<String> phaseData = new UnsortedEventPhaseData<>(String.class);
        phaseData.addListener(new Thing(1), "1");
        phaseData.addListener(new Thing(2), "2");
        phaseData.addListener(new Thing(9), "9");

        assertTrue(contains(phaseData.getCallbacks(), "1"));
        assertTrue(contains(phaseData.getCallbacks(), "2"));
        assertTrue(contains(phaseData.getCallbacks(), "9"));
    }

    @Test
    void testRemoval() {
        UnsortedEventPhaseData<String> phaseData = new UnsortedEventPhaseData<>(String.class);
        phaseData.addListener(new Thing(1), "1");
        phaseData.addListener(new Thing(2), "2");

        assertTrue(contains(phaseData.getCallbacks(), "1"));
        assertTrue(contains(phaseData.getCallbacks(), "2"));

        phaseData.removeListener(new Thing(2));

        assertTrue(contains(phaseData.getCallbacks(), "1"));
        assertFalse(contains(phaseData.getCallbacks(), "2"));
    }

    @Test
    void testDuplicateInsertion() {
        UnsortedEventPhaseData<String> phaseData = new UnsortedEventPhaseData<>(String.class);
        phaseData.addListener(new Thing(1), "1");
        Thing thing = new Thing(2);
        phaseData.addListener(thing, "2");
        phaseData.addListener(thing, "3");
        phaseData.addListener(new Thing(2), "4");

        assertTrue(contains(phaseData.getCallbacks(), "1"));
        assertTrue(contains(phaseData.getCallbacks(), "2"));
        assertTrue(contains(phaseData.getCallbacks(), "3"));
        assertTrue(contains(phaseData.getCallbacks(), "4"));
    }

    @Test
    void testDuplicateRemoval() {
        UnsortedEventPhaseData<String> phaseData = new UnsortedEventPhaseData<>(String.class);
        phaseData.addListener(new Thing(1), "1");
        Thing thing = new Thing(2);
        phaseData.addListener(thing, "2");
        phaseData.addListener(thing, "3");
        phaseData.addListener(new Thing(2), "4");

        assertTrue(contains(phaseData.getCallbacks(), "1"));
        assertTrue(contains(phaseData.getCallbacks(), "2"));
        assertTrue(contains(phaseData.getCallbacks(), "3"));
        assertTrue(contains(phaseData.getCallbacks(), "4"));

        phaseData.removeListener(new Thing(2));

        assertTrue(contains(phaseData.getCallbacks(), "1"));
        assertFalse(contains(phaseData.getCallbacks(), "2"));
        assertFalse(contains(phaseData.getCallbacks(), "3"));
        assertFalse(contains(phaseData.getCallbacks(), "4"));
    }

    @Test
    void testDuplicateRemoval2() {
        UnsortedEventPhaseData<String> phaseData = new UnsortedEventPhaseData<>(String.class);
        phaseData.addListener(new Thing(1), "1");
        Thing thing = new Thing(2);
        phaseData.addListener(thing, "2");
        phaseData.addListener(thing, "3");
        phaseData.addListener(new Thing(2), "4");
        phaseData.addListener(new Thing(9), "9");

        assertTrue(contains(phaseData.getCallbacks(), "1"));
        assertTrue(contains(phaseData.getCallbacks(), "2"));
        assertTrue(contains(phaseData.getCallbacks(), "3"));
        assertTrue(contains(phaseData.getCallbacks(), "4"));
        assertTrue(contains(phaseData.getCallbacks(), "9"));

        phaseData.removeListener(new Thing(2));

        assertTrue(contains(phaseData.getCallbacks(), "1"));
        assertFalse(contains(phaseData.getCallbacks(), "2"));
        assertFalse(contains(phaseData.getCallbacks(), "3"));
        assertFalse(contains(phaseData.getCallbacks(), "4"));
        assertTrue(contains(phaseData.getCallbacks(), "9"));
    }

    private static <T> boolean contains(T[] a, T o) {
        for (T t : a) {
            if (Objects.equals(t, o)) return true;
        }
        return false;
    }
}
