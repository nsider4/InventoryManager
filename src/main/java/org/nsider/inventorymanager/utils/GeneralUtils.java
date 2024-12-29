package org.nsider.inventorymanager.utils;

import org.bukkit.Bukkit;

public class GeneralUtils {

    /*
     * Retrieves server version by number
     */
    public static int getMCVersion() {
        String[] split = Bukkit.getBukkitVersion().split("-")[0].split("\\.");
        String verNum = split[1];

        return Integer.parseInt(verNum);
    }
}
