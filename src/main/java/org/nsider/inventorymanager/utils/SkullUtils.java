package org.nsider.inventorymanager.utils;

import com.cryptomorin.xseries.profiles.builder.XSkull;
import com.cryptomorin.xseries.profiles.objects.Profileable;
import org.bukkit.inventory.ItemStack;
import org.nsider.inventorymanager.extras.Nonnull;
import org.nsider.inventorymanager.extras.Nullable;

/** Head textures, through XSeries' XSkull, which supports every version from 1.8 to 26.x. */
public class SkullUtils {

    /**
     * Get the Base64 texture of the given skull ItemStack.
     *
     * @param itemStack The ItemStack.
     * @return The skull texture (Base64), or null if it has none.
     */
    @Nullable
    public static String getSkullTexture(@Nonnull ItemStack itemStack) {
        try {
            return Profileable.of(itemStack).getProfileValue();
        } catch (RuntimeException e) {
            return null;
        }
    }

    /**
     * Sets the texture of a skull ItemStack.
     *
     * @param itemStack The skull.
     * @param value     A Base64 texture, textures.minecraft.net URL, player name or UUID.
     * @return The skull with the texture applied.
     */
    @Nonnull
    public static ItemStack setSkullTexture(@Nonnull ItemStack itemStack, @Nonnull String value) {
        return XSkull.of(itemStack).profile(Profileable.detect(value)).lenient().apply();
    }
}
