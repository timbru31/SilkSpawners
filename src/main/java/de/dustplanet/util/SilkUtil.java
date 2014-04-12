package de.dustplanet.util;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.server.v1_7_R3.EntityTypes;
import net.minecraft.server.v1_7_R3.TileEntityMobSpawner;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_7_R3.block.CraftCreatureSpawner;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;

import de.dustplanet.silkspawners.SilkSpawners;

/**
 * This is the util class where all the magic happens!
 * 
 * @author (former) mushroomhostage
 * @author xGhOsTkiLLeRx
 */

public class SilkUtil {
    // EntityID to displayName
    /**
     * This HashMap is holding the entityID and the display name of each mob
     */
    public ConcurrentHashMap<Short, String> eid2DisplayName = new ConcurrentHashMap<Short, String>();

    // EntityID to MobID
    /**
     * This HashMap is holding the entityID and the internal Minecraft name of each mob
     */
    public ConcurrentHashMap<Short, String> eid2MobID = new ConcurrentHashMap<Short, String>();

    // MobID to entityID
    /**
     * This HashMap is holding the internal Minecraft name of each mob and the entityID
     */
    public ConcurrentHashMap<String, Short> mobID2Eid = new ConcurrentHashMap<String, Short>();

    // Name to entityID
    /**
     * This HashMap is holding the display name of each mob and the entityID
     */
    public ConcurrentHashMap<String, Short> name2Eid = new ConcurrentHashMap<String, Short>();

    // Known entityIDs
    /**
     * List of enabled (and therefore) known entityIDs
     */
    public ArrayList<Short> knownEids = new ArrayList<Short>();

    // Default is 90 = PIG
    // To prevent empty string use real ID and not 0 anymore
    /**
     * Default (fallback) entityID, standard is 90 the pig
     */
    public short defaultEntityID = 90;

    // Fields for reflection
    /**
     * Field used for reflection
     */
    public Field tileField;

    // To avoid confusing with badly name MONSTER_EGGS (silverfish), set our own
    // material
    /**
     * Custom name for the monster egg, to avoid the MONSTER_EGGS (silverfish)
     */
    public Material SPAWN_EGG = Material.MONSTER_EGG;

    // WorldGuard instance
    /**
     * WorldGuard instance, may be null
     */
    private WorldGuardPlugin wg;

    // Should we use the normal names or a colored one?
    /**
     * Boolean value to determine if spawner names should be colored
     */
    public boolean coloredNames;

    // SilkSpawners instance, not necessary
    /**
     * SilkSpawners instance
     */
    private SilkSpawners plugin;

    /**
     * Constructor to make your own SilkUtil instance
     * @param instance
     */
    public SilkUtil(SilkSpawners instance) {
	getWorldGuard(instance);
	plugin = instance;
    }

    /**
     * Constructor called without SilkSpawners instance, will
     * result in a lack of WorldGuard and other features
     */
    public SilkUtil() {
    }

    /**
     * This method will return the SilkUtil instance
     * @return SilkUtil instance
     */
    public static SilkUtil hookIntoSilkSpanwers() {
	SilkSpawners plugin = (SilkSpawners) Bukkit.getPluginManager().getPlugin("SilkSpawners");
	if (plugin != null) {
	    return new SilkUtil(plugin);
	}
	Bukkit.getLogger().warning("SilkSpawners instance not found, returning SilkUtil without SilkSpawners instance!");
	return new SilkUtil();
    }

    // Give a new SpawnerEgg with the given entityID
    /**
     * Returns a new ItemStack of a spawn egg with the specified amount and mob
     * @param entityID which mob should be spawned
     * @param amount the amount of spawn eggs
     * @return the ItemStack
     */
    public ItemStack newEggItem(short entityID, int amount) {
	return new ItemStack(SPAWN_EGG, amount, entityID);
    }

    /**
     * Returns a new ItemStack of a spawn egg with the amount one and mob
     * @param entityID which mob should be spawned
     * @return the ItemStack (amount is one)
     */
    public ItemStack newEggItem(short entityID) {
	return newEggItem(entityID, 1);
    }

