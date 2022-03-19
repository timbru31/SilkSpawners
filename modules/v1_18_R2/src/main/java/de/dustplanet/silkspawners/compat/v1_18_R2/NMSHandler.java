package de.dustplanet.silkspawners.compat.v1_18_R2;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
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
import org.bukkit.craftbukkit.v1_18_R2.CraftServer;
import org.bukkit.craftbukkit.v1_18_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_18_R2.block.CraftBlockEntityState;
import org.bukkit.craftbukkit.v1_18_R2.block.CraftCreatureSpawner;
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_18_R2.inventory.CraftItemStack;
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
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundRotateHeadPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerPlayerConnection;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;

public class NMSHandler implements NMSProvider {
    private Field tileField;
    private final Collection<Material> spawnEggs = Arrays.stream(Material.values())
            .filter(material -> material.name().endsWith("_SPAWN_EGG")).collect(Collectors.toList());

    public NMSHandler() {
        this(true);
    }

    public NMSHandler(final boolean checkForNerfFlags) {
        try {
            tileField = CraftCreatureSpawner.class.getDeclaredField("snapshot");
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
            final ServerLevel handle = ((CraftWorld) Bukkit.getServer().getWorlds().get(0)).getHandle();

            try {
                final SpigotWorldConfig spigotConfig = handle.spigotConfig;
                if (spigotConfig.nerfSpawnerMobs) {
                    Bukkit.getLogger().warning(
                            "[SilkSpawners] Warning! \"nerf-spawner-mobs\" is set to true in the spigot.yml! Spawned mobs WON'T HAVE ANY AI!");
                }
            } catch (@SuppressWarnings("unused") final NoSuchFieldError e) {
                // Silence
            }

            try {
                final Field paperConfigField = Level.class.getDeclaredField("paperConfig");
                paperConfigField.setAccessible(true);

                final Field ironGolemsCanSpawnInAirField = paperConfigField.getType().getDeclaredField("ironGolemsCanSpawnInAir");
                ironGolemsCanSpawnInAirField.setAccessible(true);
                if (!ironGolemsCanSpawnInAirField.getBoolean(paperConfigField.get(handle))) {
                    Bukkit.getLogger().warning(
                            "[SilkSpawners] Warning! \"iron-golems-can-spawn-in-air\" is set to false in the paper.yml! Iron Golem farms might not work!");
                }
            } catch (@SuppressWarnings("unused") final NoSuchFieldError | IllegalArgumentException | IllegalAccessException
                    | NoSuchFieldException | SecurityException e) {
                // Silence
            }
        }
    }

    @SuppressWarnings("resource")
    @Override
    public void spawnEntity(final org.bukkit.World w, final String entityID, final double x, final double y, final double z,
            final Player player) {
        final CompoundTag tag = new CompoundTag();
        tag.putString("id", entityID);

        final ServerLevel world = ((CraftWorld) w).getHandle();
        final Optional<Entity> entity = EntityType.create(tag, world);

        if (!entity.isPresent()) {
            Bukkit.getLogger().warning("[SilkSpawners] Failed to spawn, falling through. You should report this (entity == null)!");
            return;
        }

        final float yaw = world.random.nextFloat() * (-180 - 180) + 180;
        entity.get().moveTo(x, y, z, yaw, 0);
        ((CraftWorld) w).addEntity(entity.get(), SpawnReason.SPAWNER_EGG);
        final Packet<ClientGamePacketListener> rotationPacket = new ClientboundRotateHeadPacket(entity.get(), (byte) yaw);
        final ServerPlayerConnection connection = ((CraftPlayer) player).getHandle().connection;
        connection.send(rotationPacket);
    }

    @Override
    public List<String> rawEntityMap() {
        final List<String> entities = new ArrayList<>();
        try {
            final Registry<EntityType<?>> entityTypeRegistry = Registry.ENTITY_TYPE;
            final Iterator<EntityType<?>> iterator = entityTypeRegistry.iterator();
            while (iterator.hasNext()) {
                final EntityType<?> next = iterator.next();
                entities.add(EntityType.getKey(next).getPath());
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
            final SpawnerBlockEntity tile = (SpawnerBlockEntity) tileField.get(spawner);
            final CompoundTag resourceLocation = tile.getSpawner().nextSpawnData.entityToSpawn();
            return resourceLocation != null ? resourceLocation.getString("id").replace("minecraft:", "") : "";
        } catch (IllegalArgumentException | IllegalAccessException e) {
            Bukkit.getLogger().warning("[SilkSpawners] Reflection failed: " + e.getMessage());
            e.printStackTrace();
        }
        return "";
    }

    @Override
    public void setSpawnersUnstackable() {
        final ResourceLocation resourceLocation = new ResourceLocation(NAMESPACED_SPAWNER_ID);
        final Registry<Item> itemRegistry = Registry.ITEM;
        final Item spawner = itemRegistry.get(resourceLocation);
        try {
            final Field maxStackSize = Item.class.getDeclaredField("maxStackSize");
            maxStackSize.setAccessible(true);
            maxStackSize.set(spawner, 1);
        } catch (@SuppressWarnings("unused") NoSuchFieldException | SecurityException | IllegalArgumentException
                | IllegalAccessException e) {
            try {
                // int maxStackSize -> d
                final Field maxStackSize = Item.class.getDeclaredField("d");
                maxStackSize.setAccessible(true);
                maxStackSize.set(spawner, 1);
            } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e1) {
                e1.printStackTrace();
            }
        }
    }

