package org.nsider.inventorymanager.menu;

import org.bukkit.configuration.ConfigurationSection;
import org.nsider.inventorymanager.extras.Nonnull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Slot lists: {@code slot: 4}, {@code slots: [1, 2, '10-16']}, or a list such as list-slots. */
public final class Slots {

    private Slots() {
    }

    /** @return the item's slots, from {@code slots} or {@code slot}; empty if it has neither */
    @Nonnull
    public static List<Integer> of(@Nonnull ConfigurationSection section) {
        if (section.isList("slots")) return parse(section.getList("slots"));
        if (section.contains("slots")) return parse(Collections.singletonList(section.get("slots")));
        if (section.contains("slot")) return parse(Collections.singletonList(section.get("slot")));
        return Collections.emptyList();
    }

    /** @return the slots at a path holding a list of slots and ranges */
    @Nonnull
    public static List<Integer> at(@Nonnull ConfigurationSection section, @Nonnull String path) {
        if (section.isList(path)) return parse(section.getList(path));
        if (section.contains(path)) return parse(Collections.singletonList(section.get(path)));
        return Collections.emptyList();
    }

    /** Numbers, {@code '10-16'} ranges and {@code '1,2,3'} strings, in order. */
    @Nonnull
    public static List<Integer> parse(@Nonnull List<?> values) {
        List<Integer> slots = new ArrayList<>();
        for (Object value : values) {
            if (value instanceof Number) {
                slots.add(((Number) value).intValue());
                continue;
            }
            for (String part : String.valueOf(value).split(",")) {
                String text = part.trim();
                if (text.isEmpty()) continue;
                int dash = text.indexOf('-', 1);
                try {
                    if (dash > 0) {
                        int from = Integer.parseInt(text.substring(0, dash).trim());
                        int to = Integer.parseInt(text.substring(dash + 1).trim());
                        for (int slot = Math.min(from, to); slot <= Math.max(from, to); slot++) slots.add(slot);
                    } else {
                        slots.add(Integer.parseInt(text));
                    }
                } catch (NumberFormatException ignored) {
                    // Not a slot: skipped
                }
            }
        }
        return slots;
    }
}