    // Create a tagged a mob spawner item with it's entity ID and custom amount
    /**
     * This method will make a new MobSpawner with a custom entityID, name and amount
     * @param entityID the mob
     * @param customName if the MobSpawner should be named different
     * @param amount the wanted amount
     * @param force value even if coloredNames is disabled to ensure custom name
     * @return the ItemStack with the configured options
     */
    public ItemStack newSpawnerItem(short entityID, String customName, int amount, boolean force) {
	if (customName == null || customName.equalsIgnoreCase("")) {
	    customName = "Monster Spawner";
	}
	ItemStack item = new ItemStack(Material.MOB_SPAWNER, amount, entityID);
	// Check if we need a colored name
	if (coloredNames || force) {
	    ItemMeta meta = item.getItemMeta();
	    meta.setDisplayName(ChatColor.translateAlternateColorCodes('\u0026', customName).replace("%creature%", getCreatureName(entityID)));
	    item.setItemMeta(meta);
	}

	// The way it should be stored (double sure!)
	item.setDurability(entityID);

	// Removed the old unsafeEnchantment method since BUKKIT-329 is fixed
	// and it caused glowing issues
	// Due to this trading etc. was impossible
	return item;
    }
    
    // Create a new MobSpawner without boolean force 
    /**
     * @deprecated use {@link #newSpawnerItem(short, String, int, boolean)} instead. 
     */
    @Deprecated
    public ItemStack newSpawnerItem(short entityID, String customName, int amount) {
	return newSpawnerItem(entityID, customName, amount, false);
    }

    // Create a tagged mob spawner item with it's entityID and amount 1
    /**
     * @deprecated use {@link #newSpawnerItem(short, String, int, boolean)} instead
     */
    @Deprecated
    public ItemStack newSpawnerItem(short entityID, String customName) {
	return newSpawnerItem(entityID, customName, 1, false);
    }

    // Get the entity ID
    /**
     * Returns the entity ID of a spawner or spawn egg
     * @param item the ItemStack
     * @return the entityID
     */
    public short getStoredSpawnerItemEntityID(ItemStack item) {
	short id = item.getDurability();
	// Is it stored and working? Great return this!
	// Should work again after BUKKIT-329
	if (id != 0) {
	    return id;
	}

	// Else use the enchantment
	id = (short) item.getEnchantmentLevel(Enchantment.SILK_TOUCH);
	if (id != 0) {
	    return id;
	}
	// Return 0 -> should be default (pig)
	return 0;
    }

    // Return whether mob is recognized by Bukkit's wrappers
    /**
     * Lookup if the mob is know,
     * @param mobID the name (String) of the mob
     * @return the result, true or false
     */
    public boolean isRecognizedMob(String mobID) {
	return EntityType.fromName(mobID) != null;
    }

    // Check if the entityID is known or not
    /**
     * Lookup if the mob is known
     * @param entityID the ID (short) of the mob
     * @return the result, true of false
     */
    public boolean isKnownEntityID(short entityID) {
	return knownEids.contains(entityID);
    }

    // Better methods for setting/getting spawner type
    // These don't rely on CreatureSpawner, if possible, and instead set/get the
    // mobID directly from the tile entity
    /**
     * Returns the entity ID of a spawner (block)
     * @param block the spawner block
     * @return the entity ID
     */
    public short getSpawnerEntityID(Block block) {
	BlockState blockState = block.getState();
	if (!(blockState instanceof CreatureSpawner)) {
	    // Call it only on CreatureSpawners
	    Bukkit.getLogger().warning("getSpawnerEntityID called on non-spawner block: " + block);
	    return 0;
	}
	// Get our spawner;
	CraftCreatureSpawner spawner = ((CraftCreatureSpawner) blockState);

	// Get the mob ID ourselves if we can
	if (tileField != null) {
	    try {
		TileEntityMobSpawner tile = (TileEntityMobSpawner) tileField.get(spawner);
		// Get the name from the field of our spawner
		String mobID = tile.a().getMobName();
		// In case the block is not on our list try a fallback
		if (mobID != null && mobID2Eid.containsKey(mobID)) {
		    return mobID2Eid.get(mobID);
		}
	    } catch (Exception e) {
		Bukkit.getServer().getLogger().info("Reflection failed: " + e.getMessage());
		e.printStackTrace();
	    }
	}

	// Fallback to Bukkit
	return spawner.getSpawnedType().getTypeId();
    }

