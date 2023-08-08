package org.nsider.inventorymanager.item;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.nsider.inventorymanager.inventory.InventoryBuilder;
import org.nsider.inventorymanager.inventory.InventoryHandler;
import org.nsider.inventorymanager.utils.MaterialUtils;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class ItemBuilder {

    /**
     * Creates an ItemStack from the given ItemData.
     *
     * @param info           The ItemData containing information about the item.
     * @param forcedMaterial Optional forced Material to use. If provided, this Material will be used regardless of the material specified in the ItemData.
     * @return The created ItemStack with the specified properties.
     */
    public static ItemStack fromItemData(ItemData info, Material... forcedMaterial) {
        if (info.getMaterial().contains("head-")) {
            return MaterialUtils.addMeta(
                    MaterialUtils.getCustomTextureHead(
                            info.getMaterial().split("-")[1]),
                            info.getAmount(),
                            info.getName(),
                            info.getLore()
            );
        } else {
            Material material = forcedMaterial.length == 0 ? MaterialUtils.getMaterial(info.getMaterial()) : forcedMaterial[0];
            return MaterialUtils.createItem(
                    material,
                    info.getAmount(),
                    info.getName(),
                    info.getLore()
            );
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
                List<Integer> slots = itemsMap.get(item);

                for (ItemData itemData : builder.getItemDataList()) {
                    if (itemData.getSlots().equals(slots)) {
                        return itemData;
                    }
                }
            }

        }

        return null;
    }
}
