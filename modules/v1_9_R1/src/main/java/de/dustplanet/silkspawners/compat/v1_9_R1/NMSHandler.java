package de.dustplanet.silkspawners.compat.v1_9_R1;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.UUID;

import javax.annotation.Nullable;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.craftbukkit.v1_9_R1.CraftServer;
import org.bukkit.craftbukkit.v1_9_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_9_R1.block.CraftCreatureSpawner;
import org.bukkit.craftbukkit.v1_9_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_9_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import com.google.common.base.CaseFormat;
import com.mojang.authlib.GameProfile;

import de.dustplanet.silkspawners.compat.api.NMSProvider;
import net.minecraft.server.v1_9_R1.Entity;
import net.minecraft.server.v1_9_R1.EntityPlayer;
import net.minecraft.server.v1_9_R1.EntityTypes;
import net.minecraft.server.v1_9_R1.Item;
import net.minecraft.server.v1_9_R1.MinecraftServer;
import net.minecraft.server.v1_9_R1.NBTTagCompound;
import net.minecraft.server.v1_9_R1.NBTTagList;
import net.minecraft.server.v1_9_R1.PacketPlayOutEntityHeadRotation;
import net.minecraft.server.v1_9_R1.PlayerInteractManager;
import net.minecraft.server.v1_9_R1.TileEntityMobSpawner;
import net.minecraft.server.v1_9_R1.World;

public class NMSHandler implements NMSProvider {
    private Field tileField;
    private final SortedMap<Integer, String> sortedMap = new TreeMap<>();

