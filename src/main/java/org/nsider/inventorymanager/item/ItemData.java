package org.nsider.inventorymanager.item;


import lombok.Data;

import java.util.List;

/**
 * The ItemData class is used to store information about an ItemStack.
 * It includes the name, lore, material, amount, and ID of the ItemStack.
 */
@Data
public class ItemData {
    private String name;
    private List<String> lore;
    private String material;
    private int amount;
    private long ID;
    private List<Integer> slots;

    /**
     * Constructs an ItemData with default values.
     */
    public ItemData() {
    }

    /**
     * Builder for ItemData with default values.
     *
     * @return the ItemDataBuilder with default values.
     */
    public static ItemDataBuilder builder() {
        return new ItemDataBuilder();
    }

    /**
     * Builder for ItemData with the specified values.
     */
    public static class ItemDataBuilder {
        private ItemData itemData;

        private ItemDataBuilder() {
            itemData = new ItemData();
        }

        /**
         * Sets the name of the ItemStack.
         *
         * @param name the name to be applied to the item.
         * @return the ItemDataBuilder with a set name.
         */
        public ItemDataBuilder setName(String name) {
            itemData.name = name;
            return this;
        }

        /**
         * Sets the lore of the ItemStack.
         *
         * @param lore the lore to be applied to the item.
         * @return the ItemDataBuilder with a set lore.
         */
        public ItemDataBuilder setLore(List<String> lore) {
            itemData.lore = lore;
            return this;
        }

        /**
         * Sets the material of the ItemStack.
         *
         * @param material the material to be applied to the item.
         * @return the ItemDataBuilder with a set material.
         */
        public ItemDataBuilder setMaterial(String material) {
            itemData.material = material;
            return this;
        }

        /**
         * Sets the amount of the ItemStack.
         *
         * @param amount the amount to be applied to the item.
         * @return the ItemDataBuilder with a set amount.
         */
        public ItemDataBuilder setAmount(int amount) {
            itemData.amount = amount;
            return this;
        }

        /**
         * Sets the slots of where the item will be put into.
         *
         * @param slots the slots to use the item in.
         * @return the ItemDataBuilder with set slots.
         */
        public ItemDataBuilder setSlots(List<Integer> slots) {
            itemData.slots = slots;
            return this;
        }

        /**
         * Builds the item.
         *
         * @return the complete ItemData.
         */
        public ItemData build() {
            return itemData;
        }
    }
}
