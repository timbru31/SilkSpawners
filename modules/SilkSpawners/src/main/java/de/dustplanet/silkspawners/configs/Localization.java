package de.dustplanet.silkspawners.configs;

import de.dustplanet.util.CommentedConfiguration;

public class Localization extends AbstractConfiguration {

    public Localization(CommentedConfiguration config) {
        super(config);
    }

    @Override
    public void loadConfig() {
        loadDefaultLocalization();
        super.loadConfig();
    }

    private void loadDefaultLocalization() {
        config.addDefault("spawnerName", "&e%creature% &fSpawner");
        config.addComment("spawnerName",
                "# This line affects the new naming of spawners, to DISABLE this, change the message back to Monster Spawner");
        config.addDefault("addedEgg", "'&2Successfully added &e%amount% %creature% spawn egg(s) &2to your inventory.");
        config.addComment("addedEgg", "");
        config.addDefault("addedEggOtherPlayer", "&2Added &e%amount% %creature% spawn egg(s) &2to &e%player%&2''s inventory.");
        config.addDefault("addedSpawner", "&2Added &e%amount% %creature% spawner(s) &2to your inventory.");
        config.addDefault("addedSpawnerOtherPlayer", "&2Added &e%amount% %creature% spawner(s) &2to &e%player%&2''s inventory.");
        config.addDefault("noFreeSlot", "&4There is no free slot in the inventory!");
        config.addDefault("noPermission", "&4You do not have the permission to use this command!");
        config.addComment("noPermission", "");
        config.addDefault("noPermissionChangingEgg", "&4You do not have permission to change spawning eggs!");
        config.addDefault("noPermissionChangingSpawner", "&4You do not have permission to change spawners!");
        config.addDefault("noPermissionFreeEgg", "&4You do not have permission to spawn free eggs!");
        config.addDefault("noPermissionFreeSpawner", "&4You do not have the permission to spawn free spawners!");
        config.addDefault("noPermissionViewType", "&4You do not have the permission to view the type of this spawner!");
        config.addDefault("noPermissionChangingWithEggs", "&4You do not have permission to change spawners with spawn eggs!");
        config.addDefault("noPermissionCraft", "&4You do not have the permission to craft a(n) &e%creature% &4spawner!");
        config.addDefault("noPermissionPlace", "&4You do not have the permission to place a(n) &e%creature% &4spawner!");
        config.addDefault("changedEgg", "&2Successfully changed the spawning egg to a(n) &e%creature% spawn egg.");
        config.addComment("changedEgg", "");
        config.addDefault("changedSpawner", "&2Successfully changed the spawner to a(n) &e%creature% spawner.");
        config.addDefault("playerOffline", "&4Sorry this player is offline!");
        config.addComment("playerOffline", "");
        config.addDefault("changingDeniedFactions", "&4You are not allowed to change or place spawners in other peoples territory!");
        config.addDefault("changingDeniedWorldGuard", "&4Changing spawner type denied by WorldGuard protection.");
        config.addDefault("getSpawnerType", "&2This is a(n) &e%creature% spawner&2.");
        config.addDefault("unknownArgument", "&4Unrecognized argument. See &e/silkspawners help");
        config.addDefault("unknownCreature", "&4Unrecognized creature &e%creature%&4.");
        config.addDefault("useNumbers", "&4Please use a numeric number between 1 and 64!");
        config.addDefault("noConsole", "&4The console can not use this command!");
        config.addDefault("configsReloaded", "&2Successfully reloaded the configs!");
        config.addDefault("spawnerBroken", "&e%creature% spawner broken.");
        config.addComment("spawnerBroken", "");
        config.addDefault("spawnerPlaced", "&e%creature% spawner placed.");
        config.addDefault("placingDefault", "&ePlacing default spawner (either this spawner is invalid or disabled)");
        config.addDefault("informationOfSpawner1", "-- Monster Spawner --");
        config.addComment("informationOfSpawner1", "");
        config.addDefault("informationOfSpawner2", "-- Type: %creature% --");
        config.addDefault("informationOfSpawnerBar", "Monster Spawner, Type: &e%creature%");
        config.addDefault("spawningDenied", "&4Spawning of &e%creature% &4denied!");
        config.addComment("spawningDenied", "");
        config.addDefault("spawning", "&2Spawning &e%creature%");
        config.addDefault("noSpawnerHere", "&4A spawner can not be placed here, because the block above is blocked!");
        config.addDefault("lookAtSpawner", "&4You must be looking directly at a spawner.");
        config.addComment("lookAtSpawner", "");
        config.addDefault("spawnerNotDeterminable",
                "&4You either have no spawner or egg in your hand or a spawner or egg in both hands. Please make one empty!");
        config.addDefault("help",
                "&7------&6SilkSpawners v%version% Help Menu&7------\n" + "&e/ss help &7- &2Displays the help menu.\n"
                        + "&e/ss list|all &7- &2Displays all available creatures.\n"
                        + "&e/ss view|info &7- &2Displays information about the viewed spawner.\n"
                        + "&e/ss reload|rl &7- &2Reloads the configuration files.\n"
                        + "&e/ss change <newMob> &7- &2Changes the spawner you are currently holding or viewing at.\n"
                        + "&e/ss give|add <player> <mob> [amount] &7- &2Gives a spawner or egg to the player. Amount is optional.\n"
                        + "&e/ss selfget|i <mob> [amount] &7- &2Gives a spawner or egg to you. Amount is optional.");
    }
}
