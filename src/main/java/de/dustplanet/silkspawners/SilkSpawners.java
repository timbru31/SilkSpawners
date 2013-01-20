package de.dustplanet.silkspawners;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import net.minecraft.server.v1_4_R1.Item;
import net.minecraft.server.v1_4_R1.TileEntityMobSpawner;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.v1_4_R1.block.CraftCreatureSpawner;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.java.JavaPlugin;
import de.dustplanet.silkspawners.commands.EggCommand;
import de.dustplanet.silkspawners.commands.SilkSpawnersTabCompleter;
import de.dustplanet.silkspawners.commands.SpawnerCommand;
import de.dustplanet.silkspawners.listeners.SilkSpawnersBlockListener;
import de.dustplanet.silkspawners.listeners.SilkSpawnersInventoryListener;
import de.dustplanet.silkspawners.listeners.SilkSpawnersPlayerListener;
// ErrorLogger
import de.dustplanet.util.ErrorLogger;
import de.dustplanet.util.SilkUtil;
// Metrics
import org.mcstats.Metrics;
// Updater
import net.h31ix.updater.Updater;

/**
 * General stuff
 * @author (former) mushroomhostage
 * @author xGhOsTkiLLeRx
 */

public class SilkSpawners extends JavaPlugin {
	private SilkSpawnersBlockListener blockListener;
	private SilkSpawnersPlayerListener playerListener;
	private SilkSpawnersInventoryListener inventoryListener;
	private SpawnerCommand spawnerCommand;
	private EggCommand eggCommand;
	private SilkSpawnersTabCompleter tabCompleter;
	private SilkUtil su;
	public boolean spoutEnabled, usePermissions;
	public FileConfiguration config, localization;
	private File configFile, localizationFile;

	public void onDisbale() {
		su.clearAll();
	}

	public void onEnable() {
		// Nicer ErrorLogger http://forums.bukkit.org/threads/105321/
		ErrorLogger.register(this, "SilkSpawners", "de.dustplanet.silkspawners", "http://dev.bukkit.org/server-mods/silkspawners/tickets/");
		loadConfig();
		// Check for spout
		if (config.getBoolean("useSpout", true)) {
			if (getServer().getPluginManager().isPluginEnabled("Spout")) {
				getLogger().info("Spout present. Enabling Spout features.");
				spoutEnabled = true;
			} else {
				getLogger().info("Spout not found. Disabling Spout features.");
			}
		}

		// Check if we should enable the auto Updater
		if (config.getBoolean("autoUpdater", true)) {
			// Updater http://forums.bukkit.org/threads/96681/
			new Updater(this, "silkspawners", this.getFile(), Updater.UpdateType.DEFAULT, false);
		}

		spawnerCommand = new SpawnerCommand(this, su);
		eggCommand = new EggCommand(this, su);
		tabCompleter = new SilkSpawnersTabCompleter(su);
		getCommand("silkspawners").setExecutor(spawnerCommand);
		getCommand("egg").setExecutor(eggCommand);
		getCommand("silkspawners").setTabCompleter(tabCompleter);
		getCommand("egg").setTabCompleter(tabCompleter);
		blockListener  = new SilkSpawnersBlockListener(this, su);
		playerListener  = new SilkSpawnersPlayerListener(this, su);
		inventoryListener = new SilkSpawnersInventoryListener(this, su);

		// Listeners
		getServer().getPluginManager().registerEvents(blockListener, this);
		getServer().getPluginManager().registerEvents(playerListener, this);
		getServer().getPluginManager().registerEvents(inventoryListener, this);

		// Metrics
		try {
			Metrics metrics = new Metrics(this);
			metrics.start();
		}
		catch (IOException e) {
			getLogger().info("Couldn't start Metrics, please report this!");
			e.printStackTrace();
		}
	}
	
	// If no config is found, copy the default one(s)!
	private void copy(InputStream in, File file) {
		try {
			OutputStream out = new FileOutputStream(file);
			byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
			out.close();
			in.close();
		} catch (FileNotFoundException e) {
			getLogger().warning("Failed to copy the default config! (FileNotFound)");
		} catch (IOException e) {
			getLogger().warning("Failed to copy the default config! (I/O)");
		}
	}

