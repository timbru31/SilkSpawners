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
import java.util.logging.Level;

import javax.annotation.Nullable;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.UnsafeValues;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.permissions.Permissible;
import org.bukkit.plugin.Plugin;

import com.google.common.base.CaseFormat;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import com.vdurmont.semver4j.Semver;

import de.dustplanet.silkspawners.SilkSpawners;
import de.dustplanet.silkspawners.compat.api.NMSProvider;
import lombok.Getter;
import lombok.Setter;
import me.confuser.barapi.BarAPI;
import ru.endlesscode.mimic.Mimic;

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
    private final Map<String, String> mobIDToDisplayName = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    /**
     * This HashMap is holding the display name and aliases of each mob and the internal Minecraft name.
     */
    @Getter
    private final Map<String, String> displayNameToMobID = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    /**
     * List of enabled (and therefore) known entities.
     */
    @Getter
    private final List<String> knownEntities = new ArrayList<>();

    /**
     * Default (fallback) entityID, standard is the pig.
     */
    @SuppressWarnings("deprecation")
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
     * Mimic instance, may be null
     */
    private Mimic mimic;

    /**
     * BarAPI usage toggle.
     */
    @Getter
    @Setter
    private boolean barAPI;

    /**
     * SilkSpawners instance.
     */
    private final SilkSpawners plugin;

    /**
     * NMSHandler instance.
     */
    public NMSProvider nmsProvider;

    /**
     * Constructor to make your own SilkUtil instance.
     *
     * @param instance SilkSpawners instance
     */
    public SilkUtil(final SilkSpawners instance) {
        SilkSpawners correctedInstance = instance;
        if (instance == null) {
            Bukkit.getLogger().severe("SilkSpawners - Nag API user: Don't initialize SilkUtil without a SilkSpawners instance!");
            correctedInstance = (SilkSpawners) Bukkit.getPluginManager().getPlugin("SilkSpawners");
        }
        plugin = correctedInstance;

        final boolean verboseMode = plugin.getConfig().getBoolean("verboseMode", false);
        if (verboseMode) {
            DebugLogHandler.attachDebugLogger(plugin);
            plugin.getLogger().setLevel(Level.FINE);
        }

        getWorldGuard();
        getMimic();
        final boolean nmsProviderFound = setupNMSProvider();

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
        final SilkSpawners plugin = (SilkSpawners) Bukkit.getPluginManager().getPlugin("SilkSpawners");
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
        String version = plugin.getNmsVersion();

        // Rare cases might trigger API usage before SilkSpawners
        if (version == null) {
            final String packageName = Bukkit.getServer().getClass().getPackage().getName();
            final String nmsVersion = packageName.substring(packageName.lastIndexOf('.') + 1);
            if (nmsVersion.equals("craftbukkit")) {
                try {
                    final String minecraftVersion = (String) Server.class.getDeclaredMethod("getMinecraftVersion")
                            .invoke(Bukkit.getServer());
                    final Semver semver = new Semver(minecraftVersion);
                    if (semver.isGreaterThanOrEqualTo("1.20.5")) {
                        @SuppressWarnings("deprecation")
                        final int protocolVersion = (Integer) UnsafeValues.class.getDeclaredMethod("getProtocolVersion")
                                .invoke(Bukkit.getUnsafe());
                        version = SilkSpawners.PROTOCOL_VERSION_PACKAGE_MAP.get(protocolVersion);
                    }
                } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
                    e.printStackTrace();
                }
            }
        }

        final FileConfiguration config = plugin.getConfig();
        boolean checkForNerfFlags = true;
        if (config != null) {
            checkForNerfFlags = config.getBoolean("checkForNerfFlags", true);
        }

        try {
            final Class<?> clazz = Class.forName("de.dustplanet.silkspawners.compat." + version + ".NMSHandler");
            if (NMSProvider.class.isAssignableFrom(clazz)) {
                nmsProvider = (NMSProvider) clazz.getConstructor(Boolean.TYPE).newInstance(checkForNerfFlags);
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
        // Scan the entities
        final List<String> entities = scanEntityMap();
        plugin.getLogger().fine("Scanning the mobs");
        for (final String entityID : entities) {
            @SuppressWarnings("deprecation")
            final EntityType bukkitEntity = EntityType.fromName(entityID);
            final Class<? extends Entity> bukkitEntityClass = bukkitEntity == null ? null : bukkitEntity.getEntityClass();

            boolean enable = plugin.getConfig().getBoolean("enableCreatureDefault", true);
            if (!plugin.getMobs().contains("creatures." + entityID)) {
                plugin.getLogger().info("Entity " + entityID + " is not in the config. Adding...");
                plugin.getMobs().addDefault("creatures." + entityID + ".enable", enable);
                plugin.getMobs().save();
            } else {
                enable = plugin.getMobs().getBoolean("creatures." + entityID + ".enable", enable);
            }
            if (!enable) {
                plugin.getLogger().fine("Entity " + entityID + " = " + bukkitEntity + "[" + bukkitEntityClass + "] (disabled)");
                continue;
            }

            // Add the known ID [we omit all disabled entities]
            knownEntities.add(entityID);

            String displayName = plugin.getMobs().getString("creatures." + entityID + ".displayName");
            if (displayName == null) {
                displayName = entityID;
            }

            mobIDToDisplayName.put(entityID, displayName);

            final List<String> aliases = plugin.getMobs().getStringList("creatures." + entityID + ".aliases");
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
            final Set<String> aliasSet = new HashSet<>(aliases);

            for (final String alias : aliasSet) {
                displayNameToMobID.put(alias, entityID);
            }

            plugin.getLogger().fine("Entity " + entityID + " = " + bukkitEntity + "[" + bukkitEntityClass + "] (display name: "
                    + displayName + ", aliases: " + aliasSet + ")");
        }

        // Should we use something else as the default?
        if (plugin.getConfig().contains("defaultCreature")) {
            // Lowercase is better to search
            final String defaultCreatureString = plugin.getConfig().getString("defaultCreature", "pig").toLowerCase(Locale.ENGLISH);
            // If we know the internal name
            if (displayNameToMobID.containsKey(defaultCreatureString)) {
                setDefaultEntityID(displayNameToMobID.get(defaultCreatureString));
                plugin.getLogger().fine("Default monster spawner set to " + defaultEntityID);
            } else {
                // Unknown, fallback
                plugin.getLogger().warning("Invalid creature type: " + defaultCreatureString + ", default monster spawner fallback to PIG");
            }
        }

        // Are we allowed to use native methods?
        if (!plugin.getConfig().getBoolean("useReflection", true)) {
            setUseReflection(false);
        }

        plugin.getLogger().fine("Reflection is " + isUsingReflection());

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
    public void setDefaultEntityID(final String defaultEntityID) {
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
    public void setUseReflection(final boolean useReflection) {
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
    public ItemStack newEggItem(final String entityID, final int amount) {
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
    public ItemStack newEggItem(final String entityID, final int amount, final String displayName) {
        if (amount == 0) {
            return new ItemStack(Material.AIR);
        }
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
    public ItemStack newSpawnerItem(final String entityID, final String customName, final int amount, final boolean forceLore) {
        if (amount == 0) {
            return new ItemStack(Material.AIR);
        }

        String targetEntityID = null;
        try {
            targetEntityID = displayNameToMobID.get(entityID);
        } catch (@SuppressWarnings("unused") final NullPointerException e) {
            targetEntityID = entityID;
        }
        if (targetEntityID == null) {
            targetEntityID = entityID;
        }

        String spawnerName = customName;
        if (StringUtils.isBlank(spawnerName)) {
            spawnerName = "Monster Spawner";
        }
        final ItemStack item = new ItemStack(nmsProvider.getSpawnerMaterial(), amount);
        final ItemMeta meta = item.getItemMeta();

        if (!"Monster Spawner".equalsIgnoreCase(spawnerName)) {
            meta.setDisplayName(
                    ChatColor.translateAlternateColorCodes('\u0026', spawnerName).replace("%creature%", getCreatureName(targetEntityID)));
        }

        if ((forceLore || !isUsingReflection()) && plugin.getConfig().getBoolean("useMetadata", true)) {
            final ArrayList<String> lore = new ArrayList<>();
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
    public String getStoredEggEntityID(final ItemStack item) {
        String entityID = null;
        if (isUsingReflection()) {
            // Now try reflection for NBT tag
            entityID = nmsProvider.getSilkSpawnersNBTEntityID(item);
            plugin.getLogger().log(Level.FINE, "EntityID from egg item stack (custom tag) is {0}", entityID);
            if (entityID != null) {
                return entityID;
            }
            entityID = nmsProvider.getVanillaEggNBTEntityID(item);
            plugin.getLogger().log(Level.FINE, "EntityID from egg item stack (vanilla tag) is {0}", entityID);
            if (entityID != null) {
                return entityID;
            }
        }
        // If we still haven't found our entityID, then check for item lore or name
        if (item.hasItemMeta()) {
            final String metaEntityID = searchItemMeta(item.getItemMeta());
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
    public String getStoredSpawnerItemEntityID(final ItemStack item) {
        if (isUsingReflection()) {
            String entityID = nmsProvider.getSilkSpawnersNBTEntityID(item);
            plugin.getLogger().log(Level.FINE, "EntityID from item stack (custom tag) is {0}", entityID);
            if (StringUtils.isNotBlank(entityID)) {
                return entityID;
            }
            entityID = nmsProvider.getVanillaNBTEntityID(item);
            plugin.getLogger().log(Level.FINE, "EntityID from item stack (vanilla tag) is {0}", entityID);
            if (StringUtils.isNotBlank(entityID)) {
                return entityID.replace("minecraft:", "");
            }
            entityID = nmsProvider.getOtherPluginsNBTEntityID(item);
            plugin.getLogger().log(Level.FINE, "EntityID from item stack (other plugin tags) is {0}", entityID);
            if (StringUtils.isNotBlank(entityID)) {
                return entityID.replace("minecraft:", "");
            }
        }
        if (item.hasItemMeta()) {
            final String metaEntityID = searchItemMeta(item.getItemMeta());
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
    public String searchItemMeta(final ItemMeta meta) {
        final String entityID = null;
        if (plugin.getConfig().getBoolean("useMetadata", true) && meta.hasLore() && !meta.getLore().isEmpty()) {
            for (final String entityIDString : meta.getLore()) {
                if (!entityIDString.contains("entityID")) {
                    continue;
                }
                final String[] entityIDArray = entityIDString.split(":");
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
    @SuppressWarnings({ "deprecation", "static-method" })
    public boolean isRecognizedMob(final String mobID) {
        return EntityType.fromName(mobID) != null;
    }

    /**
     * Returns the entity ID of a spawner (block).
     *
     * @param block the spawner block
     * @return the entity ID
     */
    @SuppressWarnings("deprecation")
    @Nullable
    public String getSpawnerEntityID(final Block block) {
        final BlockState blockState = block.getState();
        if (!(blockState instanceof CreatureSpawner)) {
            plugin.getLogger().warning("getSpawnerEntityID called on non-spawner block: " + block);
            return null;
        }

        if (isUsingReflection()) {
            plugin.getLogger().fine("Using reflection to get mob name of the block");
            return nmsProvider.getMobNameOfSpawner(blockState);
        }

        final CreatureSpawner spawner = (CreatureSpawner) blockState;
        if (spawner.getSpawnedType() != null) {
            plugin.getLogger().fine("Using Bukkit fallback to get the mob name of the block");
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
    public void setSpawnerEntityID(final Block block, final String entity) {
        final BlockState blockState = block.getState();
        if (!(blockState instanceof CreatureSpawner)) {
            plugin.getLogger().warning("setSpawnerEntityID called on non-spawner block: " + block);
            return;
        }

        if (isUsingReflection()) {
            String mobID = displayNameToMobID.get(entity);
            if (mobID == null) {
                mobID = displayNameToMobID.get(defaultEntityID);
            }

            if (nmsProvider.setMobNameOfSpawner(blockState, mobID)) {
                blockState.update(true);
                return;
            }
        }

        @SuppressWarnings("deprecation")
        final EntityType ct = EntityType.fromName(entity);
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
    public boolean setSpawnerType(final Block block, final String entityID, final Player player, final String messageDenied) {
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
    public ItemStack setSpawnerType(final ItemStack item, final String entityID, final String customName) {
        final String correctedEntityID = displayNameToMobID.get(entityID);
        String correctedCustomName = customName;
        if (StringUtils.isBlank(customName)) {
            correctedCustomName = "Monster Spawner";
        }
        // Please eggs or spawners
        if (item == null
                || item.getType() != nmsProvider.getSpawnerMaterial() && !nmsProvider.getSpawnEggMaterials().contains(item.getType())) {
            return item;
        }
        final ItemMeta meta = item.getItemMeta();
        // Case spawner and check if we should color
        if (item.getType() == nmsProvider.getSpawnerMaterial() && !correctedCustomName.equalsIgnoreCase("Monster Spawner")) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('\u0026', correctedCustomName).replace("%creature%",
                    getCreatureName(correctedEntityID)));
        }

        if (!isUsingReflection() && plugin.getConfig().getBoolean("useMetadata", true)) {
            final ArrayList<String> lore = new ArrayList<>();
            lore.add("entityID:" + correctedEntityID);
            meta.setLore(lore);
        }

        // Does the item (e.g. crafted) as a lore and we set the NBT tag? Remove it
        if (isUsingReflection() && meta.hasLore()) {
            final List<String> lore = meta.getLore();
            final Iterator<String> it = lore.iterator();
            while (it.hasNext()) {
                if (it.next().contains("entityID")) {
                    it.remove();
                }
            }
            meta.setLore(lore);
        }
        item.setItemMeta(meta);

        return nmsProvider.setNBTEntityID(item, correctedEntityID);
    }

    /**
     * Get the creature name (display name) of an ID. Internal mob names are are like 'LavaSlime', this will return the in-game name like
     * 'Magma Cube'
     *
     * @param entity the entity
     * @return the displayname of the mob
     */
    @SuppressWarnings("deprecation")
    public String getCreatureName(final String entity) {
        if (entity == null) {
            return "???";
        }
        String displayName = null;
        if (mobIDToDisplayName != null) {
            try {
                displayName = mobIDToDisplayName.get(entity);
            } catch (@SuppressWarnings("unused") final NullPointerException e) {
                // Ignore
            }
        }
        if (displayName == null) {
            final EntityType entityType = EntityType.fromName(entity);
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
    public String getCreatureEggName(final String entity) {
        return getCreatureName(entity) + " Spawn Egg";
    }

    /**
     * Lists all enabled creatures to a CommandSender.
     *
     * @param sender CommandSender (player or console)
     */
    public void showAllCreatures(final CommandSender sender) {
        // For each entry in the list
        final StringBuilder builder = new StringBuilder();
        for (final Entry<String, String> entityType : displayNameToMobID.entrySet()) {
            String displayName = entityType.getKey();
            final String entityId = entityType.getValue();
            displayName = displayName.replace(" ", "");
            if (hasPermission(sender, "silkspawners.list.", entityId)) {
                builder.append(displayName + ", ");
            }
        }
        // Strip last comma out
        String message = builder.toString();
        if (StringUtils.isNotBlank(message)) {
            message = message.substring(0, message.length() - ", ".length());
        }
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
            final SortedMap<Integer, String> legacyRawEntityMap = nmsProvider.legacyRawEntityMap();
            entities = new ArrayList<>(legacyRawEntityMap.values());
            for (final Entry<Integer, String> entry : legacyRawEntityMap.entrySet()) {
                displayNameToMobID.put(entry.getKey().toString(), entry.getValue());
            }
        }
        if (entities.isEmpty()) {
            plugin.getLogger().warning(
                    "Warning, no mobs were found via reflection! This usually means another plugin messed up Minecraft's entity registry. Please report this! This is NOT a SilkSpawners bug");
        }
        // Let's scan for added entities by e.g MCPC+
        for (final EntityType type : EntityType.values()) {
            @SuppressWarnings("deprecation")
            final String name = type.getName();
            if (name == null) {
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
    public void notify(final Player player, final String spawnerName) {
        if (isBarAPI()) {
            final String shortInfo = ChatColor.translateAlternateColorCodes('\u0026',
                    plugin.localization.getString("informationOfSpawnerBar").replace("%creature%", spawnerName));
            BarAPI.setMessage(player, shortInfo, plugin.getConfig().getInt("barAPI.displayTime", 3));
        } else if (isVanillaBossBar()) {
            final String shortInfo = ChatColor.translateAlternateColorCodes('\u0026',
                    plugin.localization.getString("informationOfSpawnerBar").replace("%creature%", spawnerName));
            final String barColor = plugin.getConfig().getString("vanillaBossBar.color", "RED");
            final String barStyle = plugin.getConfig().getString("vanillaBossBar.style", "SOLID");
            final int barTime = plugin.getConfig().getInt("vanillaBossBar.displayTime", 3);
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
    public boolean isEgg(final String creatureString) {
        return creatureString.endsWith("egg");
    }

    /**
     * Check if given creature name is known or not. Aliases are supported, too
     *
     * @param creatureString the mob name
     * @return the result, true of false
     */
    public boolean isUnknown(final String creatureString) {
        return !displayNameToMobID.containsKey(creatureString);
    }

    /**
     * Check if given creature name is known or not. Aliases are supported, too.
     *
     * @param creatureString the mob name
     * @return the result, true of false
     */
    public boolean isKnown(final String creatureString) {
        return displayNameToMobID.containsKey(creatureString);
    }

    /**
     * Lookup if the mob is known.
     *
     * @param entityID the official internal name of the mob
     * @return the result, true of false
     */
    public boolean isKnownEntityID(final String entityID) {
        return knownEntities.contains(entityID);
    }

    /**
     * Check if the given string is a number.
     *
     * @param number to check
     * @return number or not found -1
     */
    @SuppressWarnings("static-method")
    public short getNumber(final String number) {
        try {
            return Short.valueOf(number);
        } catch (@SuppressWarnings("unused") final NumberFormatException e) {
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
    public boolean isValidItemAndHasSilkTouch(final ItemStack tool) {
        // No silk touch fists..
        if (tool == null) {
            return false;
        }

        boolean toolAllowed = false;
        final Material toolType = tool.getType();
        final List<String> allowedTools = plugin.getConfig().getStringList("allowedTools");
        for (final String allowedTool : allowedTools) {
            if (toolType == Material.matchMaterial(allowedTool)) {
                toolAllowed = true;
                break;
            }
            if (mimic != null && mimic.getItemsRegistry().isSameItem(tool, allowedTool)) {
                toolAllowed = true;
                break;
            }
        }
        if (!toolAllowed) {
            plugin.getLogger().log(Level.FINE, "Tool not allowed: {0}", tool.getType());
            return false;
        }

        final int minLevel = plugin.getConfig().getInt("minSilkTouchLevel", 1);
        plugin.getLogger().log(Level.FINE, "minLevel is {0}", minLevel);
        if (minLevel == 0) {
            return true;
        }

        // This check isn't actually necessary, since containsEnchantment just
        // checks level>0,
        // but is kept here for clarity, and in case Bukkit allows level-0
        // enchantments like vanilla
        if (!tool.containsEnchantment(Enchantment.SILK_TOUCH)) {
            plugin.getLogger().fine("Tool has no SilkTouch enchantment.");
            return false;
        }
        // Return if the level is enough
        final int enchantmentLevel = tool.getEnchantmentLevel(Enchantment.SILK_TOUCH);
        plugin.getLogger().log(Level.FINE, "Stored enchantment level is {0}", enchantmentLevel);
        return enchantmentLevel >= minLevel;
    }

    /**
     * Get the spawner name, specified for each mob or default. from localization.yml
     *
     * @param mobName the internal name the spawner name is wanted for
     * @return the found string
     */
    public String getCustomSpawnerName(final String mobName) {
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
    public void sendMessage(final CommandSender receiver, final String messages) {
        if (receiver == null || StringUtils.isBlank(messages)) {
            return;
        }
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
        final Plugin worldGuard = plugin.getServer().getPluginManager().getPlugin("WorldGuard");
        if (worldGuard == null || !(worldGuard instanceof WorldGuardPlugin)) {
            plugin.getLogger().info("WorldGuard was not found and support is disabled");
            return;
        }
        plugin.getLogger().info("WorldGuard was found and support is enabled");
        wg = (WorldGuardPlugin) worldGuard;
    }

    private void getMimic() {
        if (!plugin.getConfig().getBoolean("useMimic", true)) {
            plugin.getLogger().info("Mimic support is disabled due to config setting");
            return;
        }
        final Plugin mimicPlugin = plugin.getServer().getPluginManager().getPlugin("Mimic");
        if (mimicPlugin == null) {
            plugin.getLogger().info("Mimic was not found and support is disabled");
            return;
        }
        mimic = Mimic.getInstance();
        plugin.getLogger().info("Mimic was found and support is enabled");
    }

    /**
     * Checks if a player can build here (WorldGuard).
     *
     * @param player the player
     * @param location the location to check
     * @return the result, true or false
     */
    public boolean canBuildHere(final Player player, final Location location) {
        if (wg == null) {
            return true;
        }
        try {
            final WorldGuard instance = WorldGuard.getInstance();
            final RegionContainer regionContainer = instance.getPlatform().getRegionContainer();
            final RegionQuery query = regionContainer.createQuery();
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

    public boolean isPluginEnabled(final String _plugin) {
        final Plugin pluginToCheck = plugin.getServer().getPluginManager().getPlugin(_plugin);
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

    /**
     * Helper methods to check if a player has any of the aliases permissions for a given mobID.
     *
     * @param permissible - the permissible to check the permission for
     * @param basePermission - the basis permission without the specific mob
     * @param entityID - the internal mob ID (not display name)
     * @return the permission check result, true if the player has got the permission, false otherwise
     */
    public boolean hasPermission(final Permissible permissible, final String basePermission, final String entityID) {
        if (StringUtils.isBlank(entityID) || permissible == null || StringUtils.isBlank(basePermission)) {
            plugin.getLogger().fine("permission check is false because the given input is invalid");
            return false;
        }

        plugin.getLogger().log(Level.FINE, "Checking if player has permission {0} for entityID {1}",
                new Object[] { basePermission, entityID });
        final String correctedBasePermission = basePermission.endsWith(".") ? basePermission : basePermission + ".";
        for (final Entry<String, String> entry : displayNameToMobID.entrySet()) {
            final String currentEntityID = entry.getValue();
            if (currentEntityID.equalsIgnoreCase(entityID)) {
                plugin.getLogger().log(Level.FINE, "Found matching entityID from set: {0}, key is {1}",
                        new Object[] { currentEntityID, entry.getKey() });
                final boolean hasPermission = permissible
                        .hasPermission(correctedBasePermission + entry.getKey().toLowerCase(Locale.ENGLISH).replace(" ", ""));
                plugin.getLogger().log(Level.FINE, "hasPermission result is {0}", hasPermission);
                if (hasPermission) {
                    return true;
                }
            }
        }
        plugin.getLogger().fine("Permission not found or not granted, result is false");
        return false;
    }
}
