package org.nsider.inventorymanager.menu;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.nsider.inventorymanager.extras.Nonnull;

import java.util.function.Consumer;

/**
 * Where menu work runs. The default works on Spigot, Paper and Folia; a plugin with its own
 * scheduler wrapper can pass one in with {@link MenuManager#setTaskRunner}.
 */
public interface TaskRunner {

    /** Runs on the player's thread after the delay (at least one tick). */
    void runLater(@Nonnull Player player, long ticks, @Nonnull Runnable task);

    /** Runs where console commands may run (the global region on Folia). */
    void runGlobal(@Nonnull Runnable task);

    @Nonnull
    static TaskRunner standard(@Nonnull Plugin plugin) {
        return isFolia() ? new Folia(plugin) : new Standard(plugin);
    }

    static boolean isFolia() {
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    /** Spigot and Paper: the main thread. */
    final class Standard implements TaskRunner {
        private final Plugin plugin;

        Standard(Plugin plugin) {
            this.plugin = plugin;
        }

        @Override
        public void runLater(@Nonnull Player player, long ticks, @Nonnull Runnable task) {
            Bukkit.getScheduler().runTaskLater(plugin, task, Math.max(1L, ticks));
        }

        @Override
        public void runGlobal(@Nonnull Runnable task) {
            if (Bukkit.isPrimaryThread()) {
                task.run();
            } else {
                Bukkit.getScheduler().runTask(plugin, task);
            }
        }
    }

    /** Folia, through reflection because the library is built against the 1.8 API. */
    final class Folia implements TaskRunner {
        private final Plugin plugin;

        Folia(Plugin plugin) {
            this.plugin = plugin;
        }

        @Override
        public void runLater(@Nonnull Player player, long ticks, @Nonnull Runnable task) {
            try {
                // Looked up on the public interfaces: the implementations may not be accessible
                Object scheduler = org.bukkit.entity.Entity.class.getMethod("getScheduler").invoke(player);
                Consumer<Object> consumer = scheduled -> task.run();
                Class.forName("io.papermc.paper.threadedregions.scheduler.EntityScheduler").getMethod("runDelayed", Plugin.class, Consumer.class, Runnable.class, long.class)
                        .invoke(scheduler, plugin, consumer, null, Math.max(1L, ticks));
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException("Couldn't schedule a menu task on Folia", e);
            }
        }

        @Override
        public void runGlobal(@Nonnull Runnable task) {
            try {
                Object scheduler = Bukkit.class.getMethod("getGlobalRegionScheduler").invoke(null);
                Class.forName("io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler").getMethod("execute", Plugin.class, Runnable.class).invoke(scheduler, plugin, task);
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException("Couldn't schedule a global task on Folia", e);
            }
        }
    }
}
