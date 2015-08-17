package de.dustplanet.util;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;

import de.dustplanet.silkspawners.SilkSpawners;
import de.dustplanet.silkspawners.compat.api.NMSProvider;
import me.confuser.barapi.BarAPI;

/**
 * This is the util class where all the magic happens.
 *
 * @author (former) mushroomhostage
 * @author xGhOsTkiLLeRx
 */

public class SilkUtil {
    // EntityID to displayName
    /**
     * This HashMap is holding the entityID and the display name of each mob.
     */
    public Map<Short, String> eid2DisplayName = new ConcurrentHashMap<>();

    // EntityID to MobID
    /**
     * This HashMap is holding the entityID and the internal Minecraft name of each mob.
     */
    public Map<Short, String> eid2MobID = new ConcurrentHashMap<>();

    // MobID to entityID
    /**
     * This HashMap is holding the internal Minecraft name of each mob and the entityID.
     */
    public Map<String, Short> mobID2Eid = new ConcurrentHashMap<>();

    // Name to entityID
    /**
     * This HashMap is holding the display name of each mob and the entityID.
     */
    public Map<String, Short> name2Eid = new ConcurrentHashMap<>();

    // Known entityIDs
    /**
     * List of enabled (and therefore) known entityIDs.
     */
    public List<Short> knownEids = new ArrayList<>();

    // Default is 90 = PIG
    // To prevent empty string use real ID and not 0 anymore
    /**
     * Default (fallback) entityID, standard is 90 the pig.
     */
    private short defaultEntityID = EntityType.PIG.getTypeId();

    // To avoid confusing with badly name MONSTER_EGGS (silverfish), set our own
    // material
    /**
     * Custom name for the monster egg, to avoid the MONSTER_EGGS (silverfish).
     */
    public static final Material SPAWN_EGG = Material.MONSTER_EGG;

    /**
     * Boolean toogle for reflection.
     */
    private boolean useReflection = true;

    // WorldGuard instance
    /**
     * WorldGuard instance, may be null.
     */
    private WorldGuardPlugin wg;

    /**
     * BarAPI usage toggle.
     */
    private boolean barAPI;

    /**
     * SilkSpawners instance.
     */
    private SilkSpawners plugin;

    /**
     * NMSHandler instance.
     */
    public NMSProvider nmsProvider;

    /**
     * Constructor to make your own SilkUtil instance.
     * @param instance
     */
    public SilkUtil(SilkSpawners instance) {
        getWorldGuard(instance);
        plugin = instance;
        setupNMSProvider();
        load();
    }

    /**
     * This method will return the SilkUtil instance.
     * @return SilkUtil instance
     */
    public static SilkUtil hookIntoSilkSpanwers() {
        SilkSpawners plugin = (SilkSpawners) Bukkit.getPluginManager().getPlugin("SilkSpawners");
        return new SilkUtil(plugin);
    }

