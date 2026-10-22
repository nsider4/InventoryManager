# InventoryManager
### Config-driven menus for Spigot, Paper and Folia, 1.8 to 26.x

[![Version](https://jitpack.io/v/nsider4/InventoryManager.svg)](https://jitpack.io/#nsider4/InventoryManager)

Your plugin supplies the data. Each server owner's YAML decides what the menu looks like **and what every button does**. One jar runs on every version from 1.8 to 26.x and on Folia.

## Features
- 🧩 **Menus from YAML**: titles, sizes, slots, items, paged lists and templates.
- 🖱️ **Actions**: you can run commands, send messages, play sounds, open other menus, go back, turn pages, refresh, wait, or run your plugin's own actions. A button can do something different on left, right, shift and middle clicks.
- 🔒 **Requirements**: show an item, allow a click or allow opening a menu based on a permission or any placeholder comparison (with PlaceholderAPI).
- 🎨 **Items**: any material written any way (`RED_WOOL`, `WOOL:14`, `35:14`, `red wool`), glow, enchantments, flags, player heads (name, texture or URL), leather colours, custom model data, item models and hex colours.
- 🔁 **Toggles**: an item can have a `when-off` look, and live menus can refresh themselves on an interval.
- ⚙️ **Safe**: every click and drag is cancelled, clicks are matched by holder (not by title), double clicks are debounced, and work runs on the right thread on Folia.
- Built on [XSeries](https://github.com/CryptoMorin/XSeries).

## Installation
```xml
<repository>
    <id>jitpack.io</id>
    <url>https://jitpack.io</url>
</repository>

<dependency>
    <groupId>com.github.nsider4</groupId>
    <artifactId>InventoryManager</artifactId>
    <version>2.0.2</version>
</dependency>
```
Shade it into your plugin and **relocate** both `org.nsider.inventorymanager` and `com.cryptomorin.xseries`, so plugins using different versions can't clash.

## Using it
```java
MenuManager menus = new MenuManager(plugin);
menus.load(menusConfig.getConfigurationSection("menus"));   // each child section is one menu
menus.loadFolder(new File(getDataFolder(), "menus"));         // optional: one menu per file

// A provider fills in the data before the menu is drawn
menus.registerMenu("stats", menu -> {
    Player player = menu.getPlayer();
    menu.set("%kills%", kills(player));
    for (Quest quest : quests(player)) {
        Map<String, Object> values = new HashMap<>();
        values.put("%quest%", quest.getName());
        values.put("%id%", quest.getId());
        menu.addEntry(quest.isDone() ? "done" : "open", values);   // drawn with templates.done / templates.open
    }
    menu.state("notifications", settings(player).notifications());  // normal or when-off look
});

// Actions your configs can use: [myplugin:claim] %id%
menus.registerAction("myplugin:claim", (menu, argument) -> claim(menu.getPlayer(), argument));

menus.open(player, "stats");
```
A menu without a provider opens straight from its YAML, so server owners can add their own menus. Optional extras:
- `setTaskRunner(...)` plugs in your own scheduler wrapper.
- `onClick(item, handler)` binds a click in code.
- `cancel()` stops a menu from opening.

## Menu YAML
```yaml
stats:
  title: '&8Your stats &7(%page%/%pages%)'
  rows: 4                       # 1-6, or type: HOPPER / DISPENSER / ...
  update-interval: 0            # ticks between automatic refreshes while open
  open-requirements: ['permission myplugin.stats']
  open-deny-actions: ['[message] &cYou can''t open this.']
  open-actions: ['[sound] BLOCK_CHEST_OPEN']
  filler:
    material: GRAY_STAINED_GLASS_PANE
    name: ' '
  items:
    kills:
      slot: 4
      material: DIAMOND_SWORD
      name: '&cKills: &f%kills%'
      lore: ['&7Balance: &f%vault_eco_balance%']
      glow: true
    notifications:
      slot: 22
      material: LIME_DYE
      name: '&aNotifications: on'
      actions: ['[command] myplugin notify toggle', '[wait] 2', '[refresh]']
      when-off:
        material: GRAY_DYE
        name: '&cNotifications: off'
    admin:
      slot: 8
      material: COMMAND_BLOCK
      name: '&cAdmin'
      view-requirements: ['permission myplugin.admin']
      actions: ['[open] admin']
    previous: {slot: 27, material: ARROW, name: '&ePrevious', actions: ['[previous-page]']}
    next:     {slot: 35, material: ARROW, name: '&eNext', actions: ['[next-page]']}
    close:    {slot: 31, material: BARRIER, name: '&cBack', actions: ['[back]']}
  list-slots: ['10-16', '19-25']
  templates:
    open:
      material: PAPER
      name: '&e%quest%'
      left-actions: ['[myplugin:claim] %id%', '[refresh]']
      right-actions: ['[message] &7Quest id: %id%']
    done:
      material: 'head:Notch'
      name: '&a%quest% &7(done)'
```

### Item keys
| Key | |
|---|---|
| `slot` / `slots` | `4`, or `[0, '10-16', '20,21']` |
| `material` | any name from 1.8 to 26.x, `NAME:data`, numeric `id:data`, `minecraft:name`, or `head:<name\|texture\|url>` |
| `amount`, `name`, `lore` | `name` and `lore` support `&` colours and `&#RRGGBB` (1.16+). A lore line that is just a list placeholder expands into several lines |
| `glow`, `enchantments` | `glow: true`; `enchantments: ['SHARPNESS:2']` |
| `flags` | `[HIDE_ATTRIBUTES, ...]` or `[ALL]`. Default: hide enchantments and attributes |
| `head` | a player name, UUID, base64 texture or textures.minecraft.net URL |
| `color` | leather armour: `'#FF0000'` or `'255,0,0'` |
| `custom-model-data`, `item-model`, `unbreakable` | for versions that have them; ignored elsewhere |
| `state`, `when-off` | when `state` is false (or set off in code), `when-off`'s keys replace the item's |
| `view-requirements` | the item shows only if every line passes |
| `click-requirements`, `deny-actions` | when a line fails, `deny-actions` run instead |
| `actions` | also `left-`, `right-`, `shift-left-`, `shift-right-`, `middle-` and `drop-actions`. Each falls back to `actions` |
| `always-show` | page buttons hide when there's no page to turn to, unless this is true |

### Actions
| Action | |
|---|---|
| `[command] <cmd>` | the viewer runs a command |
| `[console] <cmd>` | the console runs a command |
| `[message] <text>` / `[broadcast] <text>` | to the viewer / to everyone |
| `[sound] <name> [volume] [pitch]` | any sound name from 1.8 to 26.x, or a resource-pack sound like `myserver:ding` |
| `[open] <menu> [args]` | opens another menu (args become `%arg_1%`...) |
| `[switch] <menu> [args]` | like `[open]`, but `[back]` skips it (tabs) |
| `[back] [fallback menu]` | the previous menu; closes if there is none |
| `[refresh]`, `[close]` | |
| `[next-page]`, `[previous-page]`, `[page] <n>` | |
| `[wait] <ticks>` | waits before running the rest |
| `[plugin:action] <arg>` | registered by a plugin |

### Requirements
Each line can be:
- `permission <node>`
- a comparison `<a> <op> <b>`, where op is `==`, `!=`, `>=`, `<=`, `>` or `<`. Numbers compare as numbers; text compares ignoring case.
- a plain value: `true`, `yes`, `on`, or a number other than 0 counts as passing.

Start a line with `!` to negate it. Placeholders are filled in first.

### Placeholders
- Built in: `%player%`, `%page%`, `%pages%`, `%entries%` (`%entries_<list>%` for named `lists`), `%arg_1%`..., `%args%`.
- Anything your provider sets.
- PlaceholderAPI placeholders, when it's installed.

## Single items
```java
ItemStack item = Materials.item("WOOL:14");              // red wool on every version
ItemStack built = new ItemFactory(getLogger()).build(section, ItemFactory.PLAIN);
boolean hex = Versions.isAtLeast(1, 16);                  // works with 26.x version numbers too
```

## Upgrading from 1.x
`InventoryBuilder`, `InventoryHandler`, `ItemBuilder` and `ItemData` still work but are deprecated. They share one inventory per name and have no click handling.
- The bundled `XMaterial` is gone: use XSeries' `com.cryptomorin.xseries.XMaterial` or `Materials`.
- `GeneralUtils.getMCVersion()` now returns `100 + major` on 26.x, so `< 13` checks keep working.
