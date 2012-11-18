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
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.block.CraftCreatureSpawner;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;

public class SilkSpawners extends JavaPlugin {
	Logger log = Logger.getLogger("Minecraft");
	SilkSpawnersBlockListener blockListener  = new SilkSpawnersBlockListener(this);
	SilkSpawnersPlayerListener playerListener  = new SilkSpawnersPlayerListener(this);
	SilkSpawnersInventoryListener inventoryListener  = new SilkSpawnersInventoryListener(this);

	ConcurrentHashMap<Short,String> eid2DisplayName;    // human-readable friendly name
	ConcurrentHashMap<Short,String> eid2MobID;          // internal String used by spawners
	ConcurrentHashMap<String,Short> mobID2Eid;
	ConcurrentHashMap<String,Short> name2Eid;           // aliases to entity ID

	short defaultEntityID;
	boolean usePermissions;

	Field tileField, mobIDField;

	// To avoid confusing with badly name MONSTER_EGGS (silverfish), set our own material
	final static Material SPAWN_EGG = Material.MONSTER_EGG;

	public boolean spoutEnabled = false;
	
	public void onEnable() {
		loadConfig();
		// Check for spout
		if (getServer().getPluginManager().isPluginEnabled("Spout")) {
			if (getConfig().getBoolean("useSpout")) {
				getServer().getLogger().log(Level.INFO, "[SilkSpawners] Spout present. Enabling Spout features.");
				spoutEnabled = true;
			} else {
				getServer().getLogger().log(Level.INFO, "[SilkSpawners] Disabling Spout features even though Spout is present.");
			}
		}

		// Listeners
		getServer().getPluginManager().registerEvents(blockListener, this);
		getServer().getPluginManager().registerEvents(playerListener, this);
		getServer().getPluginManager().registerEvents(inventoryListener, this);

		log.info("SilkSpawners enabled");
	}

	public boolean hasPermission(Player player, String node) {
		if (usePermissions) {
			return player.hasPermission(node);
		} else {
			if (node.equals("silkspawners.info") ||
					node.equals("silkspawners.silkdrop") ||
					node.equals("silkspawners.destroydrop") ||
					node.equals("silkspawners.viewtype")) {
				return true;
			} else {
				return player.isOp();
			}
		}
	}

	// Copy default configuration
	// Sure we could use getConfig().options().copyDefaults(true);, but it strips all comments :(
	public boolean newConfig(File file) {
		FileWriter fileWriter;
		if (!file.getParentFile().exists()) {
			file.getParentFile().mkdir();
		}

		try {
			fileWriter = new FileWriter(file);
		} catch (IOException e) {
			log.severe("Couldn't write config file: " + e.getMessage());
			Bukkit.getServer().getPluginManager().disablePlugin(this);
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
			log.info("Wrote default config");
		} catch (IOException e) {
			log.severe("Error writing config: " + e.getMessage());
		} finally {
			try {
				writer.close();
				reader.close();
			} catch (IOException e) {
				log.severe("Error saving config: " + e.getMessage());
				Bukkit.getServer().getPluginManager().disablePlugin(this);
			}
		}
		return true;
	}

	@SuppressWarnings("unchecked")
	public SortedMap<Integer,String> scanEntityMap() {
		SortedMap<Integer,String> sortedMap = new TreeMap<Integer,String>();

		// Use reflection to dump native EntityTypes
		// This bypasses Bukkit's wrappers, so it works with mods
		try {
			// https://github.com/MinecraftPortCentral/mc-dev/blob/master/net/minecraft/server/EntityTypes.java
			// f.put(s, Integer.valueOf(i));
			Field field = net.minecraft.server.EntityTypes.class.getDeclaredField(getConfig().getString("entityMapField", "f"));
			field.setAccessible(true);
			Map<String,Integer> map = (Map<String,Integer>)field.get(null);

			for (Map.Entry<String,Integer> entry: ((Map<String,Integer>)map).entrySet()) {
				sortedMap.put(entry.getValue(), entry.getKey());
			}
		} catch (Exception e) {
			log.severe("Failed to dump entity map: " + e);
		}

		return sortedMap;
	}

