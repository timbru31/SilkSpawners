package de.dustplanet.silkspawners.compat.v1_21_R5;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
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
import org.bukkit.craftbukkit.v1_21_R5.CraftServer;
import org.bukkit.craftbukkit.v1_21_R5.CraftWorld;
import org.bukkit.craftbukkit.v1_21_R5.block.CraftBlockEntityState;
import org.bukkit.craftbukkit.v1_21_R5.block.CraftCreatureSpawner;
import org.bukkit.craftbukkit.v1_21_R5.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_21_R5.inventory.CraftItemStack;
import org.bukkit.entity.AbstractWindCharge;
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
import com.mojang.logging.LogUtils;

import de.dustplanet.silkspawners.compat.api.NMSProvider;
import net.minecraft.core.Holder.Reference;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundRotateHeadPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerPlayerConnection;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.phys.Vec3;

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

        final ServerPlayer sp = ((CraftPlayer) player).getHandle();

        final ServerLevel world = ((CraftWorld) w).getHandle();
        final ProblemReporter.ScopedCollector problemReporter = new ProblemReporter.ScopedCollector(sp.problemPath(), LogUtils.getLogger());
        final ValueInput valueInput = TagValueInput.create(problemReporter, world.registryAccess(), tag);
        final Optional<Entity> entity = EntityType.create(valueInput, world, EntitySpawnReason.SPAWN_ITEM_USE);

        if (!entity.isPresent()) {
            Bukkit.getLogger().warning("[SilkSpawners] Failed to spawn, falling through. You should report this (entity == null)!");
            return;
        }

        final float yaw = world.random.nextFloat() * (-180 - 180) + 180;
        final Entity spawnedEntity = entity.get();
        // Use moveOrInterpolateTo with a Vec3 target position
        spawnedEntity.moveOrInterpolateTo(new Vec3(x, y, z), 1.0f, 0.0f); // 1.0f for instant move, 0.0f to skip interpolation
        spawnedEntity.setYRot(yaw); // Set the yaw (horizontal rotation)
        spawnedEntity.setXRot(0); // Set the pitch (vertical rotation, 0 as before)
        ((CraftWorld) w).addEntity(entity.get(), SpawnReason.SPAWNER_EGG);
        final Packet<ClientGamePacketListener> rotationPacket = new ClientboundRotateHeadPacket(entity.get(), (byte) yaw);
        final ServerPlayerConnection connection = ((CraftPlayer) player).getHandle().connection;
        connection.send(rotationPacket);
    }

    @Override
    public List<String> rawEntityMap() {
        final List<String> entities = new ArrayList<>();
        try {
            final Registry<EntityType<?>> entityTypeRegistry = BuiltInRegistries.ENTITY_TYPE;
            for (final EntityType<?> next : entityTypeRegistry) {
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
            return resourceLocation != null ? resourceLocation.getString("id").orElse("").replace("minecraft:", "") : "";
        } catch (IllegalArgumentException | IllegalAccessException e) {
            Bukkit.getLogger().warning("[SilkSpawners] Reflection failed: " + e.getMessage());
            e.printStackTrace();
        }
        return "";
    }

    @Override
    public void setSpawnersUnstackable() {
        final ResourceLocation resourceLocation = ResourceLocation.fromNamespaceAndPath("minecraft", NAMESPACED_SPAWNER_ID);
        final Registry<Item> itemRegistry = BuiltInRegistries.ITEM;
        final Optional<Reference<Item>> spawner = itemRegistry.get(resourceLocation);
        if (spawner.isEmpty()) {
            return;
        }
        final DataComponentMap currentComponents = spawner.get().value().components();
        final DataComponentMap updatedComponents = DataComponentMap.composite(currentComponents,
                DataComponentMap.builder().set(DataComponents.MAX_STACK_SIZE, 1).build());
        try {
            final Field components = Item.class.getDeclaredField("components");
            components.setAccessible(true);
            components.set(spawner, updatedComponents);
        } catch (@SuppressWarnings("unused") NoSuchFieldException | SecurityException | IllegalArgumentException
                | IllegalAccessException e) {
            try {
                // DataComponentMap components -> c
                final Field components = Item.class.getDeclaredField("c");
                components.setAccessible(true);
                components.set(spawner.get().value(), updatedComponents);
            } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e1) {
                e1.printStackTrace();
            }
        }
    }

    @SuppressWarnings("resource")
    @Override
    public boolean setMobNameOfSpawner(final BlockState blockState, final String mobID) {
        // Prevent ResourceKeyInvalidException: Non [a-z0-9/._-] character in path of location
        final String safeMobID = caseFormatOf(mobID.replace(" ", "_")).to(CaseFormat.LOWER_UNDERSCORE, mobID.replace(" ", "_"))
                .toLowerCase(Locale.ENGLISH);
        final CraftCreatureSpawner spawner = (CraftCreatureSpawner) blockState;

        try {
            final SpawnerBlockEntity tile = (SpawnerBlockEntity) tileField.get(spawner);
            final Registry<EntityType<?>> entityTypeRegistry = BuiltInRegistries.ENTITY_TYPE;
            final ResourceLocation resourceLocation = ResourceLocation.fromNamespaceAndPath("minecraft", safeMobID);
            final Optional<Reference<EntityType<?>>> entityType = entityTypeRegistry.get(resourceLocation);
            if (entityType.isEmpty()) {
                return false;
            }
            tile.getSpawner().setEntityId(entityType.get().value(), spawner.getWorldHandle().getMinecraftWorld(),
                    spawner.getWorldHandle().getRandom(), spawner.getPosition());
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
        final CustomData blockData = itemStack.getOrDefault(DataComponents.BLOCK_ENTITY_DATA, CustomData.EMPTY);
        final CustomData customData = itemStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        final CompoundTag tag = blockData.copyTag();
        final CompoundTag customTag = customData.copyTag();

        // Check for SilkSpawners key
        if (!customTag.contains("SilkSpawners")) {
            customTag.put("SilkSpawners", new CompoundTag());
        }

        // Get or create the SilkSpawners compound tag
        final CompoundTag silkSpawnersTag = customTag.getCompound("SilkSpawners").orElse(new CompoundTag());
        customTag.put("SilkSpawners", silkSpawnersTag); // Ensure it's stored back
        silkSpawnersTag.put("entity", StringTag.valueOf(entity)); // Replace putString with put and StringTag

        // EntityId - Deprecated in 1.9
        tag.put("EntityId", StringTag.valueOf(entity));
        tag.put("id", StringTag.valueOf(BlockEntityType.getKey(BlockEntityType.MOB_SPAWNER).getPath()));

        // SpawnData
        if (!tag.contains("SpawnData")) {
            tag.put("SpawnData", new CompoundTag());
        }

        // Get or create the SpawnData compound tag
        final CompoundTag spawnDataTag = tag.getCompound("SpawnData").orElse(new CompoundTag());
        tag.put("SpawnData", spawnDataTag); // Ensure it's stored back
        if (!spawnDataTag.contains("entity")) {
            spawnDataTag.put("entity", new CompoundTag());
        }

        // Get or create the entity compound tag inside SpawnData
        final CompoundTag entityTag = spawnDataTag.getCompound("entity").orElse(new CompoundTag());
        spawnDataTag.put("entity", entityTag); // Ensure it's stored back
        entityTag.put("id", StringTag.valueOf(prefixedEntity));

        // SpawnPotentials
        if (!tag.contains("SpawnPotentials")) {
            tag.put("SpawnPotentials", new ListTag());
        }

        // SpawnEgg data
        if (!tag.contains("EntityTag")) {
            tag.put("EntityTag", new CompoundTag());
        }

        // Get or create the EntityTag compound tag
        final CompoundTag entityTagForEgg = tag.getCompound("EntityTag").orElse(new CompoundTag());
        tag.put("EntityTag", entityTagForEgg); // Ensure it's stored back
        entityTagForEgg.put("id", StringTag.valueOf(prefixedEntity));

        itemStack.set(DataComponents.BLOCK_ENTITY_DATA, CustomData.of(tag));
        itemStack.set(DataComponents.CUSTOM_DATA, CustomData.of(customTag));
        return CraftItemStack.asCraftMirror(itemStack);
    }

    @Override
    @Nullable
    public String getSilkSpawnersNBTEntityID(final ItemStack item) {
        net.minecraft.world.item.ItemStack itemStack = null;
        final CraftItemStack craftStack = CraftItemStack.asCraftCopy(item);
        itemStack = CraftItemStack.asNMSCopy(craftStack);
        final CustomData blockEntityData = itemStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        final CompoundTag tag = blockEntityData.copyTag();

        if (!tag.contains("SilkSpawners")) {
            return null;
        }
        return tag.getCompound("SilkSpawners").flatMap(silkSpawnersTag -> silkSpawnersTag.getString("entity")).orElse(null);
    }

    @Override
    @Nullable
    public String getVanillaNBTEntityID(final ItemStack item) {
        net.minecraft.world.item.ItemStack itemStack = null;
        final CraftItemStack craftStack = CraftItemStack.asCraftCopy(item);
        itemStack = CraftItemStack.asNMSCopy(craftStack);
        final CustomData blockEntityData = itemStack.getOrDefault(DataComponents.BLOCK_ENTITY_DATA, CustomData.EMPTY);
        final CompoundTag tag = blockEntityData.copyTag();

        // Check for EntityId
        if (tag.contains("EntityId")) {
            return tag.getString("EntityId").orElse(null);
        }

        // Check for SpawnData -> id
        if (tag.contains("SpawnData")) {
            final Optional<CompoundTag> spawnDataOpt = tag.getCompound("SpawnData");
            if (spawnDataOpt.isPresent()) {
                final CompoundTag spawnData = spawnDataOpt.get();
                if (spawnData.contains("id")) {
                    return spawnData.getString("id").orElse(null);
                }

                // Check for SpawnData -> entity -> id
                if (spawnData.contains("entity")) {
                    final Optional<CompoundTag> entityOpt = spawnData.getCompound("entity");
                    if (entityOpt.isPresent() && entityOpt.get().contains("id")) {
                        return entityOpt.get().getString("id").orElse(null);
                    }
                }
            }
        }

        // Check for SpawnPotentials
        if (tag.contains("SpawnPotentials")) {
            final ListTag spawnPotentials = tag.getListOrEmpty("SpawnPotentials"); // 8 is the type ID for CompoundTag
            if (spawnPotentials != null && !spawnPotentials.isEmpty()) {
                final CompoundTag potential = spawnPotentials.getCompoundOrEmpty(0);
                final Optional<CompoundTag> entityOpt = potential.getCompound("Entity");
                if (entityOpt.isPresent()) {
                    return entityOpt.get().getString("id").orElse(null);
                }
            }
        }

        return null;
    }

    @Override
    public String getOtherPluginsNBTEntityID(final ItemStack item) {
        net.minecraft.world.item.ItemStack itemStack = null;
        final CraftItemStack craftStack = CraftItemStack.asCraftCopy(item);
        itemStack = CraftItemStack.asNMSCopy(craftStack);
        final CustomData blockEntityData = itemStack.getOrDefault(DataComponents.ENTITY_DATA, CustomData.EMPTY);
        final CompoundTag tag = blockEntityData.copyTag();

        if (tag == null) {
            return null;
        }
        if (tag.contains("ms_mob")) {
            return tag.getString("ms_mob").orElse(null);
        }
        return null;
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
        final CustomData blockData = itemStack.getOrDefault(DataComponents.ENTITY_DATA, CustomData.EMPTY);
        final CustomData customData = itemStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        final CompoundTag tag = blockData.copyTag();
        final CompoundTag customTag = customData.copyTag();

        if (!customTag.contains("SilkSpawners")) {
            customTag.put("SilkSpawners", new CompoundTag());
        }

        // Assuming customTag is a CompoundTag
        if (!customTag.contains("SilkSpawners")) {
            customTag.put("SilkSpawners", new CompoundTag());
        }
        final CompoundTag silkSpawnersTag = customTag.getCompound("SilkSpawners").orElse(new CompoundTag());
        silkSpawnersTag.put("entity", StringTag.valueOf(entityID));
        customTag.put("SilkSpawners", silkSpawnersTag); // Ensure the updated tag is stored back

        String prefixedEntity;
        if (!entityID.startsWith("minecraft:")) {
            prefixedEntity = "minecraft:" + entityID;
        } else {
            prefixedEntity = entityID;
        }
        tag.putString("id", prefixedEntity);

        itemStack.set(DataComponents.ENTITY_DATA, CustomData.of(tag));
        itemStack.set(DataComponents.CUSTOM_DATA, CustomData.of(customTag));
        return CraftItemStack.asCraftMirror(itemStack);
    }

    @Override
    public String getVanillaEggNBTEntityID(final ItemStack item) {
        net.minecraft.world.item.ItemStack itemStack = null;
        final CraftItemStack craftStack = CraftItemStack.asCraftCopy(item);
        itemStack = CraftItemStack.asNMSCopy(craftStack);
        final CustomData blockEntityData = itemStack.getOrDefault(DataComponents.ENTITY_DATA, CustomData.EMPTY);
        final CompoundTag tag = blockEntityData.copyTag();

        if (tag.contains("id")) {
            return tag.getString("id").orElse(null).replace("minecraft:", "");
        }

        final Registry<Item> itemRegistry = BuiltInRegistries.ITEM;
        final ResourceLocation vanillaKey = itemRegistry.getKey(itemStack.getItem());
        if (vanillaKey != null) {
            return vanillaKey.getPath().replace("minecraft:", "").replace("_spawn_egg", "");
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
        final ServerPlayer entity = new ServerPlayer(server, server.getLevel(Level.OVERWORLD), profile, ClientInformation.createDefault());

        final Player target = entity.getBukkitEntity();
        if (target != null) {
            target.loadData();
        }
        return target;
    }

    @Override
    public boolean isWindCharge(final org.bukkit.entity.Entity entity) {
        return entity instanceof AbstractWindCharge;
    }

}
