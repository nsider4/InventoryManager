package org.nsider.inventorymanager.menu;

import org.nsider.inventorymanager.extras.Nonnull;

/**
 * A plugin's own action for menu configs, written {@code [name] argument}; see
 * {@link MenuManager#registerAction}. Runs on the viewer's thread.
 */
@FunctionalInterface
public interface MenuAction {

    /**
     * @param menu     the menu it was clicked in (or opened, for open-actions)
     * @param argument the text after the action's name, placeholders filled
     */
    void run(@Nonnull Menu menu, @Nonnull String argument);
}