	private void loadConfig() {
		String filename = getDataFolder() + System.getProperty("file.separator") + "config.yml";
		File file = new File(filename);

		if (!file.exists()) {
			if (!newConfig(file)) {
				throw new IllegalArgumentException("Could not create new configuration file");
			}
		}
		reloadConfig();

		boolean verbose = getConfig().getBoolean("verboseConfig", true);

		eid2MobID = new ConcurrentHashMap<Short,String>();
		mobID2Eid = new ConcurrentHashMap<String,Short>();

		SortedMap<Integer,String> sortedMap = scanEntityMap();

		eid2DisplayName = new ConcurrentHashMap<Short,String>();
		name2Eid = new ConcurrentHashMap<String,Short>();

		for (Map.Entry<Integer,String> entry: sortedMap.entrySet()) {
			short entityID = (short)(int)entry.getKey();    // entity ID used for spawn eggs
			String mobID = entry.getValue();                // internal mod ID used for spawner type

			// Lookup creature info

			boolean enable = getConfig().getBoolean("enableCreatureDefault", true);
			enable = getConfig().getBoolean("creatures."+mobID+".enable", enable);
			if (!enable) {
				if (verbose) {
					log.info("Entity " + entityID + " = " + mobID + " (disabled)");
				}
				continue;
			}

			eid2MobID.put(entityID, mobID);
			mobID2Eid.put(mobID, entityID);


			// In-game name for user display, and other recognized names for user input lookup

			String displayName = getConfig().getString("creatures."+mobID+".displayName");
			if (displayName == null) {
				displayName = mobID;
			}

			eid2DisplayName.put(entityID, displayName);

			List<String> aliases = getConfig().getStringList("creatures."+mobID+".aliases");

			aliases.add(displayName.toLowerCase().replace(" ", ""));
			aliases.add(mobID.toLowerCase().replace(" ", ""));
			aliases.add(entityID+"");

			for (String alias: aliases) {
				name2Eid.put(alias, entityID);
			}

			if (verbose) {
				log.info("Entity " + entityID + " = " + mobID + " (display name: " + displayName + ", aliases: " + aliases + ")");
			}
		}

		// Get the entity ID of the creatures to spawn on damage 0 spawners, or otherwise not override
		// (then will default to Minecraft's default of pigs)
		defaultEntityID = 0;

		String defaultCreatureString = getConfig().getString("defaultCreature", null);
		if (defaultCreatureString != null) {
			if (name2Eid.containsKey(defaultCreatureString)) {
				short defaultEid = name2Eid.get(defaultCreatureString);
				ItemStack defaultItemStack = newEggItem(defaultEid);
				if (defaultItemStack != null) {
					defaultEntityID = defaultItemStack.getDurability();
					if (verbose) { 
						log.info("Default monster spawner set to "+eid2DisplayName.get(defaultEid));
					}
				} else {
					log.warning("Unable to lookup name of " + defaultCreatureString+", default monster spawner not set");
				}
			} else {
				log.warning("Invalid creature type: " + defaultCreatureString+", default monster spawner not set");
			}
		}

		usePermissions = getConfig().getBoolean("usePermissions", false);

		if (getConfig().getBoolean("craftableSpawners", false)) {
			loadRecipes();
		}

		if (getConfig().getBoolean("useReflection", true)) {
			try {
				tileField = CraftCreatureSpawner.class.getDeclaredField("spawner");
				tileField.setAccessible(true);

				mobIDField = net.minecraft.server.TileEntityMobSpawner.class.getDeclaredField("mobName");  // MCP "mobID"
				mobIDField.setAccessible(true);
			} catch (Exception e) {
				log.warning("Failed to reflect, falling back to wrapper methods: " + e);
				tileField = null;
				mobIDField = null;
			}
		} else {
			tileField = null;
			mobIDField = null;
		}

		// Optionally make spawners unstackable in an attempt to be more compatible with CraftBukkit++
		// Requested on http://dev.bukkit.org/server-mods/silkspawners/#c25
		if (getConfig().getBoolean("spawnersUnstackable", false)) {
			// http://forums.bukkit.org/threads/setting-max-stack-size.66364/
			try {
				Field maxStackSizeField = net.minecraft.server.Item.class.getDeclaredField(getConfig().getString("spawnersUnstackableField", "maxStackSize"));

				maxStackSizeField.setAccessible(true);
				maxStackSizeField.setInt(net.minecraft.server.Item.byId[Material.MOB_SPAWNER.getId()], 1);
			} catch (Exception e) {
				log.warning("Failed to set max stack size, ignoring spawnersUnstackable: " + e);
			}
		}
	}

	private void loadRecipes() {
		for (short entityID: eid2MobID.keySet()) {
			String mobID = eid2MobID.get(entityID);

			if (!getConfig().getBoolean("creatures."+mobID+".enableCraftingSpawner", true)) {
				if (getConfig().getBoolean("verboseConfig", true)) {
					log.info("Skipping crafting recipe for "+mobID+" per config");
				}
				continue;
			}

			ItemStack spawnerItem = newSpawnerItem(entityID);

			ShapedRecipe recipe = new ShapedRecipe(spawnerItem);

			recipe.shape(new String[] { "AAA", "ABA", "AAA" });
			recipe.setIngredient('A', Material.IRON_FENCE);
			recipe.setIngredient('B', Material.MONSTER_EGG, (int)entityID);

			Bukkit.getServer().addRecipe(recipe);
		}
	}

	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		if (!cmd.getName().equalsIgnoreCase("silkspawners")) {
			return false;
		}

