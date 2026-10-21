package org.nsider.inventorymanager.utils;

import com.cryptomorin.xseries.XMaterial;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.nsider.inventorymanager.extras.Nonnull;
import org.nsider.inventorymanager.extras.Nullable;
import org.nsider.inventorymanager.item.Materials;

import java.util.List;

public class MaterialUtils {

    /**
     * Retrieves the Material based on its name/id, on any version (see {@link Materials}).
     *
     * @param name The name of the material.
     * @return The corresponding Material.
     * @throws IllegalArgumentException If the material name is invalid.
     */
    public static Material getMaterial(String name) {

        XMaterial xMaterial = Materials.match(name)
                .orElseThrow(() -> new IllegalArgumentException("Invalid material: " + name));
        return xMaterial.get();
    }

    /**
     * Gets the Material based on the XMaterial object
     *
     * @param mat The XMaterial object.
     * @return The corresponding Material.
     */
    public static Material getMaterial(XMaterial mat) {

        return mat == null ? null : mat.get();
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
     * Prefer {@link Materials#item(String)}, which handles legacy data on every version.
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
        return SkullUtils.setSkullTexture(XMaterial.PLAYER_HEAD.parseItem(), value);
    }




    /**
     * Gets the player skin skull as an item
     *
     * @param owner The desired player's name
     * @return The ItemStack object of the skull with the player's skin.
     */
    @SuppressWarnings("deprecation")
    public static ItemStack getPlayerSkull(@Nonnull String owner) {
        // parseItem gives SKULL_ITEM:3 (a player head) before 1.13
        ItemStack head = XMaterial.PLAYER_HEAD.parseItem();
        ItemMeta meta = head.getItemMeta();

        if (meta instanceof SkullMeta) {
            ((SkullMeta) meta).setOwner(owner);
            head.setItemMeta(meta);
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
