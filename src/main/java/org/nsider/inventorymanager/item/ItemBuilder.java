package org.nsider.inventorymanager.item;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.nsider.inventorymanager.inventory.InventoryBuilder;
import org.nsider.inventorymanager.inventory.InventoryHandler;
import org.nsider.inventorymanager.utils.MaterialUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class ItemBuilder {

    private static final Map<Long, ItemStack> idToItemMap = new IdentityHashMap<>();
    private static final AtomicLong nextId = new AtomicLong(0);

    /**
     * Gets or generates an ID for the given ItemStack.
     *
     * @param item The ItemStack for which to get or generate an ID.
     * @return The ID associated with the item.
     */
    public static long getIdFor(ItemStack item) {
        for (Map.Entry<Long, ItemStack> entry : idToItemMap.entrySet()) {
            if (entry.getValue().equals(item)) {
                return entry.getKey(); // Return the existing ID if item is found
            }
        }

        long id = nextId.getAndIncrement(); // Generate a new ID
        idToItemMap.put(id, item); // Associate the item with the new ID
        return id; // Return the new ID
    }

    /**
     * Removes an item with the specified ID from the mapping.
     *
     * @param id The ID of the item to be removed.
     */
    public static void removeItem(long id) {
        idToItemMap.remove(id); // Remove the item associated with the given ID
    }

    /**
     * Returns an unmodifiable view of the mapping containing all items and their IDs.
     *
     * @return Unmodifiable map containing all items and their IDs.
     */
    public static Map<Long, ItemStack> getAllItems() {
        return Collections.unmodifiableMap(idToItemMap); // Return an unmodifiable view of the item map
    }

    /**
     * Retrieves the ItemStack associated with the given ID.
     *
     * @param id The ID of the ItemStack to be retrieved.
     * @return The ItemStack associated with the provided ID.
     */
    public static ItemStack getItemForId(long id) {
        return idToItemMap.get(id); // Retrieve the item associated with the given ID
    }


    /**
     * Creates an ItemStack from the given ItemData.
     *
     * @param info           The ItemData containing information about the item.
     * @param forcedMaterial Optional forced Material to use. If provided, this Material will be used regardless of the material specified in the ItemData.
     * @return The created ItemStack with the specified properties.
     */
    public static ItemStack fromItemData(ItemData info, Material... forcedMaterial) {
        if (info.getMaterial().contains("head-")) {
            ItemStack item = MaterialUtils.addMeta(
                    MaterialUtils.getCustomTextureHead(
                            info.getMaterial().split("-")[1]),
                            info.getAmount(),
                            info.getName(),
                            info.getLore()
            );
            info.setID(getIdFor(item));
            return item;
        } else {
            Material material = forcedMaterial.length == 0 ? MaterialUtils.getMaterial(info.getMaterial()) : forcedMaterial[0];
            ItemStack item = MaterialUtils.createItem(
                    material,
                    info.getAmount(),
                    info.getName(),
                    info.getLore()
            );
            info.setID(getIdFor(item));
            return item;
        }
    }

    /**
     * Creates an ItemData object from the given ItemStack.
     *
     * @param item The ItemStack containing information about the item.
     * @return The created ItemData object with the specified properties.
     */
    public static ItemData dataFromItemStack(ItemStack item) {
        InventoryHandler handler = InventoryHandler.getInstance();

        for (InventoryBuilder builder : handler.getInventories().values()) {
            ConcurrentHashMap<ItemStack, List<Integer>> itemsMap = builder.getItemsMap();

            if (itemsMap.containsKey(item)) {

                for (ItemData itemData : builder.getItemDataList()) {
                    if (itemData.getID() == getIdFor(item)) {
                        return itemData;
                    }
                }
            }

        }


        return null;
    }
}