    public NMSHandler() {
        try {
            tileField = CraftCreatureSpawner.class.getDeclaredField("spawner");
            tileField.setAccessible(true);
        } catch (SecurityException | NoSuchFieldException e) {
            Bukkit.getLogger().warning("[SilkSpawners] Reflection failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void spawnEntity(final org.bukkit.World w, final String entityID, final double x, final double y, final double z,
            final Player player) {
        final NBTTagCompound tag = new NBTTagCompound();
        tag.setString("id", entityID);

        final World world = ((CraftWorld) w).getHandle();
        final Entity entity = EntityTypes.a(tag, world);

        if (entity == null) {
            Bukkit.getLogger().warning("[SilkSpawners] Failed to spawn, falling through. You should report this (entity == null)!");
            return;
        }

        final float yaw = world.random.nextFloat() * (-180 - 180) + 180;
        entity.setPositionRotation(x, y, z, yaw, 0);
        world.addEntity(entity, SpawnReason.SPAWNER_EGG);
        final PacketPlayOutEntityHeadRotation rotation = new PacketPlayOutEntityHeadRotation(entity, (byte) yaw);
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(rotation);
    }

    @Override
    public SortedMap<Integer, String> legacyRawEntityMap() {
        // Use reflection to dump native EntityTypes
        // This bypasses Bukkit's wrappers, so it works with mods
        try {
            // TODO Needs 1.9 source
            // g.put(s, Integer.valueOf(i)); --> Name of ID
            final Field field = EntityTypes.class.getDeclaredField("g");
            field.setAccessible(true);
            @SuppressWarnings("unchecked")
            final Map<String, Integer> map = (Map<String, Integer>) field.get(null);
            for (final Map.Entry<String, Integer> entry : map.entrySet()) {
                sortedMap.put(entry.getValue(), entry.getKey());
            }
        } catch (SecurityException | NoSuchFieldException | IllegalArgumentException | IllegalAccessException e) {
            Bukkit.getLogger().severe("[SilkSpawners] Failed to dump entity map: " + e.getMessage());
            e.printStackTrace();
        }
        return sortedMap;
    }

    @Override
    public String getMobNameOfSpawner(final BlockState blockState) {
        final CraftCreatureSpawner spawner = (CraftCreatureSpawner) blockState;
        try {
            final TileEntityMobSpawner tile = (TileEntityMobSpawner) tileField.get(spawner);
            return tile.getSpawner().getMobName();
        } catch (IllegalArgumentException | IllegalAccessException e) {
            Bukkit.getLogger().warning("[SilkSpawners] Reflection failed: " + e.getMessage());
            e.printStackTrace();
        }
        return "";
    }

    @Override
    public void setSpawnersUnstackable() {
        Item.getById(SPAWNER_ID).d(1);
    }

    @Override
    public boolean setMobNameOfSpawner(final BlockState blockState, final String mobID) {
        // Prevent ResourceKeyInvalidException: Non [a-z0-9/._-] character in path of location
        final String safeMobID = caseFormatOf(mobID.replace(" ", "_")).to(CaseFormat.UPPER_CAMEL, mobID.replace(" ", "_"));
        final CraftCreatureSpawner spawner = (CraftCreatureSpawner) blockState;

        try {
            final TileEntityMobSpawner tile = (TileEntityMobSpawner) tileField.get(spawner);
            tile.getSpawner().setMobName(safeMobID);
            return true;
        } catch (IllegalArgumentException | IllegalAccessException e) {
            Bukkit.getLogger().warning("[SilkSpawners] Reflection failed: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public ItemStack setNBTEntityID(final ItemStack item, final String entity) {
        if (item == null || StringUtils.isBlank(entity)) {
            Bukkit.getLogger().warning("[SilkSpawners] Skipping invalid spawner to set NBT data on.");
            return null;
        }

        net.minecraft.server.v1_9_R1.ItemStack itemStack = null;
        final CraftItemStack craftStack = CraftItemStack.asCraftCopy(item);
        itemStack = CraftItemStack.asNMSCopy(craftStack);
        NBTTagCompound tag = itemStack.getTag();

        // Create tag if necessary
        if (tag == null) {
            tag = new NBTTagCompound();
            itemStack.setTag(tag);
        }

        // Check for SilkSpawners key
        if (!tag.hasKey("SilkSpawners")) {
            tag.set("SilkSpawners", new NBTTagCompound());
        }

        tag.getCompound("SilkSpawners").setString("entity", entity);

        // Check for Vanilla keys
        if (!tag.hasKey("BlockEntityTag")) {
            tag.set("BlockEntityTag", new NBTTagCompound());
        }

        // EntityId - Deprecated in 1.9
        tag.getCompound("BlockEntityTag").setString("EntityId", entity);

        // SpawnData
        if (!tag.hasKey("SpawnData")) {
            tag.set("SpawnData", new NBTTagCompound());
        }
        tag.getCompound("SpawnData").setString("id", entity);

        if (!tag.getCompound("BlockEntityTag").hasKey("SpawnData")) {
            tag.getCompound("BlockEntityTag").set("SpawnData", new NBTTagCompound());
        }
        tag.getCompound("BlockEntityTag").getCompound("SpawnData").setString("id", entity);

        if (!tag.getCompound("BlockEntityTag").hasKey("SpawnPotentials")) {
            tag.getCompound("BlockEntityTag").set("SpawnPotentials", new NBTTagCompound());
        }

        final NBTTagList tagList = new NBTTagList();
        final NBTTagCompound spawnPotentials = new NBTTagCompound();
        spawnPotentials.set("Entity", new NBTTagCompound());
        spawnPotentials.getCompound("Entity").setString("id", entity);
        spawnPotentials.setInt("Weight", 1);
        tagList.add(spawnPotentials);
        tag.getCompound("BlockEntityTag").set("SpawnPotentials", tagList);

        // SpawnEgg data
        if (!tag.hasKey("EntityTag")) {
            tag.set("EntityTag", new NBTTagCompound());
        }
        tag.getCompound("EntityTag").setString("id", entity);

        return CraftItemStack.asCraftMirror(itemStack);
    }

    @Override
    @Nullable
    public String getSilkSpawnersNBTEntityID(final ItemStack item) {
        net.minecraft.server.v1_9_R1.ItemStack itemStack = null;
        final CraftItemStack craftStack = CraftItemStack.asCraftCopy(item);
        itemStack = CraftItemStack.asNMSCopy(craftStack);
        final NBTTagCompound tag = itemStack.getTag();

        if (tag == null || !tag.hasKey("SilkSpawners")) {
            return null;
        }

        final NBTTagCompound silkSpawnersTag = tag.getCompound("SilkSpawners");
        if (silkSpawnersTag.hasKey("entity")) {
            return silkSpawnersTag.getString("entity");
        }

        if (silkSpawnersTag.hasKey("entityID")) {
            return getEntityFromNumericalID(silkSpawnersTag.getShort("entityID"));
        }

        return null;
    }

    @Override
    @Nullable
    public String getVanillaNBTEntityID(final ItemStack item) {
        net.minecraft.server.v1_9_R1.ItemStack itemStack = null;
        final CraftItemStack craftStack = CraftItemStack.asCraftCopy(item);
        itemStack = CraftItemStack.asNMSCopy(craftStack);
        NBTTagCompound tag = itemStack.getTag();

        if (tag == null || !tag.hasKey("BlockEntityTag")) {
            return null;
        }

        tag = tag.getCompound("BlockEntityTag");
        if (tag.hasKey("EntityId")) {
            return tag.getString("EntityId");
        } else if (tag.hasKey("SpawnData") && tag.getCompound("SpawnData").hasKey("id")) {
            return tag.getCompound("SpawnData").getString("id");
        } else if (tag.hasKey("SpawnPotentials") && !tag.getList("SpawnPotentials", 8).isEmpty()) {
            return tag.getList("SpawnPotentials", 8).get(0).getCompound("Entity").getString("id");
        } else {
            return null;
        }
    }

    /**
     * Return the spawner block the player is looking at, or null if isn't.
     *
     * @param player the player
     * @param distance the reach distance
     * @return the found block or null
     */
    @Override
    public Block getSpawnerFacing(final Player player, final int distance) {
        final Block block = player.getTargetBlock((Set<Material>) null, distance);
        if (block == null || block.getType() != Material.MOB_SPAWNER) {
            return null;
        }
        return block;
    }

    @Override
    public ItemStack newEggItem(final String entityID, final int amount, final String displayName) {
        final ItemStack item = new ItemStack(Material.MONSTER_EGG, amount);
        net.minecraft.server.v1_9_R1.ItemStack itemStack = null;
        final CraftItemStack craftStack = CraftItemStack.asCraftCopy(item);
        itemStack = CraftItemStack.asNMSCopy(craftStack);
        NBTTagCompound tag = itemStack.getTag();

        // Create tag if necessary
        if (tag == null) {
            tag = new NBTTagCompound();
            itemStack.setTag(tag);
        }

        if (!tag.hasKey("SilkSpawners")) {
            tag.set("SilkSpawners", new NBTTagCompound());
        }

        tag.getCompound("SilkSpawners").setString("entity", entityID);

        if (!tag.hasKey("EntityTag")) {
            tag.set("EntityTag", new NBTTagCompound());
        }
        tag.getCompound("EntityTag").setString("id", entityID);

        return CraftItemStack.asCraftMirror(itemStack);
    }

    @Override
    public String getVanillaEggNBTEntityID(final ItemStack item) {
        net.minecraft.server.v1_9_R1.ItemStack itemStack = null;
        final CraftItemStack craftStack = CraftItemStack.asCraftCopy(item);
        itemStack = CraftItemStack.asNMSCopy(craftStack);
        NBTTagCompound tag = itemStack.getTag();

        if (tag == null || !tag.hasKey("EntityTag")) {
            return null;
        }

        tag = tag.getCompound("EntityTag");
        if (tag.hasKey("id")) {
            return tag.getString("id");
        }
        return null;
    }

    @Override
    public void displayBossBar(final String title, final String colorName, final String styleName, final Player player, final Plugin plugin,
            final int period) {
        final BarColor color = BarColor.valueOf(colorName.toUpperCase());
        final BarStyle style = BarStyle.valueOf(styleName.toUpperCase());
        final BossBar bar = Bukkit.createBossBar(title, color, style);
        bar.addPlayer(player);
        bar.setVisible(true);
        final double interval = 1.0 / (period * 20L);
        new BukkitRunnable() {
            @Override
            public void run() {
                final double progress = bar.getProgress();
                final double newProgress = progress - interval;
                if (progress <= 0.0 || newProgress <= 0.0) {
                    bar.setVisible(false);
                    bar.removeAll();
                    this.cancel();
                } else {
                    bar.setProgress(newProgress);
                }
            }
        }.runTaskTimerAsynchronously(plugin, 0, 1L);
    }

    @Override
    public Player getPlayer(final String playerUUIDOrName) {
        try {
            // Try if the String could be an UUID
            final UUID playerUUID = UUID.fromString(playerUUIDOrName);
            return Bukkit.getPlayer(playerUUID);
        } catch (@SuppressWarnings("unused") final IllegalArgumentException e) {
            for (final Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                if (onlinePlayer.getName().equalsIgnoreCase(playerUUIDOrName)) {
                    return onlinePlayer;
                }
            }
        }
        return null;
    }

    @Override
    public ItemStack getItemInHand(final Player player) {
        return player.getInventory().getItemInMainHand();
    }

    @Override
    public void reduceEggs(final Player player) {
        final ItemStack itemInMainHand = player.getInventory().getItemInMainHand();
        final ItemStack itemInOffHand = player.getInventory().getItemInOffHand();
        ItemStack eggs;
        if (itemInMainHand.getType() == Material.MONSTER_EGG) {
            eggs = itemInMainHand;
            // Make it empty
            if (eggs.getAmount() == 1) {
                player.getInventory().setItemInMainHand(null);
            } else {
                // Reduce egg
                eggs.setAmount(eggs.getAmount() - 1);
                player.getInventory().setItemInMainHand(eggs);
            }
        } else {
            eggs = itemInOffHand;
            // Make it empty
            if (eggs.getAmount() == 1) {
                player.getInventory().setItemInOffHand(null);
            } else {
                // Reduce egg
                eggs.setAmount(eggs.getAmount() - 1);
                player.getInventory().setItemInOffHand(eggs);
            }
        }
    }

    @Override
    public ItemStack getSpawnerItemInHand(final Player player) {
        final PlayerInventory inv = player.getInventory();
        final ItemStack mainHand = inv.getItemInMainHand();
        final ItemStack offHand = inv.getItemInOffHand();
        if ((mainHand.getType() == Material.MONSTER_EGG || mainHand.getType() == Material.MOB_SPAWNER)
                && (offHand.getType() == Material.MONSTER_EGG || offHand.getType() == Material.MOB_SPAWNER)) {
            return null; // not determinable
        } else if (mainHand.getType() == Material.MONSTER_EGG || mainHand.getType() == Material.MOB_SPAWNER) {
            return mainHand;
        } else if (offHand.getType() == Material.MONSTER_EGG || offHand.getType() == Material.MOB_SPAWNER) {
            return offHand;
        }
        return null;
    }

    @Override
    public void setSpawnerItemInHand(final Player player, final ItemStack newItem) {
        final PlayerInventory inv = player.getInventory();
        final ItemStack mainHand = inv.getItemInMainHand();
        final ItemStack offHand = inv.getItemInOffHand();
        if ((mainHand.getType() == Material.MONSTER_EGG || mainHand.getType() == Material.MOB_SPAWNER)
                && (offHand.getType() == Material.MONSTER_EGG || offHand.getType() == Material.MOB_SPAWNER)) {
            return; // not determinable
        } else if (mainHand.getType() == Material.MONSTER_EGG || mainHand.getType() == Material.MOB_SPAWNER) {
            inv.setItemInMainHand(newItem);
        } else if (offHand.getType() == Material.MONSTER_EGG || offHand.getType() == Material.MOB_SPAWNER) {
            inv.setItemInOffHand(newItem);
        }
    }

    @Override
    public Collection<Material> getSpawnEggMaterials() {
        return Collections.singleton(Material.MONSTER_EGG);
    }

    @Override
    public Material getSpawnerMaterial() {
        return Material.MOB_SPAWNER;
    }

    @Override
    public Material getIronFenceMaterial() {
        return Material.IRON_FENCE;
    }

    @Override
    public Player loadPlayer(final OfflinePlayer offline) {
        if (!offline.hasPlayedBefore()) {
            return null;
        }

        final GameProfile profile = new GameProfile(offline.getUniqueId(),
                offline.getName() != null ? offline.getName() : offline.getUniqueId().toString());
        final MinecraftServer server = ((CraftServer) Bukkit.getServer()).getServer();
        final EntityPlayer entity = new EntityPlayer(server, server.getWorldServer(0), profile,
                new PlayerInteractManager(server.getWorldServer(0)));

        final Player target = entity.getBukkitEntity();
        if (target != null) {
            target.loadData();
        }
        return target;
    }

    @Nullable
    private String getEntityFromNumericalID(final int numericalEntityID) {
        return sortedMap.get(numericalEntityID);
    }
}
