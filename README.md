SilkSpawners - harvest mob spawners with silk touch

Ever wanted to move a mob spawner? With SilkSpawners, you can now pick up and move 
monster spawners using tools with the "silk touch" enchantment.

**New! [SilkSpawners 1.2](http://dev.bukkit.org/server-mods/silkspawners/files/10-silk-spawners-1-2/)** - for 1.2.3-R0.2, 1.1-R8, and 1.1-R4

Features:

* Spawner retains creature type
* Works on legit spawners
* Optional showing of creature type when spawners are placed or broken
* Optional /spawner command to view creature type of spawner you are looking at 
* Optional /spawner [creature] to change an existing spawner in the world, if in your crosshairs
* Optional /spawner [creature] to put a new spawner item in your empty hand
* Optional /spawner [creature]egg to put a new spawn egg in your empty hand
* Optional crafting of spawners using monster eggs + eight iron bars ([as seen here](http://imgur.com/KrWGI), 
[source](http://www.reddit.com/r/Minecraft/comments/oodql/great_idea_mob_spawner_recipe/)) 
* Optional left-click spawner with spawn egg to change type (ops only by default)
* Optional changing spawner type with spawn egg (either consuming or not consuming egg)
* Optional support for custom mobs including those from the [Natural Selection](http://www.minecraftforum.net/topic/950329-110smpforgenatural-selection-a-minecraft-survival-accession-v21/) mod
* Optional permissions support
* Flexible creature type names on input (pigman, zombiepigman, pigzombie, etc. all accepted), official names on output (Magma Cube, not "LavaSlime")

## Usage
Acquire a tool with Silk Touch, break a mob spawner using said tool, place the spawner. Your spawner has now been moved. 

Many additional features can be enabled if desired, see below.

[Forum thread](http://forums.bukkit.org/threads/mech-fix-info-admn-silkspawners-v1-0-harvest-mob-spawners-1-1-r4.59077/)

## Configuration
usePermissions (false) - Whether to use Bukkit's superperms system, or the defaults below.

useWorldGuard (true) - Whether to use [WorldGuard](http://dev.bukkit.org/server-mods/worldguard/) protection, if present.

minSilkTouchLevel (1) - Minimum enchantment level required for Silk Touch to harvest spawners. Normally Silk Touch I is required,
 but you can set this to 0 to make no enchantment required, or 2+ to require non-standard (normally unobtainable) enchantments.

destroyDropEgg (false) - Whether to give a spawn egg when spawner is destroyed without Silk Touch.

destroyDropXP (0) - Experience points to drop when spawner is destroyed.

destroyDropBars (0) - Iron bars to drop when spawner is destroyed.

consumeEgg (true) - Consume spawn eggs used to change spawners, or otherwise keep the egg in the player's inventory.

useReflection (true) - Use reflection to get/set mob IDs, or otherwise use Bukkit's wrapper. Required for custom mobs.

useExtraMobs (false) - Load custom mobs not normally recognized by Bukkit. The extra mobs must be specified in the 'creatures' section. 

spawnEggOverride (false) - Override CraftBukkit's spawn egg routine and allow you to spawn [any entity](http://www.minecraftwiki.net/wiki/Data_values#Entity_IDs)
whatsoever by right-clicking the appropriate spawn eggs -- any entity is allowed, including
enderdragons, ender crystals, primed TNT, boats, or even invalid entities, so be careful. Mainly intended for testing purposes,
or if you want to spawn modded items not recognized by CraftBukkit.

verboseConfig (false) - Log verbose configuration information on load.

spawnerCommandReachDistance (6) - How close you have to be to use the /spawner command.

craftableSpawners (false) - Enable crafting mob spawners using spawner egg + 8 iron bars.

spawnersUnstackable (false) - Prevent spawners from stacking, by setting max stack size to 1.

defaultCreature (null) - When generic spawner items are placed, spawn this creature (or null for Minecraft's default, pigs).

All spawner items obtained using SilkSpawners will have the creature type stored, but the
default creature will be used if the spawner is obtained using:

* /give player 52
* 1.9 beta pre-release 6 silk touch
* other plugins not knowledgeable of SilkSpawners' conventions


creatures - Mapping between internal mob ID string for spawners,
[entity ID](http://www.minecraftwiki.net/wiki/Data_values#Entity_IDs) for spawn eggs, and optional aliases / display name.

## Permissions
**Permission support is optional** and off by default. When turned off, the settings shown in parentheses 
below are used, intended to allow for easy setup with minimal configuration. 

For more advanced setup, 
set *usePermissions: true* in config.yml, and all permission nodes will be set to *false*, allowing for
flexible configuration through your permission plugin as desired.

silkspawners.info (true) -
Allows you to see informative messages about the spawners as you place and break them

silkspawners.silkdrop (true) -
Allows you to use silk touch to acquire mob spawner items

silkspawners.destroydrop (true) -
Allows you to destroy mob spawners to acquire mob spawn eggs / iron bars / XP (as configured)

silkspawners.viewtype (true) -
Allows you to view the spawner type using /spawner

silkspawners.changetype (op) -
Allows you to change the spawner type using /spawner [creature]

silkspawners.changetypewithegg (op) -
Allows you to change the spawner type by left-clicking with a spawn egg

silkspawners.freeitem (op) -
Allows you to get spawner items in your hand for free using /spawner [creature]

silkspawners.freeitemegg (op) -
Allows you to get spawn eggs in your hand for free using /spawner [creature]egg

## Technical Details
SilkSpawners stores the entity ID of creature in two places within the mob spawner item:

* Durability (damage value), if possible. *Since Bukkit 1.0.1+ [broke](https://bukkit.atlassian.net/browse/BUKKIT-329) storing data values on mob spawner items, SilkSpawners does not rely on this field, although it will use it if it is available.*

* Enchantment SILK\_TOUCH level

When a spawner block is broken, a spawner item drops with the appropriate entity ID stored,
obtained from the creature spawner tile entity (CraftCreatureSpawner). 
When a spawner block is placed, the entity ID is read from the item and the spawner creature
type is set (using the CreatureSpawner BlockState). 

*For plugin developers*: if you want to interoperate with SilkSpawners' monster spawner items,
use `entityID = (short)item.getEnchantmentLevel(Enchantment.SILK_TOUCH)` or
`item.addUnsafeEnchantment(Enchantment.SILK_TOUCH, entityID)` on the `ItemStack`, the 
enchantment level storing the creature type [Entity ID](http://www.minecraftwiki.net/wiki/Data_values#Entity_IDs).

## Limitations
SilkSpawners only changes the spawner type, it does not manage the spawning itself;
the spawning algorithm remains up to Minecraft. Other plugins offer more control.

If creaturebox is also installed, drops two spawners. Install either SilkSpawners or creaturebox, not both.

Not Enough Items shows all spawners as "Pig", because it does not recognize how SilkSpawners stores
the spawner creature type.

In the inventory window, item description is "Monster Spawner" for all kinds of spawners. 
Fixing this requires a client-side mod.

## See Also
Want to make Silk Touch yet more useful? Also try Pickaxe + Silk Touch II from [EnchantMore](http://dev.bukkit.org/server-mods/enchantmore/).

Other relevant plugins:

* [creaturebox](http://dev.bukkit.org/server-mods/creaturebox/)
* [MonsterBox](http://dev.bukkit.org/server-mods/monsterbox/)
* [Mob Spawner Changer](http://forums.bukkit.org/threads/misc-mech-mob-spawner-changer-v0-3-change-what-a-mob-spawner-spawns-1337.26038/)
* [SilkierTouch](http://dev.bukkit.org/server-mods/silkiertouch/)
* [ChangeSilkTouch](http://dev.bukkit.org/server-mods/changesilktouch/)
* [felega.block](http://forums.bukkit.org/threads/multiple-felegas-plugin-pile.54916/)
* [SuperSimpleSpawners](http://dev.bukkit.org/server-mods/supersimplespawners/)
* [MobSpawnerEggChanger](http://dev.bukkit.org/server-mods/sec/)

***[Fork me on GitHub](https://github.com/mushroomhostage/SilkSpawners)***
