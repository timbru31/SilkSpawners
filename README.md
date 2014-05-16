# SilkSpawners [![Build Status](http://ci.dustplanet.de/job/SilkSpawners/badge/icon)](http://ci.dustplanet.de/job/SilkSpawners/)

## Info
This CraftBukkit plugin adds a way to obtain the spawner with the corresponding mob when mined with SilkTouch and offers a various range of changing spawners.
* Crafting of each spawner (own recipe for every mob if wanted)
* Commands for player and console
* Changing spawners with spawn eggs
* Permissions support or OP fallback
* Support for
	* **Custom mobs** added by client/server mods
	* Spawning any entity with spawn eggs (dragons, non-creature entities, etc.)
* Compatible with
	* CraftBukkit++ and Spigot (see spawnersUnstackable)
	* MCPC(+) and ported mods (auto-detects IDs)
* Flexible creature type names on input (pigman, zombiepigman, pigzombie, etc. all accepted), official names on output (Magma Cube, not "LavaSlime")
* Localization
* Economy addon [SilkSpawnersEcoAddon](http://dev.bukkit.org/bukkit-plugins/silkspawnersecoaddon)

*Third party features, all of them can be disabled*
* Metrics for usage statistics
* Auto Updater (connecting to http://dev.bukkit.org for updating checking)

## License
This plugin is released under the  
*Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International (CC BY-NC-SA 4.0)* license.  
Please see [LICENSE.md](LICENSE.md) for more information.

## Standard config
````yaml
# See documentation at http://dev.bukkit.org/bukkit-plugins/silkspawners/pages/configuration

# Should the plugin automatically update if an update is available?
autoUpdater: true

# Should permissions be used
usePermissions: true

# Should be checked for WorldGuard build ability to change spawners
useWorldGuard: true

# Percentage of dropping a spawner block when TNT or creepers explode
explosionDropChance: 30

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
testMCVersion: true
````

## Commands

**SilkSpawners**
* Aliases: silkspawner, ss, spawner, silk, spawnersilk
* Description: Change or view the monster spawner creature type
* Usage: /silkspawners [creature]|[creature]egg|all|reload

**Egg**
* Aliases: eggs, eg
* Description: Change the egg type
* Usage: /egg [creature]|[creature]egg|all|reload

## Permissions
(Fallback to OPs, if no permissions system is found)
* Grant either wild card permission for all mobs (use the star [*])
* or define per mob permissions by using _creature_ and replace it with a real mob name.
	* spaces will be stripped out of names

#### General
| Permission node | Default | Description |
|:----------:|:----------:|:----------:|
| silkspawners.* | false | Grants access to all other permissions |
| silkspawners.info | true | See informative messages about the spawners as you place and break them |
| silkspawners.viewtype | true | View the spawner type using /spawner |
| silkspawners.reload | op | Reload the configs on the fly |

#### Placing spawners
| Permission node | Default | Description |
|:-----------------:|:----------:|:----------:|
| silkspawners.place.* | true | Ability to place all spawners |
| silkspawners.place._creature_ | true | Ability to place a _creature_ spawner |

#### Crafting spawners
| Permission node | Default | Description |
|:-----------------:|:----------:|:----------:|
| silkspawners.craft.* | true | Ability to craft all spawners (if enabled) |
| silkspawners.craft._creature_ | true | Ability to craft a _creature_ spawner (if enabled) |

#### Mining spawners
| Permission node | Default | Description |
|:-----------------:|:----------:|:----------:|
| silkspawners.destroydrop.* | true | Allows you to destroy all mob spawners to acquire mob spawn eggs / iron bars / XP (as configured) |
| silkspawners.destroydrop._creature_ | true | Allows you to destroy mob a _creature_ spawner to acquire mob spawn eggs / iron bars / XP (as configured) |
| silkspawners.silkdrop.* | true | Allows you to use silk touch to acquire all mob spawner items |
| silkspawners.silkdrop._creature_ | true | Allows you to use silk touch to acquire a _creautre_ mob spawner item |

#### Changing spawners
| Permission node | Default | Description |
|:-----------------:|:----------:|:----------:|
| silkspawners.changetype.* | op | Allows you to change all spawner types using /spawner _creature_ |
| silkspawners.changetype._creature_ | op | Allows you to change a _creature_ spawner type using /spawner _creature_ |
| silkspawners.changetypewithegg.* | op | Allows you to change all spawner types by left-clicking with a spawn egg |
| silkspawners.changetypewithegg._creature_ | op | Allows you to change a _creature_ spawner type by left-clicking with a spawn egg |

#### Free spawners and eggs
| Permission node | Default | Description |
|:-----------------:|:----------:|:----------:|
| silkspawners.freeitem.* | op | Allows you to get all spawner items in your hand for free using /spawner _creature_ |
| silkspawners.freeitem._creature_ | op | Allows you to get a _creature_ spawner item in your hand for free using /spawner _creature_ |
| silkspawners.freeitemegg.* | op | Allows you to get all spawner eggs in your hand for free using /egg _creature_ |
| silkspawners.freeitemegg._creature_ | op | Allows you to get a _creature_ spawner egg in your hand for free using /egg _creature_ |

## Credits
* mushroomhostage for the original SilkSpawners plugin
* Thermo_Core alias Archarin for the logo
* Kiracastle for the nice review
* smsunrato for the review and Indonesian translation
* Jeroendedoem for the Dutch translation

## Support
For support visit the dev.bukkit.org page: http://dev.bukkit.org/bukkit-plugins/silkspawners

## Pull Requests
Feel free to submit any PRs here. :)  
Please follow the Sun Coding Guidelines, thanks!

## Usage statistics
[![MCStats](http://mcstats.org/signature/SilkSpawners.png)](http://mcstats.org/plugin/SilkSpawners)

## Data usage collection of Metrics

#### Disabling Metrics
The file ../plugins/Plugin Metrics/config.yml contains an option to *opt-out*

#### The following data is **read** from the server in some way or another
* File Contents of plugins/Plugin Metrics/config.yml (created if not existent)
* Players currently online (not max player count)
* Server version string (the same version string you see in /version)
* Plugin version of the metrics-supported plugin
* Mineshafter status - it does not properly propagate Metrics requests however it is a very simple check and does not read the filesystem

#### The following data is **sent** to http://mcstats.org and can be seen under http://mcstats.org/plugin/SilkSpawners
* Metrics revision of the implementing class
* Server's GUID
* Players currently online (not max player count)
* Server version string (the same version string you see in /version)
* Plugin version of the metrics-supported plugin

## Donation
[![PayPal](https://www.paypalobjects.com/en_US/i/btn/btn_donateCC_LG.gif "Donation via PayPal")](https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=T9TEV7Q88B9M2)

![BitCoin](https://dl.dropboxusercontent.com/u/26476995/bitcoin_logo.png "Donation via BitCoins")  
Address: 1NnrRgdy7CfiYN63vKHiypSi3MSctCP55C
