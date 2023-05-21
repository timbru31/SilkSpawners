package de.dustplanet.silkspawners.compat.v1_13_R2;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

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
import org.bukkit.craftbukkit.v1_13_R2.CraftServer;
import org.bukkit.craftbukkit.v1_13_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_13_R2.block.CraftBlockEntityState;
import org.bukkit.craftbukkit.v1_13_R2.block.CraftCreatureSpawner;
import org.bukkit.craftbukkit.v1_13_R2.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_13_R2.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.spigotmc.SpigotWorldConfig;

import com.google.common.base.CaseFormat;
import com.mojang.authlib.GameProfile;

import de.dustplanet.silkspawners.compat.api.NMSProvider;
import net.minecraft.server.v1_13_R2.DimensionManager;
import net.minecraft.server.v1_13_R2.Entity;
import net.minecraft.server.v1_13_R2.EntityPlayer;
import net.minecraft.server.v1_13_R2.EntityTypes;
import net.minecraft.server.v1_13_R2.IRegistry;
import net.minecraft.server.v1_13_R2.Item;
import net.minecraft.server.v1_13_R2.MinecraftKey;
import net.minecraft.server.v1_13_R2.MinecraftServer;
import net.minecraft.server.v1_13_R2.NBTTagCompound;
import net.minecraft.server.v1_13_R2.PacketPlayOutEntityHeadRotation;
import net.minecraft.server.v1_13_R2.PlayerInteractManager;
import net.minecraft.server.v1_13_R2.TileEntityMobSpawner;
import net.minecraft.server.v1_13_R2.TileEntityTypes;
import net.minecraft.server.v1_13_R2.World;
import net.minecraft.server.v1_13_R2.WorldServer;

public class NMSHandler implements NMSProvider {
    private Field tileField;
    private final Collection<Material> spawnEggs = Arrays.stream(Material.values())
            .filter(material -> material.name().endsWith("_SPAWN_EGG")).collect(Collectors.toList());

    public NMSHandler() {
        this(true);
    }

    public NMSHandler(final boolean checkForNerfFlags) {
        try {
            tileField = CraftCreatureSpawner.class.getDeclaredField("spawner");
            tileField.setAccessible(true);
        } catch (SecurityException | NoSuchFieldException e) {
            try {
                tileField = CraftBlockEntityState.class.getDeclaredField("snapshot");
                tileField.setAccessible(true);
            } catch (NoSuchFieldException | SecurityException e1) {
                Bukkit.getLogger().warning("[SilkSpawners] Reflection failed: " + e.getMessage() + " " + e1.getMessage());
                e.printStackTrace();
                e1.printStackTrace();
            }
        }

        if (checkForNerfFlags) {
            @SuppressWarnings("resource")
            final WorldServer handle = ((CraftWorld) Bukkit.getServer().getWorlds().get(0)).getHandle();

            try {
                final SpigotWorldConfig spigotConfig = handle.spigotConfig;
                if (spigotConfig.nerfSpawnerMobs) {
                    Bukkit.getLogger().warning(
                            "[SilkSpawners] Warning! \"nerf-spawner-mobs\" is set to true in the spigot.yml! Spawned mobs WON'T HAVE ANY AI!");
                }
            } catch (@SuppressWarnings("unused") final NoSuchFieldError e) {
                // Silence
            }
        }
    }

    @SuppressWarnings("resource")
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
    public List<String> rawEntityMap() {
        final List<String> entities = new ArrayList<>();
        try {
            final IRegistry<EntityTypes<?>> entityTypeRegistry = IRegistry.ENTITY_TYPE;
            final Iterator<EntityTypes<?>> iterator = entityTypeRegistry.iterator();
            while (iterator.hasNext()) {
                final EntityTypes<?> next = iterator.next();
                entities.add(EntityTypes.getName(next).getKey());
            }
        } catch (SecurityException | IllegalArgumentException e) {
            Bukkit.getLogger().severe("[SilkSpawners] Failed to dump entity map: " + e.getMessage());
            e.printStackTrace();
        }
        return entities;
    }

    @Override
    public String getMobNameOfSpawner(final BlockState blockState) {
        final CraftCreatureSpawner spawner = (CraftCreatureSpawner) blockState;
        try {
            final TileEntityMobSpawner tile = (TileEntityMobSpawner) tileField.get(spawner);
            final MinecraftKey minecraftKey = tile.getSpawner().getMobName();
            return minecraftKey != null ? minecraftKey.getKey() : "";
        } catch (IllegalArgumentException | IllegalAccessException e) {
            Bukkit.getLogger().warning("[SilkSpawners] Reflection failed: " + e.getMessage());
            e.printStackTrace();
        }
        return "";
    }