    // Sets the creature of a spawner
    /**
     * Set the specified MonterSpawner to another entity ID
     * @param block MonsterSpawner
     * @param entityID the wanted entityID
     */
    public void setSpawnerEntityID(Block block, short entityID) {
	BlockState blockState = block.getState();
	// Call it only on CreatureSpawners
	if (!(blockState instanceof CreatureSpawner)) {
	    Bukkit.getLogger().warning("setSpawnerEntityID called on non-spawner block: " + block);
	    return;
	}
	// Get out spawner;
	CraftCreatureSpawner spawner = ((CraftCreatureSpawner) blockState);

	// Try the more powerful native methods first
	if (tileField != null) {
	    try {
		// Get the name of the mob
		String mobID = eid2MobID.get(entityID);
		// Okay the spawner is not on our list [should NOT happen anymore]
		// Fallback then!
		if (mobID == null) {
		    mobID = getCreatureName(entityID);
		}
		// uh still null, default [PIG]!
		if (mobID == null) {
		    mobID = getCreatureName((short) 90);
		}

		// Refer to the NMS TileEntityMobSpawner and change the name,
		// see
		// https://github.com/SpigotMC/mc-dev/blob/master/net/minecraft/server/TileEntityMobSpawner.java#L37
		TileEntityMobSpawner tile = (TileEntityMobSpawner) tileField.get(spawner);
		tile.a().a(mobID);

		// Call an update (force it)
		blockState.update(true);
		return;
	    } catch (Exception e) {
		// Fallback to bukkit;
		Bukkit.getServer().getLogger().info("Reflection failed: " + e.getMessage());
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
    /**
     * Set a spawner (if allowed) to a new mob
     * @param block the MonsterSpawner
     * @param entityID the new entity ID
     * @param player the player
     * @param messageDenied the message which is shown, when the player can't build here
     * see {@link #canBuildHere(Player, Location)}
     */
    public void setSpawnerType(Block block, short entityID, Player player, String messageDenied) {
	// Changing denied by WorldGuard?
	if (!canBuildHere(player, block.getLocation())) {
	    player.sendMessage(messageDenied);
	    return;
	}
	// Set the spawner and message the player
	setSpawnerEntityID(block, entityID);
    }

    /**
     * Sets a spawner item or egg to a new ID
     * @param item ItemStack (Egg or Spawner)
     * @param entityID wanted entity ID
     * @param customName if a custom name should be used (null for none)
     * @return the updated ItemStack
     */
    public ItemStack setSpawnerType(ItemStack item, short entityID, String customName) {
	// Ensure that the name is correct
	if (customName == null || customName.equalsIgnoreCase("")) {
	    customName = "Monster Spawner";
	}
	// Please eggs or spawners
	if (item == null || (item.getType() != Material.MOB_SPAWNER && item.getType() != SPAWN_EGG)) {
	    return item;
	}
	// Case spawner
	if (item.getType() == Material.MOB_SPAWNER) {
	    // Check if we should color
	    if (coloredNames) {
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(ChatColor.translateAlternateColorCodes('\u0026', customName).replace("%creature%", getCreatureName(entityID)));
		item.setItemMeta(meta);
	    }
	}
	// Case egg -> call normal method
	item.setDurability(entityID);
	return item;
    }

    // Return the spawner block the player is looking at, or null if isn't
    /**
     * Return the spawner block the player is looking at, or null if isn't
     * @param player the player
     * @param distance the reach distance
     * @return the found block or null
     */
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
    /**
     * Get the creature name (display name) of an ID
     * @param entityID the entity ID
     * @return the displayname of the mob
     */
    public String getCreatureName(short entityID) {
	String displayName = eid2DisplayName.get(entityID);
	// If the displayName is null go on (not on our list)
	if (displayName == null) {
	    // Try to to get it from the EntityType
	    EntityType ct = EntityType.fromId(entityID);
	    // Case 1, found use the name method
	    if (ct != null) {
		displayName = ct.getName();
	    }
	    // Case 2, not found -> use the number...
	    else {
		displayName = String.valueOf(entityID);
	    }
	}
	return displayName;
    }

    // Show them all the possible creature names
    /**
     * Lists all enabled creatures to a CommandSender
     * @param sender CommandSender (player or console)
     */
    public void showAllCreatures(CommandSender sender) {
	// For each entry in the list
	StringBuilder builder = new StringBuilder();
	for (String displayName : eid2DisplayName.values()) {
	    displayName = displayName.replace(" ", "");
	    builder.append(displayName + ", ");
	}
	// Strip last comma out
	String message = builder.toString();
	message = message.substring(0, message.length() - ", ".length());
	sender.sendMessage(message);
    }

    // Scan through all entities
    /**
     * Use reflection to scan through each mob and the IDs/name
     * @return Map with a result of Integer (ID), String (name)
     */
    public SortedMap<Integer, String> scanEntityMap() {
	SortedMap<Integer, String> sortedMap = new TreeMap<Integer, String>();
	// Use reflection to dump native EntityTypes
	// This bypasses Bukkit's wrappers, so it works with mods
	try {
	    // https://github.com/SpigotMC/mc-dev/blob/master/net/minecraft/server/EntityTypes.java#L32
	    // g.put(s, Integer.valueOf(i)); --> Name of ID
	    Field field = EntityTypes.class.getDeclaredField("g");
	    field.setAccessible(true);
	    @SuppressWarnings("unchecked")
	    Map<String, Integer> map = (Map<String, Integer>) field.get(null);
	    // For each entry in our name -- ID map but it into the sortedMap
	    for (Map.Entry<String, Integer> entry : ((Map<String, Integer>) map).entrySet()) {
		sortedMap.put(entry.getValue(), entry.getKey());
	    }

	    // Let's scan for added entities by e.g MCPC+
	    for (EntityType type : EntityType.values()) {
		String name = type.getName();
		short id = type.getTypeId();
		// If name is not defined or ID -1 --> skip this bad entity
		if (name == null || id == -1) {
		    continue;
		}
		if (!sortedMap.containsKey((int) id)) {
		    sortedMap.put((int) id, name);
		}
	    }
	}
	// Fail
	catch (Exception e) {
	    Bukkit.getServer().getLogger().severe("Failed to dump entity map: " + e.getMessage());
	    e.printStackTrace();
	}
	return sortedMap;
    }

    // Notify player
    // Warning: Don't call the method unless you have the SilkSpawners instance!
    /**
     * Notify a player about the spawner
     * @param player the player
     * @param spawnerName the creature name
     * @param entityID the ID
     */
    public void notify(Player player, String spawnerName, short entityID) {
	player.sendMessage(ChatColor.translateAlternateColorCodes('\u0026',
		plugin.localization.getString("informationOfSpawner1")
		.replace("%ID%", Short.toString(entityID))).replace("%creature%", spawnerName));
	player.sendMessage(ChatColor.translateAlternateColorCodes('\u0026',
		plugin.localization.getString("informationOfSpawner2")
		.replace("%ID%", Short.toString(entityID))).replace("%creature%", spawnerName));
	player.sendMessage(ChatColor.translateAlternateColorCodes('\u0026',
		plugin.localization.getString("informationOfSpawner3")
		.replace("%ID%", Short.toString(entityID))).replace("%creature%", spawnerName));
    }

    // Clear RAM
    /**
     * This method clears all HashMaps and lists
     */
    public void clearAll() {
	eid2DisplayName.clear();
	eid2MobID.clear();
	mobID2Eid.clear();
	name2Eid.clear();
	knownEids.clear();
    }

    /**
     * Test a String if it ends with egg
     * @param creatureString the name
     * @return result, true or false
     */
    public boolean isEgg(String creatureString) {
	return creatureString.endsWith("egg");
    }

    /**
     * Check if given name is known or not
     * @param creatureString the mob name
     * @return the result, true of false
     */
    public boolean isUnkown(String creatureString) {
	return !name2Eid.containsKey(creatureString);
    }

    /**
     * Check if the given string is a number
     * @param number to check
     * @return number or not found -1
     */
    public short getNumber(String number) {
	try {
	    return Short.valueOf(number);
	} catch(NumberFormatException e) {
	    return -1;
	}
    }

    /**
     * Reduces the egg (item stack) by 1
     * @param player the player
     */
    public void reduceEggs(Player player) {
	ItemStack eggs = player.getItemInHand();
	// Make it empty
	if (eggs.getAmount() == 1) {
	    player.setItemInHand(null);
	} else {
	    // Reduce egg
	    eggs.setAmount((eggs.getAmount() - 1));
	    player.setItemInHand(eggs);
	}
    }

    /*
     * WorldGuard stuff Allowed to build and enabled check
     * http://wiki.sk89q.com/wiki/WorldGuard/Regions/API
     */

    // Is WourldGuard enabled?
    /**
     * Prepare for WorldGuard support
     * @param plugin SilkSpawners instance
     */
    private void getWorldGuard(SilkSpawners plugin) {
	if (!plugin.config.getBoolean("useWorldGuard", true)) {
	    return;
	}
	Plugin worldGuard = plugin.getServer().getPluginManager().getPlugin("WorldGuard");
	if (worldGuard == null || !(worldGuard instanceof WorldGuardPlugin)) {
	    return;
	}
	wg = (WorldGuardPlugin) worldGuard;
    }

    // Is the player allowed to build here?
    /**
     * Checks if a player can build here (WorldGuard)
     * @param player the player
     * @param location the location to check
     * @return the result, true or false
     */
    public boolean canBuildHere(Player player, Location location) {
	if (wg == null) {
	    return true;
	}
	return wg.canBuild(player, location);
    }
}