	private void loadConfig() {
		// Config
		configFile = new File(getDataFolder(), "config.yml");
		// One file and the folder not existent
		if (!configFile.exists() && !configFile.getParentFile().exists()) {
			// Break if no folder can be created!
			if (!configFile.getParentFile().mkdirs()) {
				getLogger().severe("The config folder could NOT be created, make sure it's writable!");
				getLogger().severe("Disabling now!");
				setEnabled(false);
				return;
			}
		}
		// Copy default is neccessary
		if (!configFile.exists()) {
			copy(getResource("config.yml"), configFile);
		}
		
		// Localization
		localizationFile = new File(getDataFolder(), "localization.yml");
		if(!localizationFile.exists()) {
			copy(getResource("localization.yml"), localizationFile);
		}
		
		config = getConfig();
		localization = YamlConfiguration.loadConfiguration(localizationFile);
		su = new SilkUtil(this);
		if (localization.getString("spawnerName", "Monster Spawner").equalsIgnoreCase("Monster Spawner")) su.coloredNames = false;
		else su.coloredNames = true;
		// Should we display more information
		boolean verbose = config.getBoolean("verboseConfig", true);
		// Maybe we need to change it later because reflection field changed, user can adjust it then
		// Scan the entities
		SortedMap<Integer, String> sortedMap = su.scanEntityMap(config.getString("entityMapField", "f"));
		for (Map.Entry<Integer, String> entry: sortedMap.entrySet()) {
			// entity ID used for spawn eggs
			short entityID = (short)(int) entry.getKey();
			// internal mod ID used for spawner type
			String mobID = entry.getValue();
			// Lookup creature info
			boolean enable = config.getBoolean("enableCreatureDefault", true);
			enable = config.getBoolean("creatures." + mobID + ".enable", enable);
			if (!enable) {
				if (verbose) {
					getLogger().info("Entity " + entityID + " = " + mobID + " (disabled)");
				}
				continue;
			}
			// Add the known ID [we omit all disabled entities]
			su.knownEids.add(entityID);
			// Put the different value in our lists
			su.eid2MobID.put(entityID, mobID);
			su.mobID2Eid.put(mobID, entityID);

			// In-game name for user display, and other recognized names for user input lookup
			String displayName = config.getString("creatures." + mobID + ".displayName");
			if (displayName == null) {
				displayName = mobID;
			}
			// Add it the the list
			su.eid2DisplayName.put(entityID, displayName);

			// Get our lit of aliases
			List<String> aliases = config.getStringList("creatures." + mobID+ ".aliases");
			// Get the name, make it lowercase and strip out the spaces
			aliases.add(displayName.toLowerCase().replace(" ", ""));
			// Add the internal name
			aliases.add(mobID.toLowerCase().replace(" ", ""));
			// Add the ID
			aliases.add(Short.toString(entityID));
			// Add it to our names and ID list
			for (String alias: aliases) {
				su.name2Eid.put(alias, entityID);
			}
			// Detailed message
			if (verbose) {
				getLogger().info("Entity " + entityID + " = " + mobID + " (display name: " + displayName + ", aliases: " + aliases + ")");
			}
		}

		// Set the defaultID for spawners (-> memo, on some spawners it seems 0 -> pig is 90)
		su.defaultEntityID = 90;

		// Should we use something else as the default?
		if (config.getString("defaultCreature", null) != null) {
			// Lowercase is better to search
			String defaultCreatureString = config.getString("defaultCreature", null).toLowerCase();
			// If we know the internal name
			if (su.name2Eid.containsKey(defaultCreatureString)) {
				// Get our entityID
				short defaultEid = su.name2Eid.get(defaultCreatureString);
				// Change default
				su.defaultEntityID = defaultEid;
				if (verbose) getLogger().info("Default monster spawner set to " + su.eid2DisplayName.get(defaultEid));
			}
			// Unknown, fallback
			else getLogger().warning("Invalid creature type: " + defaultCreatureString+", default monster spawner fallback to PIG");
		}

		// See if we should use permissions
		usePermissions = config.getBoolean("usePermissions", false);

		// Enable craftable spawners?
		if (config.getBoolean("craftableSpawners", false)) loadRecipes();

		// Are we allowed to use native methods?
		if (config.getBoolean("useReflection", true)) {
			try {
				// Get the spawner field, see
				// https://github.com/Bukkit/CraftBukkit/blob/master/src/main/java/org/bukkit/craftbukkit/block/CraftCreatureSpawner.java#L13
				su.tileField = CraftCreatureSpawner.class.getDeclaredField("spawner");
				su.tileField.setAccessible(true);

				// Get the modID field, see
				// https://github.com/Bukkit/CraftBukkit/blob/master/src/main/java/net/minecraft/server/TileEntityMobSpawner.java#L11
				su.mobIDField = TileEntityMobSpawner.class.getDeclaredField("mobName");
				su.mobIDField.setAccessible(true);
			}
			catch (Exception e) {
				getLogger().warning("Failed to reflect, falling back to wrapper methods: " + e.getMessage());
				e.printStackTrace();
				su.tileField = null;
				su.mobIDField = null;
			}
		}
		else {
			su.tileField = null;
			su.mobIDField = null;
		}

		// Optionally make spawners unstackable in an attempt to be more compatible with CraftBukkit++
		// Requested on http://dev.bukkit.org/server-mods/silkspawners/#c25
		if (config.getBoolean("spawnersUnstackable", false)) {
			// http://forums.bukkit.org/threads/setting-max-stack-size.66364/
			try {
				Field maxStackSizeField = Item.class.getDeclaredField(config.getString("spawnersUnstackableField", "maxStackSize"));
				// Set the stackable field back to 1
				maxStackSizeField.setAccessible(true);
				maxStackSizeField.setInt(Item.byId[Material.MOB_SPAWNER.getId()], 1);
			}
			catch (Exception e) {
				getLogger().warning("Failed to set max stack size, ignoring spawnersUnstackable: " + e.getMessage());
				e.printStackTrace();
			}
		}
	}

