package org.nsider.inventorymanager.utils;

public class GeneralUtils {

    /**
     * Retrieves the server version as one number: 8 for 1.8.8, 21 for 1.21.x, and 100 + major
     * from 26.x on (126 for 26.1), so older {@code < 13} style checks keep working.
     *
     * @deprecated use {@link Versions#isAtLeast(int, int)}
     */
    @Deprecated
    public static int getMCVersion() {
        return Versions.legacyNumber(Versions.current());
    }
}
