package org.nsider.inventorymanager.menu;

import org.nsider.inventorymanager.extras.Nonnull;

/**
 * Fills a menu with a plugin's data before it is drawn: placeholders, list entries, item states
 * and code click handlers. The layout and what buttons do stay in the menu's config.
 */
@FunctionalInterface
public interface MenuProvider {

    void build(@Nonnull Menu menu);
}
