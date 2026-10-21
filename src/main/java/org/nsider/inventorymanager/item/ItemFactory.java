package org.nsider.inventorymanager.item;

import com.cryptomorin.xseries.XEnchantment;
import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.profiles.builder.XSkull;
import com.cryptomorin.xseries.profiles.objects.Profileable;
import org.bukkit.Color;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.nsider.inventorymanager.extras.Nonnull;
import org.nsider.inventorymanager.extras.Nullable;
import org.nsider.inventorymanager.utils.Text;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * Builds an item from a config section. Every key is optional:
 * <pre>
 * material: DIAMOND_SWORD     any name from 1.8 to 26.x, 'WOOL:14', '35:14', or 'head:&lt;player|texture|url&gt;'
 * amount: 1
 * name: '&amp;aName'
 * lore: ['line', ...]
 * glow: true                  enchanted look, without showing an enchantment
 * enchantments: ['SHARPNESS:2', ...]
 * flags: [HIDE_ATTRIBUTES, ...]   or [ALL]; default hides enchantments and attributes
 * head: 'Notch'               a player name, UUID, base64 texture or textures.minecraft.net URL
 * color: '#FF0000'            leather armour colour ('255,0,0' also works)
 * custom-model-data: 1001     1.14+
 * item-model: 'namespace:key' 1.21.2+
 * unbreakable: true
 * </pre>
 * Settings a server's version doesn't have are skipped quietly.
 */
public final class ItemFactory {

    private static final Pattern PLAYER_NAME = Pattern.compile("[A-Za-z0-9_]{1,16}");
    private static final List<String> DEFAULT_FLAGS = java.util.Arrays.asList("HIDE_ENCHANTS", "HIDE_ATTRIBUTES");

    /** Fills a menu's placeholders into config text. */
    public interface Resolver {
        /** @return the text with placeholders filled but no colours, for materials, heads and numbers */
        @Nonnull
        String raw(@Nonnull String text);

        /** @return the text ready to show: placeholders filled and coloured */
        @Nonnull
        String text(@Nonnull String text);

        /** @return the lines one lore line becomes (a list placeholder can be several) */
        @Nonnull
        List<String> lines(@Nonnull String line);
    }

    /** Colours only; no placeholders. */
    public static final Resolver PLAIN = new Resolver() {
        @Nonnull
        @Override
        public String raw(@Nonnull String text) {
            return text;
        }

        @Nonnull
        @Override
        public String text(@Nonnull String text) {
            return Text.color(text);
        }

        @Nonnull
        @Override
        public List<String> lines(@Nonnull String line) {
            return Collections.singletonList(Text.color(line));
        }
    };

    private final Logger logger;

    /**
     * @param logger where unknown materials and bad values are reported
     */
    public ItemFactory(@Nonnull Logger logger) {
        this.logger = logger;
    }

    @Nonnull
    public ItemStack build(@Nonnull ConfigurationSection section, @Nonnull Resolver resolver) {
        String materialName = resolver.raw(section.getString("material", "STONE")).trim();
        String head = section.getString("head", section.getString("skull-owner"));
        ItemStack item;
        if (materialName.toLowerCase(Locale.ROOT).startsWith("head:") || materialName.toLowerCase(Locale.ROOT).startsWith("head-")) {
            head = materialName.substring(5);
            item = XMaterial.PLAYER_HEAD.parseItem();
        } else {
            item = Materials.item(materialName);
            if (item == null) {
                logger.warning("'" + materialName + "' (" + section.getCurrentPath() + ") is not an item on this server version; showing stone.");
                item = XMaterial.STONE.parseItem();
            }
        }
        item.setAmount(Math.max(1, Math.min(64, number(resolver.raw(section.getString("amount", "1")), 1))));

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;
        if (section.contains("name")) meta.setDisplayName(resolver.text(section.getString("name", "")));
        List<String> lore = new ArrayList<>();
        for (String line : section.getStringList("lore")) lore.addAll(resolver.lines(line));
        if (!lore.isEmpty()) meta.setLore(lore);

        for (String entry : section.getStringList("enchantments")) enchant(meta, resolver.raw(entry));
        if (isTrue(resolver.raw(section.getString("glow", "false")))) glow(meta);
        List<String> flags = section.isList("flags") ? section.getStringList("flags") : DEFAULT_FLAGS;
        for (String flag : flags) flag(meta, flag);

        if (section.contains("custom-model-data")) {
            invoke(meta, "setCustomModelData", Integer.class, number(resolver.raw(section.getString("custom-model-data")), 0));
        }
        if (section.contains("item-model")) itemModel(meta, resolver.raw(section.getString("item-model")));
        if (section.getBoolean("unbreakable", false)) unbreakable(meta);
        if (section.contains("color") && meta instanceof LeatherArmorMeta) {
            Color color = color(resolver.raw(section.getString("color")));
            if (color != null) ((LeatherArmorMeta) meta).setColor(color);
        }
        item.setItemMeta(meta);

        if (head != null && !head.trim().isEmpty() && item.getItemMeta() instanceof SkullMeta) {
            item = head(item, resolver.raw(head).trim());
        }
        return item;
    }

