package de.dustplanet.util;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.server.v1_4_R1.EntityTypes;
import net.minecraft.server.v1_4_R1.TileEntityMobSpawner;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_4_R1.block.CraftCreatureSpawner;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.getspout.spoutapi.player.SpoutPlayer;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;

import de.dustplanet.silkspawners.SilkSpawners;

/**
 * This is the util class where all the magic happens!
 * @author (former) mushroomhostage
 * @author xGhOsTkiLLeRx
 */

public class SilkUtil {
	// EntityID to displayName
	public ConcurrentHashMap<Short,String> eid2DisplayName = new ConcurrentHashMap<Short, String>();
	// EntityID to MobID
	public ConcurrentHashMap<Short,String> eid2MobID = new ConcurrentHashMap<Short, String>();
	// MobID to entityID
	public ConcurrentHashMap<String,Short> mobID2Eid = new ConcurrentHashMap<String, Short>();
	// Name to entityID
	public ConcurrentHashMap<String,Short> name2Eid = new ConcurrentHashMap<String, Short>();
	// Known entityIDs
	public ArrayList<Short> knownEids = new ArrayList<Short>();
	// Default is 90 = PIG
	// To prevent empty string use real ID and not 0 anymore
	public short defaultEntityID = 90;
	// Fields for reflection
	public Field tileField, mobIDField;
	// To avoid confusing with badly name MONSTER_EGGS (silverfish), set our own material
	public Material SPAWN_EGG = Material.MONSTER_EGG;
	// WorldGuard instance
	private WorldGuardPlugin wg;
	// Should we use the normal names or a colored one?
	public boolean coloredNames;
	// SilkSpawners instance, not neccessary
	private SilkSpawners plugin;

	public SilkUtil(SilkSpawners instance) {
		getWorldGuard(instance);
		plugin = instance;
	}

	public SilkUtil() {}

	public static SilkUtil hookIntoSilkSpanwers() {
		SilkSpawners plugin = (SilkSpawners) Bukkit.getPluginManager().getPlugin("SilkSpawners");
		if (plugin != null) return new SilkUtil(plugin);
		System.out.println("SilkSpawners instance not found, returning SilkUtil without SilkSpawners instance!");
		return new SilkUtil();
	}


	// Give a new SpawnerEgg with the given entityID
	public ItemStack newEggItem(short entityID, int amount) {
		return new ItemStack(SPAWN_EGG, 1, entityID);
	}

	public ItemStack newEggItem(short entityID) {
		return newEggItem(entityID, 1);
	}


	// Create a tagged a mob spawner item with it's entity ID so we know what it spawns
	public ItemStack newSpawnerItem(short entityID, String customName) {
		if (customName == null || customName.equalsIgnoreCase("")) customName = "Monster Spawner";
		ItemStack item = new ItemStack(Material.MOB_SPAWNER, 1, entityID);
		// Check if we need a colored name
		if (coloredNames) {
			ItemMeta meta = item.getItemMeta();
			meta.setDisplayName(ChatColor.translateAlternateColorCodes('\u0026', customName.replaceAll("%creature%", getCreatureName(entityID))));
			item.setItemMeta(meta);
		}
		
		// The way it should be stored (double sure!)
		item.setDurability(entityID);

		// Removed the old unsafeEnchantment method since BUKKIT-329 is fixed and it caused glowing issues
		// Due to this trading etc. was impossible
		return item;
	}

	// Get the entity ID
	public short getStoredSpawnerItemEntityID(ItemStack item) {
		short id = item.getDurability();
		// Is it stored and working? Great return this!
		// Should work again after BUKKIT-329
		if (id != 0) return id;

		// Else use the enchantment
		id = (short) item.getEnchantmentLevel(Enchantment.SILK_TOUCH);
		if (id != 0) return id;
		// Return 0 -> should be default (pig)
		return 0;
	}

	// Return whether mob is recognized by Bukkit's wrappers
	public boolean isRecognizedMob(String mobID) {
		return EntityType.fromName(mobID) != null;
	}
	
	// Check if the entityID is known or not
	public boolean isKnownEntityID(short entityID) {
		return knownEids.contains(entityID);
	}

