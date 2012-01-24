SilkSpawners - pick up and move monster spawners using silk touch

Ever wanted to move a mob spawner? Now you can, using tools with the
"silk touch" enchantment. 

** Pre-release ** download for **CraftBukkit 1.1 snapshots** (will **not** work with 1.0.1-R1 RB):

> **[SilkSpawners 0.2 alpha](http://dl.dropbox.com/u/57628348/SilkSpawners-0.2.jar)**

Features:

* Spawner retains creature type
* Works on legit spawners
* If a spawner is mined _without_ silk touch, drops spawner eggs instead (optional)
* Show creature type when spawners are placed or broken
* /spawner command to view spawner you are looking at 
* /spawner [creature] to change an existing spawner in the world, if in your crosshairs
* /spawner [creature] to put a new spawner item in your empty hand
* Flexible creature type names on input (pigman, zombiepigman, pigzombie, etc. all accepted), official names on output (Magma Cube, not "LavaSlime")
* Spawners are craftable using monster eggs + eight iron bars ([as seen here](http://imgur.com/KrWGI), 
[source](http://www.reddit.com/r/Minecraft/comments/oodql/great_idea_mob_spawner_recipe/))
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
No configuration is required in the common case, but a few options can be 
tweaked in SilkSpawners/config.yml:

*craftableSpawners* (true): enable crafting mob spawners using spawner egg + 8 iron bars

*workaroundBukkitBug602* (true): workaround 
[BUKKIT-602](https://bukkit.atlassian.net/browse/BUKKIT-602#Enchantments_lost_on_crafting_recipe_output) for crafting spawners (keep enabled unless this bug is fixed)

*defaultCreature* (null): when generic spawner items are placed, spawn this creature (or null for Minecraft's default, pigs)

All spawner items obtained using SilkSpawners will have the creature type stored, but the
default creature will be used if the spawner is obtained using:

* /give player 52
* 1.9 beta pre-release 6 silk touch
* other plugins not knowledgeable of SilkSpawners' conventions


*spawnerCommandReachDistance* (6): how close you have to be to use the /spawner command

*creatures*: mapping between [CreatureType](http://jd.bukkit.org/apidocs/org/bukkit/entity/CreatureType.html),
[entity ID](http://www.minecraftwiki.net/wiki/Data_values#Entity_IDs), and optional aliases / display name

## Technical Details
SilkSpawners stores the entity ID of creature in two places within the mob spawner item:

* Durability (damage value), if possible. *Since Bukkit 1.0.1+ [broke](https://bukkit.atlassian.net/browse/BUKKIT-329) storing data values on mob spawner items, SilkSpawners does not rely on this field, although it will use it if it is available.*

* Enchantment SILK\_TOUCH level

When a spawner block is broken, a spawner item drops with the appropriate entity ID stored,
obtained from the creature spawner tile entity (CraftCreatureSpawner). 
When a spawner block is placed, the entity ID is read from the item and the spawner creature
type is set (also using CraftCreatureSpawner). 


*For plugin developers*: if you want to interoperate with SilkSpawners' monster spawner items,
use `entityID = (short)item.getEnchantmentLevel(Enchantment.SILK_TOUCH)` or
`item.addUnsafeEnchantment(Enchantment.SILK_TOUCH, entityID)` on the `ItemStack`, the 
enchantment level storing the creature type [Entity ID](http://www.minecraftwiki.net/wiki/Data_values#Entity_IDs).

## Craftable Spawners
Craftable spawners can be enabled by setting the "spawnerRecipes"
configuration option to true. The recipe is 8 iron bars surrounding the
spawner egg (

## Limitations
Requires CraftBukkit 1.1+. Will not work on 1.0.1-R1 (no plans to backport).

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

