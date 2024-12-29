package org.nsider.inventorymanager.inventory;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/*
 * The InventoryHandler class manages the collection of custom inventories.
 * It allows adding, removing, and retrieving inventories by name.
 */
public class InventoryHandler {
    /**
     * -- GETTER --
     * Returns the singleton instance of the InventoryHandler.
     */
    @Getter private static final InventoryHandler instance = new InventoryHandler();

    @Getter private Map<String, InventoryBuilder> inventories;

    /*
     * Private constructor to enforce singleton.
     * Initializes the map of inventories.
     */
    private InventoryHandler() {
        inventories = new HashMap<>();
    }

    /**
     * Adds a custom inventory to the collection.
     *
     * @param name    The name of the inventory.
     * @param builder The InventoryBuilder instance representing the custom inventory.
     */
    public void addInventory(String name, InventoryBuilder builder) {
        inventories.put(name, builder);
    }

    /**
     * Removes a custom inventory from the collection.
     *
     * @param name The name of the inventory to be removed.
     */
    public void removeInventory(String name) {
        inventories.remove(name);
    }

    /**
     * Obtains the InventoryBuilder instance for the given inventory name.
     *
     * @param name The name of the inventory to retrieve.
     * @return The InventoryBuilder instance associated with the given name,
     *         or null if the inventory with the specified name is not found.
     */
    public InventoryBuilder getInventoryBuilder(String name) {
        return inventories.get(name);
    }

    /**
     * Checks if an inventory with the given name has been added to the collection.
     *
     * @param name The name of the inventory to check for.
     * @return True if the inventory is present in the collection, false otherwise.
     */
    public boolean isAdded(String name) {
        return inventories.containsKey(name);
    }

    /**
     * Clears all inventories from the collection.
     */
    public void clearAllInventories() {
        inventories.clear();
    }
}
