package org.nsider.inventorymanager.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TextTest {

    @Test
    void hexBecomesSectionCodesOnNewServers() {
        assertEquals("§x§f§f§0§0§0§0Red", Text.color("&#ff0000Red", true));
    }

    @Test
    void hexIsDroppedOnOldServers() {
        assertEquals("§aGreen Red", Text.color("&aGreen &#ff0000Red", false));
    }
}
