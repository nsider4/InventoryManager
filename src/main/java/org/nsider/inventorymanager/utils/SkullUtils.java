package org.nsider.inventorymanager.utils;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import lombok.NonNull;
import org.bukkit.Bukkit;
import org.bukkit.SkullType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.nsider.inventorymanager.extras.Nullable;
import org.nsider.inventorymanager.extras.SkullProfile;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.UUID;
import java.util.function.Function;

public class SkullUtils {
    private static Field PROFILE_FIELD;
    private static Method SET_PROFILE_METHOD;
    private static boolean INITIALIZED = false;
    private static Method PROPERTY_VALUE_METHOD;
    private static Function<Property, String> VALUE_RESOLVER;

    /**
     * Get the Base64 texture of the given skull ItemStack.
     *
     * @param itemStack The ItemStack.
     * @return The skull texture. (Base64)
     */
    @Nullable
    public static String getSkullTexture(@NonNull ItemStack itemStack) {
        try {
            ItemMeta meta = itemStack.getItemMeta();
            if (!(meta instanceof SkullMeta)) {
                return null;
            }
            if (PROFILE_FIELD == null) {
                PROFILE_FIELD = meta.getClass().getDeclaredField("profile");
                PROFILE_FIELD.setAccessible(true);
            }
            GameProfile profile = (GameProfile) PROFILE_FIELD.get(meta);
            if (profile == null) {
                return null;
            }
            if (VALUE_RESOLVER == null) {
                try {
                    Property.class.getMethod("getValue");
                    VALUE_RESOLVER = Property::getValue;
                } catch (NoSuchMethodException ignored) {
                    PROPERTY_VALUE_METHOD = Property.class.getMethod("value");
                    VALUE_RESOLVER = property -> {
                        try {
                            return (String) PROPERTY_VALUE_METHOD.invoke(property);
                        } catch (IllegalAccessException | InvocationTargetException e) {
                            Bukkit.getLogger().severe("Failed to invoke Property#value");
                        }
                        return null;
                    };
                }
            }
            PropertyMap properties = profile.getProperties();
            Collection<Property> property = properties.get("textures");
            if (property != null && !property.isEmpty()) {
                return VALUE_RESOLVER.apply(property.iterator().next());
            }
        } catch (Exception e) {
            Bukkit.getLogger().severe("An exception occurred while setting skull texture");
        }
        return null;
    }
    /**
     * Get the Base64 texture of the given skull ItemStack.
     *
     * @param itemStack The ItemStack.
     * @return The skull texture. (Base64)
     * @deprecated Use {@link #getSkullTexture(ItemStack)} instead.
     */
    @Deprecated
    @Nullable
    public static String getTexture(@NonNull ItemStack itemStack) {
        return getSkullTexture(itemStack);
    }
    /**
     * Set the Base64 texture of the given skull ItemStack.
     *
     * @param item The ItemStack.
     * @param texture   The new skull texture (Base64).
     */
    public static ItemStack setSkullTexture(@NonNull ItemStack item, @NonNull String texture) {
        try {
            ItemMeta meta = item.getItemMeta();
            if (meta instanceof SkullMeta) {
                GameProfile profile = new GameProfile(UUID.randomUUID(), "");
                Property property = new Property("textures", texture);
                PropertyMap properties = profile.getProperties();
                properties.put("textures", property);
                if (SET_PROFILE_METHOD == null && !INITIALIZED) {
                    try {
                        //This method only exists in 1.16+ versions . For older versions, use reflection
                        SET_PROFILE_METHOD = meta.getClass().getDeclaredMethod("setProfile", GameProfile.class);
                        SET_PROFILE_METHOD.setAccessible(true);
                    } catch (NoSuchMethodException e) {
                        //Server is running an older version.
                    }
                    INITIALIZED = true;
                }
                if (SET_PROFILE_METHOD != null) {
                    SET_PROFILE_METHOD.invoke(meta, profile);
                } else {
                    if (PROFILE_FIELD == null) {
                        PROFILE_FIELD = meta.getClass().getDeclaredField("profile");
                        PROFILE_FIELD.setAccessible(true);
                    }

                    try {
                        PROFILE_FIELD.set(meta, profile);
                    } catch (Throwable ignored) {
                        PROFILE_FIELD.set(
                                meta,
                                SkullProfile.getObject(profile)
                        );
                    }
                }
            }
            item.setItemMeta(meta);
            if (GeneralUtils.getMCVersion() < 13) {
                item.setDurability((short) SkullType.PLAYER.ordinal());
            }

            return item;
        } catch (Exception e) {
            Bukkit.getLogger().severe("An exception occurred while setting skull texture");
        }

        return null;
    }
}
