package org.nsider.inventorymanager.menu;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.nsider.inventorymanager.extras.Nonnull;
import org.nsider.inventorymanager.extras.Nullable;
import org.nsider.inventorymanager.item.ItemFactory;
import org.nsider.inventorymanager.utils.Text;
import org.nsider.inventorymanager.utils.Versions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * One player's copy of a menu, built from its config section each time it opens or refreshes.
 * A {@link MenuProvider} fills it in; the layout comes from the config:
 * <pre>
 * title: '&amp;8Title %page%/%pages%'
 * rows: 3                      1-6 (or type: HOPPER, DISPENSER...)
 * filler: {material: GRAY_STAINED_GLASS_PANE, name: ' '}
 * open-requirements: [...]     see {@link Conditions}; open-deny-actions run when one fails
 * open-actions: [...]
 * update-interval: 20          ticks between automatic refreshes while open (0 = never)
 * items:
 *   key:
 *     slot: 4 (or slots: [1, '10-16'])
 *     material, name, lore...  see {@link ItemFactory}
 *     view-requirements: [...] the item only shows when every line passes
 *     state: '%toggled%'       when false (or set off in code), the when-off keys replace the item's own
 *     when-off: {material: ..., name: ...}
 *     click-requirements: [...]  deny-actions run instead when one fails
 *     actions: [...]           also left-actions, right-actions, shift-left-actions, shift-right-actions,
 *                              middle-actions, drop-actions (each falls back to actions)
 * list-slots: [10, 11, '19-25']  where {@link #addEntry} entries go, a page at a time
 * lists:
 *   other: [...]              more named lists, paged together
 * templates:
 *   key: {material, name, lore, actions...}   what each kind of entry looks like
 * </pre>
 * Placeholders: {@code %player%}, {@code %page%}, {@code %pages%}, {@code %entries%},
 * {@code %entries_<list>%}, {@code %arg_1%}..., {@code %args%}, the provider's own and PlaceholderAPI's.
 * An item whose actions turn the page only shows when that page exists.
 */
public final class Menu {

    /** Inventory titles longer than this throw on 1.8. */
    private static final int LEGACY_TITLE_LIMIT = 32;
    private static final String DEFAULT_LIST = "";

    private final MenuManager manager;
    private final Player player;
    private final MenuRequest request;
    private final ConfigurationSection section;
    private final Map<String, String> values = new LinkedHashMap<>();
    private final Map<String, List<String>> lists = new HashMap<>();
    private final Map<String, List<Entry>> entries = new LinkedHashMap<>();
    private final Map<String, ClickHandler> clicks = new HashMap<>();
    private final Map<String, Boolean> states = new HashMap<>();
    private final Set<String> hidden = new HashSet<>();
    private final Map<Integer, Binding> bindings = new HashMap<>();
    private boolean cancelled;
    private int page;
    private int pages = 1;

    Menu(@Nonnull MenuManager manager, @Nonnull Player player, @Nonnull MenuRequest request, @Nonnull ConfigurationSection section) {
        this.manager = manager;
        this.player = player;
        this.request = request;
        this.section = section;
        this.page = request.getPage();
        set("%player%", player.getName());
        List<String> args = request.getArgs();
        for (int i = 0; i < args.size(); i++) set("%arg_" + (i + 1) + "%", args.get(i));
        set("%args%", String.join(" ", args));
    }

    // ---- What providers use ----

    @Nonnull
    public Player getPlayer() {
        return player;
    }

    @Nonnull
    public String getId() {
        return request.getId();
    }

    @Nonnull
    public MenuRequest getRequest() {
        return request;
    }

    @Nonnull
    public List<String> getArgs() {
        return request.getArgs();
    }

    /** @return argument {@code index} (from 0), or the fallback if there isn't one */
    @Nullable
    public String arg(int index, @Nullable String fallback) {
        List<String> args = request.getArgs();
        return index < args.size() ? args.get(index) : fallback;
    }

    /** @return data the menu was opened with ({@link MenuManager#open(Player, MenuRequest)}) */
    @Nullable
    @SuppressWarnings("unchecked")
    public <T> T getContext(@Nonnull String key) {
        return (T) request.getContext().get(key);
    }

    /** @return the page shown, from 0 (known once the menu is drawn; the requested page before) */
    public int getPage() {
        return page;
    }

    public int getPages() {
        return pages;
    }

    @Nonnull
    public ConfigurationSection getSection() {
        return section;
    }

    @Nonnull
    public MenuManager getManager() {
        return manager;
    }

    /** Sets a placeholder for the whole menu, e.g. {@code set("%points%", 12)}. */
    @Nonnull
    public Menu set(@Nonnull String placeholder, @Nullable Object value) {
        values.put(placeholder, value == null ? "" : String.valueOf(value));
        return this;
    }

    /** @return a placeholder's value, or null if it isn't set */
    @Nullable
    public String get(@Nonnull String placeholder) {
        return values.get(placeholder);
    }

    /** A lore line that is exactly this placeholder becomes these lines. */
    @Nonnull
    public Menu list(@Nonnull String placeholder, @Nonnull List<String> lines) {
        lists.put(placeholder, new ArrayList<>(lines));
        return this;
    }

    /** Adds an entry to {@code list-slots}, drawn with {@code templates.<template>}. */
    @Nonnull
    public Menu addEntry(@Nonnull String template, @Nonnull Map<String, ?> placeholders) {
        return addEntry(DEFAULT_LIST, template, placeholders, null);
    }

    @Nonnull
    public Menu addEntry(@Nonnull String template, @Nonnull Map<String, ?> placeholders, @Nullable ClickHandler click) {
        return addEntry(DEFAULT_LIST, template, placeholders, click);
    }

    /**
     * Adds an entry to a named list ({@code lists.<list>}), or to {@code list-slots} when the
     * list is empty text. The entry's placeholders also fill its template's actions.
     */
    @Nonnull
    public Menu addEntry(@Nonnull String list, @Nonnull String template, @Nonnull Map<String, ?> placeholders,
                         @Nullable ClickHandler click) {
        Map<String, String> text = new LinkedHashMap<>();
        for (Map.Entry<String, ?> entry : placeholders.entrySet()) {
            text.put(entry.getKey(), entry.getValue() == null ? "" : String.valueOf(entry.getValue()));
        }
        List<Entry> listEntries = entries.get(list);
        if (listEntries == null) {
            listEntries = new ArrayList<>();
            entries.put(list, listEntries);
        }
        listEntries.add(new Entry(template, text, click));
        return this;
    }

    /** Runs code when the item is clicked, before its configured actions. */
    @Nonnull
    public Menu onClick(@Nonnull String item, @Nonnull ClickHandler click) {
        clicks.put(item, click);
        return this;
    }

    /** Shows the item's normal look (true) or its {@code when-off} look (false), instead of its {@code state}. */
    @Nonnull
    public Menu state(@Nonnull String item, boolean on) {
        states.put(item, on);
        return this;
    }

    /** Leaves the item out. */
    @Nonnull
    public Menu hide(@Nonnull String item) {
        hidden.add(item);
        return this;
    }

    /** Doesn't open the menu (e.g. the data it needs is missing). */
    public void cancel() {
        cancelled = true;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    /** @return a text setting of this menu (e.g. a format), placeholders filled and coloured */
    @Nonnull
    public String text(@Nonnull String path, @Nonnull String fallback) {
        return resolver(Collections.<String, String>emptyMap()).text(section.getString(path, fallback));
    }

    // ---- Navigation, for code click handlers ----

    /** Opens another menu; {@code [back]} returns here. */
    public void open(@Nonnull String id, @Nonnull String... args) {
        manager.openFrom(this, MenuRequest.of(id, args));
    }

    public void refresh() {
        manager.refresh(this);
    }

    public void close() {
        manager.close(player);
    }

    // ---- Drawing ----

    /** The drawn menu. */
    static final class Rendered {
        final String title;
        final InventoryType type;
        final int size;
        final ItemStack[] contents;

        Rendered(String title, InventoryType type, int size, ItemStack[] contents) {
            this.title = title;
            this.type = type;
            this.size = size;
            this.contents = contents;
        }
    }

    @Nonnull
    Rendered render() {
        InventoryType type = inventoryType();
        int size = type == InventoryType.CHEST ? Math.max(1, Math.min(6, section.getInt("rows", 3))) * 9 : type.getDefaultSize();

        Map<String, List<Integer>> listSlots = new LinkedHashMap<>();
        pages = 1;
        for (String list : listNames()) {
            List<Integer> slots = list.equals(DEFAULT_LIST) ? Slots.at(section, "list-slots") : Slots.at(section, "lists." + list);
            listSlots.put(list, slots);
            int count = entries.containsKey(list) ? entries.get(list).size() : 0;
            if (!slots.isEmpty()) pages = Math.max(pages, (count + slots.size() - 1) / slots.size());
            set(list.equals(DEFAULT_LIST) ? "%entries%" : "%entries_" + list + "%", count);
        }
        page = Math.max(0, Math.min(request.getPage(), pages - 1));
        set("%page%", page + 1).set("%pages%", pages);

        ItemStack[] contents = new ItemStack[size];
        bindings.clear();
        ConfigurationSection filler = section.getConfigurationSection("filler");
        if (filler != null) {
            ItemStack fill = manager.getItemFactory().build(filler, resolver(Collections.<String, String>emptyMap()));
            for (int slot = 0; slot < size; slot++) contents[slot] = fill;
        }

        ConfigurationSection items = section.getConfigurationSection("items");
        if (items != null) {
            for (String key : items.getKeys(false)) {
                ConfigurationSection item = items.getConfigurationSection(key);
                if (item == null || hidden.contains(key)) continue;
                place(contents, item, Slots.of(item), Collections.<String, String>emptyMap(), clicks.get(key), states.get(key));
            }
        }

        for (Map.Entry<String, List<Integer>> list : listSlots.entrySet()) {
            List<Integer> slots = list.getValue();
            List<Entry> listEntries = entries.get(list.getKey());
            if (slots.isEmpty() || listEntries == null) continue;
            for (int i = 0; i < slots.size(); i++) {
                int index = page * slots.size() + i;
                if (index >= listEntries.size()) break;
                Entry entry = listEntries.get(index);
                ConfigurationSection template = section.getConfigurationSection("templates." + entry.template);
                if (template == null) {
                    manager.getLogger().warning("Menu '" + getId() + "' has no template '" + entry.template + "'.");
                    continue;
                }
                place(contents, template, Collections.singletonList(slots.get(i)), entry.values, entry.click, null);
            }
        }

        String title = resolver(Collections.<String, String>emptyMap()).text(section.getString("title", ""));
        if (!Versions.isAtLeast(1, 9) && title.length() > LEGACY_TITLE_LIMIT) title = title.substring(0, LEGACY_TITLE_LIMIT);
        return new Rendered(title, type, size, contents);
    }

    private Set<String> listNames() {
        Set<String> names = new java.util.LinkedHashSet<>();
        names.add(DEFAULT_LIST);
        ConfigurationSection named = section.getConfigurationSection("lists");
        if (named != null) names.addAll(named.getKeys(false));
        names.addAll(entries.keySet());
        return names;
    }

    private InventoryType inventoryType() {
        String name = section.getString("type");
        if (name == null) return InventoryType.CHEST;
        try {
            InventoryType type = InventoryType.valueOf(name.trim().toUpperCase(Locale.ROOT));
            return type.getDefaultSize() > 0 ? type : InventoryType.CHEST;
        } catch (IllegalArgumentException e) {
            manager.getLogger().warning("Menu '" + getId() + "': unknown type '" + name + "'; using a chest.");
            return InventoryType.CHEST;
        }
    }

    private void place(ItemStack[] contents, ConfigurationSection item, List<Integer> slots, Map<String, String> extra,
                       ClickHandler click, Boolean state) {
        ItemFactory.Resolver resolver = resolver(extra);
        if (!passes(item.getStringList("view-requirements"), resolver)) return;
        if (!pageExists(item)) return;

        boolean on = state != null ? state : !item.contains("state") || Conditions.test(resolver.raw(item.getString("state", "true")), player::hasPermission);
        ConfigurationSection look = item;
        ConfigurationSection off = item.getConfigurationSection("when-off");
        if (!on && off != null) look = overlay(item, off);

        ItemStack stack = manager.getItemFactory().build(look, resolver);
        for (int slot : slots) {
            if (slot < 0 || slot >= contents.length) continue;
            contents[slot] = stack;
            bindings.put(slot, new Binding(look, extra, click));
        }
    }

    /** Page buttons hide when there's no page to turn to, unless they set always-show. */
    private boolean pageExists(ConfigurationSection item) {
        if (item.getBoolean("always-show", false)) return true;
        for (String path : ACTION_PATHS) {
            for (String action : item.getStringList(path)) {
                String name = action.trim().toLowerCase(Locale.ROOT);
                if (name.startsWith("[next-page]") && page >= pages - 1) return false;
                if (name.startsWith("[previous-page]") && page <= 0) return false;
            }
        }
        return true;
    }

    private static final String[] ACTION_PATHS = {"actions", "left-actions", "right-actions", "shift-left-actions",
            "shift-right-actions", "middle-actions", "drop-actions"};

    /** @return the base's keys with the override's on top (when-off itself left out) */
    @Nonnull
    static ConfigurationSection overlay(@Nonnull ConfigurationSection base, @Nonnull ConfigurationSection override) {
        MemoryConfiguration merged = new MemoryConfiguration();
        copy(base, merged);
        copy(override, merged);
        return merged;
    }

    private static void copy(ConfigurationSection from, MemoryConfiguration to) {
        for (String key : from.getKeys(false)) {
            if (key.equals("when-off")) continue;
            Object value = from.get(key);
            if (value instanceof ConfigurationSection) {
                to.createSection(key, ((ConfigurationSection) value).getValues(true));
            } else {
                to.set(key, value);
            }
        }
    }

    boolean passes(List<String> conditions, ItemFactory.Resolver resolver) {
        if (conditions.isEmpty()) return true;
        List<String> filled = new ArrayList<>(conditions.size());
        for (String condition : conditions) filled.add(resolver.raw(condition));
        return Conditions.all(filled, player::hasPermission);
    }

    // ---- Clicks ----

    void click(int slot, @Nonnull ClickType type) {
        Binding binding = bindings.get(slot);
        if (binding == null) return;
        ItemFactory.Resolver resolver = resolver(binding.values);
        if (!passes(binding.section.getStringList("click-requirements"), resolver)) {
            manager.runActions(this, resolve(binding.section.getStringList("deny-actions"), resolver));
            return;
        }
        if (binding.click != null) binding.click.click(this, type);
        manager.runActions(this, resolve(actionsFor(binding.section, type), resolver));
    }

    @Nonnull
    static List<String> actionsFor(@Nonnull ConfigurationSection item, @Nonnull ClickType type) {
        List<String> paths = new ArrayList<>();
        switch (type.name()) {
            case "SHIFT_LEFT": paths.add("shift-left-actions"); paths.add("left-actions"); break;
            case "SHIFT_RIGHT": paths.add("shift-right-actions"); paths.add("right-actions"); break;
            case "LEFT": case "DOUBLE_CLICK": paths.add("left-actions"); break;
            case "RIGHT": paths.add("right-actions"); break;
            case "MIDDLE": paths.add("middle-actions"); break;
            case "DROP": case "CONTROL_DROP": paths.add("drop-actions"); break;
            default: break;
        }
        paths.add("actions");
        for (String path : paths) {
            if (item.isList(path)) return item.getStringList(path);
        }
        return Collections.emptyList();
    }

    List<String> resolve(List<String> lines, ItemFactory.Resolver resolver) {
        List<String> filled = new ArrayList<>(lines.size());
        for (String line : lines) filled.add(resolver.raw(line));
        return filled;
    }

    /** Fills the menu's placeholders (and an entry's own, which win) into config text. */
    @Nonnull
    ItemFactory.Resolver resolver(@Nonnull final Map<String, String> extra) {
        return new ItemFactory.Resolver() {
            @Nonnull
            @Override
            public String raw(@Nonnull String text) {
                return Text.placeholders(player, replace(text, extra));
            }

            @Nonnull
            @Override
            public String text(@Nonnull String text) {
                return Text.color(raw(text));
            }

            @Nonnull
            @Override
            public List<String> lines(@Nonnull String line) {
                // An entry can point its own placeholder at a list, e.g. %top% -> %top_math%
                List<String> expanded = lists.get(replace(line, extra).trim());
                if (expanded == null) return Collections.singletonList(text(line));
                List<String> result = new ArrayList<>(expanded.size());
                for (String part : expanded) result.add(text(part));
                return result;
            }
        };
    }

    private String replace(String text, Map<String, String> extra) {
        if (text == null) return "";
        if (text.indexOf('%') < 0) return text;
        String result = text;
        for (Map.Entry<String, String> entry : extra.entrySet()) result = result.replace(entry.getKey(), entry.getValue());
        for (Map.Entry<String, String> entry : values.entrySet()) result = result.replace(entry.getKey(), entry.getValue());
        return result;
    }

    private static final class Entry {
        final String template;
        final Map<String, String> values;
        final ClickHandler click;

        Entry(String template, Map<String, String> values, ClickHandler click) {
            this.template = template;
            this.values = values;
            this.click = click;
        }
    }

    private static final class Binding {
        final ConfigurationSection section;
        final Map<String, String> values;
        final ClickHandler click;

        Binding(ConfigurationSection section, Map<String, String> values, ClickHandler click) {
            this.section = section;
            this.values = values;
            this.click = click;
        }
    }
}
