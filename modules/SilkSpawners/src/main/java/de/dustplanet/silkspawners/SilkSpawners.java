package de.dustplanet.silkspawners;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.bstats.bukkit.Metrics;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import de.dustplanet.silkspawners.commands.SilkSpawnersTabCompleter;
import de.dustplanet.silkspawners.commands.SpawnerCommand;
import de.dustplanet.silkspawners.configs.Config;
import de.dustplanet.silkspawners.configs.Localization;
import de.dustplanet.silkspawners.configs.Mobs;
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
    private SilkUtil su;
    private Updater updater;
    private String nmsVersion;
    private static final int PLUGIN_ID = 35890;
    private static final String[] COMPATIBLE_MINECRAFT_VERSIONS = { "v1_7_R1", "v1_7_R2", "v1_7_R3", "v1_7_R4", "v1_8_R1", "v1_8_R2",
            "v1_8_R3", "v1_9_R1", "v1_9_R2", "v1_10_R1", "v1_11_R1", "v1_12_R1", "v1_13_R1" };
    public CommentedConfiguration config;
    public CommentedConfiguration localization;
    public CommentedConfiguration mobs;

    @Override
    public void onDisable() {
        if (su != null) {
            su.clearAll();
        }
    }

    @SuppressWarnings("unused")
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
                // Updater https://bukkit.org/threads96681/
                updater = new Updater(this, PLUGIN_ID, getFile(), Updater.UpdateType.DEFAULT, true);
                getLogger().info("AutoUpdater is enabled.");
                getLogger().info("Result from AutoUpdater is: " + updater.getResult().name());
            }
        } else {
            getLogger().info("AutoUpdater is disabled due to config setting.");
        }

        // Commands
        SpawnerCommand spawnerCommand = new SpawnerCommand(this, su);
        SilkSpawnersTabCompleter tabCompleter = new SilkSpawnersTabCompleter(su);
        getCommand("silkspawners").setExecutor(spawnerCommand);
        getCommand("silkspawners").setTabCompleter(tabCompleter);

        // Listeners
        SilkSpawnersBlockListener blockListener = new SilkSpawnersBlockListener(this, su);
        SilkSpawnersPlayerListener playerListener = new SilkSpawnersPlayerListener(this, su);
        SilkSpawnersInventoryListener inventoryListener = new SilkSpawnersInventoryListener(this, su);
        SilkSpawnersEntityListener entityListener = new SilkSpawnersEntityListener(this, su);
        getServer().getPluginManager().registerEvents(blockListener, this);
        getServer().getPluginManager().registerEvents(playerListener, this);
        getServer().getPluginManager().registerEvents(inventoryListener, this);
        getServer().getPluginManager().registerEvents(entityListener, this);

        new Metrics(this);

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

        if (config.getBoolean("factionsSupport", false)) {
            Plugin factionsPlugin = getServer().getPluginManager().getPlugin("Factions");
            if (factionsPlugin == null) {
                getLogger().warning("Factions support was enabled, but Factions was not found.");
                getLogger().warning("Disabling Factions support in config.yml again");
                config.set("factionsSupport", false);
                saveConfig();
            }
        }
        if (config.getBoolean("feudalSupport", false)) {
            Plugin factionsPlugin = getServer().getPluginManager().getPlugin("Feudal");
            if (factionsPlugin == null) {
                getLogger().warning("Feudal support was enabled, but Feudal was not found.");
                getLogger().warning("Disabling Feudal support in config.yml again");
                config.set("feudalSupport", false);
                saveConfig();
            }
        }
    }

    // If no config is found, copy the default one(s)!
    private void copy(String yml, File file) {
        try (OutputStream out = new FileOutputStream(file); InputStream in = getResource(yml)) {
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
        loadPermissions("destroydrop", "Allows you to destroy mob spawners to acquire mob spawn eggs / iron bars / XP (as configured)",
                PermissionDefault.FALSE);
        loadPermissions("changetype", "Allows you to change the spawner type using /spawner [creature]", PermissionDefault.FALSE);
        loadPermissions("changetypewithegg", "Allows you to change the spawner type by left-clicking with a spawn egg",
                PermissionDefault.FALSE);
        loadPermissions("freeitem", "Allows you to get spawner items in your hand for free using /spawner [creature]",
                PermissionDefault.FALSE);
        loadPermissions("freeitemegg", "Allows you to get spawn eggs in your hand for free using /spawner [creature]egg",
                PermissionDefault.FALSE);
    }

    private void loadPermissions(String permissionPart, String description, PermissionDefault permDefault) {
        HashMap<String, Boolean> childPermissions = new HashMap<>();
        for (String mobAlias : su.getDisplayNameToMobID().keySet()) {
            mobAlias = mobAlias.toLowerCase().replace(" ", "");
            childPermissions.put("silkspawners." + permissionPart + "." + mobAlias, true);
        }
        Permission perm = new Permission("silkspawners." + permissionPart + ".*", description, permDefault, childPermissions);
        try {
            getServer().getPluginManager().addPermission(perm);
        } catch (@SuppressWarnings("unused") IllegalArgumentException e) {
            getLogger().info("Permission " + perm.getName() + " is already registered. Skipping...");
        }
    }

    private void initializeConfigs() {
        // Config
        File configFile = new File(getDataFolder(), "config.yml");
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
        File localizationFile = new File(getDataFolder(), "localization.yml");
        if (!localizationFile.exists()) {
            copy("localization.yml", localizationFile);
        }

        // Mobs
        File mobsFile = new File(getDataFolder(), "mobs.yml");
        if (!mobsFile.exists()) {
            copy("mobs.yml", mobsFile);
        }

        // Load configs
        config = new CommentedConfiguration(configFile);
        new Config(config).loadConfig();

        localization = new CommentedConfiguration(localizationFile);
        new Localization(localization).loadConfig();

        mobs = new CommentedConfiguration(mobsFile);
        new Mobs(mobs).loadConfig();

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
    @SuppressWarnings("deprecation")
    private void loadRecipes() {
        boolean verbose = config.getBoolean("verboseConfig", false);

        if (verbose) {
            getLogger().info("Loading custom recipes");
        }

        // Add "base" recipe for eggs containing no durability (not from SilkSpawners)
        // 1.9 deprecated the durability and uses NBT tags
        String baseSpawnerEntityID = su.getDefaultEntityID();
        int baseSpawnerAmount = config.getInt("recipeAmount", 1);
        ItemStack baseSpawnerItem = su.newSpawnerItem(baseSpawnerEntityID, "&e&o??? &r&fSpawner", baseSpawnerAmount, false);
        ShapedRecipe baseSpawnerRecipe = null;
        try {
            baseSpawnerRecipe = new ShapedRecipe(new NamespacedKey(this, "baseSpawner"), baseSpawnerItem);
        } catch (Exception e) {
            // Legacy
            baseSpawnerRecipe = new ShapedRecipe(baseSpawnerItem);
        }

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
                    baseSpawnerRecipe.setIngredient('X', su.nmsProvider.getSpawnEggMaterial());
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
                        material = su.nmsProvider.getIronFenceMaterial();
                    }
                    baseSpawnerRecipe.setIngredient(character, material);
                }
            } catch (IllegalArgumentException e) {
                // If the custom recipe fails, we have a fallback
                getLogger().warning("Could not add the default recipe!");
                e.printStackTrace();
                baseSpawnerRecipe.shape(new String[] { "AAA", "ABA", "AAA" });
                baseSpawnerRecipe.setIngredient('A', su.nmsProvider.getIronFenceMaterial());
                // Use the right egg!
                baseSpawnerRecipe.setIngredient('B', su.nmsProvider.getSpawnEggMaterial());
            } finally {
                // Add it
                getServer().addRecipe(baseSpawnerRecipe);
            }
        }

        // For all our entities
        for (String entityID : su.getMobIDToDisplayName().keySet()) {

            // If the mob is disabled, skip it
            if (!mobs.getBoolean("creatures." + entityID + ".enableCraftingSpawner", true)) {
                if (verbose) {
                    getLogger().info("Skipping crafting recipe for " + entityID + " per config");
                }
                continue;
            }

            // Default amount
            int amount = 1;

            // Per mob amount
            if (mobs.contains("creatures." + entityID + ".recipe.amount")) {
                amount = mobs.getInt("creatures." + entityID + ".recipe.amount", 1);
            } else {
                amount = config.getInt("recipeAmount", 1);
            }

            // Debug output
            if (verbose) {
                getLogger().info("Amount of " + entityID + ": " + amount);
            }
            // Output is a spawner of this type with a custom amount
            ItemStack spawnerItem = su.newSpawnerItem(entityID, su.getCustomSpawnerName(entityID), amount, true);
            ShapedRecipe recipe = null;
            try {
                recipe = new ShapedRecipe(new NamespacedKey(this, entityID), spawnerItem);
            } catch (Exception e) {
                recipe = new ShapedRecipe(spawnerItem);
            }

            /*
             * Default is A A A A B A A A A where A is IRON_FENCE and B is MONSTER_EGG
             */

            // We try to use the custom recipe, but we don't know if the user
            // changed it right ;)
            try {
                // Per type recipe?
                String top;
                String middle;
                String bottom;

                // Top
                if (mobs.contains("creatures." + entityID + ".recipe.top")) {
                    top = mobs.getString("creatures." + entityID + ".recipe.top", "AAA");
                } else {
                    top = config.getString("recipeTop", "AAA");
                }

                // Middle
                if (mobs.contains("creatures." + entityID + ".recipe.middle")) {
                    middle = mobs.getString("creatures." + entityID + ".recipe.middle", "AXA");
                } else {
                    middle = config.getString("recipeMiddle", "AXA");
                }

                // Bottom
                if (mobs.contains("creatures." + entityID + ".recipe.bottom")) {
                    bottom = mobs.getString("creatures." + entityID + ".recipe.bottom", "AAA");
                } else {
                    bottom = config.getString("recipeBottom", "AAA");
                }

                // Debug output
                if (verbose) {
                    getLogger().info("Shape of " + entityID + ":");
                    getLogger().info(top);
                    getLogger().info(middle);
                    getLogger().info(bottom);
                }

                // Set the shape
                recipe.shape(top, middle, bottom);

                // Per type ingredients?
                List<String> ingredientsList;
                if (mobs.contains("creatures." + entityID + ".recipe.ingredients")) {
                    ingredientsList = mobs.getStringList("creatures." + entityID + ".recipe.ingredients");
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
                    getLogger().info("Ingredients of " + entityID + ":");
                    getLogger().info(ingredientsList.toString());
                }

                List<String> shape = Arrays.asList(recipe.getShape());
                // We have an ingredient that is not in our shape. Ignore it then
                if (shapeContainsIngredient(shape, 'X')) {
                    if (verbose) {
                        getLogger().info("shape of " + entityID + " contains X");
                    }
                    // Use the right egg!
                    // TODO
                    recipe.setIngredient('X', su.nmsProvider.getSpawnEggMaterial(), 0);
                }
                for (String ingredient : ingredientsList) {
                    // They are added like this A,DIRT
                    // Lets split the "," then
                    String[] ingredients = ingredient.split(",");
                    // if our array is not exactly of the size 2, something is wrong
                    if (ingredients.length != 2) {
                        getLogger().info("ingredient length of " + entityID + " invalid: " + ingredients.length);
                        continue;
                    }
                    // Maybe they put a string in here, so first position and uppercase
                    char character = ingredients[0].toUpperCase().charAt(0);
                    // We have an ingredient that is not in our shape. Ignore it then
                    if (!shapeContainsIngredient(shape, character)) {
                        getLogger().info("shape of " + entityID + " does not contain " + character);
                        continue;
                    }
                    // We try to get the material (ID or name)
                    Material material = Material.matchMaterial(ingredients[1]);
                    // Failed!
                    if (material == null) {
                        getLogger().info("shape material " + ingredients[1] + " of " + entityID + " matched null");
                        material = su.nmsProvider.getIronFenceMaterial();
                    }
                    recipe.setIngredient(character, material);
                }
            } catch (IllegalArgumentException e) {
                // If the custom recipe fails, we have a fallback
                getLogger().warning("Could not add the recipe of " + entityID + "!");
                e.printStackTrace();
                recipe.shape(new String[] { "AAA", "ABA", "AAA" });
                recipe.setIngredient('A', su.nmsProvider.getIronFenceMaterial());
                // Use the right egg!
                // TODO
                recipe.setIngredient('B', su.nmsProvider.getSpawnEggMaterial(), 0);
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
