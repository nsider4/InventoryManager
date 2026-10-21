package org.nsider.inventorymanager.menu;

import org.bukkit.event.inventory.ClickType;
import org.nsider.inventorymanager.extras.Nonnull;

/** A click bound in code with {@link Menu#onClick} or a list entry. It runs before the item's configured actions. */
@FunctionalInterface
public interface ClickHandler {

    void click(@Nonnull Menu menu, @Nonnull ClickType type);
}
