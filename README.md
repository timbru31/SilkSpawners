SilkSpawners - pick up and move monster spawners using silk touch

Ever wanted to move a mob spawner? Now you can, using tools with the
"silk touch" enchantment. 

*Update 2012/02/08*: SilkSpawners 0.5 released for CraftBukkit 1.1-R3! 

Features:

* Spawner retains creature type
* Works on legit spawners
* If a spawner is mined _without_ silk touch, drops spawner eggs / XP / iron bars instead (optional)
* Show creature type when spawners are placed or broken
* /spawner command to view spawner you are looking at 
* /spawner [creature] to change an existing spawner in the world, if in your crosshairs
* /spawner [creature] to put a new spawner item in your empty hand
* Flexible creature type names on input (pigman, zombiepigman, pigzombie, etc. all accepted), official names on output (Magma Cube, not "LavaSlime")
* Spawners are craftable using monster eggs + eight iron bars ([as seen here](http://imgur.com/KrWGI), 
[source](http://www.reddit.com/r/Minecraft/comments/oodql/great_idea_mob_spawner_recipe/)) - *disabled by default, see below to enable*
* Left-click spawner with spawn egg to change type (optional, ops only by default)
* Changing spawner type with spawn egg consumes egg (can be turned off)
* Optional permissions support


## Configuration
usePermissions (false) - Whether to use Bukkit's superperms system, or the defaults below.

destroyDropEgg (false) - Whether to give a spawn egg when spawner is destroyed.

destroyDropXP (0) - Experience points to drop when spawner is destroyed.

destroyDropBars (0) - Iron bars to drop when spawner is destroyed.

minSilkTouchLevel (1) - Minimum enchantment level required for Silk Touch to harvest spawners. Normally Silk Touch I is required,
 but you can set this to 0 to make no enchantment required, or 2+ to require non-standard (normally unobtainable) enchantments.

consumeEgg (true) - Whether to consume spawn eggs used to change spawners, or otherwise keep the egg in the player's inventory.

spawnerCommandReachDistance (6) - How close you have to be to use the /spawner command.

craftableSpawners (false) - Enable crafting mob spawners using spawner egg + 8 iron bars.

defaultCreature (null) - When generic spawner items are placed, spawn this creature (or null for Minecraft's default, pigs).

All spawner items obtained using SilkSpawners will have the creature type stored, but the
default creature will be used if the spawner is obtained using:

* /give player 52
* 1.9 beta pre-release 6 silk touch
* other plugins not knowledgeable of SilkSpawners' conventions


creatures - Mapping between [CreatureType](http://jd.bukkit.org/apidocs/org/bukkit/entity/CreatureType.html),
[entity ID](http://www.minecraftwiki.net/wiki/Data_values#Entity_IDs), and optional aliases / display name.

## Permissions
**Permission support is optional** and off by default. When turned off, the settings shown in parentheses 
below are used, intended to allow for easy setup with minimal configuration. For more advanced setup, 
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

***[Fork me on GitHub](https://github.com/mushroomhostage/SilkSpawners)***