	// Better methods for setting/getting spawner type
	// These don't rely on CreatureSpawner, if possible, and instead set/get the 
	// mobID directly from the tile entity
	public short getSpawnerEntityID(Block block) {
		BlockState blockState = block.getState();
		if (!(blockState instanceof CreatureSpawner)) {
			// Call it only on CreatureSpawners
			throw new IllegalArgumentException("getSpawnerEntityID called on non-spawner block: " + block);
		}
		// Get our spawner;
		CraftCreatureSpawner spawner = ((CraftCreatureSpawner) blockState);

		// Get the mob ID ourselves if we can
		if (tileField != null && mobIDField != null) {
			try {
				TileEntityMobSpawner tile = (TileEntityMobSpawner) tileField.get(spawner);
				// Get the name from the field of our spawner
				String mobID = (String) mobIDField.get(tile);
				return mobID2Eid.get(mobID);
			} catch (Exception e) {
				Bukkit.getServer().getLogger().info("Reflection failed: " + e);
				e.printStackTrace();
			} 
		}

		// Fallback to bukkit
		return spawner.getSpawnedType().getTypeId();
	}

	// Sets the creature of a spawner
	public void setSpawnerEntityID(Block block, short entityID) {
		BlockState blockState = block.getState();
		// Call it only on CreatureSpawners
		if (!(blockState instanceof CreatureSpawner)) {
			throw new IllegalArgumentException("setSpawnerEntityID called on non-spawner block: " + block);
		}
		// Get out spawner;
		CraftCreatureSpawner spawner = ((CraftCreatureSpawner) blockState);

		// Try the more powerful native methods first
		if (tileField != null && mobIDField != null) {
			try {
				// Get the name of the mob
				String mobID = eid2MobID.get(entityID);
				// Okay the spawner is not on our list [should NOT happen anymore]
				// Fallback then!
				if (mobID == null) mobID = getCreatureName(entityID);
				// uh still null, default [PIG]!
				if (mobID == null) mobID = getCreatureName((short) 90);
				// Refer to the NMS TileEntityMobSpawner and change the name, see
				// https://github.com/Bukkit/CraftBukkit/blob/master/src/main/java/net/minecraft/server/TileEntityMobSpawner.java#L32
				TileEntityMobSpawner tile = (TileEntityMobSpawner) tileField.get(spawner);
				tile.a(mobID);

				// Call an update
				blockState.update(true);
				return;
			}
			catch (Exception e) {
				// Fallback to bukkit;
				Bukkit.getServer().getLogger().info("Reflection failed: " + e);
				e.printStackTrace();
			}
		}

		// Fallback to wrapper
		// Get the entityType from the ID
		EntityType ct = EntityType.fromId(entityID);
		if (ct == null) {
			throw new IllegalArgumentException("Failed to find creature type for " + entityID);
		}
		// Set the spawner (less powerful)
		spawner.setSpawnedType(ct);
		// Update the spawner
		blockState.update(true);
	}

	// Set spawner type from user
	public void setSpawnerType(Block block, short entityID, Player player, String messageDenied) {
		// Changing denied by WorldGuard?
		if (!canBuildHere(player, block.getLocation())) {
			player.sendMessage(messageDenied);
			return;
		}
		// Set the spawner and message the player
		setSpawnerEntityID(block, entityID);
		player.sendMessage(getCreatureName(entityID) + " spawner");
	}

	public ItemStack setSpawnerType(ItemStack item, short entityID, String customName) {
		// Ensure that the name is correct
		if (customName == null || customName.equalsIgnoreCase("")) customName = "Monster Spawner";
		// Please eggs or spawners
		if (item == null || (item.getType() != Material.MOB_SPAWNER && item.getType() != SPAWN_EGG)) {
			return item;
		}
		// Case spawner
		if (item.getType() == Material.MOB_SPAWNER) {
			// Check if we should color
			if (coloredNames) {
				ItemMeta meta = item.getItemMeta();
				meta.setDisplayName(ChatColor.translateAlternateColorCodes('\u0026', customName.replaceAll("%creature%", getCreatureName(entityID))));
				item.setItemMeta(meta);
			}
		}
		// Case egg -> call normal method
		item.setDurability(entityID);
		return item;
	}