    /**
     * Define which Minecraft version needs to be loaded.
     */
    private boolean setupNMSProvider() {
        String version = plugin.getVersion();

        try {
            // Check if we have a NMSHandler class at that location
            final Class<?> clazz = Class.forName("de.dustplanet.silkspawners.compat." + version + ".NMSHandler");
            // Get the last element of the package
            if (NMSProvider.class.isAssignableFrom(clazz)) {
                // Set our handler
                nmsProvider = (NMSProvider) clazz.getConstructor().newInstance();
                plugin.getLogger().info("Loading support for " + version);
                return true;
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
            e.printStackTrace();
            plugin.getLogger().severe("Could not find support for this CraftBukkit version.");
            plugin.getLogger().info("Check for updates at http://dev.bukkit.org/bukkit-plugins/silkspawners/");
            plugin.getLogger().info("Disabling SilkSpawners now!");
            clearAll();
            plugin.shutdown();
        }
        return false;
    }

    /**
     * Loads the defaults
     */
    public void load() {
        // Should we display more information
        boolean verbose = plugin.getConfig().getBoolean("verboseConfig", false);

        // Scan the entities
        SortedMap<Integer, String> sortedMap = scanEntityMap();
        if (verbose) {
            plugin.getLogger().info("Scanning the mobs");
        }
        for (Map.Entry<Integer, String> entry : sortedMap.entrySet()) {
            // entity ID used for spawn eggs
            short entityID = (short) (int) entry.getKey();
            // internal mod ID used for spawner type
            String mobID = entry.getValue();
            // bukkit's wrapper enum
            EntityType bukkitEntity = EntityType.fromId(entityID);
            Class<? extends Entity> bukkitEntityClass = bukkitEntity == null ? null : bukkitEntity.getEntityClass();

            // Lookup creature info
            boolean enable = plugin.getConfig().getBoolean("enableCreatureDefault", true);
            // Check if already known
            if (!plugin.getMobs().contains("creatures." + mobID)) {
                plugin.getLogger().info("Entity " + entityID + "/" + mobID + " is not in the config. Adding...");
                plugin.getMobs().addDefault("creatures." + mobID + ".enable", enable);
                plugin.getMobs().save();
            } else {
                enable = plugin.getMobs().getBoolean("creatures." + mobID + ".enable", enable);
            }
            if (!enable) {
                if (verbose) {
                    plugin.getLogger().info("Entity " + entityID + " = " + mobID + "/"
                            + bukkitEntity + "[" + bukkitEntityClass
                            + "] (disabled)");
                }
                continue;
            }

            // Add the known ID [we omit all disabled entities]
            knownEids.add(entityID);
            // Put the different value in our lists
            eid2MobID.put(entityID, mobID);
            mobID2Eid.put(mobID, entityID);

            // In-game name for user display, and other recognized names for
            // user input lookup
            String displayName = plugin.getMobs().getString("creatures." + mobID + ".displayName");
            if (displayName == null) {
                displayName = mobID;
            }
            // Add it the the list
            eid2DisplayName.put(entityID, displayName);

            // Get our lit of aliases
            List<String> aliases = plugin.getMobs().getStringList("creatures." + mobID + ".aliases");
            // Get the name, make it lowercase and strip out the spaces
            aliases.add(displayName.toLowerCase().replace(" ", ""));
            // Add the internal name
            aliases.add(mobID.toLowerCase().replace(" ", ""));
            // Add the ID
            aliases.add(Short.toString(entityID));
            // Add it to our names and ID list
            for (String alias : aliases) {
                name2Eid.put(alias, entityID);
            }

            // Detailed message
            if (verbose) {
                plugin.getLogger().info("Entity " + entityID + " = " + mobID + "/"
                        + bukkitEntity + "[" + bukkitEntityClass
                        + "] (display name: " + displayName
                        + ", aliases: " + aliases + ")");
            }
        }

        // Set the defaultID for spawners (-> memo, on some spawners it seems 0
        // -> pig is 90)
        setDefaultEntityID(EntityType.PIG.getTypeId());

        // Should we use something else as the default?
        if (plugin.getConfig().contains("defaultCreature")) {
            // Lowercase is better to search
            String defaultCreatureString = plugin.getConfig().getString("defaultCreature", "90").toLowerCase();
            // Try IDs first, may fail, use name then!
            try {
                short entityID = Short.valueOf(defaultCreatureString);
                // Known ID and MobName? Yes -> We use it
                if (isKnownEntityID(entityID) && isRecognizedMob(getCreatureName(entityID))) {
                    defaultCreatureString = getCreatureName(entityID).toLowerCase();
                }
            } catch (NumberFormatException e) {
                // Name then
            }
            // If we know the internal name
            if (name2Eid.containsKey(defaultCreatureString)) {
                // Get our entityID
                short defaultEid = name2Eid.get(defaultCreatureString);
                // Change default
                setDefaultEntityID(defaultEid);
                if (verbose) {
                    plugin.getLogger().info("Default monster spawner set to " + eid2DisplayName.get(defaultEid));
                }
            } else {
                // Unknown, fallback
                plugin.getLogger().warning("Invalid creature type: " + defaultCreatureString + ", default monster spawner fallback to PIG");
            }
        }

        // Are we allowed to use native methods?
        if (!plugin.getConfig().getBoolean("useReflection", true)) {
            setUseReflection(false);
        }

        if (verbose) {
            plugin.getLogger().info("Reflection is " + isUsingReflection());
        }

        // Optionally make spawners unstackable in an attempt to be more
        // compatible with CraftBukkit forks which may conflict
        // Requested on http://dev.bukkit.org/server-mods/silkspawners/#c25
        if (plugin.getConfig().getBoolean("spawnersUnstackable", false)) {
            nmsProvider.setSpawnersUnstackable();
        }
    }

    /**
     * Receives the default entityID.
     * @return the default entityID defined by SilkSpawners.
     */
    public short getDefaultEntityID() {
        return defaultEntityID;
    }

    /**
     * Sets the default entityID.
     * @param defaultEntityID short value of the default mob
     */
    public void setDefaultEntityID(short defaultEntityID) {
        this.defaultEntityID = defaultEntityID;
    }

    /**
     * Returns if SilkUtil is using reflection.
     * @return true for reflection, false for not
     */
    public boolean isUsingReflection() {
        return useReflection;
    }

    /**
     * Set if SilkUtil should use reflection.
     * @param useReflection true or false
     */
    public void setUseReflection(boolean useReflection) {
        this.useReflection = useReflection;
    }

    /**
     * Returns if BarAPI is used.
     * @return whether BarAPI is used or not
     */
    public boolean isBarAPI() {
        return barAPI;
    }

    /**
     * Sets if BarAPI should be used or not.
     * @param barAPI true or false
     */
    public void setBarAPI(boolean barAPI) {
        this.barAPI = barAPI;
    }

    // Give a new SpawnerEgg with the given entityID
    /**
     * Returns a new ItemStack of a spawn egg with the specified amount and mob.
     * @param entityID which mob should be spawned
     * @param amount the amount of spawn eggs
     * @return the ItemStack
     */
    public ItemStack newEggItem(short entityID, int amount) {
        return new ItemStack(SPAWN_EGG, amount, entityID);
    }

    /**
     * Returns a new ItemStack of a spawn egg with the amount one and mob.
     * @param entityID which mob should be spawned
     * @return the ItemStack (amount is one)
     */
    public ItemStack newEggItem(short entityID) {
        return newEggItem(entityID, 1);
    }

    // Create a tagged a mob spawner item with it's entity ID and custom amount
    /**
     * This method will make a new MobSpawner with a custom entityID, name and amount.
     * @param entityID the mob
     * @param customName if the MobSpawner should be named different
     * @param amount the wanted amount
     * @param forceLore whether the lore tag should be forces
     * @return the ItemStack with the configured options
     */
    public ItemStack newSpawnerItem(short entityID, String customName, int amount, boolean forceLore) {
        if (customName == null || customName.isEmpty()) {
            customName = "Monster Spawner";
        }
        ItemStack item = new ItemStack(Material.MOB_SPAWNER, amount, entityID);
        ItemMeta meta = item.getItemMeta();
        // Check if we need a colored name
        if (!customName.equalsIgnoreCase("Monster Spawner")) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('\u0026', customName).replace("%creature%", getCreatureName(entityID)).replace("%entityID%", Short.toString(entityID)));
        }

