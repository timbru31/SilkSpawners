package de.dustplanet.silkspawners.configs;

/**
 * Default configs
 *
 * @author (former) mushroomhostage
 * @author xGhOsTkiLLeRx
 */

import java.util.ArrayList;
import de.dustplanet.util.CommentedConfiguration;

public class Configuration {
    private CommentedConfiguration config;

    public Configuration(CommentedConfiguration config) {
        if (config == null) {
            throw new IllegalArgumentException("given config is null");
        }
        this.config = config;
    }

    public void loadConfig(String configString) {
        // Switch between our cases
        switch (configString) {
        case "config":
            loadDefaultConfig();
            break;
        case "localization":
            loadDefaultLocalization();
            break;
        case "mobs":
            loadDefaultMobs();
            break;
        default:
            loadDefaultConfig();
            break;
        }
        config.load();
        // Copy defaults and save
        config.options().copyDefaults(true);
        config.save();
        config.load();
    }

    private void loadDefaultMobs() {
        config.options().header("creatures: key is official creature type name (mobID), case-sensitive, from http://jd.bukkit.org/apidocs/org/bukkit/entity/CreatureType.html");
        ArrayList<String> tempList = new ArrayList<>();
        // Creeper
        tempList.add("c");
        tempList.add("creep");
        tempList.add("cataclysm");
        config.addDefault("creatures.Creeper.aliases", tempList);
        config.addComment("creatures.Creeper", "", " # Vanilla mobs, from http://www.minecraftwiki.net/wiki/Data_values#Entity_IDs");
        config.addDefault("creatures.Creeper.enable", true);
        config.addDefault("creatures.Creeper.enableCraftingSpawner", true);
        config.addDefault("creatures.Creeper.enableSpawnEggOverride", true);
        tempList.clear();
        // Skeleton
        tempList.add("s");
        tempList.add("sk");
        tempList.add("skelly");
        tempList.add("skellington");
        config.addDefault("creatures.Skeleton.aliases", tempList);
        config.addDefault("creatures.Skeleton.enable", true);
        config.addDefault("creatures.Skeleton.enableCraftingSpawner", true);
        config.addDefault("creatures.Skeleton.enableSpawnEggOverride", true);
        config.options().copyDefaults(true);
        tempList.clear();
        // Spider
        tempList.add("sp");
        tempList.add("bug");
        config.addDefault("creatures.Spider.aliases", tempList);
        config.addDefault("creatures.Spider.enable", true);
        config.addDefault("creatures.Spider.enableCraftingSpawner", true);
        config.addDefault("creatures.Spider.enableSpawnEggOverride", true);
        tempList.clear();
        // Giant
        tempList.add("giantzombie");
        config.addDefault("creatures.Giant.aliases", tempList);
        config.addDefault("creatures.Giant.enable", true);
        config.addDefault("creatures.Giant.enableCraftingSpawner", true);
        config.addDefault("creatures.Giant.enableSpawnEggOverride", true);
        tempList.clear();
        // Zombie
        tempList.add("z");
        tempList.add("zed");
        config.addDefault("creatures.Zombie.aliases", tempList);
        config.addDefault("creatures.Zombie.enable", true);
        config.addDefault("creatures.Zombie.enableCraftingSpawner", true);
        config.addDefault("creatures.Zombie.enableSpawnEggOverride", true);
        tempList.clear();
        // Slime
        tempList.add("sl");
        config.addDefault("creatures.Slime.aliases", tempList);
        config.addDefault("creatures.Slime.enable", true);
        config.addDefault("creatures.Slime.enableCraftingSpawner", true);
        config.addDefault("creatures.Slime.enableSpawnEggOverride", true);
        tempList.clear();
        // Ghast
        tempList.add("g");
        tempList.add("ghost");
        config.addDefault("creatures.Ghast.aliases", tempList);
        config.addDefault("creatures.Ghast.enable", true);
        config.addDefault("creatures.Ghast.enableCraftingSpawner", true);
        config.addDefault("creatures.Ghast.enableSpawnEggOverride", true);
        tempList.clear();
        // PigZombie (Zombie Pigman)
        tempList.add("pigman");
        tempList.add("zombiepigman");
        tempList.add("pg");
        tempList.add("zp");
        config.addDefault("creatures.PigZombie.aliases", tempList);
        config.addDefault("creatures.PigZombie.displayName", "Zombie Pigman");
        config.addDefault("creatures.PigZombie.enable", true);
        config.addDefault("creatures.PigZombie.enableCraftingSpawner", true);
        config.addDefault("creatures.PigZombie.enableSpawnEggOverride", true);
        tempList.clear();
        // Enderman
        tempList.add("e");
        tempList.add("ender");
        tempList.add("endermen");
        tempList.add("slenderman");
        tempList.add("slender");
        config.addDefault("creatures.Enderman.aliases", tempList);
        config.addDefault("creatures.Enderman.enable", true);
        config.addDefault("creatures.Enderman.enableCraftingSpawner", true);
        config.addDefault("creatures.Enderman.enableSpawnEggOverride", true);
        tempList.clear();
        // CaveSpider (Cave Spider)
        tempList.add("cspider");
        tempList.add("cs");
        tempList.add("bluespider");
        config.addDefault("creatures.CaveSpider.aliases", tempList);
        config.addDefault("creatures.CaveSpider.displayName", "Cave Spider");
        config.addDefault("creatures.CaveSpider.enable", true);
        config.addDefault("creatures.CaveSpider.enableCraftingSpawner", true);
        config.addDefault("creatures.CaveSpider.enableSpawnEggOverride", true);
        tempList.clear();
        // Silverfish
        tempList.add("sf");
        tempList.add("sfish");
        config.addDefault("creatures.Silverfish.aliases", tempList);
        config.addDefault("creatures.Silverfish.enable", true);
        config.addDefault("creatures.Silverfish.enableCraftingSpawner", true);
        config.addDefault("creatures.Silverfish.enableSpawnEggOverride", true);
        tempList.clear();
        // Blaze
        tempList.add("bl");
        tempList.add("b");
        config.addDefault("creatures.Blaze.aliases", tempList);
        config.addDefault("creatures.Blaze.enable", true);
        config.addDefault("creatures.Blaze.enableCraftingSpawner", true);
        config.addDefault("creatures.Blaze.enableSpawnEggOverride", true);
        tempList.clear();
        // LavaSlime (Magma Cube)
        tempList.add("magmacube");
        tempList.add("mcube");
        tempList.add("magma");
        tempList.add("m");
        tempList.add("mc");
        config.addDefault("creatures.LavaSlime.aliases", tempList);
        config.addDefault("creatures.LavaSlime.displayName", "Magma Cube");
        config.addDefault("creatures.LavaSlime.enable", true);
        config.addDefault("creatures.LavaSlime.enableCraftingSpawner", true);
        config.addDefault("creatures.LavaSlime.enableSpawnEggOverride", true);
        tempList.clear();
        // EnderDragon (Ender Dragon)
        tempList.add("dragon");
        tempList.add("raqreqentba");
        config.addDefault("creatures.EnderDragon.aliases", tempList);
        config.addDefault("creatures.EnderDragon.displayName", "Ender Dragon");
        config.addDefault("creatures.EnderDragon.enable", true);
        config.addDefault("creatures.EnderDragon.enableCraftingSpawner", true);
        config.addDefault("creatures.EnderDragon.enableSpawnEggOverride", true);
        tempList.clear();
        // Pig
        tempList.add("p");
        config.addDefault("creatures.Pig.aliases", tempList);
        config.addDefault("creatures.Pig.enable", true);
        config.addDefault("creatures.Pig.enableCraftingSpawner", true);
        config.addDefault("creatures.Pig.enableSpawnEggOverride", true);
        tempList.clear();
        // Sheep
        tempList.add("sh");
        config.addDefault("creatures.Sheep.aliases", tempList);
        config.addDefault("creatures.Sheep.enable", true);
        config.addDefault("creatures.Sheep.enableCraftingSpawner", true);
        config.addDefault("creatures.Sheep.enableSpawnEggOverride", true);
        tempList.clear();
        // Cow
        tempList.add("bovine");
        config.addDefault("creatures.Cow.aliases", tempList);
        config.addDefault("creatures.Cow.enable", true);
        config.addDefault("creatures.Cow.enableCraftingSpawner", true);
        config.addDefault("creatures.Cow.enableSpawnEggOverride", true);
        tempList.clear();
        // Chicken
        tempList.add("ch");
        tempList.add("chick");
        tempList.add("bird");
        config.addDefault("creatures.Chicken.aliases", tempList);
        config.addDefault("creatures.Chicken.enable", true);
        config.addDefault("creatures.Chicken.enableCraftingSpawner", true);
        config.addDefault("creatures.Chicken.enableSpawnEggOverride", true);
        tempList.clear();
        // Squid
        tempList.add("sq");
        tempList.add("octopus");
        config.addDefault("creatures.Squid.aliases", tempList);
        config.addDefault("creatures.Squid.enable", true);
        config.addDefault("creatures.Squid.enableCraftingSpawner", true);
        config.addDefault("creatures.Squid.enableSpawnEggOverride", true);
        tempList.clear();
        // Wolf
        tempList.add("w");
        tempList.add("dog");
        config.addDefault("creatures.Wolf.aliases", tempList);
        config.addDefault("creatures.Wolf.enable", true);
        config.addDefault("creatures.Wolf.enableCraftingSpawner", true);
        config.addDefault("creatures.Wolf.enableSpawnEggOverride", true);
        tempList.clear();
        // MushroomCow
        tempList.add("mc");
        tempList.add("mcow");
        config.addDefault("creatures.MushroomCow.aliases", tempList);
        config.addDefault("creatures.MushroomCow.displayName", "Mooshroom");
        config.addDefault("creatures.MushroomCow.enable", true);
        config.addDefault("creatures.MushroomCow.enableCraftingSpawner", true);
        config.addDefault("creatures.MushroomCow.enableSpawnEggOverride", true);
        tempList.clear();
        // SnowMan (Snow Golem)
        tempList.add("golem");
        tempList.add("sgolem");
        tempList.add("sg");
        tempList.add("sm");
        tempList.add("snowmen");
        config.addDefault("creatures.SnowMan.aliases", tempList);
        config.addDefault("creatures.SnowMan.displayName", "Snow Golem");
        config.addDefault("creatures.SnowMan.enable", true);
        config.addDefault("creatures.SnowMan.enableCraftingSpawner", true);
        config.addDefault("creatures.SnowMan.enableSpawnEggOverride", true);
        tempList.clear();
        // Ozelot (Ocelot)
        tempList.add("ocelot");
        tempList.add("oce");
        tempList.add("o");
        tempList.add("cat");
        tempList.add("kitty");
        config.addDefault("creatures.Ozelot.aliases", tempList);
        config.addDefault("creatures.Ozelot.displayName", "Ocelot");
        config.addDefault("creatures.Ozelot.enable", true);
        config.addDefault("creatures.Ozelot.enableCraftingSpawner", true);
        config.addDefault("creatures.Ozelot.enableSpawnEggOverride", true);
        tempList.clear();
        // VillagerGolem (Iron Golem)
        tempList.add("igolem");
        tempList.add("ironman");
        tempList.add("iron");
        tempList.add("ig");
        config.addDefault("creatures.VillagerGolem.aliases", tempList);
        config.addDefault("creatures.VillagerGolem.displayName", "Iron Golem");
        config.addDefault("creatures.VillagerGolem.enable", true);
        config.addDefault("creatures.VillagerGolem.enableCraftingSpawner", true);
        config.addDefault("creatures.VillagerGolem.enableSpawnEggOverride", true);
        tempList.clear();
        // Villager
        tempList.add("v");
        tempList.add("npc");
        config.addDefault("creatures.Villager.aliases", tempList);
        config.addDefault("creatures.Villager.enable", true);
        config.addDefault("creatures.Villager.enableCraftingSpawner", true);
        config.addDefault("creatures.Villager.enableSpawnEggOverride", true);
        tempList.clear();
        // Witch
        tempList.add("hag");
        tempList.add("sibly");
        tempList.add("sorceress");
        config.addDefault("creatures.Witch.aliases", tempList);
        config.addDefault("creatures.Witch.enable", true);
        config.addDefault("creatures.Witch.enableCraftingSpawner", true);
        config.addDefault("creatures.Witch.enableSpawnEggOverride", true);
        tempList.clear();
        // Bat
        tempList.add("batman");
        config.addDefault("creatures.Bat.aliases", tempList);
        config.addDefault("creatures.Bat.enable", true);
        config.addDefault("creatures.Bat.enableCraftingSpawner", true);
        config.addDefault("creatures.Bat.enableSpawnEggOverride", true);
        tempList.clear();
        // WitherBoss (Wither)
        tempList.add("wboss");
        config.addDefault("creatures.WitherBoss.aliases", tempList);
        config.addDefault("creatures.WitherBoss.displayName", "Wither");
        config.addDefault("creatures.WitherBoss.enable", true);
        config.addDefault("creatures.WitherBoss.enableCraftingSpawner", true);
        config.addDefault("creatures.WitherBoss.enableSpawnEggOverride", true);
        tempList.clear();
        // EntityHorse (Horse)
        tempList.add("horse");
        tempList.add("h");
        tempList.add("bronco");
        tempList.add("donkey");
        tempList.add("pony");
        tempList.add("mule");
        config.addDefault("creatures.EntityHorse.aliases", tempList);
        config.addDefault("creatures.EntityHorse.displayName", "Horse");
        config.addDefault("creatures.EntityHorse.enable", true);
        config.addDefault("creatures.EntityHorse.enableCraftingSpawner", true);
        config.addDefault("creatures.EntityHorse.enableSpawnEggOverride", true);
        // Item
        config.addDefault("creatures.Item.enable", false);
        config.addComment("creatures.Item", "", "# Non-mob vanilla entities", "# Enable on your own risk, some might work, some not!");
        // XPOrb
        config.addDefault("creatures.XPOrb.enable", false);
        // Painting
        config.addDefault("creatures.Painting.enable", false);
        // Arrow
        config.addDefault("creatures.Arrow.enable", false);
        // Snowball
        config.addDefault("creatures.Snowball.enable", false);
        // Fireball
        config.addDefault("creatures.Fireball.enable", false);
        // SmallFireball
        config.addDefault("creatures.SmallFireball.enable", false);
        // ThrownEnderpearl
        config.addDefault("creatures.ThrownEnderpearl.enable", false);
        // EyeOfEnderSignal
        config.addDefault("creatures.EyeOfEnderSignal.enable", false);
        // ThrownPotion
        config.addDefault("creatures.ThrownPotion.enable", false);
        // ThrownExpBottle
        config.addDefault("creatures.ThrownExpBottle.enable", false);
        // PrimedTnt
        config.addDefault("creatures.PrimedTnt.enable", false);
        // FallingSand
        config.addDefault("creatures.FallingSand.enable", false);
        // Boat
        config.addDefault("creatures.Boat.enable", false);
        // Mob
        config.addDefault("creatures.Mob.enable", false);
        // Monster
        config.addDefault("creatures.Monster.enable", false);
        // EnderCrystal
        config.addDefault("creatures.EnderCrystal.enable", false);
        // ItemFrame
        config.addDefault("creatures.ItemFrame.enable", false);
        // WitherSkull
        config.addDefault("creatures.WitherSkull.enable", false);
        // FireworksRocketEntity
        config.addDefault("creatures.FireworksRocketEntity.enable", false);
        // MinecartRideable
        config.addDefault("creatures.MinecartRideable.enable", false);
        // MinecartChest
        config.addDefault("creatures.MinecartChest.enable", false);
        // MinecartFurnace
        config.addDefault("creatures.MinecartFurnace.enable", false);
        // MinecartTNT
        config.addDefault("creatures.MinecartTNT.enable", false);
        // MinecartHopper
        config.addDefault("creatures.MinecartHopper.enable", false);
        // MinecartSpawner
        config.addDefault("creatures.MinecartSpawner.enable", false);
        // LeashKnot
        config.addDefault("creatures.LeashKnot.enable", false);
        // MinecartCommandBlock
        config.addDefault("creatures.MinecartCommandBlock.enable", false);
    }

