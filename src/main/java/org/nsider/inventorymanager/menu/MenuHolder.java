package org.nsider.inventorymanager.menu;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.nsider.inventorymanager.extras.Nonnull;

/**
 * Marks an inventory as a menu, so clicks are matched by holder rather than by title. A refresh
 * can swap in a newly built {@link Menu} while the same inventory stays open.
 */
public final class MenuHolder implements InventoryHolder {

    private Inventory inventory;
    private volatile Menu menu;
    private final String title;

    MenuHolder(@Nonnull Menu menu, @Nonnull String title) {
        this.menu = menu;
        this.title = title;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    @Nonnull
    public Menu getMenu() {
        return menu;
    }

    void setMenu(Menu menu) {
        this.menu = menu;
    }

    @Nonnull
    String getTitle() {
        return title;
    }
}
