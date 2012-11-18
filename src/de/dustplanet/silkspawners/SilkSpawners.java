package de.dustplanet.silkspawners;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import org.bukkit.Material;
import org.bukkit.craftbukkit.block.CraftCreatureSpawner;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.java.JavaPlugin;

public class SilkSpawners extends JavaPlugin {
	private SilkSpawnersBlockListener blockListener;
	private SilkSpawnersPlayerListener playerListener;
	private SilkSpawnersInventoryListener inventoryListener;
	private SilkSpawnersCommands silkCommands;
	private SilkUtil su;

	public boolean spoutEnabled, usePermissions;

	public void onDisbale() {
		su.clearAll();
	}

	public void onEnable() {
		su = new SilkUtil(this);
		loadConfig();
		// Check for spout
		if (getConfig().getBoolean("useSpout")) {
			if (getServer().getPluginManager().isPluginEnabled("Spout")) {
				getServer().getLogger().info("[SilkSpawners] Spout present. Enabling Spout features.");
				spoutEnabled = true;
			} else {
				getServer().getLogger().info("[SilkSpawners] Spout not found. Disabling Spout features.");
			}
		}

		silkCommands = new SilkSpawnersCommands(this, su);
		getCommand("spawner").setExecutor(silkCommands);
		blockListener  = new SilkSpawnersBlockListener(this, su);
		playerListener  = new SilkSpawnersPlayerListener(this, su);
		inventoryListener = new SilkSpawnersInventoryListener(this, su);

		// Listeners
		getServer().getPluginManager().registerEvents(blockListener, this);
		getServer().getPluginManager().registerEvents(playerListener, this);
		getServer().getPluginManager().registerEvents(inventoryListener, this);
	}

	// Copy default configuration
	// Sure we could use getConfig().options().copyDefaults(true);, but it strips all comments :(
	private boolean newConfig(File file) {
		FileWriter fileWriter;
		if (!file.getParentFile().exists()) {
			file.getParentFile().mkdir();
		}

		try {
			fileWriter = new FileWriter(file);
		} catch (IOException e) {
			getServer().getLogger().severe("Couldn't write config file: " + e.getMessage());
			getServer().getPluginManager().disablePlugin(this);
			return false;
		}

		BufferedReader reader = new BufferedReader(new InputStreamReader(new BufferedInputStream(getResource("config.yml"))));
		BufferedWriter writer = new BufferedWriter(fileWriter);
		try {
			String line = reader.readLine();
			while (line != null) {
				writer.write(line + System.getProperty("line.separator"));
				line = reader.readLine();
			}
			getServer().getLogger().info("Wrote default config");
		} catch (IOException e) {
			getServer().getLogger().severe("Error writing config: " + e.getMessage());
		} finally {
			try {
				writer.close();
				reader.close();
			} catch (IOException e) {
				getServer().getLogger().severe("Error saving config: " + e.getMessage());
				getServer().getPluginManager().disablePlugin(this);
			}
		}
		return true;
	}



	private void loadConfig() {
		// Copy default if not already created
		String filename = getDataFolder() + System.getProperty("file.separator") + "config.yml";
		File file = new File(filename);

		if (!file.exists()) {
			if (!newConfig(file)) {
				throw new IllegalArgumentException("Could not create new configuration file");
			}
		}
		reloadConfig();
		// Should we display more information
		boolean verbose = getConfig().getBoolean("verboseConfig", true);
		// Maybe we need to change it later because reflection field changed, user can adjust it then
		// Scan the entities
		SortedMap<Integer, String> sortedMap = su.scanEntityMap(getConfig().getString("entityMapField", "f"));
		for (Map.Entry<Integer, String> entry: sortedMap.entrySet()) {
			// entity ID used for spawn eggs
			short entityID = (short)(int) entry.getKey();
			// internal mod ID used for spawner type
			String mobID = entry.getValue();
			// Lookup creature info
			boolean enable = getConfig().getBoolean("enableCreatureDefault", true);
			enable = getConfig().getBoolean("creatures." + mobID + ".enable", enable);
			if (!enable) {
				if (verbose) {
					getServer().getLogger().info("Entity " + entityID + " = " + mobID + " (disabled)");
				}
				continue;
			}
			// Put the different value in our lists
			su.eid2MobID.put(entityID, mobID);
			su.mobID2Eid.put(mobID, entityID);

			// In-game name for user display, and other recognized names for user input lookup
			String displayName = getConfig().getString("creatures." + mobID + ".displayName");
			if (displayName == null) {
				displayName = mobID;
			}
			// Add it the the list
			su.eid2DisplayName.put(entityID, displayName);

			// Get our lit of aliases
			List<String> aliases = getConfig().getStringList("creatures." + mobID+ ".aliases");
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
				getServer().getLogger().info("Entity " + entityID + " = " + mobID + " (display name: " + displayName + ", aliases: " + aliases + ")");
			}
		}