    private void loadDefaultLocalization() {
        config.options().header("This line affects the new naming of spawners, to DISABLE this, change the message back to Monster Spawner");
        config.addDefault("spawnerName", "&e%creature% &fSpawner");
        config.addDefault("addedEgg", "'&2Successfully added a &e%creature% spawn egg &2to your inventory");
        config.addComment("addedEgg", "");
        config.addDefault("addedEggOtherPlayer", "&2Added a &e%creature% spawn egg &2to &e%player% &2''s inventory");
        config.addDefault("addedSpawner", "&2Added a &e%creature% spawner &2to your inventory");
        config.addDefault("addedSpawnerOtherPlayer", "&2Added a &e%creature% spawner &2to &e%player% &2''s inventory");
        config.addDefault("noPermission", "&4You do not have the permission to use this command");
        config.addComment("noPermission", "");
        config.addDefault("noPermissionChangingEgg", "&4You do not have permission to change spawning eggs with /egg");
        config.addDefault("noPermissionChangingSpawner", "&4You do not have permission to change spawners with /spawner");
        config.addDefault("noPermissionFreeEgg", "&4You do not have permission to spawn free eggs");
        config.addDefault("noPermissionFreeSpawner", "&4You do not have the permission to spawn free spawners");
        config.addDefault("noPermissionViewType", "&4You do not have the permission to view the type of this spawner!");
        config.addDefault("noPermissionChangingWithEggs", "&4You do not have permission to change spawners with spawn eggs");
        config.addDefault("noPermissionCraft", "&4You do not have the permission to craft a &e%creature% &4spawner!");
        config.addDefault("noPermissionPlace", "&4You do not have the permission to place a &e%creature% &4spawner!");
        config.addDefault("changedEgg", "&2Successfully changed the spawning egg to a &e%creature% spawn egg");
        config.addComment("changedEgg", "");
        config.addDefault("changedSpawner", "&2Successfully changed the spawner to a &e%creature% spawner");
        config.addDefault("playerOffline", "&4Sorry this player is offline!");
        config.addComment("playerOffline", "");
        config.addDefault("changingDeniedWorldGuard", "&4Changing spawner type denied by WorldGuard protection");
        config.addDefault("getSpawnerType", "&2This is a &e%creature% spawner");
        config.addDefault("unknownCreature", "&4Unrecognized creature &e%creature%");
        config.addDefault("useNumbers", "&4Please use a numeric number between 1 and 64!");
        config.addDefault("configsReloaded", "&2Successfully reloaded the configs!");
        config.addDefault("usageEggCommand", "&4To use this command, empty your hand (to get a free spawn egg) or have a spawn egg in your hand (to change the type)");
        config.addComment("usageEggCommand", "");
        config.addDefault("usageEggCommandCommandLine", "&4To use SilkSpawners from the command line use /egg [creature] [amount] [name]");
        config.addDefault("usageSpawnerCommand", "&4To use this command, empty your hand (to get a free spawner), point at an existing spawner or have a spawner in your hand (to change the spawner type)");
        config.addDefault("usageSpawnerCommandCommandLine", "&4To use SilkSpawners from the command line use /spawner [creature]|[creature]egg [amount] [name]");
        config.addDefault("spawnerBroken", "&e%creature% spawner broken");
        config.addComment("spawnerBroken", "");
        config.addDefault("spawnerPlaced", "&e%creature% spawner placed");
        config.addDefault("placingDefault", "&ePlacing default spawner (either this spawner is invalid or disabled)");
        config.addDefault("informationOfSpawner1", "-- Monster Spawner --");
        config.addComment("informationOfSpawner1", "");
        config.addDefault("informationOfSpawner2", "-- Type: %creature% --");
        config.addDefault("informationOfSpawner3", "-- EntityID: %ID% --");
        config.addDefault("informationOfSpawnerBar", "Monster Spawner, Type: &e%creature%&f, %ID%");
        config.addDefault("spawningDenied", "&4Spawning of &e%creature% (ID %ID%) &4denied!");
        config.addComment("spawningDenied", "");
        config.addDefault("spawning", "&2Spawning &e%creature% (ID %ID%)");
        config.addDefault("noSpawnerHere", "&4A spawner cant be placed here, because the block above is blocked!");
        config.addDefault("lookAtSpawner", "&4You must be looking directly at a spawner.");
        config.addComment("lookAtSpawner", "");
        config.addDefault("lookAtSpawnerOrInHand", "&4You must be looking directly at a spawner or have a spawner in your hand to use this command");
    }

