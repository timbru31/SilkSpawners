package de.dustplanet.silkspawners.compat.v1_15_R1;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.craftbukkit.v1_15_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_15_R1.block.CraftBlockEntityState;
import org.bukkit.craftbukkit.v1_15_R1.block.CraftCreatureSpawner;
import org.bukkit.craftbukkit.v1_15_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import de.dustplanet.silkspawners.compat.api.NMSProvider;
import net.minecraft.server.v1_15_R1.Entity;
import net.minecraft.server.v1_15_R1.EntityTypes;
import net.minecraft.server.v1_15_R1.IRegistry;
import net.minecraft.server.v1_15_R1.Item;
import net.minecraft.server.v1_15_R1.MinecraftKey;
import net.minecraft.server.v1_15_R1.NBTTagCompound;
import net.minecraft.server.v1_15_R1.RegistryID;
import net.minecraft.server.v1_15_R1.RegistryMaterials;
import net.minecraft.server.v1_15_R1.TileEntityMobSpawner;
import net.minecraft.server.v1_15_R1.World;

public class NMSHandler implements NMSProvider {
    private Field tileField;
    private Collection<Material> spawnEggs = Arrays.stream(Material.values()).filter(material -> material.name().endsWith("_SPAWN_EGG"))
            .collect(Collectors.toList());

    public NMSHandler() {
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
    }

    @Override
    public void spawnEntity(org.bukkit.World w, String entityID, double x, double y, double z) {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setString("id", entityID);

        World world = ((CraftWorld) w).getHandle();
        Optional<Entity> entity = EntityTypes.a(tag, world);

        if (!entity.isPresent()) {
            Bukkit.getLogger().warning("[SilkSpawners] Failed to spawn, falling through. You should report this (entity == null)!");
            return;
        }

        entity.get().setPositionRotation(x, y, z, world.random.nextFloat() * 360.0f, 0.0f);
        world.addEntity(entity.get(), SpawnReason.SPAWNER_EGG);
    }

    @Override
    public List<String> rawEntityMap() {
        List<String> entities = new ArrayList<>();
        try {
            Field mapField = RegistryMaterials.class.getDeclaredField("b");
            mapField.setAccessible(true);
            IRegistry<EntityTypes<?>> entityTypeRegistry = IRegistry.ENTITY_TYPE;
            @SuppressWarnings("unchecked")
            RegistryID<EntityTypes<?>> registryID = (RegistryID<EntityTypes<?>>) mapField.get(entityTypeRegistry);
            Iterator<EntityTypes<?>> iterator = registryID.iterator();
            while (iterator.hasNext()) {
                EntityTypes<?> next = iterator.next();
                entities.add(EntityTypes.getName(next).getKey());
            }
        } catch (SecurityException | NoSuchFieldException | IllegalArgumentException | IllegalAccessException e) {
            Bukkit.getLogger().severe("[SilkSpawners] Failed to dump entity map: " + e.getMessage());
            e.printStackTrace();
        }
        return entities;
    }

