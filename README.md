# SilkSpawners

[![Build Status](https://ci.dustplanet.de/job/SilkSpawners/badge/icon)](https://ci.dustplanet.de/job/SilkSpawners/)
[![Build the plugin](https://github.com/timbru31/SilkSpawners/workflows/Build%20the%20plugin/badge.svg)](https://github.com/timbru31/SilkSpawners/actions?query=workflow%3A%22Build+the+plugin%22)

[![BukkitDev](https://img.shields.io/badge/BukkitDev-v7.3.0-orange.svg)](https://dev.bukkit.org/projects/silkspawners/)
[![SpigotMC](https://img.shields.io/badge/SpigotMC-v7.3.0-orange.svg)](https://www.spigotmc.org/resources/7811/)

[![License](https://img.shields.io/badge/License-CC%20BY--NC--SA%204.0-blue.svg)](LICENSE.md)

## Info

This Bukkit (compatible with CraftBukkit, Spigot, Paper) plugin adds a way to obtain the spawner with the corresponding mob when mined with `Silk Touch`enchanted pickaxes and offers a range of features to change, obtain or craft spawners, too.

- Crafting of each spawner (own recipe for every mob if wanted)
- Commands for player and console
- Changing spawners with spawn eggs
- Permissions support or OP fallback
- Support for
  - **Custom mobs** added by client/server mods
  - Spawning any entity with spawn eggs (dragons, non-creature entities, etc.)
- Compatible with
  - CraftBukkit++ and Spigot (see spawnersUnstackable)
  - MCPC(+) and ported mods (auto-detects IDs)
- Flexible creature type names on input (pigman, zombiepigman, pigzombie, etc. all accepted), official names on output (Magma Cube, not "LavaSlime")
- Localization
- Economy addon [SilkSpawnersEcoAddon](https://dev.bukkit.org/projects/silkspawnersecoaddon)
- Shop addon [SilkSpawnersShopAddon](https://spigotmc.org/resources/12028/) (login required, Premium Plugin)
- BossBarAPI support for >= 1.9, otherwise BarAPI can be used
- Support for multiple Minecraft versions, from 1.8 to 1.19

_Third party features, all of them can be disabled_

- bStats for usage statistics
- Auto Updater (connecting to https://dev.bukkit.org for updating checking)

## License

This plugin is released under the
_Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International (CC BY-NC-SA 4.0)_ license.
Please see [LICENSE.md](LICENSE.md) for more information.

## Standard config

```yaml
# See documentation at https://dev.bukkit.org/projects/silkspawners/pages/configuration

# Should the plugin automatically update if an update is available?
autoUpdater: true

# Should a permission be required when a spawner explodes by TNT to achieve a drop
permissionExplode: false

# Should be checked for WorldGuard build ability to change spawners
useWorldGuard: true

# Percentage of dropping a spawner block when TNT or creepers explode
explosionDropChance: 30

# Percentage of dropping a iron bars when a spawner is mined
destroyDropChance: 100

# Percentage of dropping an egg when a spawner is mined
eggDropChance: 100

# Percentage of dropping the spawner when mined
silkDropChance: 100

# When generic spawner items are placed, spawn this creature (e.g. from /give or other plugins)
# PIG (90) is Minecraft default (put NAMES or IDs (for Minecraft <1.13 here!)
defaultCreature: pig

# How far is the spawner reachable with your crosshair (disable with -1)
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

# If a spawner is mined, should it be directly added to the inventory of the player
dropSpawnerToInventory: false

# Amount of spawners to be dropped when mined with valid silk touch
dropAmount: 1

# Flag a spawner as already mined to prevent XP duping
preventXPFarming: true

# Drops XP only when a spawner is destroyed and not mined via SilkTouch
dropXPOnlyOnDestroy: false

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

# Should spawners be unstackable
spawnersUnstackable: false

# Should the egg be consumed when the spawner is changed with it
consumeEgg: true

# Fallback if the creature should be enabled, if not specified for the entity
enableCreatureDefault: true

# Disable left click to change spawners, spawns a mob instead. Still blocks Vanilla right click behavior.
disableChangeTypeWithEgg: false

# Should instead of spawning a mob a MonsterSpawner be placed? (Uses consumeEgg value, too)
spawnEggToSpawner: false

# Should the spawn algorithm be overridden? Allows spawning of non-standard entities
spawnEggOverride: false

# Fallback if the creature should be spawned, if not specified for the entity
spawnEggOverrideSpawnDefault: true

# Allowed set of tools which can mine a spawner. IDs are supported, too
allowedTools:
  - WOOD_PICKAXE
  - STONE_PICKAXE
  - IRON_PICKAXE
  - GOLD_PICKAXE
  - DIAMOND_PICKAXE
  - NETHERITE_PICKAXE

# Amount of spawners or eggs given to a player when the argument is omitted
defaultAmountGive: 1

# Notify the player about the spawner when he clicks it in the inventory
notifyOnClick: true

# Notify the player about the spawner when he holds the spawner in the hand
notifyOnHold: true

# Configure displaying with BarAPI, time is in seconds
barAPI:
  enable: false
  displayTime: 3

# Configure displaying with 1.9 BossBarApi, time is in seconds
vanillaBossBar:
  enable: true
  displayTime: 3

  # Valid colors are BLUE, GREEN, PINK, PURPLE, RED, WHITE, YELLOW
  color: RED

  # Valid styles are SEGMENTED_10, SEGMENTED_12, SEGMENTED_20, SEGMENTED_6, SOLID
  style: SOLID

# Prevent that a spawner is changed via eggs in other territories. Supported are Factions, Faction3 and FactionsUUID
factionsSupport: false

# If the inventory of a player is full, drop spawners/eggs from /ss give on the ground
spillSpawnersFromCommands: false

# Check for spigot.yml and paper.yml flags that conflict with SilkSpawners
checkForNerfFlags: true

# Puts more information out on startup and interactions, such as block placement and breaking
verboseMode: false

# Internal stuff, do NOT change unless advised - the plugin might break otherwise
useReflection: true
testMCVersion: true
useMetadata: true
```

## Commands

Aliases:

- silkspawners
- silkspawner
- ss
- spawner
- silk
- spawnersilk
- egg
- eggs
- eg

|                  Command                  |                         Description                          |
| :---------------------------------------: | :----------------------------------------------------------: |
|                 /ss help                  |                   Displays the help menu.                    |
|               /ss list\|all               |              Displays all available creatures.               |
|              /ss view\|info               |        Displays information about the viewed spawner.        |
|              /ss reload\|rl               |               Reloads the configuration files.               |
|        /ss change\|set \<newMob\>         | Changes the spawner you are currently holding or viewing at. |
| /ss give\|add \<player\> \<mob\> [amount] |  Gives a spawner or egg to the player. Amount is optional.   |
|      /ss selfget\|i \<mob\> [amount]      |      Gives a spawner or egg to you. Amount is optional.      |

## Permissions

(Fallback to OPs, if no permissions system is found)

- Grant either wild card permission for all mobs (use the star [*])
- or define per mob permissions by using _creature_ and replace it with a real mob name.
  - spaces will be stripped out of names

#### General

|     Permission node      | Default |                                     Description                                      |
| :----------------------: | :-----: | :----------------------------------------------------------------------------------: |
|     silkspawners.\*      |  false  |                        Grants access to all other permissions                        |
|    silkspawners.info     |  true   |       See informative messages about the spawners as you place and break them        |
|  silkspawners.viewtype   |  true   |                         View the spawner type using /spawner                         |
|   silkspawners.reload    |   op    |                            Reload the configs on the fly                             |
| silkspawners.explodedrop |  false  | Receive a drop when Spawner explodes via TNT (off by default, see permissionExplode) |
|   silkspawners.list.\*   |  true   |                   Allows you to view the list of mobs in /ss list                    |

#### Placing spawners

|        Permission node        | Default |              Description              |
| :---------------------------: | :-----: | :-----------------------------------: |
|     silkspawners.place.\*     |  false  |     Ability to place all spawners     |
| silkspawners.place._creature_ |  false  | Ability to place a _creature_ spawner |

#### Crafting spawners

|        Permission node        | Default |                    Description                     |
| :---------------------------: | :-----: | :------------------------------------------------: |
|     silkspawners.craft.\*     |  false  |     Ability to craft all spawners (if enabled)     |
| silkspawners.craft._creature_ |  false  | Ability to craft a _creature_ spawner (if enabled) |

#### Mining spawners

|           Permission node           | Default |                                                Description                                                |
| :---------------------------------: | :-----: | :-------------------------------------------------------------------------------------------------------: |
|     silkspawners.destroydrop.\*     |  false  |     Allows you to destroy all mob spawners to acquire mob spawn eggs / iron bars / XP (as configured)     |
| silkspawners.destroydrop._creature_ |  false  | Allows you to destroy mob a _creature_ spawner to acquire mob spawn eggs / iron bars / XP (as configured) |
|      silkspawners.silkdrop.\*       |  false  |                       Allows you to use silk touch to acquire all mob spawner items                       |
|  silkspawners.silkdrop._creature_   |  false  |                   Allows you to use silk touch to acquire a _creature_ mob spawner item                   |
|       silkspawners.nosilk.\*        |  false  |                        Allows you to use any tool to acquire all mob spawner items                        |
|   silkspawners.nosilk._creature_    |  false  |                     Allows you to use any tool to acquire a _creature_ spawner items                      |

#### Changing spawners

|              Permission node              | Default |                                   Description                                    |
| :---------------------------------------: | :-----: | :------------------------------------------------------------------------------: |
|        silkspawners.changetype.\*         |  false  |         Allows you to change all spawner types using /spawner _creature_         |
|    silkspawners.changetype._creature_     |  false  |     Allows you to change a _creature_ spawner type using /spawner _creature_     |
|     silkspawners.changetypewithegg.\*     |  false  |     Allows you to change all spawner types by left-clicking with a spawn egg     |
| silkspawners.changetypewithegg._creature_ |  false  | Allows you to change a _creature_ spawner type by left-clicking with a spawn egg |

#### Free spawners and eggs

|           Permission node           | Default |                                         Description                                         |
| :---------------------------------: | :-----: | :-----------------------------------------------------------------------------------------: |
|      silkspawners.freeitem.\*       |  false  |     Allows you to get all spawner items in your hand for free using /spawner _creature_     |
|  silkspawners.freeitem._creature_   |  false  | Allows you to get a _creature_ spawner item in your hand for free using /spawner _creature_ |
|     silkspawners.freeitemegg.\*     |  false  |       Allows you to get all spawner eggs in your hand for free using /egg _creature_        |
| silkspawners.freeitemegg._creature_ |  false  |   Allows you to get a _creature_ spawner egg in your hand for free using /egg _creature_    |

## Credits

- mushroomhostage for the original SilkSpawners plugin
- Thermo_Core alias Archarin for the logo
- Kiracastle for the nice review
- smsunrato for the review and Indonesian translation
- Jeroendedoem for the Dutch translation
- electronicboy and Chazza for the 1.14 and 1.15 support during my absence

## Support

For support visit the [Bukkit](https://dev.bukkit.org/projects/silkspawners) page, the [SpigotMC](https://www.spigotmc.org/threads/69882/) thread or ask on [Discord](https://discord.gg/EZbr6Ks)!

## Reporting Bugs

Please open an [issue](https://github.com/timbru31/SilkSpawners/issues/new) and make sure to follow the issue template!

## Pull Requests

Feel free to submit any PRs here. :)  
Please follow the Sun Coding Guidelines, thanks!

## Building

Unfortunately, I can't give access to https://repo.dustplanet.de/artifactory/private due to the DMCA of CraftBukkit and the prohibiton to distribute those or Spigot's binaries. To successfully build SilkSpawners using Maven, you must first run Spigot's BuildTools for several versions in order to compile each module - this will populate the local `.m2` cache of yours.

```sh
mkdir -p BuildTools
cd BuildTools
wget -q https://hub.spigotmc.org/jenkins/job/BuildTools/lastSuccessfulBuild/artifact/target/BuildTools.jar
java -jar BuildTools.jar --rev 1.19 --remapped
java -jar BuildTools.jar --rev 1.18.2 --remapped
java -jar BuildTools.jar --rev 1.18.1 --remapped
java -jar BuildTools.jar --rev 1.17.1 --remapped
java -jar BuildTools.jar --rev 1.14.4
java -jar BuildTools.jar --rev 1.13.2
java -jar BuildTools.jar --rev 1.12.2
java -jar BuildTools.jar --rev 1.11.2
java -jar BuildTools.jar --rev 1.10.2
java -jar BuildTools.jar --rev 1.9.4
java -jar BuildTools.jar --rev 1.8.8
java -jar BuildTools.jar --rev 1.8
```

The final `SilkSpawners.jar` can now be generated by invoking `mvn clean package`. The location of this output can be found in the `modules/SilkSpawners/target` folder.

## Usage statistics

[![Usage statistics](https://bstats.org/signatures/bukkit/SilkSpawners.svg)](https://bstats.org/plugin/bukkit/SilkSpawners/273)

## Data usage collection of bStats

#### Disabling bStats

The file `./plugins/bStats/config.yml` contains an option to _opt-out_.

#### The following data is **read and sent** to https://bstats.org and can be seen under https://bstats.org/plugin/bukkit/SilkSpawners

- Your server's randomly generated UUID
- The amount of players on your server
- The online mode of your server
- The bukkit version of your server
- The java version of your system (e.g. Java 8)
- The name of your OS (e.g. Windows)
- The version of your OS
- The architecture of your OS (e.g. amd64)
- The system cores of your OS (e.g. 8)
- bStats-supported plugins
- Plugin version of bStats-supported plugins

## Donation

[![PayPal](https://www.paypalobjects.com/en_US/i/btn/btn_donateCC_LG.gif "Donation via PayPal")](https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=T9TEV7Q88B9M2)

![BitCoin](https://dustplanet.de/wp-content/uploads/2015/01/bitcoin-logo-plain.png "Donation via BitCoins")  
1NnrRgdy7CfiYN63vKHiypSi3MSctCP55C

## Support

In addition to reporting bugs here on GitHub you can join my Discord and ask your questions right away!
[![Discord support](https://discordapp.com/api/guilds/387315912283521027/widget.png?style=banner2)](https://discord.gg/mae6xxZFZr)