    private void loadDefaultConfig() {
        config.options().header("See documentation at http://dev.bukkit.org/bukkit-plugins/silkspawners/pages/configuration");
        config.addDefault("autoUpdater", true);
        config.addComment("autoUpdater", "", "# Should the plugin automatically update if an update is available?");
        config.addDefault("usePermissions", true);
        config.addComment("usePermissions", "", "# Should permissions be used");
        config.addDefault("useWorldGuard", true);
        config.addComment("useWorldGuard", "", "# Should be checked for WorldGuard build ability to change spawners");
        config.addDefault("explosionDropChance", 30);
        config.addComment("explosionDropChance", "", "# Percentage of dropping a spawner block when TNT or creepers explode");
        config.addDefault("destroyDropChance", 100);
        config.addComment("destroyDropChance", "", "# Percentage of dropping a iron bars when a spawner is mined");
        config.addDefault("eggDropChance", 100);
        config.addComment("eggDropChance", "", "# Percentage of dropping an egg when a spawner is mined");
        config.addDefault("silkDropChance", 100);
        config.addComment("silkDropChance", "", "# Percentage of dropping the spawner when mined");
        config.addDefault("defaultCreature", 90);
        config.addComment("defaultCreature", "", "# When generic spawner items are placed, spawn this creature (e.g. from /give or other plugins)", "# PIG (90) is Minecraft default (put NAMES or IDs here!)");
        config.addDefault("spawnerCommandReachDistance", 6);
        config.addComment("spawnerCommandReachDistance", "", "# How far is the spawner reachable with your crosshair");
        config.addDefault("minSilkTouchLevel", 1);
        config.addComment("minSilkTouchLevel", "", "# Minimum silk touch level [can be changed via other plugins to a higher value]", "# Set it to 0 to mine it without silk touch");
        config.addDefault("noDropsCreative", true);
        config.addComment("noDropsCreative", "", "# If a player in creative destroys a spawner nothing is dropped");
        config.addDefault("destroyDropEgg", false);
        config.addComment("destroyDropEgg", "", "# If a spawner is destroyed, should the egg be dropped");
        config.addDefault("destroyDropXP", 0);
        config.addComment("destroyDropXP", "", "# If a spawner is destroyed, should XP be dropped");
        config.addDefault("preventXPFarming", true);
        config.addComment("preventXPFarming", "", "# Flag a spawner as already mined to prevent XP duping");
        config.addDefault("destroyDropBars", 0);
        config.addComment("destroyDropBars", "", "# If a spawner is destroyed, should iron bars be dropped");
        config.addDefault("craftableSpawners", false);
        config.addComment("craftableSpawners", "", "# Should the player be able to craft spawners");
        config.addDefault("recipeTop", "AAA");
        config.addComment("recipeTop", "", "# Leave a slot empty (null/air)? Just make a space then, example 'A A' -> middle is free", "# X is always the egg");
        config.addDefault("recipeMiddle", "AXA");
        config.addDefault("recipeBottom", "AAA");
        config.addDefault("recipeAmount", 1);
        ArrayList<String> temp = new ArrayList<String>();
        temp.add("A,IRON_FENCE");
        config.addDefault("ingredients", temp);
        config.addComment("ingredients", "", "# Custom example:", "#recipeTop: 'A A'", "#recipeMiddle: 'BXA'", "#recipeBottom: 'C D'",
                "#ingredients:", "#  - 'A,IRON_FENCE'", "#  - 'B,DIRT'", "#  - 'C,2'", "#  - 'D,5'",
                "", "# You can put IDs or the NAME here (please uppercase)", "# Add it for each custom ingredient you add, too!");
        config.addDefault("spawnersUnstackable", false);
        config.addComment("spawnersUnstackable", "", "# Should spawners be stackable");
        config.addDefault("consumeEgg", true);
        config.addComment("consumeEgg", "", "# Should the egg be consumed when the spawner is changed with it");
        config.addDefault("enableCreatureDefault", true);
        config.addComment("enableCreatureDefault", "", "# Fallback if the creature should be enabled, if not specified for the entity");
        config.addDefault("ignoreCheckNumbers", false);
        config.addComment("ignoreCheckNumbers", "", "# Should numbers be ignored (on eggs) and allow every number value?");
        config.addDefault("spawnEggToSpawner", false);
        config.addComment("spawnEggToSpawner", "", "# Should instead of spawning a mob a MonsterSpawner be placed? (Uses consumeEgg value, too)");
        config.addDefault("spawnEggOverride", false);
        config.addComment("spawnEggOverride", "", "# Should the spawn algorithm be overridden? Allows spawning of non-standard entities");
        config.addDefault("spawnEggOverrideSpawnDefault", true);
        config.addComment("spawnEggOverrideSpawnDefault", "", "# Fallback if the creature should be spawned, if not specified for the entity");
        config.addDefault("notifyOnClick", true);
        config.addComment("notifyOnClick", "", "# Notify the player about the spawner when he clicks it in the inventory");
        config.addDefault("notifyOnHold", true);
        config.addComment("notifyOnHold", "", "# Notify the player about the spawner when he holds the spawner in the hand");
        config.addDefault("barAPI.enable", true);
        config.addDefault("barAPI.displayTime", 3);
        config.addComment("barAPI", "", "# Configure displaying with BarAPI, time is in seconds");
        config.addDefault("verboseConfig", false);
        config.addComment("verboseConfig", "", "# Puts more information out on startup");
        config.addDefault("useReflection", true);
        config.addComment("useReflection", "", "# Internal stuff, do NOT change unless advised - the plugin might break otherwise");
    }
}
