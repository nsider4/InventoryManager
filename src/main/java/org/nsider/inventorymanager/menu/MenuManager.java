package org.nsider.inventorymanager.menu;

import com.cryptomorin.xseries.XSound;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.Plugin;
import org.nsider.inventorymanager.extras.Nonnull;
import org.nsider.inventorymanager.extras.Nullable;
import org.nsider.inventorymanager.item.ItemFactory;
import org.nsider.inventorymanager.utils.Text;

import java.io.File;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Loads menus from config, opens them, and runs their clicks. One per plugin:
 * <pre>
 * MenuManager menus = new MenuManager(plugin);
 * menus.load(getConfig().getConfigurationSection("menus"));   // every child section is a menu
 * menus.registerMenu("stats", menu -&gt; menu.set("%kills%", kills(menu.getPlayer())));
 * menus.registerAction("myplugin:reset", (menu, argument) -&gt; reset(menu.getPlayer(), argument));
 * menus.open(player, "stats");
 * </pre>
 * A menu with no provider opens straight from its config, so server owners can add their own.
 *
 * <p>Actions, written {@code [name] argument} in an item's {@code actions} list:</p>
 * <pre>
 * [command] spawn             the viewer runs a command    [console] give %player% diamond 1
 * [message] &amp;aHi             to the viewer                 [broadcast] &amp;aHi everyone
 * [sound] UI_BUTTON_CLICK 1 1 any 1.8-26.x sound name    [open] menu-id [args...]
 * [switch] menu-id [args...]  like open, but [back] skips this menu (tabs)
 * [back]                      the menu before (or closes)  [refresh]   [close]
 * [next-page] [previous-page] [page] 2                     [wait] 20   ticks before the rest run
 * [plugin:action] argument    one a plugin registered
 * </pre>
 */
public final class MenuManager {

    private final Plugin plugin;
    private final Logger logger;
    private final ItemFactory items;
    private volatile TaskRunner runner;
    private final Map<String, ConfigurationSection> configs = new ConcurrentHashMap<>();
    private final Map<String, MenuProvider> providers = new ConcurrentHashMap<>();
    private final Map<String, MenuAction> actions = new ConcurrentHashMap<>();
    private final Map<UUID, Deque<MenuRequest>> history = new ConcurrentHashMap<>();
    private final Map<UUID, MenuHolder> open = new ConcurrentHashMap<>();
    private final Map<UUID, Long> lastClick = new ConcurrentHashMap<>();
    private volatile long clickCooldownMillis = 150L;

    public MenuManager(@Nonnull Plugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.items = new ItemFactory(logger);
        this.runner = TaskRunner.standard(plugin);
        Bukkit.getPluginManager().registerEvents(new MenuListener(this), plugin);
    }

    // ---- Setup ----

    /** Replaces every loaded menu with the child sections of {@code root} (null clears them). */
    public void load(@Nullable ConfigurationSection root) {
        configs.clear();
        if (root == null) return;
        for (String id : root.getKeys(false)) {
            ConfigurationSection menu = root.getConfigurationSection(id);
            if (menu != null) configs.put(key(id), menu);
        }
    }

    /**
     * Adds every {@code .yml} file in a folder as a menu named after the file
     * ({@code shop.yml} is {@code shop}), replacing a loaded menu of the same name.
     *
     * @return how many were loaded
     */
    public int loadFolder(@Nonnull File folder) {
        File[] files = folder.listFiles((dir, name) -> name.toLowerCase(Locale.ROOT).endsWith(".yml"));
        if (files == null) return 0;
        for (File file : files) {
            String name = file.getName();
            configs.put(key(name.substring(0, name.length() - 4)), YamlConfiguration.loadConfiguration(file));
        }
        return files.length;
    }

    /** Adds or replaces one menu. */
    public void addMenu(@Nonnull String id, @Nonnull ConfigurationSection config) {
        configs.put(key(id), config);
    }