    @Override
    public String getMobNameOfSpawner(BlockState blockState) {
        CraftCreatureSpawner spawner = (CraftCreatureSpawner) blockState;
        try {
            TileEntityMobSpawner tile = (TileEntityMobSpawner) tileField.get(spawner);
            MinecraftKey minecraftKey = tile.getSpawner().getMobName();
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
            Item spawner = Item.getById(SPAWNER_ID);
            Field maxStackSize = Item.class.getDeclaredField("maxStackSize");
            maxStackSize.setAccessible(true);
            maxStackSize.set(spawner, 1);
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean setMobNameOfSpawner(BlockState blockState, String mobID) {
        CraftCreatureSpawner spawner = (CraftCreatureSpawner) blockState;

        try {
            TileEntityMobSpawner tile = (TileEntityMobSpawner) tileField.get(spawner);
            tile.getSpawner().setMobName(IRegistry.ENTITY_TYPE.get(new MinecraftKey(mobID)));
            return true;
        } catch (IllegalArgumentException | IllegalAccessException e) {
            Bukkit.getLogger().warning("[SilkSpawners] Reflection failed: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public ItemStack setNBTEntityID(ItemStack item, String entity) {
        if (item == null || entity == null || entity.isEmpty()) {
            Bukkit.getLogger().warning("[SilkSpawners] Skipping invalid spawner to set NBT data on.");
            return null;
        }

        net.minecraft.server.v1_15_R1.ItemStack itemStack = null;
        CraftItemStack craftStack = CraftItemStack.asCraftCopy(item);
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

        if (!tag.getCompound("BlockEntityTag").hasKey("SpawnPotentials")) {
            tag.getCompound("BlockEntityTag").set("SpawnPotentials", new NBTTagCompound());
        }

        // SpawnEgg data
        if (!tag.hasKey("EntityTag")) {
            tag.set("EntityTag", new NBTTagCompound());
        }
        String prefixedEntity;
        if (!entity.startsWith("minecraft:")) {
            prefixedEntity = "minecraft:" + entity;
        } else {
            prefixedEntity = entity;
        }
        tag.getCompound("EntityTag").setString("id", prefixedEntity);

        return CraftItemStack.asCraftMirror(itemStack);
    }

    @Override
    @Nullable
    public String getSilkSpawnersNBTEntityID(ItemStack item) {
        net.minecraft.server.v1_15_R1.ItemStack itemStack = null;
        CraftItemStack craftStack = CraftItemStack.asCraftCopy(item);
        itemStack = CraftItemStack.asNMSCopy(craftStack);
        NBTTagCompound tag = itemStack.getTag();

        if (tag == null || !tag.hasKey("SilkSpawners")) {
            return null;
        }
        return tag.getCompound("SilkSpawners").getString("entity");
    }

    @Override
    @Nullable
    public String getVanillaNBTEntityID(ItemStack item) {
        net.minecraft.server.v1_15_R1.ItemStack itemStack = null;
        CraftItemStack craftStack = CraftItemStack.asCraftCopy(item);
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
    public Block getSpawnerFacing(Player player, int distance) {
        Block block = player.getTargetBlock((Set<Material>) null, distance);
        if (block == null || block.getType() != Material.SPAWNER) {
            return null;
        }
        return block;
    }

    @Override
    public ItemStack newEggItem(String entityID, int amount, String displayName) {
        Material spawnEgg = Material.matchMaterial(entityID.toUpperCase() + "_SPAWN_EGG");
        if (spawnEgg == null) {
            spawnEgg = Material.LEGACY_MONSTER_EGG;
        }

        ItemStack item = new ItemStack(spawnEgg, amount);
        if (displayName != null) {
            ItemMeta itemMeta = item.getItemMeta();
            itemMeta.setDisplayName(displayName);
            item.setItemMeta(itemMeta);
        }
        net.minecraft.server.v1_15_R1.ItemStack itemStack = null;
        CraftItemStack craftStack = CraftItemStack.asCraftCopy(item);
        itemStack = CraftItemStack.asNMSCopy(craftStack);
        NBTTagCompound tag = itemStack.getTag();

        if (tag == null) {
            tag = new NBTTagCompound();
            itemStack.setTag(tag);
        }

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
    public String getVanillaEggNBTEntityID(ItemStack item) {
        net.minecraft.server.v1_15_R1.ItemStack itemStack = null;
        CraftItemStack craftStack = CraftItemStack.asCraftCopy(item);
        itemStack = CraftItemStack.asNMSCopy(craftStack);
        NBTTagCompound tag = itemStack.getTag();

        if (tag == null || !tag.hasKey("EntityTag")) {
            return null;
        }

        tag = tag.getCompound("EntityTag");
        if (tag.hasKey("id")) {
            return tag.getString("id").replace("minecraft:", "");
        }
        return null;
    }

    @Override
    public void displayBossBar(String title, String colorName, String styleName, Player player, Plugin plugin, int period) {
        BarColor color = BarColor.valueOf(colorName.toUpperCase());
        BarStyle style = BarStyle.valueOf(styleName.toUpperCase());
        final BossBar bar = Bukkit.createBossBar(title, color, style);
        bar.addPlayer(player);
        bar.setVisible(true);
        final double interval = 1.0 / (period * 20L);
        new BukkitRunnable() {
            @Override
            public void run() {
                double progress = bar.getProgress();
                double newProgress = progress - interval;
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
    public Player getPlayer(String playerUUIDOrName) {
        try {
            UUID playerUUID = UUID.fromString(playerUUIDOrName);
            return Bukkit.getPlayer(playerUUID);
        } catch (@SuppressWarnings("unused") IllegalArgumentException e) {
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                if (onlinePlayer.getName().equalsIgnoreCase(playerUUIDOrName)) {
                    return onlinePlayer;
                }
            }
        }
        return null;
    }

    @Override
    public ItemStack getItemInHand(Player player) {
        return player.getInventory().getItemInMainHand();
    }

    @Override
    public void reduceEggs(Player player) {
        ItemStack itemInMainHand = player.getInventory().getItemInMainHand();
        ItemStack itemInOffHand = player.getInventory().getItemInOffHand();
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
    public ItemStack getSpawnerItemInHand(Player player) {
        PlayerInventory inv = player.getInventory();
        ItemStack mainHand = inv.getItemInMainHand();
        ItemStack offHand = inv.getItemInOffHand();
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
    public void setSpawnerItemInHand(Player player, ItemStack newItem) {
        PlayerInventory inv = player.getInventory();
        ItemStack mainHand = inv.getItemInMainHand();
        ItemStack offHand = inv.getItemInOffHand();
        if ((getSpawnEggMaterials().contains(mainHand.getType()) || mainHand.getType() == Material.SPAWNER)
                && (getSpawnEggMaterials().contains(offHand.getType()) || offHand.getType() == Material.SPAWNER)) {
            return; // not determinable
        } else if (getSpawnEggMaterials().contains(mainHand.getType()) || mainHand.getType() == Material.SPAWNER) {
            inv.setItemInMainHand(newItem);
        } else if (getSpawnEggMaterials().contains(offHand.getType()) || offHand.getType() == Material.SPAWNER) {
            inv.setItemInOffHand(newItem);
        }
    }

    @Override
    public Material getSpawnerMaterial() {
        return Material.SPAWNER;
    }

    @Override
    public Material getIronFenceMaterial() {
        return Material.IRON_BARS;
    }

    @Override
    public Material getSpawnEggMaterial() {
        return Material.LEGACY_MONSTER_EGG;
    }

    @Override
    public Collection<Material> getSpawnEggMaterials() {
        return spawnEggs;
    }

}
