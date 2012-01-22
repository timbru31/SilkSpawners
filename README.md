SilkSpawners - pick up and move monster spawners using silk touch

Ever wanted to move a mob spawner? Now you can, using tools with the
"silk touch" enchantment. 

Features:

* Spawner retains creature type
* Works on legit spawners
* If a spawner is mined _without_ silk touch, drops spawner eggs instead
* Informational messages showing the creature type when spawners are placed or broken
* /spawner command to view or change the creature type
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

## Configuration
No configuration file is required or copied by default, but if desired the creature names and entity IDs
can be configured in SilkSpawners/config.yml. See config.yml within the .jar for
details. 

## Technical Details
SilkSpawners stores the entity ID of creature in two places within the mob spawner item:

* Durability (damage value), if possible

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

## See Also
Related plugins:

* [SilkierTouch](http://dev.bukkit.org/server-mods/silkiertouch/)
* [ChangeSilkTouch](http://dev.bukkit.org/server-mods/changesilktouch/)
* [Mob Spawner Changer](http://forums.bukkit.org/threads/misc-mech-mob-spawner-changer-v0-3-change-what-a-mob-spawner-spawns-1337.26038/)