    public void registerMenu(@Nonnull String id, @Nonnull MenuProvider provider) {
        providers.put(key(id), provider);
    }

    /**
     * @param name used as {@code [name] argument} in configs; prefix it with your plugin
     *             ({@code myplugin:reset}) so it can't clash with a built-in action
     */
    public void registerAction(@Nonnull String name, @Nonnull MenuAction action) {
        actions.put(actionKey(name), action);
    }

    public void setTaskRunner(@Nonnull TaskRunner runner) {
        this.runner = runner;
    }

    /** Clicks closer together than this are ignored (default 150 ms), so a double click can't run actions twice. */
    public void setClickCooldownMillis(long millis) {
        this.clickCooldownMillis = Math.max(0L, millis);
    }

    public boolean hasMenu(@Nonnull String id) {
        return configs.containsKey(key(id));
    }

    @Nonnull
    public Set<String> getMenuIds() {
        return Collections.unmodifiableSet(new TreeSet<>(configs.keySet()));
    }

    @Nullable
    public ConfigurationSection getConfig(@Nonnull String id) {
        return configs.get(key(id));
    }

    @Nonnull
    public ItemFactory getItemFactory() {
        return items;
    }

    @Nonnull
    public Plugin getPlugin() {
        return plugin;
    }

    @Nonnull
    Logger getLogger() {
        return logger;
    }

    @Nonnull
    public TaskRunner getTaskRunner() {
        return runner;
    }

    // ---- Opening ----

    /**
     * Opens a menu on the player's next tick. It starts a new history, so {@code [back]} from
     * it closes the menu.
     *
     * @return false if there is no menu with that id
     */
    public boolean open(@Nonnull Player player, @Nonnull String id, @Nonnull String... args) {
        return open(player, MenuRequest.of(id, args));
    }

    public boolean open(@Nonnull Player player, @Nonnull MenuRequest request) {
        // No console warning: this is usually a player's typo, and the caller tells them
        if (!hasMenu(request.getId())) return false;
        history.remove(player.getUniqueId());
        show(player, request);
        return true;
    }

    /** Opens a menu from another, which {@code [back]} returns to. */
    void openFrom(@Nonnull Menu from, @Nonnull MenuRequest request) {
        Player player = from.getPlayer();
        if (!hasMenu(request.getId())) {
            logger.warning("Menu '" + from.getId() + "' tried to open '" + request.getId() + "', which doesn't exist.");
            return;
        }
        Deque<MenuRequest> stack = history.get(player.getUniqueId());
        if (stack == null) {
            stack = new ArrayDeque<>();
            history.put(player.getUniqueId(), stack);
        }
        stack.push(from.getRequest().withPage(from.getPage()));
        show(player, request);
    }

    void back(@Nonnull Menu from, @Nonnull String fallback) {
        Deque<MenuRequest> stack = history.get(from.getPlayer().getUniqueId());
        MenuRequest previous = stack == null ? null : stack.poll();
        if (previous != null) {
            show(from.getPlayer(), previous);
        } else if (!fallback.isEmpty()) {
            String[] parts = fallback.split("\\s+");
            show(from.getPlayer(), MenuRequest.of(parts[0], Arrays.copyOfRange(parts, 1, parts.length)));
        } else {
            close(from.getPlayer());
        }
    }

    void refresh(@Nonnull Menu menu) {
        show(menu.getPlayer(), menu.getRequest().withPage(menu.getPage()));
    }

    void page(@Nonnull Menu menu, int page) {
        show(menu.getPlayer(), menu.getRequest().withPage(page));
    }

    void close(@Nonnull Player player) {
        runner.runLater(player, 1L, player::closeInventory);
    }

