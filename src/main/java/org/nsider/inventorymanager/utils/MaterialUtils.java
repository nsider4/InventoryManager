package org.nsider.inventorymanager.utils;

import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.nsider.inventorymanager.extras.Nonnull;
import org.nsider.inventorymanager.extras.Nullable;

import java.util.List;

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
     * Creates an ItemStack with name and lore.
     *
     * @param material The material of the item.
     * @param name     The name of the item.
     * @param lore     The lore of the item.
     * @return The created ItemStack.
     */
    public static ItemStack createItem(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        return addMeta(item, name, lore);
    }

    /**
     * Creates an ItemStack with material and amount.
     *
     * @param material The material of the item.
     * @param amount   The amount of the item.
     * @return The created ItemStack.
     */
    public static ItemStack createItem(Material material, int amount) {
        return new ItemStack(material, amount);
    }

    /**
     * Creates an ItemStack with name, lore, and amount.
     *
     * @param material The material of the item.
     * @param amount   The amount of the item.
     * @param name     The name of the item.
     * @param lore     The lore of the item.
     * @return The created ItemStack.
     */
    public static ItemStack createItem(Material material, int amount, String name, List<String> lore) {
        ItemStack item = createItem(material, amount);
        return addMeta(item, name, lore);
    }

    /**
     * Creates an ItemStack with material, amount, and data.
     *
     * @param material The material of the item.
     * @param amount   The amount of the item.
     * @param data     The data of the item.
     * @return The created ItemStack.
     */
    public static ItemStack createLegacyItem(Material material, int amount, int data) {
        return new ItemStack(material, amount, (byte) data);
    }

    /**
     * Creates an ItemStack with name, lore, and amount.
     *
     * @param material The material of the item.
     * @param data     The data of the item.
     * @param amount   The amount of the item.
     * @param name     The name of the item.
     * @param lore     The lore of the item.
     * @return The created ItemStack.
     */
    public static ItemStack createLegacyItem(Material material, int data, int amount, String name, List<String> lore) {
        ItemStack item = createLegacyItem(material, amount, data);
        return addMeta(item, name, lore);
    }



    /**
     * Adds ItemMeta to an ItemStack with name and lore.
     *
     * @param item  The item to add the meta to.
     * @param name  The name of the item.
     * @param lore  The lore of the item.
     * @return The item with the meta added.
     */
    public static ItemStack addMeta(ItemStack item, String name, List<String> lore) {
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(lore);

        item.setItemMeta(meta);
        return item;
    }

    /**
     * Adds ItemMeta to an ItemStack with name, lore, and amount.
     *
     * @param item  The item to add the meta to.
     * @param amount The amount of the item.
     * @param name  The name of the item.
     * @param lore  The lore of the item.
     * @return The item with the meta added.
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
}
