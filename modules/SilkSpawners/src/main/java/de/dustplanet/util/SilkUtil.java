package de.dustplanet.util;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.annotation.Nullable;

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

import com.google.common.base.CaseFormat;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;

import de.dustplanet.silkspawners.SilkSpawners;
import de.dustplanet.silkspawners.compat.api.NMSProvider;
import lombok.Getter;
import lombok.Setter;
import me.confuser.barapi.BarAPI;

/**
 * This is the util class where all the magic happens.
 *
 * @author (former) mushroomhostage
 * @author xGhOsTkiLLeRx
 */

public class SilkUtil {
    /**
     * This HashMap is holding the internal Minecraft name of each mob and the display name of each mob.
     */
    @Getter
    private Map<String, String> mobIDToDisplayName = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    /**
     * This HashMap is holding the display name and aliases of each mob and the internal Minecraft name.
     */
    @Getter
    private Map<String, String> displayNameToMobID = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    /**
     * List of enabled (and therefore) known entities.
     */
    @Getter
    private List<String> knownEntities = new ArrayList<>();

    /**
     * Default (fallback) entityID, standard is the pig.
     */
    private String defaultEntityID = EntityType.PIG.getName();

    /**
     * Boolean toggle for reflection.
     */
    private boolean useReflection = true;

    /**
     * WorldGuard instance, may be null.
     */
    private WorldGuardPlugin wg;

    /**
     * BarAPI usage toggle.
     */
    @Getter
    @Setter
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
     *
     * @param instance SilkSpawners instance
     */
    public SilkUtil(SilkSpawners instance) {
        if (instance == null) {
            Bukkit.getLogger().severe("SilkSpawners - Nag API user: Don't initialize SilkUtil without a SilkSpawners instance!");
            instance = (SilkSpawners) Bukkit.getPluginManager().getPlugin("SilkSpawners");
        }
        plugin = instance;
        getWorldGuard();
        boolean nmsProviderFound = setupNMSProvider();
        if (nmsProviderFound) {
            load();
        }
    }

    /**
     * This method will return the SilkUtil instance.
     *
     * @return SilkUtil instance
     */
    public static SilkUtil hookIntoSilkSpanwers() {
        SilkSpawners plugin = (SilkSpawners) Bukkit.getPluginManager().getPlugin("SilkSpawners");
        if (plugin == null || plugin.getConfig() == null) {
            Bukkit.getLogger()
                    .severe("SilkSpawners is not yet ready, have you called SilkUtil.hookIntoSilkSpanwers() before your onEnable()?");
            return null;
        }
        return new SilkUtil(plugin);
    }