	// Return the spawner block the player is looking at, or null if isn't
	public Block getSpawnerFacing(Player player, int distance) {
		Block block = player.getTargetBlock(null, distance);
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
		// If the displayName is null go on (not on our list)
		if (displayName == null) {
			// Try to to get it from the EntityType
			EntityType ct = EntityType.fromId(entityID);
			// Case 1, found use the name method
			if (ct != null) displayName = ct.getName();
			// Case 2, not found -> use the number...
			else displayName = String.valueOf(entityID);
		}
		return displayName;
	}
	// Show them all the possible creature names
	public void showAllCreatures(CommandSender sender) {
		// For each entry in the list
		StringBuffer buf = new StringBuffer();
		for (String displayName: eid2DisplayName.values()) {
			displayName = displayName.replaceAll(" ", "");
			buf.append(displayName + ", ");
		}
		// Strip last comma out
		String message = buf.toString();
		message = message.substring(0, message.length() - ", ".length());
		sender.sendMessage(message);
	}

	@SuppressWarnings("unchecked")
	// Scan through all entities
	public SortedMap<Integer, String> scanEntityMap(String fieldValue) {
		SortedMap<Integer, String> sortedMap = new TreeMap<Integer, String>();
		// Use reflection to dump native EntityTypes
		// This bypasses Bukkit's wrappers, so it works with mods
		try {
			// https://github.com/Bukkit/mc-dev/blob/master/net/minecraft/server/EntityTypes.java#L21
			// f.put(s, Integer.valueOf(i)); --> Name of ID
			Field field = EntityTypes.class.getDeclaredField(fieldValue);
			field.setAccessible(true);
			Map<String, Integer> map = (Map<String, Integer>) field.get(null);
			// For each entry in our name -- ID map but it into the sortedMap
			for (Map.Entry<String,Integer> entry: ((Map<String,Integer>)map).entrySet()) {
				sortedMap.put(entry.getValue(), entry.getKey());
			}
		}
		// Fail
		catch (Exception e) {
			Bukkit.getServer().getLogger().severe("Failed to dump entity map: " + e);
			e.printStackTrace();
		}
		return sortedMap;
	}
	
	// Notify player
	// Warning: Don't call the method unless you have the SilkSpawners instance!
	public void notify(Player player, String spawnerName, short entityID) {
		// If we use Spout & the player Spoutcraft, send a notification (achievement like)
		if (plugin.spoutEnabled && ((SpoutPlayer) player).isSpoutCraftEnabled()) {
			((SpoutPlayer) player).sendNotification("Monster Spawner", spawnerName, Material.MOB_SPAWNER);
		}
		else {
			player.sendMessage(ChatColor.translateAlternateColorCodes('\u0026', plugin.localization.getString("informationOfSpawner1").replaceAll("%creature%", spawnerName).replaceAll("%ID%", Short.toString(entityID))));
			player.sendMessage(ChatColor.translateAlternateColorCodes('\u0026', plugin.localization.getString("informationOfSpawner2").replaceAll("%creature%", spawnerName).replaceAll("%ID%", Short.toString(entityID))));
			player.sendMessage(ChatColor.translateAlternateColorCodes('\u0026', plugin.localization.getString("informationOfSpawner3").replaceAll("%creature%", spawnerName).replaceAll("%ID%", Short.toString(entityID))));
		}
	}

	// Clear RAM
	public void clearAll() {
		eid2DisplayName.clear();
		eid2MobID.clear();
		mobID2Eid.clear();
		name2Eid.clear(); 
	}

	/*
	 * WorldGuard stuff
	 * Allowed to build and enabled check
	 * http://wiki.sk89q.com/wiki/WorldGuard/Regions/API
	 */

	// Is WourldGuard enabled?
	private void getWorldGuard(SilkSpawners plugin) {
		if (!plugin.config.getBoolean("useWorldGuard")) return;
		Plugin worldGuard = plugin.getServer().getPluginManager().getPlugin("WorldGuard");
		if (worldGuard == null || !(worldGuard instanceof WorldGuardPlugin)) return;
		wg = (WorldGuardPlugin) worldGuard;
	}

	// Is the player allowed to build here?
	public boolean canBuildHere(Player player, Location location) {
		if (wg == null)	return true;
		return wg.canBuild(player, location);
	}
}