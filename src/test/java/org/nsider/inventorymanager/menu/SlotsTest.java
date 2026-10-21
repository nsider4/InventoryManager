package org.nsider.inventorymanager.menu;

import org.bukkit.configuration.MemoryConfiguration;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SlotsTest {

    @Test
    void numbersRangesAndLists() {
        assertEquals(Arrays.asList(1, 10, 11, 12, 20, 21), Slots.parse(Arrays.asList(1, "10-12", "20, 21")));
        assertEquals(Arrays.asList(5, 6, 7), Slots.parse(Collections.singletonList("7-5")));
        assertEquals(Collections.singletonList(3), Slots.parse(Arrays.asList("x", 3)));
    }

    @Test
    void slotOrSlots() {
        MemoryConfiguration item = new MemoryConfiguration();
        item.set("slot", 4);
        assertEquals(Collections.singletonList(4), Slots.of(item));
        item.set("slots", Arrays.asList("0-2", 8));
        assertEquals(Arrays.asList(0, 1, 2, 8), Slots.of(item));
        assertEquals(Collections.emptyList(), Slots.of(new MemoryConfiguration()));
    }
}