    @Override
    public void setSpawnersUnstackable() {
        try {
            final Item spawner = IRegistry.ITEM.get(new MinecraftKey(NAMESPACED_SPAWNER_ID));
            final Field maxStackSize = Item.class.getDeclaredField("maxStackSize");
            maxStackSize.setAccessible(true);
            maxStackSize.set(spawner, 1);
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean setMobNameOfSpawner(final BlockState blockState, final String mobID) {
        // Prevent ResourceKeyInvalidException: Non [a-z0-9/._-] character in path of location
        final String safeMobID = caseFormatOf(mobID.replace(" ", "_")).to(CaseFormat.LOWER_UNDERSCORE, mobID.replace(" ", "_"))
                .toLowerCase(Locale.ENGLISH);
        final CraftCreatureSpawner spawner = (CraftCreatureSpawner) blockState;

        try {
            final TileEntityMobSpawner tile = (TileEntityMobSpawner) tileField.get(spawner);
            tile.getSpawner().setMobName(IRegistry.ENTITY_TYPE.get(new MinecraftKey(safeMobID)));
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

        String prefixedEntity;
        if (!entity.startsWith("minecraft:")) {
            prefixedEntity = "minecraft:" + entity;
        } else {
            prefixedEntity = entity;
        }

        net.minecraft.server.v1_13_R2.ItemStack itemStack = null;
        final CraftItemStack craftStack = CraftItemStack.asCraftCopy(item);
        itemStack = CraftItemStack.asNMSCopy(craftStack);
        NBTTagCompound tag = itemStack.getOrCreateTag();

        // Check for SilkSpawners key
        if (!tag.hasKey("SilkSpawners")) {
            tag.set("SilkSpawners", new NBTTagCompound());
        }

        tag.getCompound("SilkSpawners").setString("entity", entity);

        // Check for Vanilla keys
        if (!tag.hasKey("BlockEntityTag")) {
            tag.set("BlockEntityTag", new NBTTagCompound());
        }

        tag = tag.getCompound("BlockEntityTag");

        // EntityId - Deprecated in 1.9
        tag.setString("EntityId", entity);
        tag.setString("id", TileEntityTypes.a(TileEntityTypes.MOB_SPAWNER).getKey());

        // SpawnData
        if (!tag.hasKey("SpawnData")) {
            tag.set("SpawnData", new NBTTagCompound());
        }
        tag.getCompound("SpawnData").setString("id", prefixedEntity);

        if (!tag.hasKey("SpawnPotentials")) {
            tag.set("SpawnPotentials", new NBTTagCompound());
        }

        // SpawnEgg data
        if (!tag.hasKey("EntityTag")) {
            tag.set("EntityTag", new NBTTagCompound());
        }
        tag.getCompound("EntityTag").setString("id", prefixedEntity);
        return CraftItemStack.asCraftMirror(itemStack);
    }

    @Override
    @Nullable
    public String getSilkSpawnersNBTEntityID(final ItemStack item) {
        net.minecraft.server.v1_13_R2.ItemStack itemStack = null;
        final CraftItemStack craftStack = CraftItemStack.asCraftCopy(item);
        itemStack = CraftItemStack.asNMSCopy(craftStack);
        final NBTTagCompound tag = itemStack.getTag();

        if (tag == null || !tag.hasKey("SilkSpawners")) {
            return null;
        }
        return tag.getCompound("SilkSpawners").getString("entity");
    }

    @Override
    @Nullable
    public String getVanillaNBTEntityID(final ItemStack item) {
        net.minecraft.server.v1_13_R2.ItemStack itemStack = null;
        final CraftItemStack craftStack = CraftItemStack.asCraftCopy(item);
        itemStack = CraftItemStack.asNMSCopy(craftStack);
        NBTTagCompound tag = itemStack.getTag();

        if (tag == null || !tag.hasKey("BlockEntityTag")) {
            return null;
        }

        tag = tag.getCompound("BlockEntityTag");
        tag.setString("id", TileEntityTypes.a(TileEntityTypes.MOB_SPAWNER).getKey());
        if (tag.hasKey("EntityId")) {
            return tag.getString("EntityId");
        } else if (tag.hasKey("SpawnData") && tag.getCompound("SpawnData").hasKey("id")) {
            return tag.getCompound("SpawnData").getString("id");
        } else if (tag.hasKey("SpawnPotentials") && !tag.getList("SpawnPotentials", 8).isEmpty()) {
            return tag.getList("SpawnPotentials", 8).getCompound(0).getCompound("Entity").getString("id");
        } else {
            return null;
        }
    }

    /**
     * Return the spawner block the player is looking at, or null if isn't.
     *
     * @param player   the player
     * @param distance the reach distance
     * @return the found block or null
     */
    @Override
    public Block getSpawnerFacing(final Player player, final int distance) {
        final Block block = player.getTargetBlock((Set<Material>) null, distance);
        if (block == null || block.getType() != Material.SPAWNER) {
            return null;
        }
        return block;
    }

    @SuppressWarnings("deprecation")
    @Override
    public ItemStack newEggItem(final String entityID, final int amount, final String displayName) {
        Material spawnEgg = Material.matchMaterial(entityID.toUpperCase() + "_SPAWN_EGG");
        if (spawnEgg == null) {
            spawnEgg = Material.LEGACY_MONSTER_EGG;
        }

        final ItemStack item = new ItemStack(spawnEgg, amount);
        if (displayName != null) {
            final ItemMeta itemMeta = item.getItemMeta();
            itemMeta.setDisplayName(displayName);
            item.setItemMeta(itemMeta);
        }
        net.minecraft.server.v1_13_R2.ItemStack itemStack = null;
        final CraftItemStack craftStack = CraftItemStack.asCraftCopy(item);
        itemStack = CraftItemStack.asNMSCopy(craftStack);
        final NBTTagCompound tag = itemStack.getOrCreateTag();

        if (!tag.hasKey("SilkSpawners")) {
            tag.set("SilkSpawners", new NBTTagCompound());
        }

        tag.getCompound("SilkSpawners").setString("entity", entityID);

        if (!tag.hasKey("EntityTag")) {
            tag.set("EntityTag", new NBTTagCompound());
        }

        String prefixedEntity;
        if (!entityID.startsWith("minecraft:")) {
            prefixedEntity = "minecraft:" + entityID;
        } else {
            prefixedEntity = entityID;
        }
        tag.getCompound("EntityTag").setString("id", prefixedEntity);

        return CraftItemStack.asCraftMirror(itemStack);
    }

    @Override
    public String getVanillaEggNBTEntityID(final ItemStack item) {
        net.minecraft.server.v1_13_R2.ItemStack itemStack = null;
        final CraftItemStack craftStack = CraftItemStack.asCraftCopy(item);
        itemStack = CraftItemStack.asNMSCopy(craftStack);
        NBTTagCompound tag = itemStack.getTag();

        if (tag == null || !tag.hasKey("EntityTag")) {
            final MinecraftKey vanillaKey = IRegistry.ITEM.getKey(itemStack.getItem());
            if (vanillaKey != null) {
                return vanillaKey.getKey().replace("minecraft:", "").replace("_spawn_egg", "");
            }
        } else {
            tag = tag.getCompound("EntityTag");
            if (tag.hasKey("id")) {
                return tag.getString("id").replace("minecraft:", "");
            }
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
        if (getSpawnEggMaterials().contains(itemInMainHand.getType())) {
            eggs = itemInMainHand;
            if (eggs.getAmount() == 1) {
                player.getInventory().setItemInMainHand(null);
            } else {
                eggs.setAmount(eggs.getAmount() - 1);
                player.getInventory().setItemInMainHand(eggs);
            }
        } else {
            eggs = itemInOffHand;
            if (eggs.getAmount() == 1) {
                player.getInventory().setItemInOffHand(null);
            } else {
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
        if ((getSpawnEggMaterials().contains(mainHand.getType()) || mainHand.getType() == Material.SPAWNER)
                && (getSpawnEggMaterials().contains(offHand.getType()) || offHand.getType() == Material.SPAWNER)) {
            return null; // not determinable
        } else if (getSpawnEggMaterials().contains(mainHand.getType()) || mainHand.getType() == Material.SPAWNER) {
            return mainHand;
        } else if (getSpawnEggMaterials().contains(offHand.getType()) || offHand.getType() == Material.SPAWNER) {
            return offHand;
        }
        return null;
    }

    @Override
    public void setSpawnerItemInHand(final Player player, final ItemStack newItem) {
        final PlayerInventory inv = player.getInventory();
        final ItemStack mainHand = inv.getItemInMainHand();
        final ItemStack offHand = inv.getItemInOffHand();
        if ((getSpawnEggMaterials().contains(mainHand.getType()) || mainHand.getType() == Material.SPAWNER)
                && (getSpawnEggMaterials().contains(offHand.getType()) || offHand.getType() == Material.SPAWNER)) {
            return; // not determinable
        } else if (getSpawnEggMaterials().contains(mainHand.getType()) || mainHand.getType() == Material.SPAWNER) {
            inv.setItemInMainHand(newItem);
        } else if (getSpawnEggMaterials().contains(offHand.getType()) || offHand.getType() == Material.SPAWNER) {
            inv.setItemInOffHand(newItem);
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public Material getSpawnEggMaterial() {
        return Material.LEGACY_MONSTER_EGG;
    }

    @Override
    public Collection<Material> getSpawnEggMaterials() {
        return spawnEggs;
    }

    @SuppressWarnings("resource")
    @Override
    public Player loadPlayer(final OfflinePlayer offline) {
        if (!offline.hasPlayedBefore()) {
            return null;
        }

        final GameProfile profile = new GameProfile(offline.getUniqueId(),
                offline.getName() != null ? offline.getName() : offline.getUniqueId().toString());
        final MinecraftServer server = ((CraftServer) Bukkit.getServer()).getServer();
        final EntityPlayer entity = new EntityPlayer(server, server.getWorldServer(DimensionManager.OVERWORLD), profile,
                new PlayerInteractManager(server.getWorldServer(DimensionManager.OVERWORLD)));

        final Player target = entity.getBukkitEntity();
        if (target != null) {
            target.loadData();
        }
        return target;
    }

}
