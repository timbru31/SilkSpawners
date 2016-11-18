package de.dustplanet.silkspawners.configs;

import java.util.ArrayList;

import org.bukkit.Material;

import de.dustplanet.util.CommentedConfiguration;

/**
 * Default configs.
 *
 * @author (former) mushroomhostage
 * @author xGhOsTkiLLeRx
 */
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
        config.options().header("creatures: key is official creature type name (mobID), case-sensitive, from https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/entity/EntityType.html");
        ArrayList<String> tempList = new ArrayList<>();
        // Creeper
        tempList.add("c");
        tempList.add("creep");
        tempList.add("cataclysm");
        config.addDefault("creatures.Creeper.aliases", tempList);
        config.addComment("creatures.Creeper", "", " # Vanilla mobs, from http://minecraft.gamepedia.com/Data_values/Entity_IDs");
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
        tempList = new ArrayList<>();
        // Ozelot (Ocelot)
        tempList.add("ocelot");
        tempList.add("oce");
        tempList.add("o");
        tempList.add("cat");
        tempList.add("kitty");
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
        // LeashKnot
        config.addDefault("creatures.LeashKnot.enable", false);
        config.addDefault("creatures.leash_know.enable", false);
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
        
    }

    private void loadDefaultLocalization() {
        config.options().header("This line affects the new naming of spawners, to DISABLE this, change the message back to Monster Spawner");
        config.addDefault("spawnerName", "&e%creature% &fSpawner");
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
        config.addDefault("changingDeniedFactions", "&4You are not allowed to change spawners in other peoples territory!");
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
        config.addDefault("informationOfSpawner3", "-- EntityID: %ID% --");
        config.addDefault("informationOfSpawnerBar", "Monster Spawner, Type: &e%creature%&f, %ID%");
        config.addDefault("spawningDenied", "&4Spawning of &e%creature% (ID %ID%) &4denied!");
        config.addComment("spawningDenied", "");
        config.addDefault("spawning", "&2Spawning &e%creature% (ID %ID%)");
        config.addDefault("noSpawnerHere", "&4A spawner can not be placed here, because the block above is blocked!");
        config.addDefault("lookAtSpawner", "&4You must be looking directly at a spawner.");
        config.addComment("lookAtSpawner", "");
        config.addDefault("spawnerNotDeterminable", "&4You either have no spawner or egg in your hand or a spawner or egg in both hands. Please make one empty!");
        config.addDefault("help1", "&7------&6SilkSpawners Help Menu&7------");
        config.addComment("help1", "");
        config.addDefault("help2", "&e/ss help &7- &2Displays the help menu.");
        config.addDefault("help3", "&e/ss list|all &7- &2Displays all available creatures.");
        config.addDefault("help4", "&e/ss view &7- &2Displays information about the viewed spawner.");
        config.addDefault("help5", "&e/ss reload|rl &7- &2Reloads the configuration files");
        config.addDefault("help6", "&e/ss change <newMob> &7- &2Changes the spawner you are currently holding or viewing at.");
        config.addDefault("help7", "&e/ss give|add <player> <mob> [amount] &7- &2Gives a spawner or egg to the player. Amount is optional");
    }

    private void loadDefaultConfig() {
        config.options().header("See documentation at http://dev.bukkit.org/bukkit-plugins/silkspawners/pages/configuration");
        config.addDefault("autoUpdater", true);
        config.addComment("autoUpdater", "", "# Should the plugin automatically update if an update is available?");
        config.addDefault("permissionExplode", false);
        config.addComment("permissionExplode", "", "# Should a permission be required when a spawner explodes by TNT to achieve a drop");
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
        config.addComment("spawnerCommandReachDistance", "", "# How far is the spawner reachable with your crosshair (disable with -1)");
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
        config.addDefault("dropXPOnlyOnDestroy", false);
        config.addComment("dropXPOnlyOnDestroy", "", "# Drops XP only when a spawner is destroyed and not mined via SilkTouch");
        config.addDefault("destroyDropBars", 0);
        config.addComment("destroyDropBars", "", "# If a spawner is destroyed, should iron bars be dropped");
        config.addDefault("craftableSpawners", false);
        config.addComment("craftableSpawners", "", "# Should the player be able to craft spawners");
        config.addDefault("recipeTop", "AAA");
        config.addComment("recipeTop", "", "# Leave a slot empty (null/air)? Just make a space then, example 'A A' -> middle is free", "# X is always the egg");
        config.addDefault("recipeMiddle", "AXA");
        config.addDefault("recipeBottom", "AAA");
        config.addDefault("recipeAmount", 1);
        ArrayList<String> temp = new ArrayList<>();
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
        config.addDefault("disableChangeTypeWithEgg", false);
        config.addComment("disableChangeTypeWithEgg", "", "# Disable left click to change spawners, spawns a mob instead. Still blocks Vanilla right click behavior.");
        config.addDefault("spawnEggToSpawner", false);
        config.addComment("spawnEggToSpawner", "", "# Should instead of spawning a mob a MonsterSpawner be placed? (Uses consumeEgg value, too)");
        config.addDefault("spawnEggOverride", false);
        config.addComment("spawnEggOverride", "", "# Should the spawn algorithm be overridden? Allows spawning of non-standard entities");
        config.addDefault("spawnEggOverrideSpawnDefault", true);
        config.addComment("spawnEggOverrideSpawnDefault", "", "# Fallback if the creature should be spawned, if not specified for the entity");
        temp = new ArrayList<>();
        temp.add(Material.WOOD_PICKAXE.toString());
        temp.add(Material.STONE_PICKAXE.toString());
        temp.add(Material.IRON_PICKAXE.toString());
        temp.add(Material.GOLD_PICKAXE.toString());
        temp.add(Material.DIAMOND_PICKAXE.toString());
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
        config.addComment("factionsSupport", "", "# Prevent that a spawner is changed via eggs in other territories");
        config.addDefault("verboseConfig", false);
        config.addComment("verboseConfig", "", "# Puts more information out on startup");
        config.addDefault("useReflection", true);
        config.addComment("useReflection", "", "# Internal stuff, do NOT change unless advised - the plugin might break otherwise");
        config.addDefault("testMCVersion", true);
        config.addDefault("useMetadata", true);
        config.addDefault("useLegacyName", false);
    }
}