    @Override
    public boolean setMobNameOfSpawner(final BlockState blockState, final String mobID) {
        // Prevent ResourceKeyInvalidException: Non [a-z0-9/._-] character in path of location
        final String safeMobID = caseFormatOf(mobID.replace(" ", "_")).to(CaseFormat.LOWER_UNDERSCORE, mobID.replace(" ", "_"))
                .toLowerCase(Locale.ENGLISH);
        final CraftCreatureSpawner spawner = (CraftCreatureSpawner) blockState;

        try {
            final SpawnerBlockEntity tile = (SpawnerBlockEntity) tileField.get(spawner);
            final Registry<EntityType<?>> entityTypeRegistry = Registry.ENTITY_TYPE;
            final ResourceLocation resourceLocation = new ResourceLocation(safeMobID);
            tile.getSpawner().setEntityId(entityTypeRegistry.get(resourceLocation));
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

        net.minecraft.world.item.ItemStack itemStack = null;
        final CraftItemStack craftStack = CraftItemStack.asCraftCopy(item);
        itemStack = CraftItemStack.asNMSCopy(craftStack);
        CompoundTag tag = itemStack.getOrCreateTag();

        // Check for SilkSpawners key
        if (!tag.contains("SilkSpawners")) {
            tag.put("SilkSpawners", new CompoundTag());
        }

        tag.getCompound("SilkSpawners").putString("entity", entity);

        // Check for Vanilla keys
        if (!tag.contains("BlockEntityTag")) {
            tag.put("BlockEntityTag", new CompoundTag());
        }

        tag = tag.getCompound("BlockEntityTag");

        // EntityId - Deprecated in 1.9
        tag.putString("EntityId", entity);
        tag.putString("id", BlockEntityType.getKey(BlockEntityType.MOB_SPAWNER).getPath());

        // SpawnData
        if (!tag.contains("SpawnData")) {
            tag.put("SpawnData", new CompoundTag());
        }
        tag.getCompound("SpawnData").putString("id", prefixedEntity);

        if (!tag.contains("SpawnPotentials")) {
            tag.put("SpawnPotentials", new CompoundTag());
        }

        // SpawnEgg data
        if (!tag.contains("EntityTag")) {
            tag.put("EntityTag", new CompoundTag());
        }
        tag.getCompound("EntityTag").putString("id", prefixedEntity);

        return CraftItemStack.asCraftMirror(itemStack);
    }

    @Override
    @Nullable
    public String getSilkSpawnersNBTEntityID(final ItemStack item) {
        net.minecraft.world.item.ItemStack itemStack = null;
        final CraftItemStack craftStack = CraftItemStack.asCraftCopy(item);
        itemStack = CraftItemStack.asNMSCopy(craftStack);
        final CompoundTag tag = itemStack.getTag();

        if (tag == null || !tag.contains("SilkSpawners")) {
            return null;
        }
        return tag.getCompound("SilkSpawners").getString("entity");
    }

    @Override
    @Nullable
    public String getVanillaNBTEntityID(final ItemStack item) {
        net.minecraft.world.item.ItemStack itemStack = null;
        final CraftItemStack craftStack = CraftItemStack.asCraftCopy(item);
        itemStack = CraftItemStack.asNMSCopy(craftStack);
        CompoundTag tag = itemStack.getTag();

        if (tag == null || !tag.contains("BlockEntityTag")) {
            return null;
        }

        tag = tag.getCompound("BlockEntityTag");
        if (tag.contains("EntityId")) {
            return tag.getString("EntityId");
        } else if (tag.contains("SpawnData") && tag.getCompound("SpawnData").contains("id")) {
            return tag.getCompound("SpawnData").getString("id");
        } else if (tag.contains("SpawnPotentials") && !tag.getList("SpawnPotentials", 8).isEmpty()) {
            return tag.getList("SpawnPotentials", 8).getCompound(0).getCompound("Entity").getString("id");
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
        net.minecraft.world.item.ItemStack itemStack = null;
        final CraftItemStack craftStack = CraftItemStack.asCraftCopy(item);
        itemStack = CraftItemStack.asNMSCopy(craftStack);
        final CompoundTag tag = itemStack.getOrCreateTag();

        if (!tag.contains("SilkSpawners")) {
            tag.put("SilkSpawners", new CompoundTag());
        }

        tag.getCompound("SilkSpawners").putString("entity", entityID);

        if (!tag.contains("EntityTag")) {
            tag.put("EntityTag", new CompoundTag());
        }

        String prefixedEntity;
        if (!entityID.startsWith("minecraft:")) {
            prefixedEntity = "minecraft:" + entityID;
        } else {
            prefixedEntity = entityID;
        }
        tag.getCompound("EntityTag").putString("id", prefixedEntity);

        return CraftItemStack.asCraftMirror(itemStack);
    }

    @Override
    public String getVanillaEggNBTEntityID(final ItemStack item) {
        net.minecraft.world.item.ItemStack itemStack = null;
        final CraftItemStack craftStack = CraftItemStack.asCraftCopy(item);
        itemStack = CraftItemStack.asNMSCopy(craftStack);
        CompoundTag tag = itemStack.getTag();

        if (tag == null || !tag.contains("EntityTag")) {
            final Registry<Item> itemRegistry = Registry.ITEM;
            final ResourceLocation vanillaKey = itemRegistry.getKey(itemStack.getItem());
            if (vanillaKey != null) {
                return vanillaKey.getPath().replace("minecraft:", "").replace("_spawn_egg", "");
            }
        } else {
            tag = tag.getCompound("EntityTag");
            if (tag.contains("id")) {
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
        final ServerPlayer entity = new ServerPlayer(server, server.getLevel(Level.OVERWORLD), profile);

        final Player target = entity.getBukkitEntity();
        if (target != null) {
            target.loadData();
        }
        return target;
    }

}
