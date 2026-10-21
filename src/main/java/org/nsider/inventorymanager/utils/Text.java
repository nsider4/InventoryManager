package org.nsider.inventorymanager.utils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.nsider.inventorymanager.extras.Nonnull;
import org.nsider.inventorymanager.extras.Nullable;

import java.lang.reflect.Method;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Colour codes and PlaceholderAPI. {@code &a}-style codes work everywhere; hex colours
 * ({@code &#RRGGBB}) on 1.16+, and are dropped on older servers, which can't show them.
 */
public final class Text {

    private static final Pattern HEX = Pattern.compile("&#([0-9a-fA-F]{6})");
    private static volatile Method setPlaceholders;

    private Text() {
    }

    @Nonnull
    public static String color(@Nullable String text) {
        if (text == null || text.isEmpty()) return "";
        return color(text, Versions.isAtLeast(1, 16));
    }

    @Nonnull
    static String color(@Nonnull String text, boolean hex) {
        Matcher matcher = HEX.matcher(text);
        StringBuffer result = new StringBuffer();
        while (matcher.find()) {
            String replacement = "";
            if (hex) {
                // Written as section codes directly: older ChatColor versions don't translate &x
                StringBuilder codes = new StringBuilder().append(ChatColor.COLOR_CHAR).append('x');
                for (char c : matcher.group(1).toLowerCase(Locale.ROOT).toCharArray()) codes.append(ChatColor.COLOR_CHAR).append(c);
                replacement = codes.toString();
            }
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(result);
        return ChatColor.translateAlternateColorCodes('&', result.toString());
    }

    /**
     * Fills PlaceholderAPI placeholders when it's installed; otherwise returns the text.
     * Called by reflection, so the library needs no PlaceholderAPI dependency to build.
     */
    @Nonnull
    public static String placeholders(@Nullable OfflinePlayer player, @Nonnull String text) {
        if (text.indexOf('%') < 0 || !hasPlaceholderApi()) return text;
        try {
            if (setPlaceholders == null) {
                setPlaceholders = Class.forName("me.clip.placeholderapi.PlaceholderAPI")
                        .getMethod("setPlaceholders", OfflinePlayer.class, String.class);
            }
            return (String) setPlaceholders.invoke(null, player, text);
        } catch (ReflectiveOperationException | LinkageError e) {
            return text;
        }
    }

    public static boolean hasPlaceholderApi() {
        return Bukkit.getServer() != null && Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI");
    }
}
