package org.nsider.inventorymanager.item;

import com.cryptomorin.xseries.XMaterial;
import org.bukkit.inventory.ItemStack;
import org.nsider.inventorymanager.extras.Nonnull;
import org.nsider.inventorymanager.extras.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Optional;
import java.util.Properties;

/**
 * Reads a material written any way a config might, on any version from 1.8 to 26.x:
 * modern names ({@code RED_WOOL}), legacy names with data ({@code WOOL:14}), numeric ids
 * ({@code 35:14}), spaces or lower case ({@code red wool}) and a {@code minecraft:} prefix.
 */
public final class Materials {

    private static Properties legacyIds;

    private Materials() {
    }

    @Nonnull
    public static Optional<XMaterial> match(@Nullable String name) {
        if (name == null) return Optional.empty();
        String value = name.trim();
        if (value.toLowerCase(Locale.ROOT).startsWith("minecraft:")) value = value.substring("minecraft:".length());
        if (value.isEmpty()) return Optional.empty();
        if (Character.isDigit(value.charAt(0))) return matchId(value);
        return XMaterial.matchXMaterial(value);
    }

    private static Optional<XMaterial> matchId(String value) {
        String[] parts = value.split(":", 2);
        String name = legacyIds().getProperty(parts[0].trim());
        if (name == null) return Optional.empty();
        return XMaterial.matchXMaterial(parts.length > 1 ? name + ":" + parts[1].trim() : name);
    }

    /** id -> 1.8 name. Bukkit can only look ids up before 1.13, so the table ships with the library. */
    private static synchronized Properties legacyIds() {
        if (legacyIds == null) {
            Properties ids = new Properties();
            try (InputStream in = Materials.class.getResourceAsStream("/inventorymanager/legacy-ids.properties")) {
                if (in != null) ids.load(in);
            } catch (IOException ignored) {
                // Numeric ids just won't match
            }
            legacyIds = ids;
        }
        return legacyIds;
    }

    /**
     * @return the item (with its legacy data value on old servers), or null if the name is
     * unknown or the material doesn't exist on this server's version
     */
    @Nullable
    public static ItemStack item(@Nullable String name) {
        Optional<XMaterial> material = match(name);
        if (!material.isPresent() || !material.get().isSupported()) return null;
        return material.get().parseItem();
    }
}