    /** Closes every open menu, e.g. when the plugin disables. */
    public void closeAll() {
        for (UUID id : new ArrayList<>(open.keySet())) {
            Player player = Bukkit.getPlayer(id);
            if (player != null) {
                try {
                    player.closeInventory();
                } catch (RuntimeException ignored) {
                    // Folia may refuse from this thread; the inventory goes when the plugin's holder does
                }
            }
        }
        open.clear();
        history.clear();
    }

    /** The next tick, on the player's thread: never switch inventories inside a click event. */
    private void show(Player player, MenuRequest request) {
        runner.runLater(player, 1L, () -> display(player, request));
    }

    private void display(Player player, MenuRequest request) {
        if (!player.isOnline()) return;
        ConfigurationSection config = configs.get(key(request.getId()));
        if (config == null) return;
        Menu menu = new Menu(this, player, request, config);
        ItemFactory.Resolver resolver = menu.resolver(Collections.<String, String>emptyMap());
        if (!menu.passes(config.getStringList("open-requirements"), resolver)) {
            runActions(menu, menu.resolve(config.getStringList("open-deny-actions"), resolver));
            return;
        }
        MenuProvider provider = providers.get(key(request.getId()));
        if (provider != null) {
            try {
                provider.build(menu);
            } catch (RuntimeException e) {
                logger.log(Level.SEVERE, "Menu '" + request.getId() + "' failed to build", e);
                return;
            }
        }
        if (menu.isCancelled()) return;

        Menu.Rendered rendered = menu.render();
        MenuHolder current = open.get(player.getUniqueId());
        // A refresh, page turn or update of the same menu doesn't count as opening it again
        boolean reopened = current != null && current.getMenu().getId().equalsIgnoreCase(request.getId());
        if (current != null && current.getInventory() != null && current.getTitle().equals(rendered.title)
                && current.getInventory().getSize() == rendered.size && current.getInventory().getType() == rendered.type) {
            // Same window: swap the contents in place, which keeps the cursor where it is
            current.setMenu(menu);
            current.getInventory().setContents(rendered.contents);
        } else {
            MenuHolder holder = new MenuHolder(menu, rendered.title);
            Inventory inventory = rendered.type == InventoryType.CHEST
                    ? Bukkit.createInventory(holder, rendered.size, rendered.title)
                    : Bukkit.createInventory(holder, rendered.type, rendered.title);
            inventory.setContents(rendered.contents);
            holder.setInventory(inventory);
            player.openInventory(inventory);
            open.put(player.getUniqueId(), holder);
            current = holder;
        }
        if (!reopened) runActions(menu, menu.resolve(config.getStringList("open-actions"), resolver));
        int interval = config.getInt("update-interval", 0);
        if (interval > 0) scheduleUpdate(player, current, menu, interval);
    }

    private void scheduleUpdate(Player player, MenuHolder holder, Menu menu, int interval) {
        runner.runLater(player, interval, () -> {
            if (open.get(player.getUniqueId()) == holder && holder.getMenu() == menu) {
                display(player, menu.getRequest().withPage(menu.getPage()));
            }
        });
    }

    // ---- Events ----

    void click(@Nonnull Player player, @Nonnull MenuHolder holder, int slot, @Nonnull ClickType type) {
        long now = System.currentTimeMillis();
        Long last = lastClick.put(player.getUniqueId(), now);
        if (last != null && now - last < clickCooldownMillis) return;
        Menu menu = holder.getMenu();
        try {
            menu.click(slot, type);
        } catch (RuntimeException e) {
            logger.log(Level.SEVERE, "A click in menu '" + menu.getId() + "' failed", e);
        }
    }

    void closed(@Nonnull Player player, @Nonnull MenuHolder holder) {
        open.remove(player.getUniqueId(), holder);
    }

    void forget(@Nonnull UUID player) {
        open.remove(player);
        history.remove(player);
        lastClick.remove(player);
    }

    // ---- Actions ----

    /** Runs action lines (placeholders already filled) in order. */
    public void runActions(@Nonnull Menu menu, @Nonnull List<String> lines) {
        runActions(menu, lines, 0);
    }

