package org.nsider.inventorymanager.inventory;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.nsider.inventorymanager.item.ItemBuilder;
import org.nsider.inventorymanager.item.ItemData;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/*
 * The InventoryBuilder class is used to create custom inventories and add items to them.
 */
@Getter
public class InventoryBuilder {

    private ConcurrentHashMap<ItemStack, List<Integer>> itemsMap;
    private Inventory inventory;
    @Setter private List<ItemData> itemDataList;
    @Setter private String inventoryName;
    @Setter private int inventorySize;
    @Setter private String baseInventoryName;

    /**
     * Constructs an InventoryBuilder with the specified name and size.
     *
     * @param inventoryName The name of the inventory.
     * @param inventorySize The size of the inventory.
     */
    public InventoryBuilder(String inventoryName, int inventorySize) {
        this.itemsMap = new ConcurrentHashMap<>();
        this.itemDataList = new ArrayList<>();
        this.inventoryName = inventoryName;
        this.inventorySize = inventorySize;
        this.inventory = Bukkit.createInventory(null, inventorySize, inventoryName);
        this.baseInventoryName = null;
    }

    /**
     * Adds an item to the inventory.
     *
     * @param data The ItemData containing information about the item to be added.
     */
    public void addItem(ItemData data) {
        itemsMap.put(ItemBuilder.fromItemData(data), data.getSlots());
        itemDataList.add(data);
    }

    /**
     * Removes an item from the inventory.
     *
     * @param data The ItemData containing information about the item to be removed.
     */
    public void removeItem(ItemData data) {
        ItemStack itemToRemove = ItemBuilder.fromItemData(data);
        itemsMap.remove(itemToRemove);
        itemDataList.remove(data);
        if(inventory.contains(itemToRemove)) { inventory.remove(itemToRemove); }
    }

    /**
     * Saves the inventory name and InventoryBuilder object to the InventoryHandler
     * This method should be called right after you've finished adding all ItemData to the itemsMap(addItem) for that inventory.
     */
    public void save() {
        InventoryHandler handler = InventoryHandler.getInstance();

        if (handler.isAdded(inventoryName)) {
            handler.removeInventory(inventoryName);
        }

        handler.addInventory(inventoryName, this);
    }


    /**
     * Checks if the inventory has a base inventory
     *
     * @return True if the inventory has a base inventory, false otherwise
     */
    public boolean hasBaseInventory() {
        return baseInventoryName != null;
    }

    /**
     * Clears the inventory and adds the items from the itemsMap to their respective slots.
     * This method should be called before opening the inventory for a player.
     */
    public void load() {
        inventory.clear();

        if (hasBaseInventory()) {
            InventoryBuilder baseInventory = InventoryHandler.getInstance().getInventoryBuilder(baseInventoryName);

            if (baseInventory != null) {
                for (Map.Entry<ItemStack, List<Integer>> entry : baseInventory.getItemsMap().entrySet()) {
                    ItemStack baseItem = entry.getKey();
                    List<Integer> baseSlots = entry.getValue();
                    for (int slot : baseSlots) {
                        inventory.setItem(slot, baseItem.clone());
                    }
                }
            }
        }

        for (Map.Entry<ItemStack, List<Integer>> entry : itemsMap.entrySet()) {
            ItemStack item = entry.getKey();
            List<Integer> slots = entry.getValue();

            for (int slot : slots) {
                inventory.setItem(slot, item.clone());
            }
        }
    }


    /**
     * Clears the inventory and the itemsMap
     */
    public void clear() {
        itemsMap.clear();
        inventory.clear();
        itemDataList.clear();
    }

    /**
     * Finds the ItemData with the specified name.
     *
     * @param targetName The name to look for in the data.
     * @return The ItemData containing information about the item.                
     */
    public Optional<ItemData> getItemDataWithName(String targetName) {
        return itemDataList.stream()
                .filter(itemData -> itemData.getName().equals(targetName))
                .findFirst();
    }

    /**
     * Updates an item with the updated data
     *
     * @param data The ItemData containing information about the item
     */
    public void updateItem(ItemData data) {
        long id = data.getID();

        ItemStack item = ItemBuilder.getItemForId(id);
        if(item != null) {
            itemsMap.remove(item);
            itemsMap.put(ItemBuilder.fromItemData(data), data.getSlots());
        }
    }


    /**
     * Opens the inventory for the specified player.
     *
     * @param p The player to open the inventory for.
     */
    public void openInventory(Player p) {
        p.openInventory(inventory);
    }
}