		if (!(sender instanceof Player)) {
			// Would like to handle the non-player (console command) case, but, I use the block the
			// player is looking at, so...
			return false;
		}

		Player player = (Player)sender;

		if (args.length == 0) {
			// Get spawner type
			if (!hasPermission(player, "silkspawners.viewtype")) {
				sender.sendMessage("You do not have permission to view the spawner type");
				return true;
			}

			Block block = getSpawnerFacing(player);

			if (block == null) {
				sender.sendMessage("You must be looking directly at a spawner to use this command");
				return false;
			}

			try {
				short entityID = getSpawnerEntityID(block);

				sender.sendMessage(getCreatureName(entityID) + " spawner");
			} catch (Exception e) {
				informPlayer(player, "Failed to identify spawner: " + e);
			}

		} else {
			// Set or get spawner

			Block block = getSpawnerFacing(player);

			String creatureString = args[0];
			if (creatureString.equalsIgnoreCase("all")) {
				// Get list of all creatures..anyone can do this
				showAllCreatures(player);
				return true;
			}

			boolean isEgg = false;

			if (creatureString.endsWith("egg")) {
				isEgg = true;
				creatureString = creatureString.replaceFirst("egg$", "");
			}

			if (!name2Eid.containsKey(creatureString)) {
				player.sendMessage("Unrecognized creature "+creatureString);
				return true;
			}

			short entityID = name2Eid.get(creatureString);

			if (block != null && !isEgg) {
				if (!hasPermission(player, "silkspawners.changetype")) {
					player.sendMessage("You do not have permission to change spawners with /spawner");
					return true;
				}

				setSpawnerType(block, entityID, player);
			} else {
				// Get free spawner item in hand
				if (!hasPermission(player, "silkspawners.freeitem")) {
					if (hasPermission(player, "silkspawners.viewtype")) {
						sender.sendMessage("You must be looking directly at a spawner to use this command");
					} else {
						sender.sendMessage("You do not have permission to use this command");
					}
					return true;
				}

				if (player.getItemInHand() != null && player.getItemInHand().getType() != Material.AIR) {
					sender.sendMessage("To use this command, empty your hand (to get a free spawner item) or point at an existing spawner (to change the spawner type)");
					return true;
				}

				if (isEgg) {
					player.setItemInHand(newEggItem(entityID));
					sender.sendMessage(getCreatureName(entityID) + " spawn egg");
				} else {
					player.setItemInHand(newSpawnerItem(entityID));
					sender.sendMessage(getCreatureName(entityID) + " spawner");
				}
			}
		}

