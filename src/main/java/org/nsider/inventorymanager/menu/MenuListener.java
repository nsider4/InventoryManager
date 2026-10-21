package org.nsider.inventorymanager.menu;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.InventoryHolder;

/**
 * Cancels every click and drag while a menu is open (items can never be taken or put in),
 * and passes clicks on the menu's own slots to it. Uses {@code event.getInventory()}, the top
 * inventory, never {@code getView()}: InventoryView became an interface in 1.21, so code built
 * against older versions breaks on it.
 */
final class MenuListener implements Listener {

    private final MenuManager manager;

    MenuListener(MenuManager manager) {
        this.manager = manager;
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onClick(InventoryClickEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();
        if (!(holder instanceof MenuHolder)) return;
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player)) return;
        int slot = event.getRawSlot();
        if (slot < 0 || slot >= event.getInventory().getSize()) return;
        manager.click((Player) event.getWhoClicked(), (MenuHolder) holder, slot, event.getClick());
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onDrag(InventoryDragEvent event) {
        if (event.getInventory().getHolder() instanceof MenuHolder) event.setCancelled(true);
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();
        if (holder instanceof MenuHolder && event.getPlayer() instanceof Player) {
            manager.closed((Player) event.getPlayer(), (MenuHolder) holder);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        manager.forget(event.getPlayer().getUniqueId());
    }
}