    private void runActions(Menu menu, List<String> lines, int start) {
        Player player = menu.getPlayer();
        for (int i = start; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            if (line.isEmpty()) continue;
            if (!line.startsWith("[") || line.indexOf(']') < 0) {
                logger.warning("Menu '" + menu.getId() + "': '" + line + "' is not an action; write it as [name] argument.");
                continue;
            }
            int end = line.indexOf(']');
            String name = actionKey(line.substring(1, end));
            String argument = line.substring(end + 1).trim();
            if (name.equals("wait") || name.equals("delay")) {
                int next = i + 1;
                runner.runLater(player, Math.max(1, number(argument, 1)), () -> {
                    if (player.isOnline()) runActions(menu, lines, next);
                });
                return;
            }
            try {
                run(menu, name, argument);
            } catch (RuntimeException e) {
                logger.log(Level.SEVERE, "Menu action '" + line + "' failed", e);
            }
        }
    }

    private void run(Menu menu, String name, String argument) {
        Player player = menu.getPlayer();
        switch (name) {
            case "command":
            case "player":
                player.performCommand(argument.startsWith("/") ? argument.substring(1) : argument);
                return;
            case "console":
                String command = argument.startsWith("/") ? argument.substring(1) : argument;
                runner.runGlobal(() -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command));
                return;
            case "message":
                player.sendMessage(Text.color(argument));
                return;
            case "broadcast":
                String message = Text.color(argument);
                runner.runGlobal(() -> Bukkit.broadcastMessage(message));
                return;
            case "sound":
                sound(player, argument);
                return;
            case "open":
                String[] parts = argument.split("\\s+");
                if (parts[0].isEmpty()) return;
                openFrom(menu, MenuRequest.of(parts[0], Arrays.copyOfRange(parts, 1, parts.length)));
                return;
            case "switch":
                String[] target = argument.split("\\s+");
                if (target[0].isEmpty() || !hasMenu(target[0])) {
                    logger.warning("Menu '" + menu.getId() + "' tried to switch to '" + target[0] + "', which doesn't exist.");
                    return;
                }
                show(player, MenuRequest.of(target[0], Arrays.copyOfRange(target, 1, target.length)));
                return;
            case "back":
                back(menu, argument);
                return;
            case "refresh":
                refresh(menu);
                return;
            case "close":
                close(player);
                return;
            case "next-page":
                page(menu, menu.getPage() + 1);
                return;
            case "previous-page":
                page(menu, menu.getPage() - 1);
                return;
            case "page":
                page(menu, number(argument, 1) - 1);
                return;
            default:
                MenuAction action = actions.get(name);
                if (action == null) {
                    logger.warning("Menu '" + menu.getId() + "': there is no action called [" + name + "].");
                } else {
                    action.run(menu, argument);
                }
        }
    }

    private void sound(Player player, String argument) {
        String[] parts = argument.split("\\s+");
        Optional<XSound> sound = XSound.matchXSound(parts[0]);
        if (!sound.isPresent() || !sound.get().isSupported()) {
            logger.warning("Unknown sound '" + parts[0] + "' on this server version.");
            return;
        }
        float volume = parts.length > 1 ? (float) decimal(parts[1], 1) : 1f;
        float pitch = parts.length > 2 ? (float) decimal(parts[2], 1) : 1f;
        sound.get().play(player, volume, pitch);
    }

    private static String key(String id) {
        return id.trim().toLowerCase(Locale.ROOT);
    }

    private static String actionKey(String name) {
        String key = name.trim().toLowerCase(Locale.ROOT);
        if (key.startsWith("[") && key.endsWith("]")) key = key.substring(1, key.length() - 1).trim();
        return key;
    }

    private static int number(String text, int fallback) {
        try {
            return Integer.parseInt(text.trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private static double decimal(String text, double fallback) {
        try {
            return Double.parseDouble(text.trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }
}