		return true;
	}

	// Set spawner type from user
	public void setSpawnerType(Block block, short entityID, Player player) {
		if (!canBuildHere(player, block.getLocation())) {
			player.sendMessage("Changing spawner type denied by WorldGuard protection");
			return;
		}

		try {
			setSpawnerEntityID(block, entityID);
		} catch (Exception e) {
			informPlayer(player, "Failed to set type: " + e);
		}

		player.sendMessage(getCreatureName(entityID) + " spawner");
	}

	// Return the spawner block the player is looking at, or null if isn't
	// TODO Replace the config
	private Block getSpawnerFacing(Player player) {
		Block block = player.getTargetBlock(null, getConfig().getInt("spawnerCommandReachDistance", 6));
		if (block == null || block.getType() != Material.MOB_SPAWNER) {
			return null;
		}
		return block;
	}



	// Get a creature name suitable for displaying to the user
	// Internal mob names are are like 'LavaSlime', this will return
	// the in-game name like 'Magma Cube'
	public String getCreatureName(short entityID) {
		String displayName = eid2DisplayName.get(entityID);

		if (displayName == null) {
			EntityType ct = EntityType.fromId(entityID);
			if (ct != null) {
				displayName = "("+ct.getName()+")";
			} else {
				displayName = String.valueOf(entityID);
			}
		}

		return displayName;
	}

	public static ItemStack newEggItem(short entityID, int amount) {
		return new ItemStack(SPAWN_EGG, 1, entityID);
	}

	public static ItemStack newEggItem(short entityID) {
		return newEggItem(entityID, 1);
	}


	// Create a tagged a mob spawner item with it's entity ID so we know what it spawns
	// This is not part of vanilla, but our own convention
	// TODO Checked
	public static ItemStack newSpawnerItem(short entityID) {
		ItemStack item = new ItemStack(Material.MOB_SPAWNER, 1, entityID);
		// Tag the entity ID several ways, for compatibility

		// The way it should be stored
		item.setDurability(entityID);
		
		// TODO: Creaturebox compatibility
		// see http://dev.bukkit.org/server-mods/creaturebox/pages/trading-mob-spawners/
		//item.addUnsafeEnchantment(Enchantment.OXYGEN, entityID);
		
		
		// Second method, for old compat and to be sure!
		item.addUnsafeEnchantment(Enchantment.SILK_TOUCH, entityID);
		return item;
	}

	// Get the entity ID
	// TODO Checked
	public static short getStoredSpawnerItemEntityID(ItemStack item) {
		short id = item.getDurability();
		// Is it stored and working? Great return this!
		if (id != 0) return id;

		// TODO: compatibility with Creaturebox's 0-22
		/*
        id = (short)item.getEnchantmentLevel(Enchantment.OXYGEN);
        if (id != 0) {
            return id;
        }*/
		
		// Else use the enchantment
		id = (short) item.getEnchantmentLevel(Enchantment.SILK_TOUCH);
		if (id != 0) return id;
		// Return 0 -> should be default
		return 0;
	}

	public void showAllCreatures(Player player) {
		String message = "";
		for (String displayName: eid2DisplayName.values()) {
			displayName = displayName.replaceAll(" ", "");
			message += displayName + ", ";
		}
		message = message.substring(0, message.length() - ", ".length());
		player.sendMessage(message);
	}

	public void informPlayer(Player player, String message) {
		if (hasPermission(player, "silkspawners.info")) {
			player.sendMessage(message);
		}
	}

	// Return whether mob is recognized by Bukkit's wrappers
	public boolean isRecognizedMob(String mobID) {
		return EntityType.fromName(mobID) != null;
	}

	// Better methods for setting/getting spawner type
	// These don't rely on CreatureSpawner, if possible, and instead set/get the 
	// mobID directly from the tile entity

	public short getSpawnerEntityID(Block block) {
		BlockState blockState = block.getState();
		if (!(blockState instanceof CreatureSpawner)) {
			throw new IllegalArgumentException("getSpawnerEntityID called on non-spawner block: " + block);
		}

		CraftCreatureSpawner spawner = ((CraftCreatureSpawner)blockState);

		// Get the mob ID ourselves if we can
		if (tileField != null && mobIDField != null) {
			try {
				net.minecraft.server.TileEntityMobSpawner tile = (net.minecraft.server.TileEntityMobSpawner)tileField.get(spawner);
				//log.info("tile ="+tile);

				String mobID = (String)mobIDField.get(tile);
				//log.info("mobID ="+mobID);

				return mobID2Eid.get(mobID);
			} catch (Exception e) {
				log.info("Reflection failed: " + e);
				// fall through
			} 
		}

		// or ask Bukkit if we have to
		//int entityID = spawner.getSpawnedType().getTypeId();    // TODO: 1.1-R5
		return spawner.getSpawnedType().getTypeId();
	}

	public void setSpawnerEntityID(Block block, short entityID) {
		BlockState blockState = block.getState();
		if (!(blockState instanceof CreatureSpawner)) {
			throw new IllegalArgumentException("setSpawnerEntityID called on non-spawner block: " + block);
		}

		CraftCreatureSpawner spawner = ((CraftCreatureSpawner)blockState);

		// Try the more powerful native methods first
		if (tileField != null && mobIDField != null) {
			try {
				String mobID = eid2MobID.get(entityID);

				net.minecraft.server.TileEntityMobSpawner tile = (net.minecraft.server.TileEntityMobSpawner)tileField.get(spawner);
				//log.info("tile ="+tile);

				tile.a(mobID);      // MCP setMobID
				return;
			} catch (Exception e) {
				log.info("Reflection failed: " + e);
				// fall through
			}
		}

		// Fallback to wrapper
		EntityType ct = EntityType.fromId(entityID);
		if (ct == null) {
			throw new IllegalArgumentException("Failed to find creature type for "+entityID);
		}

		spawner.setSpawnedType(ct);
		//spawner.setSpawnedType(EntityType.fromId(entityID)); // TODO: 1.1-R5
		blockState.update();
	}

	/*
	 * WorldGuard stuff
	 * Allowed to build and enabled check
	 * http://wiki.sk89q.com/wiki/WorldGuard/Regions/API
	 */
	
	// Is WourldGuard enabled?
	// TODO Checked
	private WorldGuardPlugin getWorldGuard() {
		if (!getConfig().getBoolean("useWorldGuard")) return null;
		Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("WorldGuard");
		if (plugin == null || !(plugin instanceof WorldGuardPlugin)) return null;
		return (WorldGuardPlugin)plugin;
	}

	// Is the player allowed to build here?
	// TODO Checked
	public boolean canBuildHere(Player player, Location location) {
		WorldGuardPlugin wg = getWorldGuard();
		if (wg == null)	return true;
		return wg.canBuild(player, location);
	}
}


