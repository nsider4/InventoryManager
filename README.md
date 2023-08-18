# InventoryManager | The perfect API to optimize inventory management!

[![](https://jitpack.io/v/nsider4/InventoryManager.svg)](https://jitpack.io/#nsider4/InventoryManager)
#
- Supports 1.8 to 1.20
- Doesen't use any external API to make itself work, no dependencies
- Supports numerical id's as well as Material name from any version
- Inventory handler that contains all active inventories
- Easy to update ItemStack information


# TO-DO List
- Adding support to use a normal inventory as base for a per player inventory.

#

Maven:
```XML
<repository>
    <id>jitpack.io</id>
    <url>https://jitpack.io</url>
</repository>
```
```XML
<dependency>
    <groupId>com.github.nsider4</groupId>
    <artifactId>InventoryManager</artifactId>
    <version>0.9</version>
    <scope>compile</scope>
</dependency>
```

Gradle:
```GRADLE
repositories {
	  ...
	  maven { url 'https://jitpack.io' }
}
```
```GRADLE
dependencies {
	  implementation 'com.github.nsider4:InventoryManager:0.9'
}
```

# Inventory Creation (Example Usage):

Creating the inventory(example with title "Name" and size 27 slots):
```Java
InventoryBuilder inv = new InventoryBuilder("Name", 27);
```

Building an Item (Material can be set to any minecraft material):
```Java
ItemData data = ItemData.builder()
                .setMaterial("STONE")
                .setName(String)
                .setLore(List<String>)
                .setAmount(int)
                .setSlots(List<Integer>)
                .build();
```

Optional item creation without Builder (you'd need to set all information this way):
```Java
ItemData data = new ItemData();
data.setMaterial("STONE");
```

Adding the item to the inventory(Inventory has to be loaded after adding for it to appear in the inventory in-game):
```Java
inv.addItem(data);
```

Removing the item from the inventory:
```Java
inv.removeItem(data);
```

Loading the whole inventory to create the updated inventory object:
```Java
inv.load();
```

# Useful Methods:

Creates the itemstack related to the data given. Optional Material argument to force a material if it has to be changed at time of creation.
```Java
ItemStack item = ItemBuilder.fromItemData(ItemData info, Material... forcedMaterial);
```

Retrieving the ItemData object from the given ItemStack:
```Java
ItemData data = ItemBuilder.dataFromItemStack(ItemStack);
```

String utils (color, centered messages):
```Java
String coloredString = GeneralUtils.color(String); #Color string
GeneralUtils.sendCenteredMessage(CommandSender, String); #Send normal message centered
GeneralUtils.sendCenteredComponentMessage(Player, String, TextComponent); #Sends centered message that contains normal text + component at end of the string.
```