		// Get the entity ID of the creatures to spawn on damage 0 spawners, or otherwise not override
		// (then will default to Minecraft's default of pigs)
		su.defaultEntityID = 0;

		// Should we use something else as the default?
		String defaultCreatureString = getConfig().getString("defaultCreature", null);
		if (defaultCreatureString != null) {
			// If we know the internal name
			if (su.name2Eid.containsKey(defaultCreatureString)) {
				// Get our entityID
				short defaultEid = su.name2Eid.get(defaultCreatureString);
				// TODO
				// Check for egg support?
				ItemStack defaultItemStack = su.newEggItem(defaultEid);
				if (defaultItemStack != null) {
					// Change default
					su.defaultEntityID = defaultItemStack.getDurability();
					if (verbose) getServer().getLogger().info("Default monster spawner set to " + su.eid2DisplayName.get(defaultEid));
				}
				else getServer().getLogger().warning("Unable to lookup name of " + defaultCreatureString + ", default monster spawner not set");
			}
			// Unknown, fallback
			else getServer().getLogger().warning("Invalid creature type: " + defaultCreatureString+", default monster spawner fallback to PIG");
		}

		// See if we should use permissions
		usePermissions = getConfig().getBoolean("usePermissions", false);

		// Enable craftable spawners?
		if (getConfig().getBoolean("craftableSpawners", false)) loadRecipes();

		// Are we allowed to use native methods?
		if (getConfig().getBoolean("useReflection", true)) {
			try {
				// Get the spawner field, see
				// https://github.com/Bukkit/CraftBukkit/blob/master/src/main/java/org/bukkit/craftbukkit/block/CraftCreatureSpawner.java#L13
				su.tileField = CraftCreatureSpawner.class.getDeclaredField("spawner");
				su.tileField.setAccessible(true);

				// Get the modID field, see
				// https://github.com/Bukkit/CraftBukkit/blob/master/src/main/java/net/minecraft/server/TileEntityMobSpawner.java#L8
				su.mobIDField = net.minecraft.server.TileEntityMobSpawner.class.getDeclaredField("mobName");
				su.mobIDField.setAccessible(true);
			}
			catch (Exception e) {
				getServer().getLogger().warning("Failed to reflect, falling back to wrapper methods: " + e);
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
		if (getConfig().getBoolean("spawnersUnstackable", false)) {
			// http://forums.bukkit.org/threads/setting-max-stack-size.66364/
			try {
				Field maxStackSizeField = net.minecraft.server.Item.class.getDeclaredField(getConfig().getString("spawnersUnstackableField", "maxStackSize"));
				// Set the stackable field back to 1
				maxStackSizeField.setAccessible(true);
				maxStackSizeField.setInt(net.minecraft.server.Item.byId[Material.MOB_SPAWNER.getId()], 1);
			} catch (Exception e) {
				getServer().getLogger().warning("Failed to set max stack size, ignoring spawnersUnstackable: " + e);
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
			if (!getConfig().getBoolean("creatures." + mobID + ".enableCraftingSpawner", true)) {
				if (getConfig().getBoolean("verboseConfig", true)) {
					getServer().getLogger().info("Skipping crafting recipe for "+mobID+" per config");
				}
				continue;
			}
			// Output is one (1) spawner of this type
			ItemStack spawnerItem = su.newSpawnerItem(entityID);
			ShapedRecipe recipe = new ShapedRecipe(spawnerItem);
			/*
			 * A A A
			 * A B A
			 * A A A
			 */
			recipe.shape(new String[] { "AAA", "ABA", "AAA" });
			recipe.setIngredient('A', Material.IRON_FENCE);
			// Use the right egg!
			recipe.setIngredient('B', Material.MONSTER_EGG, (int) entityID);
			// Add it
			getServer().addRecipe(recipe);
		}
	}

	// If the user has the permisison, message
	public void informPlayer(Player player, String message) {
		if (hasPermission(player, "silkspawners.info")) {
			player.sendMessage(message);
		}
	}

	// Check for permisisons
	public boolean hasPermission(Player player, String node) {
		// Normal check if we use permisisons
		if (usePermissions) return player.hasPermission(node);
		// Else check more detailed
		else {
			// Any of the nodes, -> yes
			if (node.equals("silkspawners.info") ||
					node.equals("silkspawners.silkdrop") ||
					node.equals("silkspawners.destroydrop") ||
					node.equals("silkspawners.viewtype")) {
				return true;
			}
			// Else ask for Op status
			else return player.isOp();
		}
	}
}