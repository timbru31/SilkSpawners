package de.dustplanet.silkspawners;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;

import org.apache.commons.lang.StringUtils;
import org.bstats.bukkit.Metrics;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
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
import lombok.Getter;
import lombok.Setter;
import net.gravitydevelopment.updater.Updater;

/**
 * General stuff.
 *
 * @author (former) mushroomhostage
 * @author timbru31
 */

public class SilkSpawners extends JavaPlugin {
    private SilkUtil su;
    @Getter
    @Setter
    private String nmsVersion;
    private static final int PLUGIN_ID = 35_890;
    private static final int BSTATS_PLUGIN_ID = 273;
    private static final String[] COMPATIBLE_MINECRAFT_VERSIONS = { "v1_8_R1", "v1_8_R3", "v1_9_R2", "v1_10_R1", "v1_11_R1", "v1_12_R1",
            "v1_13_R2", "v1_14_R1", "v1_15_R1", "v1_16_R1", "v1_16_R2", "v1_16_R3", "v1_17_R1", "v1_18_R1", "v1_18_R2" };
    public CommentedConfiguration config;
    public CommentedConfiguration localization;
    @Getter
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
        final String packageName = getServer().getClass().getPackage().getName();
        // org.bukkit.craftbukkit.version
        // Get the last element of the package
        setNmsVersion(packageName.substring(packageName.lastIndexOf('.') + 1));

        // Test for right Minecraft version
        if (config.getBoolean("testMCVersion", true)) {
            if (!Arrays.asList(COMPATIBLE_MINECRAFT_VERSIONS).contains(getNmsVersion())) {
                getLogger().info("This version of the plugin is NOT compatible with your Minecraft version!");
                getLogger().info("Please check your versions to make sure they match!");
                getLogger().info("Disabling now!");
                getLogger().log(Level.INFO, "Compatible versions are: {0}", Arrays.toString(COMPATIBLE_MINECRAFT_VERSIONS));
                getLogger().log(Level.INFO, "Your version is: {0}", getNmsVersion());
                getLogger().info("You can disable this check by setting testMCVersion to false in the config!");
                shutdown();
                return;
            }
        }

        // Heart of SilkSpawners is the SilkUtil class which holds all of our
        // important methods
        su = new SilkUtil(this);

        loadPermissions();

        loadRecipes();

        // Check if we should enable the auto Updater & have no snapshot (dev build)
        if (config.getBoolean("autoUpdater", true)) {
            if (getDescription().getVersion().contains("SNAPSHOT")) {
                getLogger().info("AutoUpdater is disabled because you are running a dev build!");
            } else {
                try {
                    new Updater(this, PLUGIN_ID, getFile(), Updater.UpdateType.DEFAULT, updaterResult -> {
                        getLogger().log(Level.INFO, "Result from AutoUpdater is: {0}", updaterResult.getResult());
                    }, true);
                    getLogger().info("AutoUpdater is enabled and now running.");

                } catch (final Exception e) {
                    getLogger().log(Level.INFO, "Error while auto updating:", e);
                }
            }
        } else {
            getLogger().info("AutoUpdater is disabled due to config setting.");
        }

        // Commands
        final SpawnerCommand spawnerCommand = new SpawnerCommand(this, su);
        final SilkSpawnersTabCompleter tabCompleter = new SilkSpawnersTabCompleter(su);
        getCommand("silkspawners").setExecutor(spawnerCommand);
        getCommand("silkspawners").setTabCompleter(tabCompleter);

        // Listeners
        final SilkSpawnersBlockListener blockListener = new SilkSpawnersBlockListener(this, su);
        final SilkSpawnersPlayerListener playerListener = new SilkSpawnersPlayerListener(this, su);
        final SilkSpawnersInventoryListener inventoryListener = new SilkSpawnersInventoryListener(this, su);
        final SilkSpawnersEntityListener entityListener = new SilkSpawnersEntityListener(this, su);
        getServer().getPluginManager().registerEvents(blockListener, this);
        getServer().getPluginManager().registerEvents(playerListener, this);
        getServer().getPluginManager().registerEvents(inventoryListener, this);
        getServer().getPluginManager().registerEvents(entityListener, this);

        new Metrics(this, BSTATS_PLUGIN_ID);

