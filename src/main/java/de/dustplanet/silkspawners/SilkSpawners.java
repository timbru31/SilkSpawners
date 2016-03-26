package de.dustplanet.silkspawners;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.mcstats.Metrics;

import de.dustplanet.silkspawners.commands.SilkSpawnersTabCompleter;
import de.dustplanet.silkspawners.commands.SpawnerCommand;
import de.dustplanet.silkspawners.configs.Configuration;
import de.dustplanet.silkspawners.listeners.SilkSpawnersBlockListener;
import de.dustplanet.silkspawners.listeners.SilkSpawnersEntityListener;
import de.dustplanet.silkspawners.listeners.SilkSpawnersInventoryListener;
import de.dustplanet.silkspawners.listeners.SilkSpawnersPlayerListener;
import de.dustplanet.util.CommentedConfiguration;
import de.dustplanet.util.SilkUtil;
import net.gravitydevelopment.updater.Updater;

/**
 * General stuff.
 *
 * @author (former) mushroomhostage
 * @author xGhOsTkiLLeRx
 */

public class SilkSpawners extends JavaPlugin {
    private SilkSpawnersBlockListener blockListener;
    private SilkSpawnersPlayerListener playerListener;
    private SilkSpawnersInventoryListener inventoryListener;
    private SilkSpawnersEntityListener entityListener;
    private SpawnerCommand spawnerCommand;
    private SilkSpawnersTabCompleter tabCompleter;
    private SilkUtil su;
    private File configFile, localizationFile, mobsFile;
    private Updater updater;
    private String nmsVersion;
    private static final int PLUGIN_ID = 35890;
    private static final String[] COMPATIBLE_MINECRAFT_VERSIONS = {"v1_5_R1", "v1_5_R2", "v1_5_R3", "v1_6_R1", "v1_6_R2", "v1_6_R3", "v1_7_R1", "v1_7_R2", "v1_7_R3", "v1_7_R4", "v1_8_R1", "v1_8_R2", "v1_8_R3", "v1_9_R1"};
    public CommentedConfiguration config, localization, mobs;

    @Override
    public void onDisable() {
        if (su != null) {
            su.clearAll();
        }
    }