        // The way it should be stored (double sure!)
        item.setDurability(entityID);

        // 1.8 broke durability, workaround is the lore
        if ((forceLore || !isUsingReflection()) && plugin.config.getBoolean("useMetadata", true)) {
            ArrayList<String> lore = new ArrayList<>();
            lore.add("entityID:" + entityID);
            meta.setLore(lore);
        }
        item.setItemMeta(meta);

        return nmsProvider.setNBTEntityID(item, entityID, eid2MobID.get(entityID));
    }

    // Create a new MobSpawner without and ignore (old) force value
    /**
     * @deprecated use {@link #newSpawnerItem(short, String, int, boolean)} instead.
     */
    @Deprecated
    public ItemStack newSpawnerItem(short entityID, String customName, int amount) {
        return newSpawnerItem(entityID, customName, amount, false);
    }

    // Create a tagged mob spawner item with it's entityID and amount 1
    /**
     * @deprecated use {@link #newSpawnerItem(short, String, int, boolean)} instead.
     */
    @Deprecated
    public ItemStack newSpawnerItem(short entityID, String customName) {
        return newSpawnerItem(entityID, customName, 1, false);
    }

    // Get the entity ID
    /**
     * Returns the entity ID of a spawner or spawn egg.
     * @param item the ItemStack
     * @return the entityID
     */
    public short getStoredSpawnerItemEntityID(ItemStack item) {
        short durability = item.getDurability();
        // Try durability first, works until 1.8
        if (durability != 0) {
            return durability;
        }
        short entityID = 0;
        if (isUsingReflection()) {
            // Now try reflection for NBT tag
            entityID = nmsProvider.getSilkSpawnersNBTEntityID(item);
            if (entityID != 0) {
                return entityID;
            }
            String entity = nmsProvider.getVanillaNBTEntityID(item);
            if (entity != null &&  mobID2Eid.containsKey(entity)) {
                return mobID2Eid.get(entity);
            }
        }
        // If we still haven't found our entityID, then check for item lore or name
        if (item.hasItemMeta()) {
            ItemMeta meta = item.getItemMeta();
            if (plugin.config.getBoolean("useMetadata", true) && meta.hasLore() && !meta.getLore().isEmpty()) {
                for (String entityIDString : meta.getLore()) {
                    if (!entityIDString.contains("entityID")) {
                        // Continue if the lore does not contain entityID
                        continue;
                    }
                    String[] entityIDArray = entityIDString.split(":");
                    if (entityIDArray.length == 2) {
                        try {
                            durability = Short.valueOf(entityIDArray[1]);
                            if (durability != 0) {
                                return durability;
                            }
                        } catch (NumberFormatException e) {
                            return 0;
                        }
                    }
                }
            }
            // Last call - legacy mapping of the name. // TODO remove when applicable
            if (plugin.config.getBoolean("useLegacyName", false) && meta.hasDisplayName()) {
                String displayName = meta.getDisplayName();
                if (displayName != null && !displayName.isEmpty()) {
                    String[] nameParts = ChatColor.stripColor(displayName.toLowerCase()).split(" ");
                    for (String part : nameParts) {
                        // Continue if 'spawner' is matched
                        if (part.equalsIgnoreCase("spawner")) {
                            continue;
                        }
                        if (isKnown(part)) {
                            return name2Eid.get(part);
                        }
                    }
                }
            }
        }
        return 0; // :(
    }

    // Return whether mob is recognized by Bukkit's wrappers
    /**
     * Lookup if the mob is known.
     * @param mobID the name (String) of the mob
     * @return the result, true or false
     */
    public boolean isRecognizedMob(String mobID) {
        return EntityType.fromName(mobID) != null;
    }

    // Check if the entityID is known or not
    /**
     * Lookup if the mob is known.
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
     * Returns the entity ID of a spawner (block).
     * @param block the spawner block
     * @return the entity ID
     */
    public short getSpawnerEntityID(Block block) {
        BlockState blockState = block.getState();
        if (!(blockState instanceof CreatureSpawner)) {
            // Call it only on CreatureSpawners
            plugin.getLogger().warning("getSpawnerEntityID called on non-spawner block: " + block);
            return 0;
        }

        if (isUsingReflection()) {
            String mobID = nmsProvider.getMobNameOfSpawner(blockState);
            // In case the block is not on our list try a fallback
            if (mobID != null && mobID2Eid.containsKey(mobID)) {
                return mobID2Eid.get(mobID);
            }
        }

        // Fallback to Bukkit
        return ((CreatureSpawner) blockState).getSpawnedType().getTypeId();
    }

    // Sets the creature of a spawner
    /**
     * Set the specified MonterSpawner to another entity ID.
     * @param block MonsterSpawner
     * @param entityID the wanted entityID
     */
    public void setSpawnerEntityID(Block block, short entityID) {
        BlockState blockState = block.getState();
        // Call it only on CreatureSpawners
        if (!(blockState instanceof CreatureSpawner)) {
            plugin.getLogger().warning("setSpawnerEntityID called on non-spawner block: " + block);
            return;
        }

        // Try the more powerful native methods first
        if (isUsingReflection()) {
            // Get the name of the mob
            String mobID = eid2MobID.get(entityID);
            // Okay the spawner is not on our list [should NOT happen anymore]
            // Fallback then!
            if (mobID == null) {
                mobID = getCreatureName(entityID);
            }
            // uh still null, default [PIG]!
            if (mobID == null) {
                mobID = getCreatureName(defaultEntityID);
            }

            // Successful? Stop here
            if (nmsProvider.setMobNameOfSpawner(blockState, mobID)) {
                // Call an update (force it)
                blockState.update(true);
                return;
            }
        }

        // Fallback to wrapper
        // Get the entityType from the ID
        EntityType ct = EntityType.fromId(entityID);
        if (ct == null) {
            throw new IllegalArgumentException("Failed to find creature type for " + entityID);
        }
        // Set the spawner (less powerful)
        ((CreatureSpawner) blockState).setSpawnedType(ct);
        // Update the spawner
        blockState.update(true);
    }

    // Set spawner type from user
    /**
     * Set a spawner (if allowed) to a new mob.
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
     * Sets a spawner item or egg to a new ID.
     * @param item ItemStack (Egg or Spawner)
     * @param entityID wanted entity ID
     * @param customName if a custom name should be used (null for none)
     * @return the updated ItemStack
     */
    public ItemStack setSpawnerType(ItemStack item, short entityID, String customName) {
        // Ensure that the name is correct
        if (customName == null || customName.isEmpty()) {
            customName = "Monster Spawner";
        }
        // Please eggs or spawners
        if (item == null || item.getType() != Material.MOB_SPAWNER && item.getType() != SPAWN_EGG) {
            return item;
        }
        ItemMeta meta = item.getItemMeta();
        // Case spawner and check if we should color
        if (item.getType() == Material.MOB_SPAWNER && !customName.equalsIgnoreCase("Monster Spawner")) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('\u0026', customName).replace("%creature%", getCreatureName(entityID)));
        }

        // 1.8 broke durability, workaround is the lore
        if (!isUsingReflection() && plugin.config.getBoolean("useMetadata", true)) {
            ArrayList<String> lore = new ArrayList<>();
            lore.add("entityID:" + entityID);
            meta.setLore(lore);
        }

        // Does the item (e.g. crafted) as a lore and we set the NBT tag? Remove it
        if (isUsingReflection() && meta.hasLore()) {
            List<String> lore = meta.getLore();
            Iterator<String> it = lore.iterator();
            while (it.hasNext()) {
                if (it.next().contains("entityID")) {
                    it.remove();
                }
            }
            meta.setLore(lore);
        }
        item.setItemMeta(meta);

        // Case egg -> call normal method
        item.setDurability(entityID);

        return nmsProvider.setNBTEntityID(item, entityID, eid2MobID.get(entityID));
    }

    // Get a creature name suitable for displaying to the user
    // Internal mob names are are like 'LavaSlime', this will return
    // the in-game name like 'Magma Cube'
    /**
     * Get the creature name (display name) of an ID.
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
            } else {
                // Case 2, not found -> use the number...
                displayName = String.valueOf(entityID);
            }
        }
        return displayName;
    }

    // Show them all the possible creature names
    /**
     * Lists all enabled creatures to a CommandSender.
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
     * Use reflection to scan through each mob and the IDs/name.
     * @return Map with a result of Integer (ID), String (name)
     */
    public SortedMap<Integer, String> scanEntityMap() {
        SortedMap<Integer, String> sortedMap = nmsProvider.rawEntityMap();
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
        return sortedMap;
    }

    // Notify player
    /**
     * Notify a player about the spawner.
     * @param player the player
     * @param spawnerName the creature name
     * @param entityID the ID
     */
    public void notify(Player player, String spawnerName, short entityID) {
        if (isBarAPI()) {
            String shortInfo = ChatColor.translateAlternateColorCodes('\u0026',
                    plugin.localization.getString("informationOfSpawnerBar")
                    .replace("%ID%", Short.toString(entityID)).replace("%creature%", spawnerName));
            // Old bars will be overridden
            BarAPI.setMessage(player, shortInfo, plugin.config.getInt("barAPI.displayTime", 3));
        } else {
            player.sendMessage(ChatColor.translateAlternateColorCodes('\u0026',
                    plugin.localization.getString("informationOfSpawner1")
                    .replace("%ID%", Short.toString(entityID)).replace("%creature%", spawnerName)));
            player.sendMessage(ChatColor.translateAlternateColorCodes('\u0026',
                    plugin.localization.getString("informationOfSpawner2")
                    .replace("%ID%", Short.toString(entityID)).replace("%creature%", spawnerName)));
            player.sendMessage(ChatColor.translateAlternateColorCodes('\u0026',
                    plugin.localization.getString("informationOfSpawner3")
                    .replace("%ID%", Short.toString(entityID)).replace("%creature%", spawnerName)));
        }
    }

    // Clear RAM
    /**
     * This method clears all HashMaps and lists.
     */
    public void clearAll() {
        eid2DisplayName.clear();
        eid2MobID.clear();
        mobID2Eid.clear();
        name2Eid.clear();
        knownEids.clear();
    }

    /**
     * Test a String if it ends with egg.
     * @param creatureString the name
     * @return result, true or false
     */
    public boolean isEgg(String creatureString) {
        return creatureString.endsWith("egg");
    }

    /**
     * Check if given creature name is known or not. Aliases are supported, too
     * @param creatureString the mob name
     * @return the result, true of false
     */
    public boolean isUnkown(String creatureString) {
        return !name2Eid.containsKey(creatureString);
    }

    /**
     * Check if given creature name is known or not. Aliases are supported, too.
     * @param creatureString the mob name
     * @return the result, true of false
     */
    public boolean isKnown(String creatureString) {
        return name2Eid.containsKey(creatureString);
    }

    /**
     * Check if the given string is a number.
     * @param number to check
     * @return number or not found -1
     */
    public short getNumber(String number) {
        try {
            return Short.valueOf(number);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    /**
     * Reduces the egg (item stack) by 1.
     * @param player the player
     */
    public void reduceEggs(Player player) {
        ItemStack eggs = player.getItemInHand();
        // Make it empty
        if (eggs.getAmount() == 1) {
            player.setItemInHand(null);
        } else {
            // Reduce egg
            eggs.setAmount(eggs.getAmount() - 1);
            player.setItemInHand(eggs);
        }
    }


    // Checks if the given ItemStack has got the SilkTouch
    /**
     * Checks if a given ItemStack has a high enough SilkTouch level.
     * @param tool ItemStack to check
     * @return the result if the tool hasSilkTouch
     */
    public boolean hasSilkTouch(ItemStack tool) {
        int minLevel = plugin.config.getInt("minSilkTouchLevel", 1);
        // Always have it
        if (minLevel == 0) {
            return true;
        }
        // No silk touch fists..
        if (tool == null) {
            return false;
        }

        // This check isn't actually necessary, since containsEnchantment just
        // checks level>0,
        // but is kept here for clarity, and in case Bukkit allows level-0
        // enchantments like vanilla
        if (!tool.containsEnchantment(Enchantment.SILK_TOUCH)) {
            return false;
        }
        // Return if the level is enough
        return tool.getEnchantmentLevel(Enchantment.SILK_TOUCH) >= minLevel;
    }

    /**
     * Get the spawner name, specified for each mob or default.
     * from localization.yml
     * @param mobName the internal name the spawner name is wanted for
     * @return the found string
     */
    public String getCustomSpawnerName(String mobName) {
        if (plugin.mobs.contains("creatures." + mobName + ".spawnerName")) {
            return ChatColor.translateAlternateColorCodes('&', plugin.mobs.getString("creatures." + mobName + ".spawnerName", "Monster Spawner"));
        }
        return ChatColor.translateAlternateColorCodes('&', plugin.localization.getString("spawnerName", "Monster Spawner"));
    }

    /**
     * Get the player regardless of UUID or name supplied.
     * @param playerUUIDOrName a String with a name or UUID
     * @return the player if found otherwise null
     */
    public Player getPlayer(String playerUUIDOrName) {
        try {
            // Try if the String could be an UUID
            UUID playerUUID = UUID.fromString(playerUUIDOrName);
            return plugin.getServer().getPlayer(playerUUID);
        } catch (NoSuchMethodError | IllegalArgumentException e) {
            // Use deprecated method as a fallback.
            // Potential candidate to be used in NMS interface.
            // Old (1.6 e.g.) versions do not support UUID and return an Array, not a Collection
            // That's why plugin.getServer().getOnlinePlayers() doesn't work.
            return plugin.getServer().getPlayerExact(playerUUIDOrName);
        }
    }

    /*
     * WorldGuard stuff
     * Enabled check and build permission check
     * http://wiki.sk89q.com/wiki/WorldGuard/Regions/API
     */

    // Is WorldGuard enabled?
    /**
     * Prepare for WorldGuard support.
     * @param plugin SilkSpawners instance
     */
    private void getWorldGuard(SilkSpawners silkSpawnersPlugin) {
        if (!silkSpawnersPlugin.config.getBoolean("useWorldGuard", true)) {
            silkSpawnersPlugin.getLogger().info("WorldGuard support is disabled due to config setting");
            return;
        }
        Plugin worldGuard = silkSpawnersPlugin.getServer().getPluginManager().getPlugin("WorldGuard");
        if (worldGuard == null || !(worldGuard instanceof WorldGuardPlugin)) {
            silkSpawnersPlugin.getLogger().info("WorldGuard was not found and support is disabled");
            return;
        }
        silkSpawnersPlugin.getLogger().info("WorldGuard was found and support is enabled");
        wg = (WorldGuardPlugin) worldGuard;
    }

    // Is the player allowed to build here?
    /**
     * Checks if a player can build here (WorldGuard).
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