    /**
     * Define which Minecraft version needs to be loaded.
     *
     * @return true if a valid NMSHandler could be found, false for not
     */
    private boolean setupNMSProvider() {
        String version = plugin.getNMSVersion();

        // Rare cases might trigger API usage before SilkSpawners
        if (version == null) {
            String packageName = Bukkit.getServer().getClass().getPackage().getName();
            version = (packageName.substring(packageName.lastIndexOf('.') + 1));
        }

        try {
            final Class<?> clazz = Class.forName("de.dustplanet.silkspawners.compat." + version + ".NMSHandler");
            if (NMSProvider.class.isAssignableFrom(clazz)) {
                nmsProvider = (NMSProvider) clazz.getConstructor().newInstance();
                plugin.getLogger().info("Loading support for " + version);
                return true;
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException | NoSuchMethodException | SecurityException e) {
            e.printStackTrace();
            plugin.getLogger().severe("Could not find support for this CraftBukkit version.");
            plugin.getLogger().info("Check for updates at https://dev.bukkit.org/projects/silkspawners/");
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
        List<String> entities = scanEntityMap();
        if (verbose) {
            plugin.getLogger().info("Scanning the mobs");
        }
        for (String entityID : entities) {
            EntityType bukkitEntity = EntityType.fromName(entityID);
            Class<? extends Entity> bukkitEntityClass = bukkitEntity == null ? null : bukkitEntity.getEntityClass();

            boolean enable = plugin.getConfig().getBoolean("enableCreatureDefault", true);
            if (!plugin.getMobs().contains("creatures." + entityID)) {
                plugin.getLogger().info("Entity " + entityID + " is not in the config. Adding...");
                plugin.getMobs().addDefault("creatures." + entityID + ".enable", enable);
                plugin.getMobs().save();
            } else {
                enable = plugin.getMobs().getBoolean("creatures." + entityID + ".enable", enable);
            }
            if (!enable) {
                if (verbose) {
                    plugin.getLogger().info("Entity " + entityID + " = " + bukkitEntity + "[" + bukkitEntityClass + "] (disabled)");
                }
                continue;
            }

            // Add the known ID [we omit all disabled entities]
            knownEntities.add(entityID);

            String displayName = plugin.getMobs().getString("creatures." + entityID + ".displayName");
            if (displayName == null) {
                displayName = entityID;
            }

            mobIDToDisplayName.put(entityID, displayName);

            List<String> aliases = plugin.getMobs().getStringList("creatures." + entityID + ".aliases");
            aliases.add(displayName.toLowerCase(Locale.ENGLISH).replace(" ", ""));
            aliases.add(displayName.toLowerCase(Locale.ENGLISH).replace(" ", "_"));
            aliases.add(entityID.toLowerCase(Locale.ENGLISH).replace(" ", ""));
            aliases.add(entityID.toLowerCase(Locale.ENGLISH).replace(" ", "_"));
            aliases.add(entityID.toLowerCase(Locale.ENGLISH).replace("_", ""));
            aliases.add(CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, entityID));
            if (bukkitEntity != null) {
                aliases.add(bukkitEntity.name().toLowerCase(Locale.ENGLISH).replace(" ", ""));
                aliases.add(bukkitEntity.name().toLowerCase(Locale.ENGLISH).replace(" ", "_"));
            }
            Set<String> aliasSet = new HashSet<>(aliases);

            for (String alias : aliasSet) {
                displayNameToMobID.put(alias, entityID);
            }

            if (verbose) {
                plugin.getLogger().info("Entity " + entityID + " = " + bukkitEntity + "[" + bukkitEntityClass + "] (display name: "
                        + displayName + ", aliases: " + aliasSet + ")");
            }
        }

        // Should we use something else as the default?
        if (plugin.getConfig().contains("defaultCreature")) {
            // Lowercase is better to search
            String defaultCreatureString = plugin.getConfig().getString("defaultCreature", "pig").toLowerCase(Locale.ENGLISH);
            // If we know the internal name
            if (displayNameToMobID.containsKey(defaultCreatureString)) {
                setDefaultEntityID(defaultEntityID);
                if (verbose) {
                    plugin.getLogger().info("Default monster spawner set to " + defaultEntityID);
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

        if (plugin.getConfig().getBoolean("spawnersUnstackable", false)) {
            nmsProvider.setSpawnersUnstackable();
        }
    }

    /**
     * Receives the default entity ID.
     *
     * @return the default entity ID defined by SilkSpawners.
     */
    public String getDefaultEntityID() {
        return defaultEntityID;
    }

    /**
     * Sets the default entity ID.
     *
     * @param defaultEntityID short value of the default mob
     */
    public void setDefaultEntityID(String defaultEntityID) {
        this.defaultEntityID = defaultEntityID;
    }

    /**
     * Returns if SilkUtil is using reflection.
     *
     * @return true for reflection, false for not
     */
    public boolean isUsingReflection() {
        return useReflection;
    }

    /**
     * Set if SilkUtil should use reflection.
     *
     * @param useReflection true or false
     */
    public void setUseReflection(boolean useReflection) {
        this.useReflection = useReflection;
    }

    /**
     * Returns if vanilla boss bar is used.
     *
     * @return whether vanilla boss bar is used or not
     */
    public boolean isVanillaBossBar() {
        return plugin.getConfig().getBoolean("vanillaBossBar.enable", true);
    }

    /**
     * Returns a new ItemStack of a spawn egg with the specified amount and mob.
     *
     * @deprecated Use {@link SilkUtil#newEggItem(String, int, String)} instead.
     * @param entityID which mob should be spawned
     * @param amount the amount of spawn eggs
     * @return the ItemStack
     */
    @Deprecated
    public ItemStack newEggItem(String entityID, int amount) {
        return nmsProvider.newEggItem(entityID, amount);
    }

    /**
     * Returns a new ItemStack of a spawn egg with the specified amount and mob.
     *
     * @param entityID which mob should be spawned
     * @param amount the amount of spawn eggs
     * @param displayName the display name of the egg in case of unknown entities
     * @return the ItemStack
     */
    public ItemStack newEggItem(String entityID, int amount, String displayName) {
        return nmsProvider.newEggItem(entityID, amount, displayName);
    }

    // Create a tagged a mob spawner item with it's entity ID and custom amount
    /**
     * This method will make a new MobSpawner with a custom entityID, name and amount.
     *
     * @param entityID the mob
     * @param customName if the MobSpawner should be named different
     * @param amount the wanted amount
     * @param forceLore whether the lore tag should be forces
     * @return the ItemStack with the configured options
     */
    public ItemStack newSpawnerItem(String entityID, String customName, int amount, boolean forceLore) {
        String targetEntityID = null;
        try {
            targetEntityID = displayNameToMobID.get(entityID);
        } catch (@SuppressWarnings("unused") NullPointerException e) {
            targetEntityID = entityID;
        }
        if (targetEntityID == null) {
            targetEntityID = entityID;
        }

        String spawnerName = customName;
        if (customName == null || customName.isEmpty()) {
            spawnerName = "Monster Spawner";
        }
        ItemStack item = new ItemStack(nmsProvider.getSpawnerMaterial(), amount);
        ItemMeta meta = item.getItemMeta();

        if (!"Monster Spawner".equalsIgnoreCase(spawnerName)) {
            meta.setDisplayName(
                    ChatColor.translateAlternateColorCodes('\u0026', spawnerName).replace("%creature%", getCreatureName(targetEntityID)));
        }

        if ((forceLore || !isUsingReflection()) && plugin.getConfig().getBoolean("useMetadata", true)) {
            ArrayList<String> lore = new ArrayList<>();
            lore.add("entityID:" + targetEntityID);
            meta.setLore(lore);
        }
        item.setItemMeta(meta);

        return nmsProvider.setNBTEntityID(item, targetEntityID);
    }

    /**
     * Returns the entity ID of a spawn egg.
     *
     * @param item the egg
     * @return the entityID
     */
    @Nullable
    public String getStoredEggEntityID(ItemStack item) {
        String entityID = null;
        if (isUsingReflection()) {
            // Now try reflection for NBT tag
            entityID = nmsProvider.getSilkSpawnersNBTEntityID(item);
            if (entityID != null) {
                return entityID;
            }
            entityID = nmsProvider.getVanillaEggNBTEntityID(item);
            if (entityID != null) {
                return entityID;
            }
        }
        // If we still haven't found our entityID, then check for item lore or name
        if (item.hasItemMeta()) {
            String metaEntityID = searchItemMeta(item.getItemMeta());
            if (metaEntityID != null) {
                return metaEntityID;
            }
        }
        return null;
    }

    /**
     * Returns the entity of a spawner.
     *
     * @param item the spawner
     * @return the entity
     */
    @Nullable
    public String getStoredSpawnerItemEntityID(ItemStack item) {
        if (isUsingReflection()) {
            String entityID = nmsProvider.getSilkSpawnersNBTEntityID(item);
            if (entityID != null) {
                return entityID;
            }
            entityID = nmsProvider.getVanillaNBTEntityID(item);
            if (entityID != null) {
                return entityID;
            }
        }
        if (item.hasItemMeta()) {
            String metaEntityID = searchItemMeta(item.getItemMeta());
            if (metaEntityID != null) {
                return metaEntityID;
            }
        }
        return null;
    }

    /**
     * Searches item lore and display name for entity ID.
     *
     * @param meta the ItemMeta
     * @return entityID if found or null
     */
    @Nullable
    public String searchItemMeta(ItemMeta meta) {
        String entityID = null;
        if (plugin.getConfig().getBoolean("useMetadata", true) && meta.hasLore() && !meta.getLore().isEmpty()) {
            for (String entityIDString : meta.getLore()) {
                if (!entityIDString.contains("entityID")) {
                    continue;
                }
                String[] entityIDArray = entityIDString.split(":");
                if (entityIDArray.length == 2) {
                    return displayNameToMobID.get(entityIDArray[1]);
                }
            }
        }
        return entityID;
    }

    /**
     * Lookup if mob is recognized by Bukkit's wrappers.
     *
     * @param mobID the name (String) of the mob
     * @return the result, true or false
     */
    public boolean isRecognizedMob(String mobID) {
        return EntityType.fromName(mobID) != null;
    }

    /**
     * Returns the entity ID of a spawner (block).
     *
     * @param block the spawner block
     * @return the entity ID
     */
    @Nullable
    public String getSpawnerEntityID(Block block) {
        BlockState blockState = block.getState();
        if (!(blockState instanceof CreatureSpawner)) {
            plugin.getLogger().warning("getSpawnerEntityID called on non-spawner block: " + block);
            return null;
        }

        if (isUsingReflection()) {
            return nmsProvider.getMobNameOfSpawner(blockState);
        }

        CreatureSpawner spawner = (CreatureSpawner) blockState;
        if (spawner.getSpawnedType() != null) {
            return spawner.getSpawnedType().getName();
        }
        return null;
    }

    /**
     * Set the specified MonterSpawner to another entity ID.
     *
     * @param block MonsterSpawner
     * @param entity the wanted entity
     */
    public void setSpawnerEntityID(Block block, String entity) {
        BlockState blockState = block.getState();
        if (!(blockState instanceof CreatureSpawner)) {
            plugin.getLogger().warning("setSpawnerEntityID called on non-spawner block: " + block);
            return;
        }

        if (isUsingReflection()) {
            String mobID = displayNameToMobID.get(entity);
            if (mobID == null) {
                mobID = getCreatureName(defaultEntityID);
            }

            if (nmsProvider.setMobNameOfSpawner(blockState, mobID)) {
                blockState.update(true);
                return;
            }
        }

        @SuppressWarnings("deprecation")
        EntityType ct = EntityType.fromName(entity);
        if (ct == null) {
            throw new IllegalArgumentException("Failed to find creature type for " + entity);
        }
        ((CreatureSpawner) blockState).setSpawnedType(ct);
        blockState.update(true);
    }

    /**
     * Set a spawner (if allowed) to a new mob.
     *
     * @param block the MonsterSpawner
     * @param entityID the new entity ID
     * @param player the player
     * @param messageDenied the message which is shown, when the player can't build here see {@link #canBuildHere(Player, Location)}
     * @return whether the operation was successful or not
     */
    public boolean setSpawnerType(Block block, String entityID, Player player, String messageDenied) {
        // Changing denied by WorldGuard?
        if (!canBuildHere(player, block.getLocation())) {
            sendMessage(player, messageDenied);
            return false;
        }
        // Set the spawner and message the player
        setSpawnerEntityID(block, entityID);
        return true;
    }

    /**
     * Sets a spawner item or egg to a new ID.
     *
     * @param item ItemStack (Egg or Spawner)
     * @param entityID wanted entity ID
     * @param customName if a custom name should be used (null for none)
     * @return the updated ItemStack
     */
    public ItemStack setSpawnerType(ItemStack item, String entityID, String customName) {
        entityID = displayNameToMobID.get(entityID);

        // Ensure that the name is correct
        if (customName == null || customName.isEmpty()) {
            customName = "Monster Spawner";
        }
        // Please eggs or spawners
        if (item == null
                || item.getType() != nmsProvider.getSpawnerMaterial() && !nmsProvider.getSpawnEggMaterials().contains(item.getType())) {
            return item;
        }
        ItemMeta meta = item.getItemMeta();
        // Case spawner and check if we should color
        if (item.getType() == nmsProvider.getSpawnerMaterial() && !customName.equalsIgnoreCase("Monster Spawner")) {
            meta.setDisplayName(
                    ChatColor.translateAlternateColorCodes('\u0026', customName).replace("%creature%", getCreatureName(entityID)));
        }

        // 1.8 broke durability, workaround is the lore
        if (!isUsingReflection() && plugin.getConfig().getBoolean("useMetadata", true)) {
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

        return nmsProvider.setNBTEntityID(item, entityID);
    }

    /**
     * Get the creature name (display name) of an ID. Internal mob names are are like 'LavaSlime', this will return the in-game name like
     * 'Magma Cube'
     *
     * @param entity the entity
     * @return the displayname of the mob
     */
    @SuppressWarnings("deprecation")
    public String getCreatureName(String entity) {
        if (entity == null) {
            return "???";
        }
        String displayName = null;
        if (mobIDToDisplayName != null) {
            try {
                displayName = mobIDToDisplayName.get(entity);
            } catch (@SuppressWarnings("unused") NullPointerException e) {
                // Ignore
            }
        }
        if (displayName == null) {
            EntityType entityType = EntityType.fromName(entity);
            if (entityType != null) {
                displayName = entityType.getName();
            } else {
                displayName = entity;
            }
        }
        return displayName;
    }

    /**
     * Get the creature name (display name) of an ID. Internal mob names are are like 'LavaSlime', this will return the in-game name like
     * 'Magma Cube'
     *
     * @param entity the entity
     * @return the displayname of the mob
     */
    public String getCreatureEggName(String entity) {
        return getCreatureName(entity) + " Spawn Egg";
    }

    /**
     * Lists all enabled creatures to a CommandSender.
     *
     * @param sender CommandSender (player or console)
     */
    public void showAllCreatures(CommandSender sender) {
        // For each entry in the list
        StringBuilder builder = new StringBuilder();
        for (String displayName : displayNameToMobID.keySet()) {
            displayName = displayName.replace(" ", "");
            builder.append(displayName + ", ");
        }
        // Strip last comma out
        String message = builder.toString();
        message = message.substring(0, message.length() - ", ".length());
        sendMessage(sender, message);
    }

    /**
     * Use reflection to scan through each mob and the IDs/name.
     *
     * @return Map with a result of Integer (ID), String (name)
     */
    public List<String> scanEntityMap() {
        List<String> entities = nmsProvider.rawEntityMap();
        // Legacy support, this will add the IDs as aliases
        if (entities == null) {
            SortedMap<Integer, String> legacyRawEntityMap = nmsProvider.legacyRawEntityMap();
            entities = new ArrayList<>(legacyRawEntityMap.values());
            for (Entry<Integer, String> entry : legacyRawEntityMap.entrySet()) {
                displayNameToMobID.put(entry.getKey().toString(), entry.getValue());
            }
        }
        // Let's scan for added entities by e.g MCPC+
        for (EntityType type : EntityType.values()) {
            String name = type.getName();
            short id = type.getTypeId();
            // If name is not defined or ID -1 --> skip this bad entity
            if (name == null || id == -1) {
                continue;
            }
            if (!entities.stream().anyMatch(name::equalsIgnoreCase)) {
                entities.add(name);
            }
        }
        return entities;
    }

    /**
     * Notify a player about the spawner.
     *
     * @param player the player
     * @param spawnerName the creature name
     */
    @SuppressWarnings("deprecation")
    public void notify(Player player, String spawnerName) {
        if (isBarAPI()) {
            String shortInfo = ChatColor.translateAlternateColorCodes('\u0026',
                    plugin.localization.getString("informationOfSpawnerBar").replace("%creature%", spawnerName));
            BarAPI.setMessage(player, shortInfo, plugin.getConfig().getInt("barAPI.displayTime", 3));
        } else if (isVanillaBossBar()) {
            String shortInfo = ChatColor.translateAlternateColorCodes('\u0026',
                    plugin.localization.getString("informationOfSpawnerBar").replace("%creature%", spawnerName));
            String barColor = plugin.getConfig().getString("vanillaBossBar.color", "RED");
            String barStyle = plugin.getConfig().getString("vanillaBossBar.style", "SOLID");
            int barTime = plugin.getConfig().getInt("vanillaBossBar.displayTime", 3);
            nmsProvider.displayBossBar(shortInfo, barColor, barStyle, player, plugin, barTime);
        } else {
            sendMessage(player, ChatColor.translateAlternateColorCodes('\u0026',
                    plugin.localization.getString("informationOfSpawner1").replace("%creature%", spawnerName)));
            sendMessage(player, ChatColor.translateAlternateColorCodes('\u0026',
                    plugin.localization.getString("informationOfSpawner2").replace("%creature%", spawnerName)));
        }
    }

    /**
     * This method clears all HashMaps and lists.
     */
    public void clearAll() {
        displayNameToMobID.clear();
        mobIDToDisplayName.clear();
        knownEntities.clear();
    }

    /**
     * Test a String if it ends with egg.
     *
     * @param creatureString the name
     * @return result, true or false
     */
    @SuppressWarnings("static-method")
    public boolean isEgg(String creatureString) {
        return creatureString.endsWith("egg");
    }

    /**
     * Check if given creature name is known or not. Aliases are supported, too
     *
     * @param creatureString the mob name
     * @return the result, true of false
     */
    public boolean isUnknown(String creatureString) {
        return !displayNameToMobID.containsKey(creatureString);
    }

    /**
     * Check if given creature name is known or not. Aliases are supported, too.
     *
     * @param creatureString the mob name
     * @return the result, true of false
     */
    public boolean isKnown(String creatureString) {
        return displayNameToMobID.containsKey(creatureString);
    }

    /**
     * Lookup if the mob is known.
     *
     * @param entityID the official internal name of the mob
     * @return the result, true of false
     */
    public boolean isKnownEntityID(String entityID) {
        return knownEntities.contains(entityID);
    }

    /**
     * Check if the given string is a number.
     *
     * @param number to check
     * @return number or not found -1
     */
    @SuppressWarnings("static-method")
    public short getNumber(String number) {
        try {
            return Short.valueOf(number);
        } catch (@SuppressWarnings("unused") NumberFormatException e) {
            return -1;
        }
    }

    // Checks if the given ItemStack has got the SilkTouch
    /**
     * Checks if a given ItemStack is in the list of allowed tools and if the SilkTouch level is high enough.
     *
     * @param tool ItemStack to check
     * @return the result if the tool hasSilkTouch
     */
    public boolean isValidItemAndHasSilkTouch(ItemStack tool) {
        // No silk touch fists..
        if (tool == null) {
            return false;
        }

        boolean toolAllowed = false;
        Material toolType = tool.getType();
        List<String> allowedTools = plugin.getConfig().getStringList("allowedTools");
        for (String allowedTool : allowedTools) {
            if (toolType == Material.matchMaterial(allowedTool)) {
                toolAllowed = true;
                break;
            }
        }
        if (!toolAllowed) {
            return false;
        }

        int minLevel = plugin.getConfig().getInt("minSilkTouchLevel", 1);
        // Always have it
        if (minLevel == 0) {
            return true;
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
     * Get the spawner name, specified for each mob or default. from localization.yml
     *
     * @param mobName the internal name the spawner name is wanted for
     * @return the found string
     */
    public String getCustomSpawnerName(String mobName) {
        if (plugin.mobs.contains("creatures." + mobName + ".spawnerName")) {
            return ChatColor.translateAlternateColorCodes('&',
                    plugin.mobs.getString("creatures." + mobName + ".spawnerName", "Monster Spawner"));
        }
        return ChatColor.translateAlternateColorCodes('&', plugin.localization.getString("spawnerName", "Monster Spawner"));
    }

    /**
     * Sends a message to a Player or CommandSender with support for newlines
     *
     * @param receiver the receiver of the message
     * @param messages message with support for newlines
     */
    @SuppressWarnings("static-method")
    public void sendMessage(CommandSender receiver, String messages) {
        receiver.sendMessage(messages.split("\n"));
    }

    /**
     * Prepare for WorldGuard support.
     *
     * @param plugin SilkSpawners instance
     */
    private void getWorldGuard() {
        if (!plugin.getConfig().getBoolean("useWorldGuard", true)) {
            plugin.getLogger().info("WorldGuard support is disabled due to config setting");
            return;
        }
        Plugin worldGuard = plugin.getServer().getPluginManager().getPlugin("WorldGuard");
        if (worldGuard == null || !(worldGuard instanceof WorldGuardPlugin)) {
            plugin.getLogger().info("WorldGuard was not found and support is disabled");
            return;
        }
        plugin.getLogger().info("WorldGuard was found and support is enabled");
        wg = (WorldGuardPlugin) worldGuard;
    }

    /**
     * Checks if a player can build here (WorldGuard).
     *
     * @param player the player
     * @param location the location to check
     * @return the result, true or false
     */
    public boolean canBuildHere(Player player, Location location) {
        if (wg == null) {
            return true;
        }
        try {
            WorldGuard instance = WorldGuard.getInstance();
            RegionContainer regionContainer = instance.getPlatform().getRegionContainer();
            RegionQuery query = regionContainer.createQuery();
            return query.testBuild(BukkitAdapter.adapt(location), wg.wrapPlayer(player), Flags.BUILD);
        } catch (@SuppressWarnings("unused") Exception | NoClassDefFoundError e) {
            try {
                return (boolean) wg.getClass().getDeclaredMethod("canBuild", Player.class, Location.class).invoke(wg, player, location);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException
                    | SecurityException e1) {
                e1.printStackTrace();
            }
            return false;
        }
    }

    public boolean isPluginEnabled(String _plugin) {
        Plugin pluginToCheck = plugin.getServer().getPluginManager().getPlugin(_plugin);
        return pluginToCheck != null && pluginToCheck.isEnabled();
    }

    /**
     * Checks if there is only one spawn egg material. This means SilkSpawners is running pre Minecraft 1.13. Minecraft added own materials
     * for all mobs in the 1.13 update. This effects for instance the egg crafting.
     *
     * @return the check result, true for pre 1.13 or false for 1.13 or newer
     */
    public boolean isLegacySpawnEggs() {
        return nmsProvider.getSpawnEggMaterials().size() == 1;
    }
}
