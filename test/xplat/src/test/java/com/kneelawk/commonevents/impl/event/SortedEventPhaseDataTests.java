package com.kneelawk.commonevents.impl.event;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
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

        assertEquals(1, phaseData.removeListener(new Thing1(1)));
        assertArrayEquals(new String[]{"2", "3"}, phaseData.getCallbacks());
    }

    @Test
    void testDuplicateKey() {
        SortedEventPhaseData<String> phaseData = new SortedEventPhaseData<>(String.class);
        phaseData.addListener(new Thing1(1), "1");
        phaseData.addListener(new Thing1(3), "3");
        phaseData.addListener(new Thing1(2), "2");
        phaseData.addListener(new Thing1(3), "32");

        String[] callbacks = phaseData.getCallbacks();
        assertEquals("1", callbacks[0]);
        assertEquals("2", callbacks[1]);
        assertTrue(callbacks[2].equals("3") || callbacks[2].equals("32"));
        assertTrue(callbacks[3].equals("32") || callbacks[3].equals("3"));
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

    @Test
    void testRemovalDuplicateKey() {
        SortedEventPhaseData<String> phaseData = new SortedEventPhaseData<>(String.class);
        phaseData.addListener(new Thing1(1), "1");
        phaseData.addListener(new Thing1(3), "3");
        phaseData.addListener(new Thing1(2), "2");
        phaseData.addListener(new Thing1(3), "32");
        phaseData.addListener(new Thing1(4), "4");

        String[] callbacks = phaseData.getCallbacks();
        assertEquals("1", callbacks[0]);
        assertEquals("2", callbacks[1]);
        assertTrue(callbacks[2].equals("3") || callbacks[2].equals("32"));
        assertTrue(callbacks[3].equals("32") || callbacks[3].equals("3"));
        assertEquals("4", callbacks[4]);

        assertEquals(2, phaseData.removeListener(new Thing1(3)));
        assertArrayEquals(new String[]{"1", "2", "4"}, phaseData.getCallbacks());
    }

    @Test
    void testRemovalDuplicateKeyHashConflict() {
        SortedEventPhaseData<String> phaseData = new SortedEventPhaseData<>(String.class);
        phaseData.addListener(new Thing1(1), "1");
        phaseData.addListener(new Thing1(3), "3");
        phaseData.addListener(new Thing1(2), "2");
        phaseData.addListener(new Thing1(3), "32");
        phaseData.addListener(new Thing1(4), "4");
        phaseData.addListener(new Thing2(2), "22");

        String[] callbacks = phaseData.getCallbacks();
        assertEquals("1", callbacks[0]);
        assertTrue(callbacks[1].equals("2") || callbacks[1].equals("22"));
        assertTrue(callbacks[2].equals("22") || callbacks[2].equals("2"));
        assertTrue(callbacks[3].equals("3") || callbacks[3].equals("32"));
        assertTrue(callbacks[4].equals("32") || callbacks[4].equals("3"));
        assertEquals("4", callbacks[5]);

        assertEquals(2, phaseData.removeListener(new Thing1(3)));
        assertEquals(1, phaseData.removeListener(new Thing1(2)));

        assertArrayEquals(new String[]{"1", "22", "4"}, phaseData.getCallbacks());
    }
}
