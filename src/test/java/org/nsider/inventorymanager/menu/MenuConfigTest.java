package org.nsider.inventorymanager.menu;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.event.inventory.ClickType;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class MenuConfigTest {

    @Test
    void clickTypesFallBackToActions() {
        MemoryConfiguration item = new MemoryConfiguration();
        item.set("actions", Collections.singletonList("[close]"));
        item.set("right-actions", Collections.singletonList("[refresh]"));
        assertEquals(Collections.singletonList("[close]"), Menu.actionsFor(item, ClickType.LEFT));
        assertEquals(Collections.singletonList("[refresh]"), Menu.actionsFor(item, ClickType.RIGHT));
        assertEquals(Collections.singletonList("[refresh]"), Menu.actionsFor(item, ClickType.SHIFT_RIGHT));
        assertEquals(Collections.singletonList("[close]"), Menu.actionsFor(item, ClickType.NUMBER_KEY));
        assertTrue(Menu.actionsFor(new MemoryConfiguration(), ClickType.LEFT).isEmpty());
    }

    @Test
    void whenOffReplacesOnlyItsKeys() {
        MemoryConfiguration item = new MemoryConfiguration();
        item.set("slot", 4);
        item.set("material", "LIME_DYE");
        item.set("name", "&aOn");
        item.set("actions", Arrays.asList("[command] toggle", "[refresh]"));
        item.set("when-off.material", "GRAY_DYE");
        item.set("when-off.name", "&cOff");
        ConfigurationSection off = Menu.overlay(item, item.getConfigurationSection("when-off"));
        assertEquals("GRAY_DYE", off.getString("material"));
        assertEquals("&cOff", off.getString("name"));
        assertEquals(4, off.getInt("slot"));
        assertEquals(Arrays.asList("[command] toggle", "[refresh]"), off.getStringList("actions"));
        assertFalse(off.contains("when-off"));
    }
}
