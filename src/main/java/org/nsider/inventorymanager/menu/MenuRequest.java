package org.nsider.inventorymanager.menu;

import org.nsider.inventorymanager.extras.Nonnull;
import org.nsider.inventorymanager.extras.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** What to open: a menu id, its arguments, the page, and optional data for its provider. Kept for {@code [back]}. */
public final class MenuRequest {

    private final String id;
    private final List<String> args;
    private final int page;
    private final Map<String, Object> context;

    public MenuRequest(@Nonnull String id, @Nonnull List<String> args, int page, @Nullable Map<String, Object> context) {
        this.id = id;
        this.args = Collections.unmodifiableList(args);
        this.page = Math.max(0, page);
        this.context = context == null ? Collections.<String, Object>emptyMap() : Collections.unmodifiableMap(new HashMap<>(context));
    }

    @Nonnull
    public static MenuRequest of(@Nonnull String id, @Nonnull String... args) {
        return new MenuRequest(id, Arrays.asList(args), 0, null);
    }

    @Nonnull
    public String getId() {
        return id;
    }

    @Nonnull
    public List<String> getArgs() {
        return args;
    }

    public int getPage() {
        return page;
    }

    @Nonnull
    public Map<String, Object> getContext() {
        return context;
    }

    @Nonnull
    public MenuRequest withPage(int page) {
        return new MenuRequest(id, args, page, context);
    }
}