    @Override
    public void onEnable() {
        // Make files and copy defaults
        initializeConfigs();

        // Get full package string of CraftServer
        String packageName = getServer().getClass().getPackage().getName();
        // org.bukkit.craftbukkit.version
        // Get the last element of the package
        setNMSVersion(packageName.substring(packageName.lastIndexOf('.') + 1));

        // Test for right Minecraft version
        if (config.getBoolean("testMCVersion", true)) {
            if (!Arrays.asList(COMPATIBLE_MINECRAFT_VERSIONS).contains(getNMSVersion())) {
                getLogger().info("This version of the plugin is NOT compatible with your Minecraft version!");
                getLogger().info("Please check your versions to make sure they match!");
                getLogger().info("Disabling now!");
                getLogger().info("Compatible versions are: " + Arrays.toString(COMPATIBLE_MINECRAFT_VERSIONS));
                getLogger().info("Your version is: " + getNMSVersion());
                getLogger().info("You can disable this check by setting testMCVersion to false in the config!");
                shutdown();
                return;
            }
        }

        // Heart of SilkSpawners is the SilkUtil class which holds all of our
        // important methods
        su = new SilkUtil(this);

        loadPermissions();

        loadConfig();

        // Check if we should enable the auto Updater & have no snapshot (dev build)
        if (config.getBoolean("autoUpdater", true)) {
            if (getDescription().getVersion().contains("SNAPSHOT")) {
                getLogger().info("AutoUpdater is disabled because you are running a dev build!");
            } else {
                // Updater http://forums.bukkit.org/threads/96681/
                updater = new Updater(this, PLUGIN_ID, getFile(), Updater.UpdateType.DEFAULT, true);
                getLogger().info("AutoUpdater is enabled.");
                getLogger().info("Result from AutoUpdater is: " + updater.getResult().name());
            }
        } else {
            getLogger().info("AutoUpdater is disabled due to config setting.");
        }

        // Commands
        spawnerCommand = new SpawnerCommand(this, su);
        tabCompleter = new SilkSpawnersTabCompleter(su);
        getCommand("silkspawners").setExecutor(spawnerCommand);
        getCommand("silkspawners").setTabCompleter(tabCompleter);


        // Listeners
        blockListener = new SilkSpawnersBlockListener(this, su);
        playerListener = new SilkSpawnersPlayerListener(this, su);
        inventoryListener = new SilkSpawnersInventoryListener(this, su);
        entityListener = new SilkSpawnersEntityListener(this, su);
        getServer().getPluginManager().registerEvents(blockListener, this);
        getServer().getPluginManager().registerEvents(playerListener, this);
        getServer().getPluginManager().registerEvents(inventoryListener, this);
        getServer().getPluginManager().registerEvents(entityListener, this);

        // Metrics
        try {
            Metrics metrics = new Metrics(this);
            metrics.start();
        } catch (IOException e) {
            getLogger().info("Couldn't start Metrics, please report this!");
            e.printStackTrace();
        }

        // BarAPI check
        if (config.getBoolean("barAPI.enable", false)) {
            Plugin barAPI = getServer().getPluginManager().getPlugin("BarAPI");
            if (barAPI != null) {
                // If BarAPI is enabled, load the economy
                getLogger().info("Loaded BarAPI successfully!");
                su.setBarAPI(true);
            } else {
                // Else tell the admin about the missing of BarAPI
                getLogger().info("BarAPI was not found and remains disabled!");
            }
        } else {
            getLogger().info("BarAPI is disabled due to config setting.");
        }

        // Check if Factions support is enabled
        if (config.getBoolean("factionsSupport", false)) {
            Plugin factionsPlugin = getServer().getPluginManager().getPlugin("Factions");
            if (factionsPlugin == null) {
                getLogger().warning("Factions support was enabled, but Factions was not found.");
                getLogger().warning("Disabling Factions support in config.yml again");
                config.set("factionsSupport", false);
                saveConfig();
            }
        }
    }

    // If no config is found, copy the default one(s)!
    private void copy(String yml, File file) {
        try (OutputStream out = new FileOutputStream(file);
                InputStream in = getResource(yml)) {
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
        } catch (IOException e) {
            getLogger().warning("Failed to copy the default config! (I/O)");
            e.printStackTrace();
        }
    }

    private void loadPermissions() {
        loadPermissions("craft", "Allows you to craft the specific spawner", PermissionDefault.FALSE);
        loadPermissions("place", "Allows you to place the specific spawner", PermissionDefault.FALSE);
        loadPermissions("silkdrop", "Allows you to use silk touch to acquire mob spawner items", PermissionDefault.FALSE);
        loadPermissions("destroydrop", "Allows you to destroy mob spawners to acquire mob spawn eggs / iron bars / XP (as configured)", PermissionDefault.FALSE);
        loadPermissions("changetype", "Allows you to change the spawner type using /spawner [creature]", PermissionDefault.FALSE);
        loadPermissions("changetypewithegg", "Allows you to change the spawner type by left-clicking with a spawn egg", PermissionDefault.FALSE);
        loadPermissions("freeitem", "Allows you to get spawner items in your hand for free using /spawner [creature]", PermissionDefault.FALSE);
        loadPermissions("freeitemegg", "Allows you to get spawn eggs in your hand for free using /spawner [creature]egg", PermissionDefault.FALSE);
    }

    private void loadPermissions(String permissionPart, String description, PermissionDefault permDefault) {
        HashMap<String, Boolean> childPermissions = new HashMap<>();
        for (String mobAlias : su.eid2DisplayName.values()) {
            mobAlias = mobAlias.toLowerCase().replace(" ", "");
            childPermissions.put("silkspawners." + permissionPart + "." + mobAlias, true);
        }
        Permission perm = new Permission("silkspawners." + permissionPart + ".*", description, permDefault, childPermissions);
        getServer().getPluginManager().addPermission(perm);
    }