	// Add the recipes
	private void loadRecipes() {
		// For all our entities
		for (short entityID: su.eid2MobID.keySet()) {
			// Name
			String mobID = su.eid2MobID.get(entityID);
			// If the mob is disabled, skip it
			if (!config.getBoolean("creatures." + mobID + ".enableCraftingSpawner", true)) {
				if (config.getBoolean("verboseConfig", true)) {
					getLogger().info("Skipping crafting recipe for " + mobID + " per config");
				}
				continue;
			}
			// Output is one (1) spawner of this type
			ItemStack spawnerItem = su.newSpawnerItem(entityID, localization.getString("spawnerName"));
			ShapedRecipe recipe = new ShapedRecipe(spawnerItem);
			/*
			 * A A A
			 * A B A
			 * A A A
			 */

			// We try to use the custom recipe, but we don't know if the user changed it right ;)
			try {
				// At leaste we have defaults here!
				recipe.shape(config.getString("recipeTop", "AAA"), config.getString("recipeMiddle", "AXA"), config.getString("recipeBottom", "AAA"));
				// No list what we should use -> not adding
				if (!config.contains("ingredients")) return;
				for (String ingredient : config.getStringList("ingredients")) {
					// They are added like this A,DIRT
					// Lets split the "," then
					String[] ingredients = ingredient.split(",");
					// Maybe they put a string in here, so first position and uppercase
					char character = ingredients[0].toUpperCase().charAt(0);
					// We try to get the material
					Material material=  Material.getMaterial(ingredients[1]);
					// Failed!
					if (material == null)  {
						// Maybe the put an integer here?
						try {
							int id = Integer.valueOf(ingredients[1]);
							material = Material.getMaterial(id);
							// Not all IDs are valid!
							if (material == null) material = Material.IRON_FENCE;
						}
						// Still no ID, fallback
						catch (IllegalArgumentException e) {
							material = Material.IRON_FENCE;
						}
					}
					// Just in case my logic was wrong...
					if (material == null) material = Material.IRON_FENCE;
					recipe.setIngredient(character, material);
				}
				// Use the right egg!
				recipe.setIngredient('X', Material.MONSTER_EGG, (int) entityID);
			}
			// If the custom recipe fails, we have a fallback
			catch (IllegalArgumentException e) {
				e.printStackTrace();
				recipe.shape(new String[] { "AAA", "ABA", "AAA" });
				recipe.setIngredient('A', Material.IRON_FENCE);
				// Use the right egg!
				recipe.setIngredient('B', Material.MONSTER_EGG, (int) entityID);
			}
			finally {
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
		if (usePermissions)	return player.hasPermission(node);
		// Else check more detailed
		else {
			// Any of the nodes, -> yes
			if (node.equals("silkspawners.info") ||
					node.startsWith("silkspawners.silkdrop") ||
					node.startsWith("silkspawners.destroydrop") ||
					node.equals("silkspawners.viewtype")) {
				return true;
			}
			// Else ask for Op status
			else return player.isOp();

		}
	}
}