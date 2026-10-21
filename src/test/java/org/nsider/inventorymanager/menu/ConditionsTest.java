package org.nsider.inventorymanager.menu;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.*;

class ConditionsTest {

    private final Predicate<String> perms = node -> node.equals("menu.admin");

    @Test
    void permissions() {
        assertTrue(Conditions.test("permission menu.admin", perms));
        assertTrue(Conditions.test("permission: menu.admin", perms));
        assertFalse(Conditions.test("permission menu.other", perms));
        assertTrue(Conditions.test("!permission menu.other", perms));
    }

    @Test
    void comparisons() {
        assertTrue(Conditions.test("5 >= 3", perms));
        assertTrue(Conditions.test("1,500 > 999", perms));
        assertFalse(Conditions.test("2 > 3", perms));
        assertTrue(Conditions.test("10 == 10.0", perms));
        assertTrue(Conditions.test("Daily == daily", perms));
        assertTrue(Conditions.test("daily != weekly", perms));
        assertFalse(Conditions.test("abc > abd", perms));
        assertTrue(Conditions.test("!3 < 2", perms));
    }

    @Test
    void truthiness() {
        assertTrue(Conditions.test("true", perms));
        assertTrue(Conditions.test("ON", perms));
        assertTrue(Conditions.test("2", perms));
        assertFalse(Conditions.test("0", perms));
        assertFalse(Conditions.test("false", perms));
        assertFalse(Conditions.test("", perms));
        assertTrue(Conditions.test("!false", perms));
    }

    @Test
    void everyLineMustPass() {
        assertTrue(Conditions.all(Arrays.asList("permission menu.admin", "3 > 1"), perms));
        assertFalse(Conditions.all(Arrays.asList("permission menu.admin", "3 < 1"), perms));
    }
}
