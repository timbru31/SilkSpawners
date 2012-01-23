SilkSpawners - pick up and move monster spawners using silk touch

Ever wanted to move a mob spawner? Now you can, using tools with the
"silk touch" enchantment. 

** Pre-release ** download for **CraftBukkit 1.1 snapshots** (will **not** work with 1.0.1-R1 RB):

> **[SilkSpawners 0.2 alpha](http://dl.dropbox.com/u/57628348/SilkSpawners-0.2.jar)**

Featurs:

* Spawner retains creature type
* Works on legit spawners
* If a spawner is mined _without_ silk touch, drops spawner eggs instead (optional)
* Show creature type when spawners are placed or broken
* /spawner command to view spawner you are looking at 
* /spawner [creature] to change an existing spawner in the world, if in your crosshairs
* /spawner [creature] to put a new spawner item in your empty hand
* Flexible creature type names on input (pigman, zombiepigman, pigzombie, etc. all accepted), official names on output (Magma Cube, not "LavaSlime")
* Permissions support

## Permissions
silkspawners.info (true) -
Allows you to see informative messages about the spawners as you place and break them

silkspawners.silkdrop (true) -
Allows you to use silk touch to acquire mob spawner items

silkspawners.eggdrop (true) -
Allows you to destroy mob spawners to acquire mob spawn eggs

silkspawners.viewtype (true) -
Allows you to view the spawner type using /spawner

silkspawners.changetype (op) -
Allows you to change the spawner type using /spawner [creature]

silkspawners.freeitem (op) -
Allows you to get spawner items in your hand for free using /spawner [creature]

## Configuration
No configuration file is required or copied by default, but if desired the creature names and entity IDs
can be configured in SilkSpawners/config.yml. See config.yml within the .jar for
details. 

## Technical Details
SilkSpawners stores the entity ID of creature in two places within the mob spawner item:

* Durability (damage value), if possible. *Since Bukkit 1.0.1+ [broke](https://bukkit.atlassian.net/browse/BUKKIT-329) storing data values on mob spawner items, SilkSpawners does not rely on this field, although it will use it if it is available.*

* Enchantment SILK\_TOUCH level

When a spawner block is broken, a spawner item drops with the appropriate entity ID stored,
obtained from the creature spawner tile entity (CraftCreatureSpawner). 
When a spawner block is placed, the entity ID is read from the item and the spawner creature
type is set (also using CraftCreatureSpawner). 

All monster spawner items obtained using SilkSpawners will have the stored entity ID, but 
if you obtain a spawner item by other means (/give player 52, 
1.9 beta pre-release 6's silk touch, or other plugins not
knowledgeable of SilkSpawner's conventions), it will not know what to spawn. In this case,
pigs will be spawned by default, but this can be changed using the "defaultCreature"
configuration option.

*For plugin developers*: if you want to interoperate with SilkSpawners' monster spawner items,
use `entityID = (short)item.getEnchantmentLevel(Enchantment.SILK_TOUCH)` or
`item.addUnsafeEnchantment(Enchantment.SILK_TOUCH, entityID)` on the `ItemStack`, the 
enchantment level storing the creature type [Entity ID](http://www.minecraftwiki.net/wiki/Data_values#Entity_IDs).

## Limitations
SilkSpawners only changes the spawner type, it does not manage the spawning itself;
the spawning algorithm remains up to Minecraft. Other plugins offer more control.

In the inventory window, item description is "Monster Spawner" for all kinds of spawners. 
Fixing this requires a client-side mod.

## See Also
Want to make Silk Touch yet more useful? Also try [Sublimation](http://dev.bukkit.org/server-mods/sublimation/).

Other relevant plugins:

* [SilkierTouch](http://dev.bukkit.org/server-mods/silkiertouch/)
* [ChangeSilkTouch](http://dev.bukkit.org/server-mods/changesilktouch/)
* [Mob Spawner Changer](http://forums.bukkit.org/threads/misc-mech-mob-spawner-changer-v0-3-change-what-a-mob-spawner-spawns-1337.26038/)
* [felega.block](http://forums.bukkit.org/threads/multiple-felegas-plugin-pile.54916/)
* [MonsterBox](http://dev.bukkit.org/server-mods/monsterbox/)
* [creaturebox](http://dev.bukkit.org/server-mods/creaturebox/)

