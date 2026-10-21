package org.nsider.inventorymanager.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class VersionsTest {

    @Test
    void readsBothNumberingSchemes() {
        assertArrayEquals(new int[]{1, 8, 8}, Versions.parse("1.8.8-R0.1-SNAPSHOT"));
        assertArrayEquals(new int[]{1, 21, 0}, Versions.parse("1.21-R0.1-SNAPSHOT"));
        assertArrayEquals(new int[]{26, 1, 2}, Versions.parse("26.1.2-R0.1-SNAPSHOT"));
        assertArrayEquals(new int[]{26, 1, 0}, Versions.parse("26.1"));
    }

    @Test
    void yearVersionsAreNewerThanEveryOneDotX() {
        int[] modern = Versions.parse("26.1.2");
        assertTrue(Versions.compare(modern, 1, 21) > 0);
        assertEquals(0, Versions.compare(modern, 26, 1));
        assertEquals(0, Versions.compare(Versions.parse("1.16.5"), 1, 16));
        assertTrue(Versions.compare(Versions.parse("1.8.8"), 1, 9) < 0);
    }

    @Test
    void legacyNumberKeepsOldChecksWorking() {
        assertEquals(8, Versions.legacyNumber(Versions.parse("1.8.8")));
        assertEquals(21, Versions.legacyNumber(Versions.parse("1.21.4")));
        assertTrue(Versions.legacyNumber(Versions.parse("26.1")) >= 13);
    }
}
