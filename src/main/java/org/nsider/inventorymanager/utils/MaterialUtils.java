package org.nsider.inventorymanager.utils;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.SkullType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.nsider.inventorymanager.extras.Nonnull;
import org.nsider.inventorymanager.extras.Nullable;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.lang.reflect.Field;
import java.util.List;
import java.util.UUID;

public class MaterialUtils {

    /**
     * Retrieves the Material based on its name/id.
     *
     * @param name The name of the material.
     * @return The corresponding Material.
     * @throws IllegalArgumentException If the material name is invalid.
     */
    public static Material getMaterial(String name) {

        XMaterial xMaterial = XMaterial.matchXMaterial(name)
                .orElseThrow(() -> new IllegalArgumentException("Invalid material: " + name));
        Material material = null;
        if (xMaterial != null) {
            material = xMaterial.parseMaterial();
        }

        return material;
    }

    /**
     * Gets the Material based on the XMaterial object
     *
     * @param mat The XMaterial object.
     * @return The corresponding Material.
     */
    public static Material getMaterial(XMaterial mat) {

        Material material = null;
        if (mat != null) {
            material = mat.parseMaterial();
        }

        return material;
    }

    /**
     * All createItem methods create items, it gives option for using fewer parameters or more!
     */

    public static ItemStack createItem(Material material, int amount) {
        return new ItemStack(material, amount);
    }

    public static ItemStack createItem(Material material, int amount, String name, List<String> lore) {
        ItemStack item = createItem(material, amount);
        return addMeta(item, name, lore);
    }

    public static ItemStack createLegacyItem(Material material, int amount, int data) {
        return new ItemStack(material, amount, (byte) data);
    }

    public static ItemStack createLegacyItem(Material material, int data, int amount, String name, List<String> lore) {
        ItemStack item = createLegacyItem(material, amount, data);
        return addMeta(item, name, lore);
    }

    /**
     * Adds ItemMeta to an ItemStack with name and lore.
     */
    public static ItemStack addMeta(ItemStack item, String name, List<String> lore) {
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);

        List<String> coloredLore = new ArrayList<>();
        for (String loreLine : lore) {
            coloredLore.add(MaterialUtils.color(loreLine));
        }
        meta.setLore(coloredLore);

        item.setItemMeta(meta);
        return item;
    }

    /**
     * Adds ItemMeta to an ItemStack with amount, name, and lore.
     */
    public static ItemStack addMeta(ItemStack item, int amount, String name, List<String> lore) {
        item.setAmount(amount);
        return addMeta(item, name, lore);
    }

    /**
     * Gets a custom texture head using value
     *
     * @param value The value of the custom head texture (starts with 'ey')
     * @return The ItemStack object of the skull with the new skin.
     */
    public static ItemStack getCustomTextureHead(String value) {
        ItemStack head = new ItemStack(Material.valueOf(GeneralUtils.getMCVersion() < 13 ? "SKULL_ITEM" : "PLAYER_HEAD"), 1);

        return SkullUtils.setSkullTexture(head, value);
    }




    /**
     * Gets the player skin skull as an item
     *
     * @param owner The desired player's name
     * @return The ItemStack object of the skull with the player's skin.
     */
    @SuppressWarnings("deprecation")
    public static ItemStack getPlayerSkull(@Nonnull String owner) {
        ItemStack head = new ItemStack(Material.valueOf(GeneralUtils.getMCVersion() < 13 ? "SKULL_ITEM" : "PLAYER_HEAD"), 1);
        ItemMeta meta = head.getItemMeta();

        if (meta instanceof SkullMeta) {
            ((SkullMeta) meta).setOwner(owner);
            head.setItemMeta(meta);
            if (GeneralUtils.getMCVersion() < 13) {
                head.setDurability((short) SkullType.PLAYER.ordinal());
            }

            return head;
        }

        return null;
    }

    /**
     * Get the owner of the given skull ItemStack.
     *
     * @param itemStack The ItemStack.
     * @return The skull owner.
     */
    @SuppressWarnings("deprecation")
    @Nullable
    public static boolean skullHasOwner(@Nonnull ItemStack itemStack) {
        ItemMeta meta = itemStack.getItemMeta();
        if (meta instanceof SkullMeta) {
            return ((SkullMeta) meta).hasOwner();
        }
        return false;
    }

    /**
     * Get the owner of the given skull ItemStack.
     *
     * @param itemStack The ItemStack.
     * @return The skull owner.
     */
    @SuppressWarnings("deprecation")
    @Nullable
    public static String getSkullOwner(@Nonnull ItemStack itemStack) {
        ItemMeta meta = itemStack.getItemMeta();
        if (meta instanceof SkullMeta) {
            return ((SkullMeta) meta).getOwner();
        }
        return null;
    }

    private static String color(String message) {
        return GeneralUtils.color(message);
    }
}
