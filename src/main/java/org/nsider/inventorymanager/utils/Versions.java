package org.nsider.inventorymanager.utils;

import org.bukkit.Bukkit;
import org.nsider.inventorymanager.extras.Nonnull;

/**
 * The server's Minecraft version. Handles both numbering schemes: {@code 1.8.8} to {@code 1.21.x},
 * and the year-based {@code 26.1} onwards, which is newer than every {@code 1.x}.
 */
public final class Versions {

    private static int[] current;

    private Versions() {
    }

    /**
     * @return {major, minor, patch}, e.g. {1, 21, 1} or {26, 1, 2}
     */
    @Nonnull
    public static int[] parse(@Nonnull String bukkitVersion) {
        String[] parts = bukkitVersion.split("-")[0].split("\\.");
        int[] version = new int[3];
        for (int i = 0; i < Math.min(3, parts.length); i++) {
            version[i] = leadingNumber(parts[i]);
        }
        return version;
    }

    private static int leadingNumber(String text) {
        int end = 0;
        while (end < text.length() && Character.isDigit(text.charAt(end))) end++;
        return end == 0 ? 0 : Integer.parseInt(text.substring(0, end));
    }

    /** @return the running server's version; {1, 8, 8} when there is no server (tests) */
    @Nonnull
    public static int[] current() {
        if (current == null) {
            current = Bukkit.getServer() == null ? new int[]{1, 8, 8} : parse(Bukkit.getBukkitVersion());
        }
        return current;
    }

    /** @return whether the server is this version or newer, e.g. {@code isAtLeast(1, 16)} */
    public static boolean isAtLeast(int major, int minor) {
        return compare(current(), major, minor) >= 0;
    }

    static int compare(int[] version, int major, int minor) {
        if (version[0] != major) return Integer.compare(version[0], major);
        return Integer.compare(version[1], minor);
    }

    /**
     * The old single-number version: the minor of a {@code 1.x} server (8 for 1.8.8), and
     * 100 + major from 26.x on, so checks such as {@code < 13} keep working.
     */
    public static int legacyNumber(@Nonnull int[] version) {
        return version[0] == 1 ? version[1] : 100 + version[0];
    }
}
