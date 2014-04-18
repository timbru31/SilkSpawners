This is the README of SilkSpawners!
For support visit the dev.bukkit.org page:
http://dev.bukkit.org/bukkit-plugins/silkspawners/
Thanks to mushroomhostage for the original plugin!
Thanks for using!

This plugin sends usage statistics! If you wish to disable the usage stats, look at plugins/PluginMetrics/config.yml!
This plugin is released under the Creative Commons Attribution-NonCommercial-ShareAlike 3.0 Unported (CC BY-NC-SA 3.0) license.

Standard config:

# See documentation at http://dev.bukkit.org/server-mods/silkspawners/pages/configuration
 
# Should the plugin automatically update if an update is available?
autoUpdater: true
 
# Should permissions be used
usePermissions: false
 
# Should be checked for WorldGuard build ability to change spawners
useWorldGuard: true
 
# When generic spawner items are placed, spawn this creature (e.g. from /give or other plugins)
# PIG (90) is Minecraft default (put NAMES or IDs here!)
defaultCreature: 90
 
# How far is the spawner reachable with your crosshair
spawnerCommandReachDistance: 6
 
# Minimum silk touch level [can be changed via other plugins to a higher value]
# Set it to 0 to mine it without silk touch
minSilkTouchLevel: 1
 
# If a player in creative destroys a spawner nothing is dropped
noDropsCreative: true
 
# If a spawner is destroyed, should the egg be dropped
destroyDropEgg: false
 
# If a spawner is destroyed, should XP be dropped
destroyDropXP: 0
 
# If a spawner is destroyed, should iron bars be dropped
destroyDropBars: 0
 
# Should the player be able to craft spawners
craftableSpawners: false
 
# Leave a slot empty (null/air)? Just make a space then, example 'A A' -> middle is free
# X is always the egg
recipeTop: AAA
recipeMiddle: AXA
recipeBottom: AAA
recipeAmount: 1
 
# Custom example:
#recipeTop: 'A A'
#recipeMiddle: 'BXA'
#recipeBottom: 'C D'
#ingredients:
#  - 'A,IRON_FENCE'
#  - 'B,DIRT'
#  - 'C,2'
#  - 'D,5'
 
# You can put IDs or the NAME here (please uppercase)
# Add it for each custom ingredient you add, too!
ingredients:
- A,IRON_FENCE
 
# Should spawners be stackable
spawnersUnstackable: false
 
# Should the egg be consumed when the spawner is changed with it
consumeEgg: true
 
# Fallback if the creature should be enabled, if not specified for the entity
enableCreatureDefault: true

# Should numbers be ignored (on eggs) and allow every number value?
ignoreCheckNumbers: false

# Should instead of spawning a mob a MonsterSpawner be placed? (Uses consumeEgg value, too)
spawnEggToSpawner: false
 
# Should the spawn algorithm be overridden? Allows spawning of non-standard entities
spawnEggOverride: false
 
# Fallback if the creature should be spawned, if not specified for the entity
spawnEggOverrideSpawnDefault: true
 
# Notify the player about the spawner when he clicks it in the inventory
notifyOnClick: true
 
# Notify the player about the spawner when he holds the spawner in the hand
notifyOnHold: true
 
# Configure displaying with BarAPI, time is in seconds
barAPI:
  enable: true
  displayTime: 3
 
# Puts more information out on startup
verboseConfig: false
 
# Internal stuff, do NOT change unless advised - the plugin might break otherwise
useReflection: true
useErrorLogger: true
testMCVersion: true

Commands & Permissions (if no permissions system is detected, only OPs are able to use the commands!)
Only bukkit's permissions system is supported!


SilkSpawners:
Aliases: silkspawner, ss, spawner, silk, spawnersilk
Description: Change or view the monster spawner creature type
Usage: /silkspawners [creature]|[creature]egg|all|reload

Egg:
Aliases: eggs, eg
Description: Change the egg type
Usage: /egg [creature]|[creature]egg|all|reload

**General**
silkspawners.* (only used when usePermissions is set to true) - Grants access to all other permission (including other wildcard permissions)
*silkspawners.info (true) - Allows you to see informative messages about the spawners as you place and break them
*silkspawners.viewtype (true) - Allows you to view the spawner type using /spawner
*silkspawners.reload (op) - Allows you to reload the configs on the fly

**Placing spawners**
silkspawners.place.* or silkspawners.place.<creaturetype> (true) - Allows you to place a spawner

**Crafting spawners**
silkspawners.craft.* or silkspawners.craft.<creaturetype> (true) - Allows you to craft a spawner

**Mining spawners**
silkspawners.silkdrop.* or silkspawners.silkdrop.<creaturetype> (true) - Allows you to use silk touch to acquire mob spawner items
silkspawners.destroydrop.* or silkspawners.destroydrop.<creaturetype> (true) - Allows you to destroy mob spawners to acquire mob spawn eggs / iron bars / XP (as configured)

**Changing spawners**
silkspawners.changetype.* or silkspawners.changetype.<creaturetype> (op) - Allows you to change the spawner type using /spawner [creature]
silkspawners.changetypewithegg. or silkspawners.changetypewithegg.<creaturetype> (op) - Allows you to change the spawner type by left-clicking with a spawn egg

** Free spawners**
silkspawners.freeitem.* or silkspawners.freeitem.<creaturetype> (op) - Allows you to get spawner items in your hand for free using /spawner [creature]
silkspawners.freeitemegg.* or silkspawners.freeitemegg.<creaturetype> (op) - Allows you to get a spawner egg in your hand for free using /egg [creature]