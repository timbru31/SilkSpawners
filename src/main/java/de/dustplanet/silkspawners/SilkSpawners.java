package de.dustplanet.silkspawners;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Updater
import net.gravitydevelopment.updater.Updater;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
// Metrics
import org.mcstats.Metrics;

import de.dustplanet.silkspawners.commands.EggCommand;
import de.dustplanet.silkspawners.commands.SilkSpawnersTabCompleter;
import de.dustplanet.silkspawners.commands.SpawnerCommand;
import de.dustplanet.silkspawners.configs.Configuration;
import de.dustplanet.silkspawners.listeners.SilkSpawnersBlockListener;
import de.dustplanet.silkspawners.listeners.SilkSpawnersEntityListener;
import de.dustplanet.silkspawners.listeners.SilkSpawnersInventoryListener;
import de.dustplanet.silkspawners.listeners.SilkSpawnersPlayerListener;
import de.dustplanet.util.CommentedConfiguration;
import de.dustplanet.util.SilkUtil;

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
    private EggCommand eggCommand;
    private SilkSpawnersTabCompleter tabCompleter;
    private SilkUtil su;
    private boolean usePermissions = true;
    public CommentedConfiguration config, localization, mobs;
    private File configFile, localizationFile, mobsFile;
    private static final String[] COMPATIBLE_MINECRAFT_VERSIONS = {"1.5", "1.5.1", "1.5.2", "1.6.1", "1.6.2", "1.6.4", "1.7.2", "1.7.5", "1.7.8", "1.7.9", "1.7.10", "1.8", "1.8.3"};
    private Updater updater;

    @Override
    public void onDisable() {
        su.clearAll();
    }

    @Override
    public void onEnable() {
        // Make files and copy defaults
        initializeConfigs();

        // Test for right Minecraft version
        if (config.getBoolean("testMCVersion", true)) {
            // We can't use the MinercraftServer import because it might be broken.
            // Regex is our friend and helper
            // Find MC: 0.0.0, last occurrence is optional
            Pattern pat = Pattern.compile("MC: \\d+.\\d+(.\\d+)?");
            Matcher matcher = pat.matcher(getServer().getVersion());
            String mcVersion = "";
            if (matcher.find()) {
                mcVersion = matcher.group(0);
            }
            // Strip MC:
            String serverVersion = mcVersion.substring(mcVersion.indexOf(' ') + 1);
            if (!Arrays.asList(COMPATIBLE_MINECRAFT_VERSIONS).contains(serverVersion)) {
                getLogger().info("This version of the plugin is NOT compatible with your Minecraft version!");
                getLogger().info("Please check your versions to make sure they match!");
                getLogger().info("Disabling now!");
                getLogger().info("Compatible versions are: " + Arrays.toString(COMPATIBLE_MINECRAFT_VERSIONS));
                getLogger().info("Your version is: " + serverVersion);
                getLogger().info("You can disable this check by setting testMCVersion to false in the config!");
                shutdown();
                return;
            }
        }

        // Heart of SilkSpawners is the SilkUtil class which holds all of our
        // important methods
        su = new SilkUtil(this);

        loadConfig();

        // Check if we should enable the auto Updater & have no snapshot (dev build)
        if (config.getBoolean("autoUpdater", true)) {
            if (getDescription().getVersion().contains("SNAPSHOT")) {
                getLogger().info("AutoUpdater is disabled because you are running a dev build!");
            } else {
                // Updater http://forums.bukkit.org/threads/96681/
                updater = new Updater(this, 35890, getFile(), Updater.UpdateType.DEFAULT, true);
                getLogger().info("AutoUpdater is enabled.");
                getLogger().info("Result from AutoUpdater is: " + updater.getResult().name());
            }
        } else {
            getLogger().info("AutoUpdater is disabled due to config setting.");
        }

        // Commands
        spawnerCommand = new SpawnerCommand(this, su);
        eggCommand = new EggCommand(this, su);
        tabCompleter = new SilkSpawnersTabCompleter(su);
        getCommand("silkspawners").setExecutor(spawnerCommand);
        getCommand("egg").setExecutor(eggCommand);
        getCommand("silkspawners").setTabCompleter(tabCompleter);
        getCommand("egg").setTabCompleter(tabCompleter);

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
        Plugin barAPI = getServer().getPluginManager().getPlugin("BarAPI");
        if (config.getBoolean("barAPI.enable", true)) {
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

        // See if we should use permissions
        usePermissions = config.getBoolean("usePermissions", true);
        if (config.getBoolean("verboseConfig", false)) {
            getLogger().info("Permissions are " +  usePermissions);
        }
    }

    // Add the recipes
    private void loadRecipes() {
        boolean verbose = config.getBoolean("verboseConfig", false);

        if (verbose) {
            getLogger().info("Loading custom recipes");
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
                    recipe.setIngredient('X', Material.MONSTER_EGG, entityID);
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
                recipe.setIngredient('B', Material.MONSTER_EGG, entityID);
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
        if (hasPermission(player, "silkspawners.info")) {
            player.sendMessage(message);
        }
    }

    // Check for permissions
    public boolean hasPermission(Player player, String node) {
        // Normal check if we use permissions
        if (usePermissions) {
            return player.hasPermission(node);
        }
        // Else check more detailed
        // Any of the nodes, -> yes
        if (node.equals("silkspawners.info")
                || node.startsWith("silkspawners.silkdrop")
                || node.startsWith("silkspawners.destroydrop")
                || node.equals("silkspawners.viewtype")
                || node.equals("silkspawners.explodedrop")
                || node.startsWith("silkspawners.place")
                || node.startsWith("silkspawners.craft")) {
            return true;
        }
        // Else ask for Op status
        return player.isOp();
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
}