    private void initializeConfigs() {
        // Config
        configFile = new File(getDataFolder(), "config.yml");
        // One file and the folder not existent
        if (!configFile.exists() && !getDataFolder().exists() && !getDataFolder().mkdirs()) {
            getLogger().severe("The config folder could NOT be created, make sure it's writable!");
            getLogger().severe("Disabling now!");
            shutdown();
            return;
        }
        // Copy default is necessary
        if (!configFile.exists()) {
            copy("config.yml", configFile);
        }

        // Localization
        localizationFile = new File(getDataFolder(), "localization.yml");
        if (!localizationFile.exists()) {
            copy("localization.yml", localizationFile);
        }

        // Mobs
        mobsFile = new File(getDataFolder(), "mobs.yml");
        if (!mobsFile.exists()) {
            copy("mobs.yml", mobsFile);
        }

        // Load configs
        config = new CommentedConfiguration(configFile);
        new Configuration(config).loadConfig("config");

        localization = new CommentedConfiguration(localizationFile);
        new Configuration(localization).loadConfig("localization");

        mobs = new CommentedConfiguration(mobsFile);
        new Configuration(mobs).loadConfig("mobs");

        // We need to migrate the old mobs from config.yml to mobs.yml
        if (config.contains("creatures")) {
            getLogger().info("Found entries of creatures in the config.yml, will migrate them into the mobs.yml!");
            ConfigurationSection creatures = config.getConfigurationSection("creatures");
            // Remove from config and save
            config.set("creatures", null);
            config.save();
            // Set updated list
            mobs.set("creatures", creatures);
            mobs.save();
            getLogger().info("Successfully migrated the creatures into the mobs.yml!");
        }
    }

    private void loadConfig() {
        // Enable craftable spawners?
        if (config.getBoolean("craftableSpawners", false)) {
            loadRecipes();
        }
    }

