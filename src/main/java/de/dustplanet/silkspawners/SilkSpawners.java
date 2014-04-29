package de.dustplanet.silkspawners;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import de.dustplanet.silkspawners.commands.EggCommand;
import de.dustplanet.silkspawners.commands.SilkSpawnersTabCompleter;
import de.dustplanet.silkspawners.commands.SpawnerCommand;
import de.dustplanet.silkspawners.configs.Configuration;
import de.dustplanet.silkspawners.listeners.SilkSpawnersEntityListener;
import de.dustplanet.silkspawners.listeners.SilkSpawnersBlockListener;
import de.dustplanet.silkspawners.listeners.SilkSpawnersInventoryListener;
import de.dustplanet.silkspawners.listeners.SilkSpawnersPlayerListener;
import de.dustplanet.util.CommentedConfiguration;
import de.dustplanet.util.SilkUtil;


// Metrics
import org.mcstats.Metrics;


// Updater
import net.gravitydevelopment.updater.Updater;

/**
 * General stuff
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
    public static final String[] COMPATIBLE_MINECRAFT_VERSIONS = {"1.7.2", "1.7.5", "1.7.8", "1.7.9"};

    public void onDisbale() {
	su.clearAll();
    }

    public void onEnable() {
	// Make files and copy defaults
	initializeConfigs();

	// Heart of SilkSpawners is the SilkUtil class which holds all of our
	// important methods
	su = new SilkUtil(this);

	// Load configs
	loadConfigs();

	// Test for right Minecraft version
	if (config.getBoolean("testMCVersion", true)) {
	    // We can't use the MinercraftServer import because it might be broken. Regex is our friend and helper
	    // Find MC: 0.0.0, last occurence is optional
	    Pattern pat = Pattern.compile("MC: \\d{1}.\\d{1}(.\\d{1})?");
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
		getLogger().info("You can disable this check by setting testMCVersion to false in the config!");
		setEnabled(false);
		return;
	    }
	}

	// Check if we should enable the auto Updater & have no snapshot (dev build)
	if (config.getBoolean("autoUpdater", true)) {
	    if (getDescription().getVersion().contains("SNAPSHOT")) {
		getLogger().info("AutoUpdater is disabled because you are running a dev build!");
	    } else {
		// Updater http://forums.bukkit.org/threads/96681/
		new Updater(this, 35890, getFile(), Updater.UpdateType.DEFAULT, true);
		getLogger().info("AutoUpdater is enabled.");
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
		su.barAPI = true;
	    } else {
		// Else tell the admin about the missing of BarAPI
		getLogger().info("BarAPI was not found and remains disabled!");
	    }
	} else {
	    getLogger().info("BarAPI is disabled due to config setting.");
	}
    }

    // If no config is found, copy the default one(s)!
    private void copy(InputStream in, File file) {
	OutputStream out = null;
	try {
	    out = new FileOutputStream(file);
	    byte[] buf = new byte[1024];
	    int len;
	    while ((len = in.read(buf)) > 0) {
		out.write(buf, 0, len);
	    }
	} catch (IOException e) {
	    getLogger().warning("Failed to copy the default config! (I/O)");
	    e.printStackTrace();
	} finally {
	    try {
		if (out != null) {
		    out.close();
		}
	    } catch (IOException e) {
		getLogger().warning("Failed to close the streams! (I/O -> Output)");
		e.printStackTrace();
	    }
	    try {
		if (in != null) {
		    in.close();
		}
	    } catch (IOException e) {
		getLogger().warning("Failed to close the streams! (I/O -> Input)");
		e.printStackTrace();
	    }
	}
    }

    private void initializeConfigs() {
	// Config
	configFile = new File(getDataFolder(), "config.yml");
	// One file and the folder not existent
	if (!configFile.exists() && !getDataFolder().exists()) {
	    // Break if no folder can be created!
	    if (!getDataFolder().mkdirs()) {
		getLogger().severe("The config folder could NOT be created, make sure it's writable!");
		getLogger().severe("Disabling now!");
		setEnabled(false);
		return;
	    }
	}
	// Copy default is necessary
	if (!configFile.exists()) {
	    copy(getResource("config.yml"), configFile);
	}

	// Localization
	localizationFile = new File(getDataFolder(), "localization.yml");
	if (!localizationFile.exists()) {
	    copy(getResource("localization.yml"), localizationFile);
	}

	// Mobs
	mobsFile = new File(getDataFolder(), "mobs.yml");
	if (!mobsFile.exists()) {
	    copy(getResource("mobs.yml"), mobsFile);
	}

	// Load configs
	config = new CommentedConfiguration(configFile);
	new Configuration(config).loadNum(1);

	localization = new CommentedConfiguration(localizationFile);
	new Configuration(localization).loadNum(2);

	mobs = new CommentedConfiguration(mobsFile);
	new Configuration(mobs).loadNum(3);

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

    private void loadConfigs() {
	// Should we display more information
	boolean verbose = config.getBoolean("verboseConfig", false);

	// Scan the entities
	SortedMap<Integer, String> sortedMap = su.scanEntityMap();
	for (Map.Entry<Integer, String> entry : sortedMap.entrySet()) {
	    // entity ID used for spawn eggs
	    short entityID = (short) (int) entry.getKey();
	    // internal mod ID used for spawner type
	    String mobID = entry.getValue();
	    // bukkit's wrapper enum
	    EntityType bukkitEntity = EntityType.fromId(entityID);
	    Class<? extends Entity> bukkitEntityClass = bukkitEntity == null ? null : bukkitEntity.getEntityClass();

	    // Lookup creature info
	    boolean enable = config.getBoolean("enableCreatureDefault", true);
	    // Check if already known
	    if (!mobs.contains("creatures." + mobID)) {
		getLogger().info("Entity " + entityID + "/" + mobID + " is not in the config. Adding...");
		mobs.addDefault("creatures." + mobID + ".enable", enable);
		mobs.save();
	    } else {
		enable = mobs.getBoolean("creatures." + mobID + ".enable", enable);
	    }
	    if (!enable) {
		if (verbose) {
		    getLogger().info("Entity " + entityID + " = " + mobID + "/"
			    + bukkitEntity + "[" + bukkitEntityClass
			    + "] (disabled)");
		}
		continue;
	    }

	    // Add the known ID [we omit all disabled entities]
	    su.knownEids.add(entityID);
	    // Put the different value in our lists
	    su.eid2MobID.put(entityID, mobID);
	    su.mobID2Eid.put(mobID, entityID);

	    // In-game name for user display, and other recognized names for
	    // user input lookup
	    String displayName = mobs.getString("creatures." + mobID + ".displayName");
	    if (displayName == null) {
		displayName = mobID;
	    }
	    // Add it the the list
	    su.eid2DisplayName.put(entityID, displayName);

	    // Get our lit of aliases
	    List<String> aliases = mobs.getStringList("creatures." + mobID + ".aliases");
	    // Get the name, make it lowercase and strip out the spaces
	    aliases.add(displayName.toLowerCase().replace(" ", ""));
	    // Add the internal name
	    aliases.add(mobID.toLowerCase().replace(" ", ""));
	    // Add the ID
	    aliases.add(Short.toString(entityID));
	    // Add it to our names and ID list
	    for (String alias : aliases) {
		su.name2Eid.put(alias, entityID);
	    }

	    // Detailed message
	    if (verbose) {
		getLogger().info("Entity " + entityID + " = " + mobID + "/"
			+ bukkitEntity + "[" + bukkitEntityClass
			+ "] (display name: " + displayName
			+ ", aliases: " + aliases + ")");
	    }
	}

	// Set the defaultID for spawners (-> memo, on some spawners it seems 0
	// -> pig is 90)
	su.defaultEntityID = 90;

	// Should we use something else as the default?
	if (config.contains("defaultCreature")) {
	    // Lowercase is better to search
	    String defaultCreatureString = config.getString("defaultCreature", "90").toLowerCase();
	    // Try IDs first, may fail, use name then!
	    try {
		short entityID = Short.valueOf(defaultCreatureString);
		// Known ID and MobName? Yes -> We use it
		if (su.isKnownEntityID(entityID) && su.isRecognizedMob(su.getCreatureName(entityID))) {
		    defaultCreatureString = su.getCreatureName(entityID).toLowerCase();
		}
	    } catch (NumberFormatException e) {
		// Name then
	    }
	    // If we know the internal name
	    if (su.name2Eid.containsKey(defaultCreatureString)) {
		// Get our entityID
		short defaultEid = su.name2Eid.get(defaultCreatureString);
		// Change default
		su.defaultEntityID = defaultEid;
		if (verbose) {
		    getLogger().info("Default monster spawner set to " + su.eid2DisplayName.get(defaultEid));
		}
	    } else {
		// Unknown, fallback
		getLogger().warning("Invalid creature type: " + defaultCreatureString + ", default monster spawner fallback to PIG");
	    }
	}

	// See if we should use permissions
	usePermissions = config.getBoolean("usePermissions", true);

	// Enable craftable spawners?
	if (config.getBoolean("craftableSpawners", false)) {
	    loadRecipes();
	}

	// Are we allowed to use native methods?
	if (config.getBoolean("useReflection", true)) {
	    su.useReflection = true;
	}

	// Optionally make spawners unstackable in an attempt to be more
	// compatible with CraftBukkit forks which may conflict
	// Requested on http://dev.bukkit.org/server-mods/silkspawners/#c25
	if (config.getBoolean("spawnersUnstackable", false)) {
	    su.nmsProvider.setSpawnersUnstackable();
	}
    }

    // Add the recipes
    private void loadRecipes() {
	// For all our entities
	for (short entityID : su.eid2MobID.keySet()) {

	    // Name
	    String mobID = su.eid2MobID.get(entityID);

	    // If the mob is disabled, skip it
	    if (!mobs.getBoolean("creatures." + mobID + ".enableCraftingSpawner", true)) {
		if (config.getBoolean("verboseConfig", true)) {
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
	    if (config.getBoolean("verboseConfig", true)) {
		getLogger().info("Amount of " + mobID + ": " + amount);
	    }
	    // Output is a spawner of this type with a custom amount
	    ItemStack spawnerItem = su.newSpawnerItem(entityID, su.getCustomSpawnerName(mobID), amount);
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
		if (config.getBoolean("verboseConfig", true)) {
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
		    ingredientsList = mobs.getStringList("creatures." + mobID + "recipe.ingredients");
		} else {
		    // No list what we should use -> not adding
		    if (!config.contains("ingredients")) {
			continue;
		    } else {
			ingredientsList = config.getStringList("ingredients");
		    }
		}

		// Security first
		if (ingredientsList == null) {
		    continue;
		}

		// Debug output
		if (config.getBoolean("verboseConfig", true)) {
		    getLogger().info("Ingredients of " + mobID + ":");
		    getLogger().info(ingredientsList.toString());
		}

		for (String ingredient : ingredientsList) {
		    // They are added like this A,DIRT
		    // Lets split the "," then
		    String[] ingredients = ingredient.split(",");
		    // if our array is not exactly of the size 2, something is wrong
		    if (ingredients.length != 2) {
			continue;
		    }
		    // Maybe they put a string in here, so first position and uppercase
		    char character = ingredients[0].toUpperCase().charAt(0);
		    // We try to get the material (ID or name)
		    Material material = Material.matchMaterial(ingredients[1]);
		    // Failed!
		    if (material == null) {
			material = Material.IRON_FENCE;
		    }
		    recipe.setIngredient(character, material);
		}
		// Use the right egg!
		recipe.setIngredient('X', Material.MONSTER_EGG, (int) entityID);
	    } catch (IllegalArgumentException e) {
		// If the custom recipe fails, we have a fallback
		getLogger().warning("Could not add the recipe!");
		e.printStackTrace();
		recipe.shape(new String[] {"AAA", "ABA", "AAA"});
		recipe.setIngredient('A', Material.IRON_FENCE);
		// Use the right egg!
		recipe.setIngredient('B', Material.MONSTER_EGG, (int) entityID);
	    } finally {
		// Add it
		getServer().addRecipe(recipe);
	    }
	}
    }

    // If the user has the permission, message
    public void informPlayer(Player player, String message) {
	if (hasPermission(player, "silkspawners.info")) {
	    player.sendMessage(message);
	}
    }

    // Check for permissions
    public boolean hasPermission(Player player, String node) {
	// Normal check if we use permissions
	if (usePermissions) {
	    return player.hasPermission(node);
	} else {
	    // Else check more detailed
	    // Any of the nodes, -> yes
	    if (node.equals("silkspawners.info")
		    || node.startsWith("silkspawners.silkdrop")
		    || node.startsWith("silkspawners.destroydrop")
		    || node.equals("silkspawners.viewtype")
		    || node.startsWith("silkspawners.place")
		    || node.startsWith("silkspawners.craft")) {
		return true;
	    }
	    // Else ask for Op status
	    else {
		return player.isOp();
	    }
	}
    }

    public void reloadConfigs() {
	config.load();
	config.save();
	loadConfigs();
	mobs.load();
	mobs.save();
	localization.load();
	localization.save();
    }

    public void shutdown() {
	setEnabled(true);
    }
}