    private void enchant(ItemMeta meta, String entry) {
        String[] parts = entry.trim().split("[: ]", 2);
        Optional<XEnchantment> enchantment = XEnchantment.of(parts[0]);
        Enchantment bukkit = enchantment.isPresent() ? enchantment.get().getEnchant() : null;
        if (bukkit == null) {
            logger.warning("Unknown enchantment '" + parts[0] + "' on this server version.");
            return;
        }
        meta.addEnchant(bukkit, parts.length > 1 ? number(parts[1].trim(), 1) : 1, true);
    }

    private static void glow(ItemMeta meta) {
        // 1.20.5+ has a real glint switch; older versions need a hidden enchantment
        if (invoke(meta, "setEnchantmentGlintOverride", Boolean.class, Boolean.TRUE)) return;
        Enchantment unbreaking = XEnchantment.UNBREAKING.getEnchant();
        if (unbreaking != null) meta.addEnchant(unbreaking, 1, true);
        flag(meta, "HIDE_ENCHANTS");
    }

    private static void flag(ItemMeta meta, String name) {
        String wanted = name.trim().toUpperCase(Locale.ROOT).replace(' ', '_');
        if (wanted.equals("HIDE_ENCHANTMENTS")) wanted = "HIDE_ENCHANTS";
        for (ItemFlag flag : ItemFlag.values()) {
            if (wanted.equals("ALL") || flag.name().equals(wanted)) meta.addItemFlags(flag);
        }
    }

    private static void itemModel(ItemMeta meta, String key) {
        try {
            Class<?> namespaced = Class.forName("org.bukkit.NamespacedKey");
            Object value = namespaced.getMethod("fromString", String.class).invoke(null, key);
            if (value != null) meta.getClass().getMethod("setItemModel", namespaced).invoke(meta, value);
        } catch (ReflectiveOperationException | LinkageError ignored) {
            // Before 1.21.2
        }
    }

    private static void unbreakable(ItemMeta meta) {
        if (invoke(meta, "setUnbreakable", boolean.class, true)) return;
        try {
            Object spigot = meta.getClass().getMethod("spigot").invoke(meta);
            spigot.getClass().getMethod("setUnbreakable", boolean.class).invoke(spigot, true);
        } catch (ReflectiveOperationException | LinkageError ignored) {
            // Not available on this server
        }
    }

    @SuppressWarnings("deprecation")
    private ItemStack head(ItemStack item, String value) {
        if (PLAYER_NAME.matcher(value).matches()) {
            // A name is set directly and the client loads the skin, so nothing waits on Mojang
            SkullMeta meta = (SkullMeta) item.getItemMeta();
            meta.setOwner(value);
            item.setItemMeta(meta);
            return item;
        }
        try {
            return XSkull.of(item).profile(Profileable.detect(value)).lenient().apply();
        } catch (RuntimeException e) {
            logger.warning("Couldn't apply head '" + value + "': " + e.getMessage());
            return item;
        }
    }

    private static boolean invoke(ItemMeta meta, String method, Class<?> type, Object value) {
        try {
            Method setter = meta.getClass().getMethod(method, type);
            setter.invoke(meta, value);
            return true;
        } catch (ReflectiveOperationException | LinkageError e) {
            return false;
        }
    }

    @Nullable
    static Color color(@Nonnull String value) {
        String text = value.trim();
        try {
            if (text.startsWith("#")) return Color.fromRGB(Integer.parseInt(text.substring(1), 16));
            String[] rgb = text.split("\\s*,\\s*");
            if (rgb.length == 3) return Color.fromRGB(Integer.parseInt(rgb[0]), Integer.parseInt(rgb[1]), Integer.parseInt(rgb[2]));
        } catch (IllegalArgumentException ignored) {
            // Bad colour: left as is
        }
        return null;
    }

    static int number(String text, int fallback) {
        try {
            return Integer.parseInt(text.trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    static boolean isTrue(String text) {
        String value = text.trim().toLowerCase(Locale.ROOT);
        return value.equals("true") || value.equals("yes") || value.equals("on") || value.equals("1");
    }
}