        // BarAPI check
        if (config.getBoolean("barAPI.enable", false)) {
            final Plugin barAPI = getServer().getPluginManager().getPlugin("BarAPI");
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
            final Plugin factionsPlugin = getServer().getPluginManager().getPlugin("Factions");
            if (factionsPlugin == null) {
                getLogger().warning("Factions support was enabled, but Factions was not found.");
                getLogger().warning("Disabling Factions support in config.yml again");
                config.set("factionsSupport", false);
                saveConfig();
            }
        }
    }

    // If no config is found, copy the default one(s)!
    private void copy(final String yml, final File file) {
        try (OutputStream out = new FileOutputStream(file); InputStream in = getResource(yml)) {
            final byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
        } catch (final IOException e) {
            getLogger().log(Level.WARNING, "Failed to copy the default config! (I/O)", e);
        }
    }

    private void loadPermissions() {
        loadPermissions("craft", "Allows you to craft the specific spawner", PermissionDefault.FALSE);
        loadPermissions("place", "Allows you to place the specific spawner", PermissionDefault.FALSE);
        loadPermissions("silkdrop", "Allows you to use silk touch to acquire mob spawner items", PermissionDefault.FALSE);
        loadPermissions("nosilk", "Allows you to use any tool to acquire mob spawner items", PermissionDefault.FALSE);
        loadPermissions("destroydrop", "Allows you to destroy mob spawners to acquire mob spawn eggs / iron bars / XP (as configured)",
                PermissionDefault.FALSE);
        loadPermissions("changetype", "Allows you to change the spawner type using /spawner [creature]", PermissionDefault.FALSE);
        loadPermissions("changetypewithegg", "Allows you to change the spawner type by left-clicking with a spawn egg",
                PermissionDefault.FALSE);
        loadPermissions("freeitem", "Allows you to get spawner items in your hand for free using /spawner [creature]",
                PermissionDefault.FALSE);
        loadPermissions("freeitemegg", "Allows you to get spawn eggs in your hand for free using /spawner [creature]egg",
                PermissionDefault.FALSE);
        loadPermissions("list", "Allows you to list the available mobs", PermissionDefault.TRUE);
    }

    private void loadPermissions(final String permissionPart, final String description, final PermissionDefault permDefault) {
        final HashMap<String, Boolean> childPermissions = new HashMap<>();
        for (String mobAlias : su.getDisplayNameToMobID().keySet()) {
            mobAlias = mobAlias.toLowerCase(Locale.ENGLISH).replace(" ", "");
            childPermissions.put("silkspawners." + permissionPart + "." + mobAlias, true);
        }
        final Permission perm = new Permission("silkspawners." + permissionPart + ".*", description, permDefault, childPermissions);
        try {
            getServer().getPluginManager().addPermission(perm);
        } catch (@SuppressWarnings("unused") final IllegalArgumentException e) {
            getLogger().log(Level.INFO, "Permission {0} is already registered. Skipping...", perm.getName());
        }
    }

    private void initializeConfigs() {
        // Config
        final File configFile = new File(getDataFolder(), "config.yml");
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
        final File localizationFile = new File(getDataFolder(), "localization.yml");
        if (!localizationFile.exists()) {
            copy("localization.yml", localizationFile);
        }

        // Mobs
        final File mobsFile = new File(getDataFolder(), "mobs.yml");
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
    }

    // Add the recipes
    @SuppressWarnings("deprecation")
    private void loadRecipes() {
        if (!config.getBoolean("craftableSpawners", false)) {
            return;
        }

        getLogger().fine("Loading custom recipes");

        final boolean legacySpawnEggs = su.isLegacySpawnEggs();
        if (legacySpawnEggs) {
            loadBaseEggRecipe();
        }

        // For all our entities
        for (final String entityID : su.getKnownEntities()) {
            boolean skip = false;
            // If the mob is disabled, skip it
            if (!mobs.getBoolean("creatures." + entityID + ".enableCraftingSpawner", true)) {
                getLogger().log(Level.FINE, "Skipping crafting recipe for {0} per config", entityID);
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

            getLogger().log(Level.FINE, "Amount of {0}: {1}", new Object[] { entityID, amount });

            // Output is a spawner of this type with a custom amount
            final ItemStack spawnerItem = su.newSpawnerItem(entityID, su.getCustomSpawnerName(entityID), amount, true);
            ShapedRecipe recipe = null;
            try {
                recipe = new ShapedRecipe(new NamespacedKey(this, entityID), spawnerItem);
            } catch (@SuppressWarnings("unused") Exception | Error e) {
                recipe = new ShapedRecipe(spawnerItem);
            }

            /*
             * Default is A A A A B A A A A. where A is IRON_FENCE and B is MONSTER_EGG
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

                getLogger().log(Level.FINE, "Shape of {0}:", entityID);
                getLogger().fine(top);
                getLogger().fine(middle);
                getLogger().fine(bottom);

                // Set the shape
                recipe.shape(top, middle, bottom);

                // Per type ingredients?
                List<String> ingredientsList;
                if (mobs.contains("creatures." + entityID + ".recipe.ingredients")) {
                    ingredientsList = mobs.getStringList("creatures." + entityID + ".recipe.ingredients");
                } else {
                    // No list what we should use -> not adding
                    if (!config.contains("ingredients")) {
                        skip = true;
                        continue;
                    }
                    ingredientsList = config.getStringList("ingredients");
                }

                // Security first
                if (ingredientsList == null || ingredientsList.isEmpty()) {
                    skip = true;
                    continue;
                }

                getLogger().log(Level.FINE, "Ingredients of {0}:", entityID);
                getLogger().fine(ingredientsList.toString());

                final List<String> shape = Arrays.asList(recipe.getShape());
                // We have an ingredient that is not in our shape. Ignore it then
                if (shapeContainsIngredient(shape, 'X')) {
                    getLogger().log(Level.FINE, "shape of {0} contains X", entityID);

                    if (legacySpawnEggs) {
                        recipe.setIngredient('X', su.nmsProvider.getSpawnEggMaterial(), su.nmsProvider.getIDForEntity(entityID));
                    } else {
                        final Material material = Material.getMaterial(entityID.toUpperCase() + "_SPAWN_EGG");
                        if (material == null) {
                            getLogger().log(Level.FINE, "could not find egg material for {0}", entityID);
                            skip = true;
                            continue;
                        }
                        recipe.setIngredient('X', material);
                    }
                }
                for (final String ingredient : ingredientsList) {
                    // They are added like this A,DIRT
                    // Lets split the "," then
                    final String[] ingredients = ingredient.split(",");
                    // if our array is not exactly of the size 2, something is wrong
                    if (ingredients.length != 2) {
                        getLogger().log(Level.INFO, "ingredient length of {0} invalid: {1}", new Object[] { entityID, ingredients.length });
                        skip = true;
                        continue;
                    }
                    // Maybe they put a string in here, so first position and uppercase
                    final char character = ingredients[0].toUpperCase().charAt(0);
                    // We have an ingredient that is not in our shape. Ignore it then
                    if (!shapeContainsIngredient(shape, character)) {
                        getLogger().log(Level.INFO, "shape of {0} does not contain {1}", new Object[] { entityID, character });
                        skip = true;
                        continue;
                    }
                    // We try to get the material (ID or name)
                    Material material = Material.matchMaterial(ingredients[1]);
                    // Failed!
                    if (material == null) {
                        getLogger().log(Level.INFO, "shape material {0} of {1} matched null, falling back to IRON_BARS",
                                new Object[] { ingredients[1], entityID });
                        material = su.nmsProvider.getIronFenceMaterial();
                    }
                    recipe.setIngredient(character, material);
                }
            } catch (final IllegalArgumentException e) {
                // If the custom recipe fails, we have a fallback
                getLogger().log(Level.WARNING, "Could not add the recipe of {0}!", entityID);
                getLogger().log(Level.WARNING, "Error:", e);
                recipe.shape(new String[] { "AAA", "ABA", "AAA" });
                recipe.setIngredient('A', su.nmsProvider.getIronFenceMaterial());
                if (legacySpawnEggs) {
                    recipe.setIngredient('X', su.nmsProvider.getSpawnEggMaterial(), 0);
                } else {
                    final Material material = Material.getMaterial(entityID.toUpperCase() + "_SPAWN_EGG");
                    if (material == null) {
                        getLogger().log(Level.INFO, "Could not find egg material for {0}", entityID);
                        skip = true;
                        continue;
                    }
                    recipe.setIngredient('X', material);
                }
            } finally {
                // Add it
                try {
                    if (!skip) {
                        final boolean recipeAdded = getServer().addRecipe(recipe);
                        getLogger().log(Level.FINE, "Recipe of {0} added: {1}", new Object[] { entityID, recipeAdded });
                    }
                } catch (final IllegalStateException | NullPointerException e) {
                    getLogger().log(Level.INFO, "Unable to add recipe of {0}", entityID);
                    getLogger().log(Level.INFO, "Error:", e);
                }
            }
        }
    }

    @SuppressWarnings("deprecation")
    private void loadBaseEggRecipe() {
        // Add "base" recipe for eggs containing no durability (not from SilkSpawners)
        // 1.9 deprecated the durability and uses NBT tags
        final String baseSpawnerEntityID = su.getDefaultEntityID();
        final int baseSpawnerAmount = config.getInt("recipeAmount", 1);
        final ItemStack baseSpawnerItem = su.newSpawnerItem(baseSpawnerEntityID, "&e&o??? &r&fSpawner", baseSpawnerAmount, false);
        ShapedRecipe baseSpawnerRecipe = null;
        try {
            baseSpawnerRecipe = new ShapedRecipe(new NamespacedKey(this, "baseSpawner"), baseSpawnerItem);
        } catch (@SuppressWarnings("unused") Exception | Error e) {
            // Legacy
            baseSpawnerRecipe = new ShapedRecipe(baseSpawnerItem);
        }

        final String baseSpawnerTop = config.getString("recipeTop", "AAA");
        final String baseSpawnerMiddle = config.getString("recipeMiddle", "AXA");
        final String baseSpawnerBottom = config.getString("recipeBottom", "AAA");

        // Set the shape
        baseSpawnerRecipe.shape(baseSpawnerTop, baseSpawnerMiddle, baseSpawnerBottom);

        final List<String> baseSpawnerIngredientsList = config.getStringList("ingredients");

        // Security first
        if (baseSpawnerIngredientsList != null && !baseSpawnerIngredientsList.isEmpty()) {
            boolean skip = false;
            try {
                final List<String> baseSpawnerShape = Arrays.asList(baseSpawnerRecipe.getShape());
                // We have an ingredient that is not in our shape. Ignore it then
                if (shapeContainsIngredient(baseSpawnerShape, 'X')) {
                    // Use the right egg!
                    baseSpawnerRecipe.setIngredient('X', su.nmsProvider.getSpawnEggMaterial());
                }

                for (final String ingredient : baseSpawnerIngredientsList) {
                    // They are added like this A,DIRT
                    // Lets split the "," then
                    final String[] ingredients = ingredient.split(",");
                    // if our array is not exactly of the size 2, something is wrong
                    if (ingredients.length != 2) {
                        getLogger().log(Level.INFO, "ingredient length of default invalid: {0}", ingredients.length);
                        skip = true;
                        continue;
                    }
                    // Maybe they put a string in here, so first position and uppercase
                    final char character = ingredients[0].toUpperCase().charAt(0);
                    // We have an ingredient that is not in our shape. Ignore it then
                    if (!shapeContainsIngredient(baseSpawnerShape, character)) {
                        getLogger().log(Level.INFO, "shape of default does not contain {0}", character);
                        skip = true;
                        continue;
                    }
                    // We try to get the material (ID or name)
                    Material material = Material.matchMaterial(ingredients[1]);
                    // Failed!
                    if (material == null) {
                        getLogger().log(Level.INFO, "shape material {0} of default spawner matched null", ingredients[1]);
                        material = su.nmsProvider.getIronFenceMaterial();
                    }
                    baseSpawnerRecipe.setIngredient(character, material);
                }
            } catch (final IllegalArgumentException e) {
                // If the custom recipe fails, we have a fallback
                getLogger().log(Level.WARNING, "Could not add the default recipe!", e);
                baseSpawnerRecipe.shape(new String[] { "AAA", "ABA", "AAA" });
                baseSpawnerRecipe.setIngredient('A', su.nmsProvider.getIronFenceMaterial());
                // Use the right egg!
                baseSpawnerRecipe.setIngredient('B', su.nmsProvider.getSpawnEggMaterial());
            } finally {
                // Add it
                if (!skip) {
                    getServer().addRecipe(baseSpawnerRecipe);
                }
            }
        }
    }

    @SuppressWarnings("static-method")
    private boolean shapeContainsIngredient(final List<String> shape, final char c) {
        boolean match = false;
        for (final String recipePart : shape) {
            for (final char recipeIngredient : recipePart.toCharArray()) {
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

    /**
     * Sends a message to the player if the 'silkspawners.info' permission is granted. Empty messages are ignored and not are not sent.
     *
     * @param player the player to message
     * @param message the message to send
     */
    public void informPlayer(final Player player, final String message) {
        if (StringUtils.isBlank(message)) {
            return;
        }
        if (player.hasPermission("silkspawners.info")) {
            su.sendMessage(player, message);
        }
    }

    public void reloadConfigs() {
        config.load();
        config.save();
        loadRecipes();
        su.load();
        mobs.load();
        mobs.save();
        localization.load();
        localization.save();
    }

    public void shutdown() {
        setEnabled(false);
    }

    @Override
    public void reloadConfig() {
        reloadConfigs();
    }

    @Override
    public FileConfiguration getConfig() {
        return config;
    }

}
