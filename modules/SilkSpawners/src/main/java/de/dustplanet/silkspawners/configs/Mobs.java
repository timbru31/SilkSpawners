package de.dustplanet.silkspawners.configs;

import java.util.ArrayList;

import de.dustplanet.util.CommentedConfiguration;

public class Mobs extends AbstractConfiguration {
    public Mobs(final CommentedConfiguration config) {
        super(config);
    }

    @Override
    public void loadConfig() {
        loadDefaultMobs();
        super.loadConfig();
    }

    private void loadDefaultMobs() {
        config.addComment("creatures",
                "# The creatures key is official creature type name (mobID), case-sensitive, from https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/entity/EntityType.html");
        ArrayList<String> tempList = new ArrayList<>();
        // Creeper
        tempList.add("c");
        tempList.add("creep");
        tempList.add("cataclysm");
        config.addDefault("creatures.Creeper.aliases", tempList);
        config.addComment("creatures.Creeper", "",
                "# Vanilla mobs, from https://minecraft.gamepedia.com/Java_Edition_data_values#Entities for 1.13+ and https://minecraft.gamepedia.com/Java_Edition_data_values/Pre-flattening/Entity_IDs for up to 1.12");
        config.addDefault("creatures.Creeper.enable", true);
        config.addDefault("creatures.Creeper.enableCraftingSpawner", true);
        config.addDefault("creatures.Creeper.enableSpawnEggOverride", true);
        config.addDefault("creatures.creeper.aliases", tempList);
        config.addDefault("creatures.creeper.enable", true);
        config.addDefault("creatures.creeper.enableCraftingSpawner", true);
        config.addDefault("creatures.creeper.enableSpawnEggOverride", true);
        config.addDefault("creatures.creeper.displayName", "Creeper");
        tempList = new ArrayList<>();
        // Skeleton
        tempList.add("s");
        tempList.add("sk");
        tempList.add("skelly");
        tempList.add("skellington");
        config.addDefault("creatures.Skeleton.aliases", tempList);
        config.addDefault("creatures.Skeleton.enable", true);
        config.addDefault("creatures.Skeleton.enableCraftingSpawner", true);
        config.addDefault("creatures.Skeleton.enableSpawnEggOverride", true);
        config.addDefault("creatures.skeleton.aliases", tempList);
        config.addDefault("creatures.skeleton.enable", true);
        config.addDefault("creatures.skeleton.enableCraftingSpawner", true);
        config.addDefault("creatures.skeleton.enableSpawnEggOverride", true);
        config.addDefault("creatures.skeleton.displayName", "Skeleton");
        config.options().copyDefaults(true);
        tempList = new ArrayList<>();
        // Spider
        tempList.add("sp");
        tempList.add("bug");
        config.addDefault("creatures.Spider.aliases", tempList);
        config.addDefault("creatures.Spider.enable", true);
        config.addDefault("creatures.Spider.enableCraftingSpawner", true);
        config.addDefault("creatures.Spider.enableSpawnEggOverride", true);
        config.addDefault("creatures.spider.aliases", tempList);
        config.addDefault("creatures.spider.enable", true);
        config.addDefault("creatures.spider.enableCraftingSpawner", true);
        config.addDefault("creatures.spider.enableSpawnEggOverride", true);
        config.addDefault("creatures.spider.displayName", "Spider");
        tempList = new ArrayList<>();
        // Giant
        tempList.add("giantzombie");
        config.addDefault("creatures.Giant.aliases", tempList);
        config.addDefault("creatures.Giant.enable", true);
        config.addDefault("creatures.Giant.enableCraftingSpawner", true);
        config.addDefault("creatures.Giant.enableSpawnEggOverride", true);
        config.addDefault("creatures.giant.aliases", tempList);
        config.addDefault("creatures.giant.enable", true);
        config.addDefault("creatures.giant.enableCraftingSpawner", true);
        config.addDefault("creatures.giant.enableSpawnEggOverride", true);
        config.addDefault("creatures.giant.displayName", "Giant");
        tempList = new ArrayList<>();
        // Zombie
        tempList.add("z");
        tempList.add("zed");
        config.addDefault("creatures.Zombie.aliases", tempList);
        config.addDefault("creatures.Zombie.enable", true);
        config.addDefault("creatures.Zombie.enableCraftingSpawner", true);
        config.addDefault("creatures.Zombie.enableSpawnEggOverride", true);
        config.addDefault("creatures.zombie.aliases", tempList);
        config.addDefault("creatures.zombie.enable", true);
        config.addDefault("creatures.zombie.enableCraftingSpawner", true);
        config.addDefault("creatures.zombie.enableSpawnEggOverride", true);
        config.addDefault("creatures.zombie.displayName", "Zombie");
        tempList = new ArrayList<>();
        // Slime
        tempList.add("sl");
        config.addDefault("creatures.Slime.aliases", tempList);
        config.addDefault("creatures.Slime.enable", true);
        config.addDefault("creatures.Slime.enableCraftingSpawner", true);
        config.addDefault("creatures.Slime.enableSpawnEggOverride", true);
        config.addDefault("creatures.slime.aliases", tempList);
        config.addDefault("creatures.slime.enable", true);
        config.addDefault("creatures.slime.enableCraftingSpawner", true);
        config.addDefault("creatures.slime.enableSpawnEggOverride", true);
        config.addDefault("creatures.slime.displayName", "Slime");
        tempList = new ArrayList<>();
        // Ghast
        tempList.add("g");
        tempList.add("ghost");
        config.addDefault("creatures.Ghast.aliases", tempList);
        config.addDefault("creatures.Ghast.enable", true);
        config.addDefault("creatures.Ghast.enableCraftingSpawner", true);
        config.addDefault("creatures.Ghast.enableSpawnEggOverride", true);
        config.addDefault("creatures.ghast.aliases", tempList);
        config.addDefault("creatures.ghast.enable", true);
        config.addDefault("creatures.ghast.enableCraftingSpawner", true);
        config.addDefault("creatures.ghast.enableSpawnEggOverride", true);
        config.addDefault("creatures.ghast.displayName", "Ghast");
        tempList = new ArrayList<>();
        // PigZombie (Zombie Pigman)
        tempList.add("pigman");
        tempList.add("zombiepigman");
        tempList.add("pg");
        tempList.add("zp");
        tempList.add("pig_man");
        tempList.add("pig_zombie");
        tempList.add("pigzombie");
        tempList.add("zombie_pigman");
        config.addDefault("creatures.PigZombie.aliases", tempList);
        config.addDefault("creatures.PigZombie.displayName", "Zombie Pigman");
        config.addDefault("creatures.PigZombie.enable", true);
        config.addDefault("creatures.PigZombie.enableCraftingSpawner", true);
        config.addDefault("creatures.PigZombie.enableSpawnEggOverride", true);
        config.addDefault("creatures.zombie_pigman.aliases", tempList);
        config.addDefault("creatures.zombie_pigman.enable", true);
        config.addDefault("creatures.zombie_pigman.enableCraftingSpawner", true);
        config.addDefault("creatures.zombie_pigman.enableSpawnEggOverride", true);
        config.addDefault("creatures.zombie_pigman.displayName", "Zombie Pigman");
        config.addDefault("creatures.zombified_piglin.aliases", tempList);
        config.addDefault("creatures.zombified_piglin.enable", true);
        config.addDefault("creatures.zombified_piglin.enableCraftingSpawner", true);
        config.addDefault("creatures.zombified_piglin.enableSpawnEggOverride", true);
        config.addDefault("creatures.zombified_piglin.displayName", "Zombified Piglin");
        tempList = new ArrayList<>();
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
        config.addDefault("creatures.enderman.aliases", tempList);
        config.addDefault("creatures.enderman.enable", true);
        config.addDefault("creatures.enderman.enableCraftingSpawner", true);
        config.addDefault("creatures.enderman.enableSpawnEggOverride", true);
        config.addDefault("creatures.enderman.displayName", "Enderman");
        tempList = new ArrayList<>();
        // CaveSpider (Cave Spider)
        tempList.add("cspider");
        tempList.add("cs");
        tempList.add("bluespider");
        config.addDefault("creatures.CaveSpider.aliases", tempList);
        config.addDefault("creatures.CaveSpider.enable", true);
        config.addDefault("creatures.CaveSpider.enableCraftingSpawner", true);
        config.addDefault("creatures.CaveSpider.enableSpawnEggOverride", true);
        config.addDefault("creatures.CaveSpider.displayName", "Cave Spider");
        config.addDefault("creatures.cave_spider.aliases", tempList);
        config.addDefault("creatures.cave_spider.enable", true);
        config.addDefault("creatures.cave_spider.enableCraftingSpawner", true);
        config.addDefault("creatures.cave_spider.enableSpawnEggOverride", true);
        config.addDefault("creatures.cave_spider.displayName", "Cave Spider");
        tempList = new ArrayList<>();
        // Silverfish
        tempList.add("sf");
        tempList.add("sfish");
        config.addDefault("creatures.Silverfish.aliases", tempList);
        config.addDefault("creatures.Silverfish.enable", true);
        config.addDefault("creatures.Silverfish.enableCraftingSpawner", true);
        config.addDefault("creatures.Silverfish.enableSpawnEggOverride", true);
        config.addDefault("creatures.silverfish.aliases", tempList);
        config.addDefault("creatures.silverfish.enable", true);
        config.addDefault("creatures.silverfish.enableCraftingSpawner", true);
        config.addDefault("creatures.silverfish.enableSpawnEggOverride", true);
        config.addDefault("creatures.silverfish.displayName", "Silverfish");
        tempList = new ArrayList<>();
        // Blaze
        tempList.add("bl");
        tempList.add("b");
        config.addDefault("creatures.Blaze.aliases", tempList);
        config.addDefault("creatures.Blaze.enable", true);
        config.addDefault("creatures.Blaze.enableCraftingSpawner", true);
        config.addDefault("creatures.Blaze.enableSpawnEggOverride", true);
        config.addDefault("creatures.blaze.aliases", tempList);
        config.addDefault("creatures.blaze.enable", true);
        config.addDefault("creatures.blaze.enableCraftingSpawner", true);
        config.addDefault("creatures.blaze.enableSpawnEggOverride", true);
        config.addDefault("creatures.blaze.displayName", "Blaze");
        tempList = new ArrayList<>();
        // LavaSlime (Magma Cube)
        tempList.add("magmacube");
        tempList.add("mcube");
        tempList.add("magma");
        tempList.add("m");
        tempList.add("mc");
        tempList.add("lava_slime");
        tempList.add("lavaslime");
        tempList.add("lava");
        config.addDefault("creatures.LavaSlime.aliases", tempList);
        config.addDefault("creatures.LavaSlime.enable", true);
        config.addDefault("creatures.LavaSlime.enableCraftingSpawner", true);
        config.addDefault("creatures.LavaSlime.enableSpawnEggOverride", true);
        config.addDefault("creatures.LavaSlime.displayName", "Magma Cube");
        config.addDefault("creatures.magma_cube.aliases", tempList);
        config.addDefault("creatures.magma_cube.enable", true);
        config.addDefault("creatures.magma_cube.enableCraftingSpawner", true);
        config.addDefault("creatures.magma_cube.enableSpawnEggOverride", true);
        config.addDefault("creatures.magma_cube.displayName", "Magma Cube");
        tempList = new ArrayList<>();
        // EnderDragon (Ender Dragon)
        tempList.add("dragon");
        tempList.add("raqreqentba");
        config.addDefault("creatures.EnderDragon.aliases", tempList);
        config.addDefault("creatures.EnderDragon.enable", true);
        config.addDefault("creatures.EnderDragon.enableCraftingSpawner", true);
        config.addDefault("creatures.EnderDragon.enableSpawnEggOverride", true);
        config.addDefault("creatures.EnderDragon.displayName", "Ender Dragon");
        config.addDefault("creatures.ender_dragon.aliases", tempList);
        config.addDefault("creatures.ender_dragon.enable", true);
        config.addDefault("creatures.ender_dragon.enableCraftingSpawner", true);
        config.addDefault("creatures.ender_dragon.enableSpawnEggOverride", true);
        config.addDefault("creatures.ender_dragon.displayName", "Ender Dragon");
        tempList = new ArrayList<>();
        // Pig
        tempList.add("p");
        config.addDefault("creatures.Pig.aliases", tempList);
        config.addDefault("creatures.Pig.enable", true);
        config.addDefault("creatures.Pig.enableCraftingSpawner", true);
        config.addDefault("creatures.Pig.enableSpawnEggOverride", true);
        config.addDefault("creatures.pig.aliases", tempList);
        config.addDefault("creatures.pig.enable", true);
        config.addDefault("creatures.pig.enableCraftingSpawner", true);
        config.addDefault("creatures.pig.enableSpawnEggOverride", true);
        config.addDefault("creatures.pig.displayName", "Pig");
        tempList = new ArrayList<>();
        // Sheep
        tempList.add("sh");
        config.addDefault("creatures.Sheep.aliases", tempList);
        config.addDefault("creatures.Sheep.enable", true);
        config.addDefault("creatures.Sheep.enableCraftingSpawner", true);
        config.addDefault("creatures.Sheep.enableSpawnEggOverride", true);
        config.addDefault("creatures.sheep.aliases", tempList);
        config.addDefault("creatures.sheep.enable", true);
        config.addDefault("creatures.sheep.enableCraftingSpawner", true);
        config.addDefault("creatures.sheep.enableSpawnEggOverride", true);
        config.addDefault("creatures.sheep.displayName", "Sheep");
        tempList = new ArrayList<>();
        // Cow
        tempList.add("bovine");
        config.addDefault("creatures.Cow.aliases", tempList);
        config.addDefault("creatures.Cow.enable", true);
        config.addDefault("creatures.Cow.enableCraftingSpawner", true);
        config.addDefault("creatures.Cow.enableSpawnEggOverride", true);
        config.addDefault("creatures.cow.aliases", tempList);
        config.addDefault("creatures.cow.enable", true);
        config.addDefault("creatures.cow.enableCraftingSpawner", true);
        config.addDefault("creatures.cow.enableSpawnEggOverride", true);
        config.addDefault("creatures.cow.displayName", "Cow");
        tempList = new ArrayList<>();
        // Chicken
        tempList.add("ch");
        tempList.add("chick");
        tempList.add("bird");
        config.addDefault("creatures.Chicken.aliases", tempList);
        config.addDefault("creatures.Chicken.enable", true);
        config.addDefault("creatures.Chicken.enableCraftingSpawner", true);
        config.addDefault("creatures.Chicken.enableSpawnEggOverride", true);
        config.addDefault("creatures.chicken.aliases", tempList);
        config.addDefault("creatures.chicken.enable", true);
        config.addDefault("creatures.chicken.enableCraftingSpawner", true);
        config.addDefault("creatures.chicken.enableSpawnEggOverride", true);
        config.addDefault("creatures.chicken.displayName", "Chicken");
        tempList = new ArrayList<>();
        // Squid
        tempList.add("sq");
        tempList.add("octopus");
        config.addDefault("creatures.Squid.aliases", tempList);
        config.addDefault("creatures.Squid.enable", true);
        config.addDefault("creatures.Squid.enableCraftingSpawner", true);
        config.addDefault("creatures.Squid.enableSpawnEggOverride", true);
        config.addDefault("creatures.squid.aliases", tempList);
        config.addDefault("creatures.squid.enable", true);
        config.addDefault("creatures.squid.enableCraftingSpawner", true);
        config.addDefault("creatures.squid.enableSpawnEggOverride", true);
        config.addDefault("creatures.squid.displayName", "Squid");
        tempList = new ArrayList<>();
        // Wolf
        tempList.add("w");
        tempList.add("dog");
        config.addDefault("creatures.Wolf.aliases", tempList);
        config.addDefault("creatures.Wolf.enable", true);
        config.addDefault("creatures.Wolf.enableCraftingSpawner", true);
        config.addDefault("creatures.Wolf.enableSpawnEggOverride", true);
        config.addDefault("creatures.wolf.aliases", tempList);
        config.addDefault("creatures.wolf.enable", true);
        config.addDefault("creatures.wolf.enableCraftingSpawner", true);
        config.addDefault("creatures.wolf.enableSpawnEggOverride", true);
        config.addDefault("creatures.wolf.displayName", "Wolf");
        tempList = new ArrayList<>();
        // MushroomCow
        tempList.add("mc");
        tempList.add("mcow");
        tempList.add("mushroomcow");
        tempList.add("mushroom_cow");
        tempList.add("mushroom");
        config.addDefault("creatures.MushroomCow.aliases", tempList);
        config.addDefault("creatures.MushroomCow.enable", true);
        config.addDefault("creatures.MushroomCow.enableCraftingSpawner", true);
        config.addDefault("creatures.MushroomCow.enableSpawnEggOverride", true);
        config.addDefault("creatures.MushroomCow.displayName", "Mooshroom");
        config.addDefault("creatures.mooshroom.aliases", tempList);
        config.addDefault("creatures.mooshroom.enable", true);
        config.addDefault("creatures.mooshroom.enableCraftingSpawner", true);
        config.addDefault("creatures.mooshroom.enableSpawnEggOverride", true);
        config.addDefault("creatures.mooshroom.displayName", "Mooshroom");
        tempList = new ArrayList<>();
        // SnowMan (Snow Golem)
        tempList.add("golem");
        tempList.add("sgolem");
        tempList.add("sg");
        tempList.add("sm");
        tempList.add("snowmen");
        tempList.add("snowman");
        tempList.add("snow_golem");
        tempList.add("snow_man");
        config.addDefault("creatures.SnowMan.aliases", tempList);
        config.addDefault("creatures.SnowMan.enable", true);
        config.addDefault("creatures.SnowMan.enableCraftingSpawner", true);
        config.addDefault("creatures.SnowMan.enableSpawnEggOverride", true);
        config.addDefault("creatures.SnowMan.displayName", "Snow Golem");
        config.addDefault("creatures.snowman.aliases", tempList);
        config.addDefault("creatures.snowman.enable", true);
        config.addDefault("creatures.snowman.enableCraftingSpawner", true);
        config.addDefault("creatures.snowman.enableSpawnEggOverride", true);
        config.addDefault("creatures.snowman.displayName", "Snow Golem");
        config.addDefault("creatures.snow_golem.aliases", tempList);
        config.addDefault("creatures.snow_golem.enable", true);
        config.addDefault("creatures.snow_golem.enableCraftingSpawner", true);
        config.addDefault("creatures.snow_golem.enableSpawnEggOverride", true);
        config.addDefault("creatures.snow_golem.displayName", "Snow Golem");
        tempList = new ArrayList<>();
        // Ozelot (Ocelot)
        tempList.add("ocelot");
        tempList.add("oce");
        tempList.add("o");
        config.addDefault("creatures.Ozelot.aliases", tempList);
        config.addDefault("creatures.Ozelot.enable", true);
        config.addDefault("creatures.Ozelot.enableCraftingSpawner", true);
        config.addDefault("creatures.Ozelot.enableSpawnEggOverride", true);
        config.addDefault("creatures.Ozelot.displayName", "Ocelot");
        config.addDefault("creatures.ocelot.aliases", tempList);
        config.addDefault("creatures.ocelot.enable", true);
        config.addDefault("creatures.ocelot.enableCraftingSpawner", true);
        config.addDefault("creatures.ocelot.enableSpawnEggOverride", true);
        config.addDefault("creatures.ocelot.displayName", "Ocelot");
        tempList = new ArrayList<>();
        // VillagerGolem (Iron Golem)
        tempList.add("igolem");
        tempList.add("ironman");
        tempList.add("iron");
        tempList.add("ig");
        config.addDefault("creatures.VillagerGolem.aliases", tempList);
        config.addDefault("creatures.VillagerGolem.enable", true);
        config.addDefault("creatures.VillagerGolem.enableCraftingSpawner", true);
        config.addDefault("creatures.VillagerGolem.enableSpawnEggOverride", true);
        config.addDefault("creatures.VillagerGolem.displayName", "Iron Golem");
        config.addDefault("creatures.villager_golem.aliases", tempList);
        config.addDefault("creatures.villager_golem.enable", true);
        config.addDefault("creatures.villager_golem.enableCraftingSpawner", true);
        config.addDefault("creatures.villager_golem.enableSpawnEggOverride", true);
        config.addDefault("creatures.villager_golem.displayName", "Iron Golem");
        tempList = new ArrayList<>();
        // Villager
        tempList.add("v");
        tempList.add("npc");
        config.addDefault("creatures.Villager.aliases", tempList);
        config.addDefault("creatures.Villager.enable", true);
        config.addDefault("creatures.Villager.enableCraftingSpawner", true);
        config.addDefault("creatures.Villager.enableSpawnEggOverride", true);
        config.addDefault("creatures.villager.aliases", tempList);
        config.addDefault("creatures.villager.enable", true);
        config.addDefault("creatures.villager.enableCraftingSpawner", true);
        config.addDefault("creatures.villager.enableSpawnEggOverride", true);
        config.addDefault("creatures.villager.displayName", "Villager");
        tempList = new ArrayList<>();
        // Witch
        tempList.add("hag");
        tempList.add("sibly");
        tempList.add("sorceress");
        config.addDefault("creatures.Witch.aliases", tempList);
        config.addDefault("creatures.Witch.enable", true);
        config.addDefault("creatures.Witch.enableCraftingSpawner", true);
        config.addDefault("creatures.Witch.enableSpawnEggOverride", true);
        config.addDefault("creatures.witch.aliases", tempList);
        config.addDefault("creatures.witch.enable", true);
        config.addDefault("creatures.witch.enableCraftingSpawner", true);
        config.addDefault("creatures.witch.enableSpawnEggOverride", true);
        config.addDefault("creatures.witch.displayName", "Witch");
        tempList = new ArrayList<>();
        // Bat
        tempList.add("batman");
        config.addDefault("creatures.Bat.aliases", tempList);
        config.addDefault("creatures.Bat.enable", true);
        config.addDefault("creatures.Bat.enableCraftingSpawner", true);
        config.addDefault("creatures.Bat.enableSpawnEggOverride", true);
        config.addDefault("creatures.bat.aliases", tempList);
        config.addDefault("creatures.bat.enable", true);
        config.addDefault("creatures.bat.enableCraftingSpawner", true);
        config.addDefault("creatures.bat.enableSpawnEggOverride", true);
        config.addDefault("creatures.bat.displayName", "Bat");
        tempList = new ArrayList<>();
        // WitherBoss (Wither)
        tempList.add("wboss");
        config.addDefault("creatures.WitherBoss.aliases", tempList);
        config.addDefault("creatures.WitherBoss.enable", true);
        config.addDefault("creatures.WitherBoss.enableCraftingSpawner", true);
        config.addDefault("creatures.WitherBoss.enableSpawnEggOverride", true);
        config.addDefault("creatures.WitherBoss.displayName", "Wither");
        config.addDefault("creatures.wither.aliases", tempList);
        config.addDefault("creatures.wither.enable", true);
        config.addDefault("creatures.wither.enableCraftingSpawner", true);
        config.addDefault("creatures.wither.enableSpawnEggOverride", true);
        config.addDefault("creatures.wither.displayName", "Wither");
        tempList = new ArrayList<>();
        // EntityHorse (Horse)
        tempList.add("horse");
        tempList.add("h");
        tempList.add("bronco");
        tempList.add("pony");
        config.addDefault("creatures.EntityHorse.aliases", tempList);
        config.addDefault("creatures.EntityHorse.enable", true);
        config.addDefault("creatures.EntityHorse.enableCraftingSpawner", true);
        config.addDefault("creatures.EntityHorse.enableSpawnEggOverride", true);
        config.addDefault("creatures.EntityHorse.displayName", "Horse");
        config.addDefault("creatures.horse.aliases", tempList);
        config.addDefault("creatures.horse.enable", true);
        config.addDefault("creatures.horse.enableCraftingSpawner", true);
        config.addDefault("creatures.horse.enableSpawnEggOverride", true);
        config.addDefault("creatures.horse.displayName", "Horse");
        tempList = new ArrayList<>();
        // Rabbit
        tempList.add("r");
        tempList.add("rab");
        tempList.add("bunny");
        tempList.add("hare");
        tempList.add("cony");
        tempList.add("coney");
        config.addDefault("creatures.Rabbit.aliases", tempList);
        config.addDefault("creatures.Rabbit.enable", true);
        config.addDefault("creatures.Rabbit.enableCraftingSpawner", true);
        config.addDefault("creatures.Rabbit.enableSpawnEggOverride", true);
        config.addDefault("creatures.rabbit.aliases", tempList);
        config.addDefault("creatures.rabbit.enable", true);
        config.addDefault("creatures.rabbit.enableCraftingSpawner", true);
        config.addDefault("creatures.rabbit.enableSpawnEggOverride", true);
        config.addDefault("creatures.rabbit.displayName", "Rabbit");
        tempList = new ArrayList<>();
        // Endermite
        tempList.add("mite");
        tempList.add("acarid");
        tempList.add("acarian");
        tempList.add("acarine");
        config.addDefault("creatures.Endermite.aliases", tempList);
        config.addDefault("creatures.Endermite.enable", true);
        config.addDefault("creatures.Endermite.enableCraftingSpawner", true);
        config.addDefault("creatures.Endermite.enableSpawnEggOverride", true);
        config.addDefault("creatures.endermite.aliases", tempList);
        config.addDefault("creatures.endermite.enable", true);
        config.addDefault("creatures.endermite.enableCraftingSpawner", true);
        config.addDefault("creatures.endermite.enableSpawnEggOverride", true);
        config.addDefault("creatures.endermite.displayName", "Endermite");
        tempList = new ArrayList<>();
        // Guardian
        tempList.add("keeper");
        tempList.add("guard");
        tempList.add("watcher");
        config.addDefault("creatures.Guardian.aliases", tempList);
        config.addDefault("creatures.Guardian.enable", true);
        config.addDefault("creatures.Guardian.enableCraftingSpawner", true);
        config.addDefault("creatures.Guardian.enableSpawnEggOverride", true);
        config.addDefault("creatures.guardian.aliases", tempList);
        config.addDefault("creatures.guardian.enable", true);
        config.addDefault("creatures.guardian.enableCraftingSpawner", true);
        config.addDefault("creatures.guardian.enableSpawnEggOverride", true);
        config.addDefault("creatures.guardian.displayName", "Guardian");
        tempList = new ArrayList<>();
        // Shulker
        tempList.add("shulk");
        tempList.add("shelllurker");
        tempList.add("shell_lurker");
        config.addDefault("creatures.Shulker.aliases", tempList);
        config.addDefault("creatures.Shulker.enable", true);
        config.addDefault("creatures.Shulker.enableCraftingSpawner", true);
        config.addDefault("creatures.Shulker.enableSpawnEggOverride", true);
        config.addDefault("creatures.shulker.aliases", tempList);
        config.addDefault("creatures.shulker.enable", true);
        config.addDefault("creatures.shulker.enableCraftingSpawner", true);
        config.addDefault("creatures.shulker.enableSpawnEggOverride", true);
        config.addDefault("creatures.shulker.displayName", "Shulker");
        tempList = new ArrayList<>();
        // PolarBear
        tempList.add("bear");
        tempList.add("polar");
        tempList.add("ursus");
        tempList.add("whitebear");
        tempList.add("icebear");
        config.addDefault("creatures.PolarBear.aliases", tempList);
        config.addDefault("creatures.PolarBear.enable", true);
        config.addDefault("creatures.PolarBear.enableCraftingSpawner", true);
        config.addDefault("creatures.PolarBear.enableSpawnEggOverride", true);
        config.addDefault("creatures.PolarBear.displayName", "Polar Bear");
        config.addDefault("creatures.polar_bear.aliases", tempList);
        config.addDefault("creatures.polar_bear.enable", true);
        config.addDefault("creatures.polar_bear.enableCraftingSpawner", true);
        config.addDefault("creatures.polar_bear.enableSpawnEggOverride", true);
        config.addDefault("creatures.polar_bear.displayName", "Polar Bear");
        tempList = new ArrayList<>();
        // Elder Guardian
        tempList.add("elder");
        config.addDefault("creatures.elder_guardian.aliases", tempList);
        config.addDefault("creatures.elder_guardian.enable", true);
        config.addDefault("creatures.elder_guardian.enableCraftingSpawner", true);
        config.addDefault("creatures.elder_guardian.enableSpawnEggOverride", true);
        config.addDefault("creatures.elder_guardian.displayName", "Elder Guardian");
        tempList = new ArrayList<>();
        // Wither Skeleton
        tempList.add("witherskeleton");
        config.addDefault("creatures.wither_skeleton.aliases", tempList);
        config.addDefault("creatures.wither_skeleton.enable", true);
        config.addDefault("creatures.wither_skeleton.enableCraftingSpawner", true);
        config.addDefault("creatures.wither_skeleton.enableSpawnEggOverride", true);
        config.addDefault("creatures.wither_skeleton.displayName", "Wither Skeleton");
        tempList = new ArrayList<>();
        // Stray
        config.addDefault("creatures.stray.enable", true);
        config.addDefault("creatures.stray.enableCraftingSpawner", true);
        config.addDefault("creatures.stray.enableSpawnEggOverride", true);
        config.addDefault("creatures.stray.displayName", "Stray");
        // Husk
        config.addDefault("creatures.husk.enable", true);
        config.addDefault("creatures.husk.enableCraftingSpawner", true);
        config.addDefault("creatures.husk.enableSpawnEggOverride", true);
        config.addDefault("creatures.husk.displayName", "Husk");
        // Zombie Villager
        tempList.add("zombievillager");
        config.addDefault("creatures.zombie_villager.aliases", tempList);
        config.addDefault("creatures.zombie_villager.enable", true);
        config.addDefault("creatures.zombie_villager.enableCraftingSpawner", true);
        config.addDefault("creatures.zombie_villager.enableSpawnEggOverride", true);
        config.addDefault("creatures.zombie_villager.displayName", "Zombie Villager");
        tempList = new ArrayList<>();
        // Skeleton Horse
        tempList.add("skeletonhorse");
        config.addDefault("creatures.skeleton_horse.aliases", tempList);
        config.addDefault("creatures.skeleton_horse.enable", true);
        config.addDefault("creatures.skeleton_horse.enableCraftingSpawner", true);
        config.addDefault("creatures.skeleton_horse.enableSpawnEggOverride", true);
        config.addDefault("creatures.skeleton_horse.displayName", "Skeleton Horse");
        tempList = new ArrayList<>();
        // Zombie Horse
        tempList.add("zombiehorse");
        config.addDefault("creatures.zombie_horse.aliases", tempList);
        config.addDefault("creatures.zombie_horse.enable", true);
        config.addDefault("creatures.zombie_horse.enableCraftingSpawner", true);
        config.addDefault("creatures.zombie_horse.enableSpawnEggOverride", true);
        config.addDefault("creatures.zombie_horse.displayName", "Zombie Horse");
        tempList = new ArrayList<>();
        // Donkey
        config.addDefault("creatures.donkey.enable", true);
        config.addDefault("creatures.donkey.enableCraftingSpawner", true);
        config.addDefault("creatures.donkey.enableSpawnEggOverride", true);
        config.addDefault("creatures.donkey.displayName", "Donkey");
        // Mule
        config.addDefault("creatures.mule.enable", true);
        config.addDefault("creatures.mule.enableCraftingSpawner", true);
        config.addDefault("creatures.mule.enableSpawnEggOverride", true);
        config.addDefault("creatures.mule.displayName", "Mule");
        // Evoker
        tempList.add("evoker");
        config.addDefault("creatures.evocation_illager.aliases", tempList);
        config.addDefault("creatures.evocation_illager.enable", true);
        config.addDefault("creatures.evocation_illager.enableCraftingSpawner", true);
        config.addDefault("creatures.evocation_illager.enableSpawnEggOverride", true);
        config.addDefault("creatures.evocation_illager.displayName", "Evoker");
        tempList = new ArrayList<>();
        // Vex
        config.addDefault("creatures.vex.enable", true);
        config.addDefault("creatures.vex.enableCraftingSpawner", true);
        config.addDefault("creatures.vex.enableSpawnEggOverride", true);
        config.addDefault("creatures.vex.displayName", "Vex");
        // Vindicator
        tempList.add("vindicator");
        config.addDefault("creatures.vindication_illager.aliases", tempList);
        config.addDefault("creatures.vindication_illager.enable", true);
        config.addDefault("creatures.vindication_illager.enableCraftingSpawner", true);
        config.addDefault("creatures.vindication_illager.enableSpawnEggOverride", true);
        config.addDefault("creatures.vindication_illager.displayName", "Vindicator");
        tempList = new ArrayList<>();
        // Llama
        tempList.add("spitter");
        config.addDefault("creatures.llama.aliases", tempList);
        config.addDefault("creatures.llama.enable", true);
        config.addDefault("creatures.llama.enableCraftingSpawner", true);
        config.addDefault("creatures.llama.enableSpawnEggOverride", true);
        config.addDefault("creatures.llama.displayName", "Llama");
        tempList = new ArrayList<>();
        // Illusioner
        tempList.add("illusioner");
        tempList.add("illusion");
        config.addDefault("creatures.illusion_illager.aliases", tempList);
        config.addDefault("creatures.illusion_illager.enable", true);
        config.addDefault("creatures.illusion_illager.enableCraftingSpawner", true);
        config.addDefault("creatures.illusion_illager.enableSpawnEggOverride", true);
        config.addDefault("creatures.illusion_illager.displayName", "Illusioner");
        tempList = new ArrayList<>();
        // Parrot
        tempList.add("macaw");
        tempList.add("pirate_bird");
        tempList.add("popinjay");
        config.addDefault("creatures.parrot.aliases", tempList);
        config.addDefault("creatures.parrot.enable", true);
        config.addDefault("creatures.parrot.enableCraftingSpawner", true);
        config.addDefault("creatures.parrot.enableSpawnEggOverride", true);
        config.addDefault("creatures.parrot.displayName", "Parrot");
        tempList = new ArrayList<>();
        // Cod
        tempList.add("codfish");
        tempList.add("codling");
        config.addDefault("creatures.cod.aliases", tempList);
        config.addDefault("creatures.cod.enable", true);
        config.addDefault("creatures.cod.enableCraftingSpawner", true);
        config.addDefault("creatures.cod.enableSpawnEggOverride", true);
        config.addDefault("creatures.cod.displayName", "Cod");
        tempList = new ArrayList<>();
        // Dolphin
        tempList.add("delphinus");
        config.addDefault("creatures.dolphin.aliases", tempList);
        config.addDefault("creatures.dolphin.enable", true);
        config.addDefault("creatures.dolphin.enableCraftingSpawner", true);
        config.addDefault("creatures.dolphin.enableSpawnEggOverride", true);
        config.addDefault("creatures.dolphin.displayName", "Dolphin");
        tempList = new ArrayList<>();
        // Drowned
        tempList.add("underwater_z");
        tempList.add("underwater_zombie");
        tempList.add("underwaterz");
        tempList.add("underwaterzombie");
        config.addDefault("creatures.drowned.aliases", tempList);
        config.addDefault("creatures.drowned.enable", true);
        config.addDefault("creatures.drowned.enableCraftingSpawner", true);
        config.addDefault("creatures.drowned.enableSpawnEggOverride", true);
        config.addDefault("creatures.drowned.displayName", "Drowned");
        tempList = new ArrayList<>();
        // Evoker
        config.addDefault("creatures.evoker.enable", true);
        config.addDefault("creatures.evoker.enableCraftingSpawner", true);
        config.addDefault("creatures.evoker.enableSpawnEggOverride", true);
        config.addDefault("creatures.evoker.displayName", "Evoker");
        // Illusioner
        tempList.add("illusion");
        config.addDefault("creatures.illusioner.aliases", tempList);
        config.addDefault("creatures.illusioner.enable", true);
        config.addDefault("creatures.illusioner.enableCraftingSpawner", true);
        config.addDefault("creatures.illusioner.enableSpawnEggOverride", true);
        config.addDefault("creatures.illusioner.displayName", "Illusioner");
        tempList = new ArrayList<>();
        // Pufferfish
        tempList.add("globefish");
        tempList.add("puffer");
        tempList.add("blowfish");
        config.addDefault("creatures.pufferfish.aliases", tempList);
        config.addDefault("creatures.pufferfish.enable", true);
        config.addDefault("creatures.pufferfish.enableCraftingSpawner", true);
        config.addDefault("creatures.pufferfish.enableSpawnEggOverride", true);
        config.addDefault("creatures.pufferfish.displayName", "Pufferfish");
        tempList = new ArrayList<>();
        // Salmon
        tempList.add("samlet");
        tempList.add("smolt");
        tempList.add("grilse");
        config.addDefault("creatures.salmon.aliases", tempList);
        config.addDefault("creatures.salmon.enable", true);
        config.addDefault("creatures.salmon.enableCraftingSpawner", true);
        config.addDefault("creatures.salmon.enableSpawnEggOverride", true);
        config.addDefault("creatures.salmon.displayName", "Salmon");
        tempList = new ArrayList<>();
        // Tropical Fish
        config.addDefault("creatures.tropical_fish.enable", true);
        config.addDefault("creatures.tropical_fish.enableCraftingSpawner", true);
        config.addDefault("creatures.tropical_fish.enableSpawnEggOverride", true);
        config.addDefault("creatures.tropical_fish.displayName", "Tropical Fish");
        // Turtle
        tempList.add("tortoise");
        tempList.add("testudo");
        config.addDefault("creatures.turtle.aliases", tempList);
        config.addDefault("creatures.turtle.enable", true);
        config.addDefault("creatures.turtle.enableCraftingSpawner", true);
        config.addDefault("creatures.turtle.enableSpawnEggOverride", true);
        config.addDefault("creatures.turtle.displayName", "Turtle");
        tempList = new ArrayList<>();
        // Vindicator
        config.addDefault("creatures.vindicator.enable", true);
        config.addDefault("creatures.vindicator.enableCraftingSpawner", true);
        config.addDefault("creatures.vindicator.enableSpawnEggOverride", true);
        config.addDefault("creatures.vindicator.displayName", "Vindicator");
        // Phantom
        tempList.add("spectre");
        tempList.add("specter");
        config.addDefault("creatures.phantom.aliases", tempList);
        config.addDefault("creatures.phantom.enable", true);
        config.addDefault("creatures.phantom.enableCraftingSpawner", true);
        config.addDefault("creatures.phantom.enableSpawnEggOverride", true);
        config.addDefault("creatures.phantom.displayName", "Phantom");
        tempList = new ArrayList<>();
        // Iron Golem
        tempList.add("igolem");
        tempList.add("ironman");
        tempList.add("iron");
        tempList.add("ig");
        tempList.add("villager_golem");
        tempList.add("villagergolem");
        config.addDefault("creatures.iron_golem.aliases", tempList);
        config.addDefault("creatures.iron_golem.enable", true);
        config.addDefault("creatures.iron_golem.enableCraftingSpawner", true);
        config.addDefault("creatures.iron_golem.enableSpawnEggOverride", true);
        config.addDefault("creatures.iron_golem.displayName", "Iron Golem");
        tempList = new ArrayList<>();
        // Cat
        tempList.add("feline");
        tempList.add("mog");
        tempList.add("kitty");
        config.addDefault("creatures.cat.aliases", tempList);
        config.addDefault("creatures.cat.enable", true);
        config.addDefault("creatures.cat.enableCraftingSpawner", true);
        config.addDefault("creatures.cat.enableSpawnEggOverride", true);
        config.addDefault("creatures.cat.displayName", "Cat");
        config.options().copyDefaults(true);
        tempList = new ArrayList<>();
        // Fox
        config.addDefault("creatures.fox.enable", true);
        config.addDefault("creatures.fox.enableCraftingSpawner", true);
        config.addDefault("creatures.fox.enableSpawnEggOverride", true);
        config.addDefault("creatures.fox.displayName", "Fox");
        config.options().copyDefaults(true);
        // Panda
        config.addDefault("creatures.panda.enable", true);
        config.addDefault("creatures.panda.enableCraftingSpawner", true);
        config.addDefault("creatures.panda.enableSpawnEggOverride", true);
        config.addDefault("creatures.panda.displayName", "Panda");
        config.options().copyDefaults(true);
        // Trader Llama
        config.addDefault("creatures.trader_llama.enable", true);
        config.addDefault("creatures.trader_llama.enableCraftingSpawner", true);
        config.addDefault("creatures.trader_llama.enableSpawnEggOverride", true);
        config.addDefault("creatures.trader_llama.displayName", "Trader Llama");
        config.options().copyDefaults(true);
        // Pillager
        tempList.add("looter");
        tempList.add("plunderer");
        tempList.add("depredator");
        tempList.add("raider");
        tempList.add("scavenger");
        config.addDefault("creatures.pillager.aliases", tempList);
        config.addDefault("creatures.pillager.enable", true);
        config.addDefault("creatures.pillager.enableCraftingSpawner", true);
        config.addDefault("creatures.pillager.enableSpawnEggOverride", true);
        config.addDefault("creatures.pillager.displayName", "Pillager");
        config.options().copyDefaults(true);
        tempList = new ArrayList<>();
        // Wandering Trader
        tempList.add("wanderer");
        config.addDefault("creatures.wandering_trader.aliases", tempList);
        config.addDefault("creatures.wandering_trader.enable", true);
        config.addDefault("creatures.wandering_trader.enableCraftingSpawner", true);
        config.addDefault("creatures.wandering_trader.enableSpawnEggOverride", true);
        config.addDefault("creatures.wandering_trader.displayName", "Wandering Trader");
        config.options().copyDefaults(true);
        tempList = new ArrayList<>();
        // Ravager
        tempList.add("devastator");
        tempList.add("desolater");
        tempList.add("desolator");
        config.addDefault("creatures.ravager.aliases", tempList);
        config.addDefault("creatures.ravager.enable", true);
        config.addDefault("creatures.ravager.enableCraftingSpawner", true);
        config.addDefault("creatures.ravager.enableSpawnEggOverride", true);
        config.addDefault("creatures.ravager.displayName", "Ravager");
        config.options().copyDefaults(true);
        tempList = new ArrayList<>();
        // Bee
        tempList.add("bees");
        config.addDefault("creatures.bee.aliases", tempList);
        config.addDefault("creatures.bee.enable", true);
        config.addDefault("creatures.bee.enableCraftingSpawner", true);
        config.addDefault("creatures.bee.enableSpawnEggOverride", true);
        config.addDefault("creatures.bee.displayName", "Bee");
        config.options().copyDefaults(true);
        tempList = new ArrayList<>();
        // Hoglin
        tempList.add("hoglin");
        config.addDefault("creatures.hoglin.aliases", tempList);
        config.addDefault("creatures.hoglin.enable", true);
        config.addDefault("creatures.hoglin.enableCraftingSpawner", true);
        config.addDefault("creatures.hoglin.enableSpawnEggOverride", true);
        config.addDefault("creatures.hoglin.displayName", "Hoglin");
        config.options().copyDefaults(true);
        tempList = new ArrayList<>();
        // Zoglin
        tempList.add("zoglin");
        config.addDefault("creatures.zoglin.aliases", tempList);
        config.addDefault("creatures.zoglin.enable", true);
        config.addDefault("creatures.zoglin.enableCraftingSpawner", true);
        config.addDefault("creatures.zoglin.enableSpawnEggOverride", true);
        config.addDefault("creatures.zoglin.displayName", "Zoglin");
        config.options().copyDefaults(true);
        tempList = new ArrayList<>();
        // Piglin
        tempList.add("piglin");
        config.addDefault("creatures.piglin.aliases", tempList);
        config.addDefault("creatures.piglin.enable", true);
        config.addDefault("creatures.piglin.enableCraftingSpawner", true);
        config.addDefault("creatures.piglin.enableSpawnEggOverride", true);
        config.addDefault("creatures.piglin.displayName", "Piglin");
        config.options().copyDefaults(true);
        tempList = new ArrayList<>();
        // Strider
        tempList.add("strider");
        tempList.add("runner");
        config.addDefault("creatures.strider.aliases", tempList);
        config.addDefault("creatures.strider.enable", true);
        config.addDefault("creatures.strider.enableCraftingSpawner", true);
        config.addDefault("creatures.strider.enableSpawnEggOverride", true);
        config.addDefault("creatures.strider.displayName", "Strider");
        config.options().copyDefaults(true);
        tempList = new ArrayList<>();
        // Piglin Brute
        tempList.add("piglin_brute");
        tempList.add("piglinbrute");
        tempList.add("brute");
        config.addDefault("creatures.piglin_brute.aliases", tempList);
        config.addDefault("creatures.piglin_brute.enable", true);
        config.addDefault("creatures.piglin_brute.enableCraftingSpawner", true);
        config.addDefault("creatures.piglin_brute.enableSpawnEggOverride", true);
        config.addDefault("creatures.piglin_brute.displayName", "Piglin Brute");
        config.options().copyDefaults(true);
        tempList = new ArrayList<>();
        // Axolotl
        tempList.add("mudpuppy");
        tempList.add("mud_puppy");
        config.addDefault("creatures.axolotl.aliases", tempList);
        config.addDefault("creatures.axolotl.enable", true);
        config.addDefault("creatures.axolotl.enableCraftingSpawner", true);
        config.addDefault("creatures.axolotl.enableSpawnEggOverride", true);
        config.addDefault("creatures.axolotl.displayName", "Axolotl");
        config.options().copyDefaults(true);
        tempList = new ArrayList<>();
        // Glow Squid
        tempList.add("glowsq");
        tempList.add("glowsquid");
        tempList.add("gsquid");
        tempList.add("octopus");
        tempList.add("goctopus");
        tempList.add("glowoctopus");
        config.addDefault("creatures.glow_squid.aliases", tempList);
        config.addDefault("creatures.glow_squid.enable", true);
        config.addDefault("creatures.glow_squid.enableCraftingSpawner", true);
        config.addDefault("creatures.glow_squid.enableSpawnEggOverride", true);
        config.addDefault("creatures.glow_squid.displayName", "Glow Squid");
        config.options().copyDefaults(true);
        tempList = new ArrayList<>();
        // Goat
        tempList.add("capra");
        config.addDefault("creatures.goat.aliases", tempList);
        config.addDefault("creatures.goat.enable", true);
        config.addDefault("creatures.goat.enableCraftingSpawner", true);
        config.addDefault("creatures.goat.enableSpawnEggOverride", true);
        config.addDefault("creatures.goat.displayName", "Goat");
        config.options().copyDefaults(true);
        tempList = new ArrayList<>();
        // Allay
        tempList.add("helper");
        config.addDefault("creatures.allay.aliases", tempList);
        config.addDefault("creatures.allay.enable", true);
        config.addDefault("creatures.allay.enableCraftingSpawner", true);
        config.addDefault("creatures.allay.enableSpawnEggOverride", true);
        config.addDefault("creatures.allay.displayName", "Allay");
        config.options().copyDefaults(true);
        tempList = new ArrayList<>();
        // Frog
        tempList.add("squib");
        tempList.add("toad");
        config.addDefault("creatures.frog.aliases", tempList);
        config.addDefault("creatures.frog.enable", true);
        config.addDefault("creatures.frog.enableCraftingSpawner", true);
        config.addDefault("creatures.frog.enableSpawnEggOverride", true);
        config.addDefault("creatures.frog.displayName", "Frog");
        config.options().copyDefaults(true);
        tempList = new ArrayList<>();
        // Tadpole
        tempList.add("polliwog");
        tempList.add("pollywog");
        tempList.add("tad");
        config.addDefault("creatures.tadpole.aliases", tempList);
        config.addDefault("creatures.tadpole.enable", true);
        config.addDefault("creatures.tadpole.enableCraftingSpawner", true);
        config.addDefault("creatures.tadpole.enableSpawnEggOverride", true);
        config.addDefault("creatures.tadpole.displayName", "Tadpole");
        config.options().copyDefaults(true);
        tempList = new ArrayList<>();
        // Warden
        config.addDefault("creatures.warden.enable", true);
        config.addDefault("creatures.warden.enableCraftingSpawner", true);
        config.addDefault("creatures.warden.enableSpawnEggOverride", true);
        config.addDefault("creatures.warden.displayName", "Warden");
        config.options().copyDefaults(true);

        // Non-mob vanilla entities below
        // Item
        config.addDefault("creatures.Item.enable", false);
        config.addComment("creatures.Item", "", "# Non-mob vanilla entities", "# Enable on your own risk, some might work, some not!");
        config.addDefault("creatures.item.enable", false);
        // XPOrb
        config.addDefault("creatures.XPOrb.enable", false);
        config.addDefault("creatures.xp_orb.enable", false);
        // Painting
        config.addDefault("creatures.Painting.enable", false);
        config.addDefault("creatures.painting.enable", false);
        // Arrow
        config.addDefault("creatures.Arrow.enable", false);
        config.addDefault("creatures.arrow.enable", false);
        // Snowball
        config.addDefault("creatures.Snowball.enable", false);
        config.addDefault("creatures.snowball.enable", false);
        // Fireball
        config.addDefault("creatures.Fireball.enable", false);
        config.addDefault("creatures.fireball.enable", false);
        // SmallFireball
        config.addDefault("creatures.SmallFireball.enable", false);
        config.addDefault("creatures.small_fireball.enable", false);
        // ThrownEnderpearl
        config.addDefault("creatures.ThrownEnderpearl.enable", false);
        config.addDefault("creatures.enderpearl.enable", false);
        config.addDefault("creatures.ender_pearl.enable", false);
        // EyeOfEnderSignal
        config.addDefault("creatures.EyeOfEnderSignal.enable", false);
        config.addDefault("creatures.eye_of_ender_signal.enable", false);
        // ThrownPotion
        config.addDefault("creatures.ThrownPotion.enable", false);
        config.addDefault("creatures.potion.enable", false);
        // ThrownExpBottle
        config.addDefault("creatures.ThrownExpBottle.enable", false);
        config.addDefault("creatures.xp_bottle.enable", false);
        // PrimedTnt
        config.addDefault("creatures.PrimedTnt.enable", false);
        config.addDefault("creatures.tnt.enable", false);
        // FallingSand
        config.addDefault("creatures.FallingSand.enable", false);
        config.addDefault("creatures.falling_block.enable", false);
        // Boat
        config.addDefault("creatures.Boat.enable", false);
        config.addDefault("creatures.boat.enable", false);
        // Mob
        config.addDefault("creatures.Mob.enable", false);
        // Monster
        config.addDefault("creatures.Monster.enable", false);
        // EnderCrystal
        config.addDefault("creatures.EnderCrystal.enable", false);
        config.addDefault("creatures.ender_crystal.enable", false);
        // ItemFrame
        config.addDefault("creatures.ItemFrame.enable", false);
        config.addDefault("creatures.item_frame.enable", false);
        // WitherSkull
        config.addDefault("creatures.WitherSkull.enable", false);
        config.addDefault("creatures.wither_skull.enable", false);
        // FireworksRocketEntity
        config.addDefault("creatures.FireworksRocketEntity.enable", false);
        config.addDefault("creatures.fireworks_rocket.enable", false);
        // MinecartRideable
        config.addDefault("creatures.MinecartRideable.enable", false);
        config.addDefault("creatures.minecart.enable", false);
        // MinecartChest
        config.addDefault("creatures.MinecartChest.enable", false);
        config.addDefault("creatures.chest_minecart.enable", false);
        // MinecartFurnace
        config.addDefault("creatures.MinecartFurnace.enable", false);
        config.addDefault("creatures.furnace_minecart.enable", false);
        // MinecartTNT
        config.addDefault("creatures.MinecartTNT.enable", false);
        config.addDefault("creatures.tnt_minecart.enable", false);
        // MinecartHopper
        config.addDefault("creatures.MinecartHopper.enable", false);
        config.addDefault("creatures.hopper_minecart.enable", false);
        // MinecartSpawner
        config.addDefault("creatures.MinecartSpawner.enable", false);
        config.addDefault("creatures.spawner_minecart.enable", false);
        config.addDefault("creatures.MinecartMobSpawner.enable", false);
        // LeashKnot
        config.addDefault("creatures.LeashKnot.enable", false);
        config.addDefault("creatures.leash_knot.enable", false);
        // MinecartCommandBlock
        config.addDefault("creatures.MinecartCommandBlock.enable", false);
        config.addDefault("creatures.commandblock_minecart.enable", false);
        // ArmorStand
        config.addDefault("creatures.ArmorStand.enable", false);
        config.addDefault("creatures.armor_stand.enable", false);
        // ThrownEgg
        config.addDefault("creatures.ThrownEgg.enable", false);
        config.addDefault("creatures.egg.enable", false);
        // AreaEffectCloud
        config.addDefault("creatures.AreaEffectCloud.enable", false);
        config.addDefault("creatures.area_effect_cloud.enable", false);
        // TippedArrow
        config.addDefault("creatures.TippedArrow.enable", false);
        config.addDefault("creatures.tipped_arrow.enable", false);
        // SpectralArrow
        config.addDefault("creatures.SpectralArrow.enable", false);
        config.addDefault("creatures.spectral_arrow.enable", false);
        // ShulkerBullet
        config.addDefault("creatures.ShulkerBullet.enable", false);
        config.addDefault("creatures.shulker_bullet.enable", false);
        // DragonFireball
        config.addDefault("creatures.DragonFireball.enable", false);
        config.addDefault("creatures.dragon_fireball.enable", false);
        // llama_spit
        config.addDefault("creatures.llama_spit.enable", false);
        // evocation_fangs
        config.addDefault("creatures.evocation_fangs.enable", false);
        // end_crystal
        config.addDefault("creatures.end_crystal.enable", false);
        // evoker_fangs
        config.addDefault("creatures.evoker_fangs.enable", false);
        // experience_orb
        config.addDefault("creatures.experience_orb.enable", false);
        // eye_of_ender
        config.addDefault("creatures.eye_of_ender.enable", false);
        // firework_rocket
        config.addDefault("creatures.firework_rocket.enable", false);
        // command_block_minecart
        config.addDefault("creatures.command_block_minecart.enable", false);
        // experience_bottle
        config.addDefault("creatures.experience_bottle.enable", false);
        // lightning_bolt
        config.addDefault("creatures.lightning_bolt.enable", false);
        // player
        config.addDefault("creatures.player.enable", false);
        // fishing_bobber
        config.addDefault("creatures.fishing_bobber.enable", false);
        // trident
        config.addDefault("creatures.trident.enable", false);
        // glow_item_frame
        config.addDefault("creatures.glow_item_frame.enable", false);
        // chest_boat
        config.addDefault("creatures.chest_boat.enable", false);
        // interaction
        config.addDefault("creatures.interaction.enable", false);
        // marker
        config.addDefault("creatures.marker.enable", false);
        // block_display
        config.addDefault("creatures.block_display.enable", false);
        // item_display
        config.addDefault("creatures.item_display.enable", false);
        // text_display
        config.addDefault("creatures.text_display.enable", false);
        // camel
        config.addDefault("creatures.camel.enable", false);
        // sniffer
        config.addDefault("creatures.sniffer.enable", false);
    }
}
