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
    <version>1.0.6</version>
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
	  implementation 'com.github.nsider4:InventoryManager:1.0.6'
}
```

# Example Usage

Main.java:
```Java
@Override
public void onEnable() {

    createInventory(); //Method where you created the inventories by adding ItemData objects

    //Loop through all inventories to load on start (if only one you can just call that inventory and load it without loop)
    for (InventoryBuilder inventories : InventoryHandler.getInstance().getInventories().values()) {
	inventories.load();
    }
}
```

Example.java:
```Java
public void createInventory() {
    InventoryBuilder inv = new InventoryBuilder("test", 27);

    for(String section : getConfig().getConfigurationSection("items").getKeys(false)) {

	ItemData item = ItemData.builder()
		.setMaterial(getConfig().getString("items."+section+".material"))
		.setName(getConfig().getString("items."+section+".name"))
		.setLore(getConfig().getStringList("items."+section+".lore"))
		.setAmount(getConfig().getInt("items."+section+".amount"))
		.setSlots(getConfig().getIntegerList("items."+section+".slots"))
		.build();


	inv.addItem(item);
    }

    inv.save();
}

//One way to open an inventory:
public void openInventory(String invTitle, Player player){
    InventoryBuilder builder = InventoryHandler.getInstance().getInventoryBuilder(invTitle);
    builder.openInventory(player);
}
```

# Inventory Creation:

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


Retrieving the ItemData with a certain name (this is the only method to find the data based on ItemMeta included in API, you can do this yourself by looping through itemDataList and checking for a different thing like "Amount". See InventoryBuilder.java)
```Java
Optional<ItemData> itemData = builder.getItemDataWithName("Item"); //Trying to get the ItemData with name "Item"

itemData.ifPresent(data -> { //This checks if it exists and gives it the identifier "data"
    data.setName("NEW NAME");
    builder.updateItem(data);
    // Perform other operations on the itemData here
    // ...
});
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

#
Class used for material compatibility: https://github.com/CryptoMorin/XSeries/blob/master/src/main/java/com/cryptomorin/xseries/XMaterial.java
