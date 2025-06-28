package de.dustplanet.silkspawners.configs;

import java.util.ArrayList;

import org.bukkit.Material;

import de.dustplanet.util.CommentedConfiguration;

public class Config extends AbstractConfiguration {

    public Config(final CommentedConfiguration config) {
        super(config);
    }

    @Override
    public void loadConfig() {
        loadDefaultConfig();
        super.loadConfig();
    }

    private void loadDefaultConfig() {
        config.addDefault("autoUpdater", true);
        config.addComment("autoUpdater", "# See documentation at https://dev.bukkit.org/projects/silkspawners/pages/configuration", "",
                "# Should the plugin automatically update if an update is available?");
        config.addDefault("permissionExplode", false);
        config.addComment("permissionExplode", "", "# Should a permission be required when a spawner explodes by TNT to achieve a drop");
        config.addDefault("useWorldGuard", true);
        config.addComment("useWorldGuard", "", "# Should be checked for WorldGuard build ability to change spawners");
        config.addDefault("useMimic", true);
        config.addComment("useMimic", "", "# Allows you to use Mimic item IDs in allowedTools");
        config.addDefault("explosionDropChance", 30);
        config.addComment("explosionDropChance", "", "# Percentage of dropping a spawner block when TNT or creepers explode");
        config.addDefault("destroyDropChance", 100);
        config.addComment("destroyDropChance", "", "# Percentage of dropping a iron bars when a spawner is mined");
        config.addDefault("eggDropChance", 100);
        config.addComment("eggDropChance", "", "# Percentage of dropping an egg when a spawner is mined");
        config.addDefault("silkDropChance", 100);
        config.addComment("silkDropChance", "", "# Percentage of dropping the spawner when mined");
        config.addDefault("defaultCreature", "pig");
        config.addComment("defaultCreature", "",
                "# When generic spawner items are placed, spawn this creature (e.g. from /give or other plugins)",
                "# PIG (90) is Minecraft default (put NAMES or IDs (for Minecraft <1.13 here!)");
        config.addDefault("spawnerCommandReachDistance", 6);
        config.addComment("spawnerCommandReachDistance", "", "# How far is the spawner reachable with your crosshair (disable with -1)");
        config.addDefault("minSilkTouchLevel", 1);
        config.addComment("minSilkTouchLevel", "", "# Minimum silk touch level [can be changed via other plugins to a higher value]",
                "# Set it to 0 to mine it without silk touch");
        config.addDefault("noDropsCreative", true);
        config.addComment("noDropsCreative", "", "# If a player in creative destroys a spawner nothing is dropped");
        config.addDefault("destroyDropEgg", false);
        config.addComment("destroyDropEgg", "", "# If a spawner is destroyed, should the egg be dropped");
        config.addDefault("destroyDropXP", 0);
        config.addComment("destroyDropXP", "", "# If a spawner is destroyed, should XP be dropped");
        config.addDefault("dropSpawnerToInventory", false);
        config.addComment("dropSpawnerToInventory", "",
                "# If a spawner is mined, should it be directly added to the inventory of the player");
        config.addDefault("dropAmount", 1);
        config.addComment("dropAmount", "", "# Amount of spawners to be dropped when mined with valid silk touch");
        config.addDefault("preventXPFarming", true);
        config.addComment("preventXPFarming", "", "# Flag a spawner as already mined to prevent XP duping");
        config.addDefault("dropXPOnlyOnDestroy", false);
        config.addComment("dropXPOnlyOnDestroy", "", "# Drops XP only when a spawner is destroyed and not mined via silk touch");
        config.addDefault("destroyDropBars", 0);
        config.addComment("destroyDropBars", "", "# If a spawner is destroyed, should iron bars be dropped");
        config.addDefault("craftableSpawners", false);
        config.addComment("craftableSpawners", "", "# Should the player be able to craft spawners");
        config.addDefault("recipeTop", "AAA");
        config.addComment("recipeTop", "", "# Leave a slot empty (null/air)? Just make a space then, example 'A A' -> middle is free",
                "# X is always the egg");
        config.addDefault("recipeMiddle", "AXA");
        config.addDefault("recipeBottom", "AAA");
        config.addDefault("recipeAmount", 1);
        ArrayList<String> temp = new ArrayList<>();
        temp.add("A,IRON_FENCE");
        config.addDefault("ingredients", temp);
        config.addComment("ingredients", "", "# Custom example:", "#recipeTop: 'A A'", "#recipeMiddle: 'BXA'", "#recipeBottom: 'C D'",
                "#ingredients:", "#  - 'A,IRON_FENCE'", "#  - 'B,DIRT'", "#  - 'C,2'", "#  - 'D,5'", "",
                "# You can put IDs or the NAME here (please uppercase)", "# Add it for each custom ingredient you add, too!");
        config.addDefault("spawnersUnstackable", false);
        config.addComment("spawnersUnstackable", "", "# Should spawners be unstackable");
        config.addDefault("consumeEgg", true);
        config.addComment("consumeEgg", "", "# Should the egg be consumed when the spawner is changed with it");
        config.addDefault("enableCreatureDefault", true);
        config.addComment("enableCreatureDefault", "", "# Fallback if the creature should be enabled, if not specified for the entity");
        config.addDefault("disableChangeTypeWithEgg", false);
        config.addComment("disableChangeTypeWithEgg", "",
                "# Disable left click to change spawners, spawns a mob instead. Still blocks Vanilla right click behavior.");
        config.addDefault("spawnEggToSpawner", false);
        config.addComment("spawnEggToSpawner", "",
                "# Should instead of spawning a mob a MonsterSpawner be placed? (Uses consumeEgg value, too)");
        config.addDefault("spawnEggOverride", false);
        config.addComment("spawnEggOverride", "", "# Should the spawn algorithm be overridden? Allows spawning of non-standard entities");
        config.addDefault("spawnEggOverrideSpawnDefault", true);
        config.addComment("spawnEggOverrideSpawnDefault", "",
                "# Fallback if the creature should be spawned, if not specified for the entity");
        temp = new ArrayList<>();
        temp.add("WOOD_PICKAXE");
        temp.add("WOODEN_PICKAXE");
        temp.add(Material.STONE_PICKAXE.toString());
        temp.add(Material.IRON_PICKAXE.toString());
        temp.add("GOLD_PICKAXE");
        temp.add("GOLDEN_PICKAXE");
        temp.add(Material.DIAMOND_PICKAXE.toString());
        temp.add("NETHERITE_PICKAXE");
        config.addDefault("allowedTools", temp);
        config.addComment("allowedTools", "", "# Allowed set of tools which can mine a spawner. IDs are supported, too");
        config.addDefault("defaultAmountGive", 1);
        config.addComment("defaultAmountGive", "", "# Amount of spawners or eggs given to a player when the argument is omitted");
        config.addDefault("notifyOnClick", true);
        config.addComment("notifyOnClick", "", "# Notify the player about the spawner when he clicks it in the inventory");
        config.addDefault("notifyOnHold", true);
        config.addComment("notifyOnHold", "", "# Notify the player about the spawner when he holds the spawner in the hand");
        config.addDefault("barAPI.enable", false);
        config.addDefault("barAPI.displayTime", 3);
        config.addComment("barAPI", "", "# Configure displaying with BarAPI, time is in seconds");
        config.addDefault("vanillaBossBar.enable", true);
        config.addDefault("vanillaBossBar.displayTime", 3);
        config.addDefault("vanillaBossBar.color", "RED");
        config.addComment("vanillaBossBar.color", "", "# Valid colors are BLUE, GREEN, PINK, PURPLE, RED, WHITE, YELLOW");
        config.addDefault("vanillaBossBar.style", "SOLID");
        config.addComment("vanillaBossBar.style", "", "# Valid styles are SEGMENTED_10, SEGMENTED_12, SEGMENTED_20, SEGMENTED_6, SOLID");
        config.addComment("vanillaBossBar", "", "# Configure displaying with 1.9 BossBarApi, time is in seconds");
        config.addDefault("factionsSupport", false);
        config.addComment("factionsSupport", "",
                "# Prevent that a spawner is changed via eggs in other territories. Supported are Factions, Faction3 and FactionsUUID");
        config.addDefault("spillSpawnersFromCommands", false);
        config.addComment("spillSpawnersFromCommands", "",
                "# If the inventory of a player is full, drop spawners/eggs from /ss give on the ground");
        config.addDefault("checkForNerfFlags", true);
        config.addComment("checkForNerfFlags", "", "# Check for spigot.yml and paper.yml flags that conflict with SilkSpawners");
        config.addDefault("showNoSilkMessage", false);
        config.addComment("showNoSilkMessage", "",
                "# Shows a warning message to players that the given tool is not powerful enough to drop the spawner");
        config.addDefault("verboseMode", false);
        config.addComment("verboseMode", "",
                "# Puts more information out on startup and interactions, such as block placement and breaking");
        config.addDefault("useReflection", true);
        config.addComment("useReflection", "", "# Internal stuff, do NOT change unless advised - the plugin might break otherwise");
        config.addDefault("testMCVersion", true);
        config.addDefault("useMetadata", true);
    }
}
