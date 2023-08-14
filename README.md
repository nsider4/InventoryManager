# InventoryManager | The perfect API to optimize inventory management!

[![](https://jitpack.io/v/nsider4/InventoryManager.svg)](https://jitpack.io/#nsider4/InventoryManager)

- Supports 1.8 to 1.20
- Doesen't use any external API to make itself work, no dependencies
- Supports numerical id's as well as Material name from any version
- Inventory handler that contains all active inventories
- Easy to update ItemStack information


# TO-DO List
- Adding support to use a normal inventory as basis for a per player inventory.

Maven:
```
<repository>
    <id>jitpack.io</id>
    <url>https://jitpack.io</url>
</repository>
```
```
<dependency>
    <groupId>com.github.nsider4</groupId>
    <artifactId>InventoryManager</artifactId>
    <version>0.9</version>
    <scope>compile</scope>
</dependency>
```

Gradle:
```
repositories {
	  ...
	  maven { url 'https://jitpack.io' }
}
```
```
dependencies {
	  implementation 'com.github.nsider4:InventoryManager:0.9'
}
```