    // Add the recipes
    private void loadRecipes() {
        boolean verbose = config.getBoolean("verboseConfig", false);

        if (verbose) {
            getLogger().info("Loading custom recipes");
        }

        // Add "base" recipe for eggs containing no durability (not from SilkSpawners)
        // 1.9 deprecated the durability and uses NBT tags
        short baseSpawnerEntityID  = su.name2Eid.get(config.getString("defaultCreature", "90"));
        int baseSpawnerAmount = config.getInt("recipeAmount", 1);
        ItemStack baseSpawnerItem = su.newSpawnerItem(baseSpawnerEntityID, "&e&o??? &r&fSpawner", baseSpawnerAmount, false);
        ShapedRecipe baseSpawnerRecipe = new ShapedRecipe(baseSpawnerItem);


        String baseSpawnerTop = config.getString("recipeTop", "AAA");
        String baseSpawnerMiddle = config.getString("recipeMiddle", "AXA");
        String baseSpawnerBottom = config.getString("recipeBottom", "AAA");

        // Set the shape
        baseSpawnerRecipe.shape(baseSpawnerTop, baseSpawnerMiddle, baseSpawnerBottom);

        List<String> baseSpawnerIngredientsList = config.getStringList("ingredients");

        // Security first
        if (baseSpawnerIngredientsList != null && !baseSpawnerIngredientsList.isEmpty()) {
            try {
                List<String> baseSpawnerShape = Arrays.asList(baseSpawnerRecipe.getShape());
                // We have an ingredient that is not in our shape. Ignore it then
                if (shapeContainsIngredient(baseSpawnerShape, 'X')) {
                    // Use the right egg!
                    baseSpawnerRecipe.setIngredient('X', SilkUtil.SPAWN_EGG);
                }

                for (String ingredient : baseSpawnerIngredientsList) {
                    // They are added like this A,DIRT
                    // Lets split the "," then
                    String[] ingredients = ingredient.split(",");
                    // if our array is not exactly of the size 2, something is wrong
                    if (ingredients.length != 2) {
                        getLogger().info("ingredient length of default invalid: " + ingredients.length);
                        continue;
                    }
                    // Maybe they put a string in here, so first position and uppercase
                    char character = ingredients[0].toUpperCase().charAt(0);
                    // We have an ingredient that is not in our shape. Ignore it then
                    if (!shapeContainsIngredient(baseSpawnerShape, character)) {
                        getLogger().info("shape of default does not contain " + character);
                        continue;
                    }
                    // We try to get the material (ID or name)
                    Material material = Material.matchMaterial(ingredients[1]);
                    // Failed!
                    if (material == null) {
                        getLogger().info("shape material " + ingredients[1] + " of default spawner matched null");
                        material = Material.IRON_FENCE;
                    }
                    baseSpawnerRecipe.setIngredient(character, material);
                }
            } catch (IllegalArgumentException e) {
                // If the custom recipe fails, we have a fallback
                getLogger().warning("Could not add the default recipe!");
                e.printStackTrace();
                baseSpawnerRecipe.shape(new String[] {"AAA", "ABA", "AAA"});
                baseSpawnerRecipe.setIngredient('A', Material.IRON_FENCE);
                // Use the right egg!
                baseSpawnerRecipe.setIngredient('B', SilkUtil.SPAWN_EGG);
            } finally {
                // Add it
                getServer().addRecipe(baseSpawnerRecipe);
            }
        }

        // For all our entities
        for (short entityID : su.eid2MobID.keySet()) {

            // Name
            String mobID = su.eid2MobID.get(entityID);

            // If the mob is disabled, skip it
            if (!mobs.getBoolean("creatures." + mobID + ".enableCraftingSpawner", true)) {
                if (verbose) {
                    getLogger().info("Skipping crafting recipe for " + mobID + " per config");
                }
                continue;
            }

            // Default amount
            int amount = 1;

            // Per mob amount
            if (mobs.contains("creatures." + mobID + ".recipe.amount")) {
                amount = mobs.getInt("creatures." + mobID + ".recipe.amount", 1);
            } else {
                amount = config.getInt("recipeAmount", 1);
            }

            // Debug output
            if (verbose) {
                getLogger().info("Amount of " + mobID + ": " + amount);
            }
            // Output is a spawner of this type with a custom amount
            ItemStack spawnerItem = su.newSpawnerItem(entityID, su.getCustomSpawnerName(mobID), amount, true);
            ShapedRecipe recipe = new ShapedRecipe(spawnerItem);
            /*
             * Default is
             * A A A
             * A B A
             * A A A
             *
             * where A is IRON_FENCE
             * and B is MONSTER_EGG
             */

            // We try to use the custom recipe, but we don't know if the user
            // changed it right ;)
            try {
                // Per type recipe?
                String top, middle, bottom;

                // Top
                if (mobs.contains("creatures." + mobID + ".recipe.top")) {
                    top = mobs.getString("creatures." + mobID + ".recipe.top", "AAA");
                } else {
                    top = config.getString("recipeTop", "AAA");
                }

                // Middle
                if (mobs.contains("creatures." + mobID + ".recipe.middle")) {
                    middle = mobs.getString("creatures." + mobID + ".recipe.middle", "AXA");
                } else {
                    middle = config.getString("recipeMiddle", "AXA");
                }

                // Bottom
                if (mobs.contains("creatures." + mobID + ".recipe.bottom")) {
                    bottom = mobs.getString("creatures." + mobID + ".recipe.bottom", "AAA");
                } else {
                    bottom = config.getString("recipeBottom", "AAA");
                }

                // Debug output
                if (verbose) {
                    getLogger().info("Shape of " + mobID + ":");
                    getLogger().info(top);
                    getLogger().info(middle);
                    getLogger().info(bottom);
                }

                // Set the shape
                recipe.shape(top, middle, bottom);

                // Per type ingredients?
                List<String> ingredientsList;
                if (mobs.contains("creatures." + mobID + ".recipe.ingredients")) {
                    ingredientsList = mobs.getStringList("creatures." + mobID + ".recipe.ingredients");
                } else {
                    // No list what we should use -> not adding
                    if (!config.contains("ingredients")) {
                        continue;
                    }
                    ingredientsList = config.getStringList("ingredients");
                }

                // Security first
                if (ingredientsList == null || ingredientsList.isEmpty()) {
                    continue;
                }

                // Debug output
                if (verbose) {
                    getLogger().info("Ingredients of " + mobID + ":");
                    getLogger().info(ingredientsList.toString());
                }

                List<String> shape = Arrays.asList(recipe.getShape());
                // We have an ingredient that is not in our shape. Ignore it then
                if (shapeContainsIngredient(shape, 'X')) {
                    if (verbose) {
                        getLogger().info("shape of " +  mobID + " contains X");
                    }
                    // Use the right egg!
                    recipe.setIngredient('X', SilkUtil.SPAWN_EGG, entityID);
                }
                for (String ingredient : ingredientsList) {
                    // They are added like this A,DIRT
                    // Lets split the "," then
                    String[] ingredients = ingredient.split(",");
                    // if our array is not exactly of the size 2, something is wrong
                    if (ingredients.length != 2) {
                        getLogger().info("ingredient length of " + mobID + " invalid: " + ingredients.length);
                        continue;
                    }
                    // Maybe they put a string in here, so first position and uppercase
                    char character = ingredients[0].toUpperCase().charAt(0);
                    // We have an ingredient that is not in our shape. Ignore it then
                    if (!shapeContainsIngredient(shape, character)) {
                        getLogger().info("shape of " +  mobID + " does not contain " + character);
                        continue;
                    }
                    // We try to get the material (ID or name)
                    Material material = Material.matchMaterial(ingredients[1]);
                    // Failed!
                    if (material == null) {
                        getLogger().info("shape material " + ingredients[1] + " of " + mobID + " matched null");
                        material = Material.IRON_FENCE;
                    }
                    recipe.setIngredient(character, material);
                }
            } catch (IllegalArgumentException e) {
                // If the custom recipe fails, we have a fallback
                getLogger().warning("Could not add the recipe of " + mobID + "!");
                e.printStackTrace();
                recipe.shape(new String[] {"AAA", "ABA", "AAA"});
                recipe.setIngredient('A', Material.IRON_FENCE);
                // Use the right egg!
                recipe.setIngredient('B', SilkUtil.SPAWN_EGG, entityID);
            } finally {
                // Add it
                getServer().addRecipe(recipe);
            }
        }
    }

    private boolean shapeContainsIngredient(List<String> shape, char c) {
        boolean match = false;
        for (String recipePart : shape) {
            for (char recipeIngredient : recipePart.toCharArray()) {
                if (recipeIngredient == c) {
                    match = true;
                    break;
                }
            }
            if (match) {
                break;
            }
        }
        return match;
    }

    // If the user has the permission, message
    public void informPlayer(Player player, String message) {
        // Ignore empty messages
        if (message == null || message.isEmpty()) {
            return;
        }
        if (player.hasPermission("silkspawners.info")) {
            su.sendMessage(player, message);
        }
    }

    public void reloadConfigs() {
        config.load();
        config.save();
        loadConfig();
        su.load();
        mobs.load();
        mobs.save();
        localization.load();
        localization.save();
    }

    public void shutdown() {
        setEnabled(false);
    }

    public CommentedConfiguration getMobs() {
        return mobs;
    }

    public String getNMSVersion() {
        return nmsVersion;
    }

    public void setNMSVersion(String nmsVersion) {
        this.nmsVersion = nmsVersion;
    }
}
