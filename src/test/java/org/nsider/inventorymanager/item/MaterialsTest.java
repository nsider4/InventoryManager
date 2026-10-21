package org.nsider.inventorymanager.item;

import com.cryptomorin.xseries.XMaterial;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class MaterialsTest {

    @Test
    void acceptsEveryWayOfWritingAMaterial() {
        assertEquals(Optional.of(XMaterial.RED_WOOL), Materials.match("RED_WOOL"));
        assertEquals(Optional.of(XMaterial.RED_WOOL), Materials.match("WOOL:14"));
        assertEquals(Optional.of(XMaterial.RED_WOOL), Materials.match("35:14"));
        assertEquals(Optional.of(XMaterial.RED_WOOL), Materials.match("red wool"));
        assertEquals(Optional.of(XMaterial.DIAMOND), Materials.match("minecraft:diamond"));
        assertEquals(Optional.of(XMaterial.STONE), Materials.match("1"));
        assertEquals(Optional.of(XMaterial.PLAYER_HEAD), Materials.match("397:3"));
        assertEquals(Optional.of(XMaterial.GRAY_STAINED_GLASS_PANE), Materials.match("stained_glass_pane:7"));
    }

    @Test
    void unknownNamesAreEmpty() {
        assertFalse(Materials.match("not_a_material").isPresent());
        assertFalse(Materials.match("99999").isPresent());
        assertFalse(Materials.match("").isPresent());
        assertFalse(Materials.match(null).isPresent());
    }

    @Test
    void colours() {
        assertEquals(0xFF0000, ItemFactory.color("#FF0000").asRGB());
        assertEquals(0x00FF00, ItemFactory.color("0, 255, 0").asRGB());
        assertNull(ItemFactory.color("red"));
    }
}
