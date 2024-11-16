package com.kneelawk.commonevents.impl.event;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SortedEventPhaseDataTests {
    record Thing1(int i) {
        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Thing1(int i1))) return false;

            return i == i1;
        }

        @Override
        public int hashCode() {
            return i;
        }
    }

    record Thing2(int i) {
        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Thing2(int i1))) return false;

            return i == i1;
        }

        @Override
        public int hashCode() {
            return i;
        }
    }

    @Test
    void testInsertion() {
        SortedEventPhaseData<String> phaseData = new SortedEventPhaseData<>(String.class);
        phaseData.addListener(new Thing1(1), "1");
        phaseData.addListener(new Thing1(3), "3");
        phaseData.addListener(new Thing1(2), "2");
        assertArrayEquals(new String[]{"1", "2", "3"}, phaseData.getCallbacks());
    }

    @Test
    void testRemoval() {
        SortedEventPhaseData<String> phaseData = new SortedEventPhaseData<>(String.class);
        phaseData.addListener(new Thing1(1), "1");
        phaseData.addListener(new Thing1(3), "3");
        phaseData.addListener(new Thing1(2), "2");
        assertArrayEquals(new String[]{"1", "2", "3"}, phaseData.getCallbacks());

        phaseData.removeListener(new Thing1(1));
        assertArrayEquals(new String[]{"2", "3"}, phaseData.getCallbacks());
    }

    @Test
    void testDuplicateKey() {
        SortedEventPhaseData<String> phaseData = new SortedEventPhaseData<>(String.class);
        phaseData.addListener(new Thing1(1), "1");
        phaseData.addListener(new Thing1(3), "3");
        phaseData.addListener(new Thing1(2), "2");
        assertThrows(IllegalArgumentException.class, () -> phaseData.addListener(new Thing1(3), "32"));
    }

    @Test
    void testHashConflict() {
        SortedEventPhaseData<String> phaseData = new SortedEventPhaseData<>(String.class);
        phaseData.addListener(new Thing1(1), "1");
        phaseData.addListener(new Thing1(3), "3");
        phaseData.addListener(new Thing1(2), "2");
        phaseData.addListener(new Thing2(2), "22");

        String[] callbacks = phaseData.getCallbacks();
        assertEquals("1", callbacks[0]);
        assertTrue(callbacks[1].equals("2") || callbacks[1].equals("22"));
        assertTrue(callbacks[2].equals("22") || callbacks[2].equals("2"));
        assertEquals("3", callbacks[3]);
    }
}
