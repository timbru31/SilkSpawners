package de.dustplanet.silkspawners.compat.v1_13_R1;

import java.lang.reflect.Field;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.craftbukkit.libs.it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import org.bukkit.craftbukkit.v1_13_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_13_R1.block.CraftBlockEntityState;
import org.bukkit.craftbukkit.v1_13_R1.block.CraftCreatureSpawner;
import org.bukkit.craftbukkit.v1_13_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import de.dustplanet.silkspawners.compat.api.NMSProvider;
import net.minecraft.server.v1_13_R1.DataConverterMaterialId;
import net.minecraft.server.v1_13_R1.Entity;
import net.minecraft.server.v1_13_R1.EntityTypes;
import net.minecraft.server.v1_13_R1.Item;
import net.minecraft.server.v1_13_R1.MinecraftKey;
import net.minecraft.server.v1_13_R1.NBTTagCompound;
import net.minecraft.server.v1_13_R1.RegistryMaterials;
import net.minecraft.server.v1_13_R1.TileEntityMobSpawner;
import net.minecraft.server.v1_13_R1.World;

public class NMSHandler implements NMSProvider {

    private Field tileField;

    public NMSHandler() {
        try {
            // Get the spawner field
            // TODO Needs 1.13 source
            tileField = CraftCreatureSpawner.class.getDeclaredField("spawner");
            tileField.setAccessible(true);
        } catch (SecurityException | NoSuchFieldException e) {
            try {
                Class.forName("org.bukkit.craftbukkit.v1_13_R1.block.CraftBlockEntityState");
                tileField = CraftBlockEntityState.class.getDeclaredField("snapshot");
                tileField.setAccessible(true);
            } catch (NoSuchFieldException | SecurityException | ClassNotFoundException e1) {
                Bukkit.getLogger().warning("[SilkSpawners] Reflection failed: " + e.getMessage() + " " + e1.getMessage());
                e.printStackTrace();
                e1.printStackTrace();
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void spawnEntity(org.bukkit.World w, short entityID, double x, double y, double z) {
        // TODO Needs 1.13 source
        World world = ((CraftWorld) w).getHandle();
        RegistryMaterials<MinecraftKey, Class<? extends Entity>> registry = null;
        try {
            Field field = EntityTypes.class.getDeclaredField("REGISTRY");
            field.setAccessible(true);
            registry = (RegistryMaterials<MinecraftKey, Class<? extends Entity>>) field.get(null);
        } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
            e.printStackTrace();
        }
        if (registry == null) {
            Bukkit.getLogger()
                    .warning("[SilkSpawners] Failed to spawn, falling through. You should report this (RegistryMaterials == null)!");
            return;
        }
        Class<? extends Entity> entityClazz = registry.getId(entityID);
        if (entityClazz == null) {
            Bukkit.getLogger().warning("[SilkSpawners] Failed to spawn, falling through. You should report this (entityClazz == null)!");
            return;
        }
        MinecraftKey minecraftKey = registry.b(entityClazz);
        if (minecraftKey == null) {
            Bukkit.getLogger().warning("[SilkSpawners] Failed to spawn, falling through. You should report this (minecraftKey == null)!");
            return;
        }
        Entity entity = EntityTypes.a(world, minecraftKey);
        // Should actually never happen since the method above
        // contains a null check, too
        if (entity == null) {
            Bukkit.getLogger().warning("[SilkSpawners] Failed to spawn, falling through. You should report this (entity == null)!");
            return;
        }

        // Random facing
        entity.setPositionRotation(x, y, z, world.random.nextFloat() * 360.0f, 0.0f);
        // We need to add the entity to the world, reason is of
        // course a spawn egg so that other events can handle this
        world.addEntity(entity, SpawnReason.SPAWNER_EGG);
    }

    @Override
    public SortedMap<Integer, String> rawEntityMap() {
        SortedMap<Integer, String> sortedMap = new TreeMap<>();
        // Use reflection to dump native EntityTypes
        // This bypasses Bukkit's wrappers, so it works with mods
        try {
            // TODO Needs 1.13 source
            Field field2 = EntityTypes.class.getDeclaredField("REGISTRY");
            @SuppressWarnings("unchecked")
            RegistryMaterials<MinecraftKey, EntityTypes<?>> registry = (RegistryMaterials<MinecraftKey, EntityTypes<?>>) field2.get(null);
            // For each entry in our name -- ID map but it into the sortedMap
            Int2ObjectMap<String> idMapping = DataConverterMaterialId.ID_MAPPING;
            for (Entry<Integer, String> entry : idMapping.entrySet()) {
                int entityID = entry.getKey();
                String displayName = entry.getValue();
                System.out.println(entityID + ": " + displayName);
                if (displayName == null) {
                    continue;
                }
                // EntityTypes<?> entity = registry.get(new MinecraftKey(displayName));
                EntityTypes<?> entity = EntityTypes.a(displayName);
                if (entity == null) {
                    Bukkit.getLogger().severe("[SilkSpawners] Failed to dump entity map: entity is null, entityID: " + entityID);
                    continue;
                }
                MinecraftKey minecraftKey = null;

                try {
                    minecraftKey = registry.b(entity);
                } catch (@SuppressWarnings("unused") ClassCastException e) {
                    Bukkit.getLogger().severe("[SilkSpawners] Failed to dump entity map: entity is invalid, entityID: " + entityID);
                    Bukkit.getLogger().severe(
                            "[SilkSpawners] Failed to dump entity map: entity is invalid, entity: " + entity.getClass().getSimpleName());
                    continue;
                }

                if (minecraftKey == null) {
                    Bukkit.getLogger().severe("[SilkSpawners] Failed to dump entity map: minecraftKey is null, entityID: " + entityID);
                    Bukkit.getLogger().severe(
                            "[SilkSpawners] Failed to dump entity map: minecraftKey is null, entity: " + entity.getClass().getSimpleName());
                    continue;
                }
                System.out.println("adding " + entityID + " with " + minecraftKey.getKey());
                sortedMap.put(entityID, minecraftKey.getKey());
            }
        } catch (SecurityException | NoSuchFieldException | IllegalArgumentException | IllegalAccessException e) {
            Bukkit.getLogger().severe("[SilkSpawners] Failed to dump entity map: " + e.getMessage());
            e.printStackTrace();
        }
        return sortedMap;
    }

    @Override
    public String getMobNameOfSpawner(BlockState blockState) {
        // Get our spawner
        CraftCreatureSpawner spawner = (CraftCreatureSpawner) blockState;
        // Get the mob ID ourselves if we can
        try {
            TileEntityMobSpawner tile = (TileEntityMobSpawner) tileField.get(spawner);
            // Get the name from the field of our spawner
            MinecraftKey minecraftKey = tile.getSpawner().getMobName();
            return minecraftKey != null ? minecraftKey.b() : "";
        } catch (IllegalArgumentException | IllegalAccessException e) {
            Bukkit.getLogger().warning("[SilkSpawners] Reflection failed: " + e.getMessage());
            e.printStackTrace();
        }
        return "";
    }

    @Override
    public void setSpawnersUnstackable() {
        try {
            Item spawner = Item.getById(52);
            Field maxStackSize = Item.class.getDeclaredField("maxStackSize");
            maxStackSize.setAccessible(true);
            maxStackSize.set(spawner, 1);
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean setMobNameOfSpawner(BlockState blockState, String mobID) {
        // Get out spawner;
        CraftCreatureSpawner spawner = (CraftCreatureSpawner) blockState;

        try {
            // Refer to the NMS TileEntityMobSpawner and change the name, see
            // TODO Needs 1.13 source
            TileEntityMobSpawner tile = (TileEntityMobSpawner) tileField.get(spawner);
            // Changes as of 1.7.10
            // TODO Needs 1.13 source
            // alternative: EntityTypes.a(string s)
            tile.getSpawner().setMobName(EntityTypes.REGISTRY.get(new MinecraftKey(mobID)));
            return true;
        } catch (IllegalArgumentException | IllegalAccessException e) {
            Bukkit.getLogger().warning("[SilkSpawners] Reflection failed: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public ItemStack setNBTEntityID(ItemStack item, short entityID, String entity) {
        if (item == null || entityID == 0 || entity == null || entity.isEmpty()) {
            Bukkit.getLogger().warning("[SilkSpawners] Skipping invalid spawner to set NBT data on.");
            return null;
        }

        net.minecraft.server.v1_13_R1.ItemStack itemStack = null;
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
        tag.getCompound("SilkSpawners").setShort("entityID", entityID);

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
    public short getSilkSpawnersNBTEntityID(ItemStack item) {
        net.minecraft.server.v1_13_R1.ItemStack itemStack = null;
        CraftItemStack craftStack = CraftItemStack.asCraftCopy(item);
        itemStack = CraftItemStack.asNMSCopy(craftStack);
        NBTTagCompound tag = itemStack.getTag();

        if (tag == null || !tag.hasKey("SilkSpawners")) {
            return 0;
        }
        return tag.getCompound("SilkSpawners").getShort("entityID");
    }

    @Override
    public String getVanillaNBTEntityID(ItemStack item) {
        net.minecraft.server.v1_13_R1.ItemStack itemStack = null;
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
    public ItemStack newEggItem(short entityID, String entity, int amount) {
        ItemStack item = new ItemStack(Material.LEGACY_MONSTER_EGG, amount, entityID);
        net.minecraft.server.v1_13_R1.ItemStack itemStack = null;
        CraftItemStack craftStack = CraftItemStack.asCraftCopy(item);
        itemStack = CraftItemStack.asNMSCopy(craftStack);
        NBTTagCompound tag = itemStack.getTag();

        // Create tag if necessary
        if (tag == null) {
            tag = new NBTTagCompound();
            itemStack.setTag(tag);
        }

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
    public String getVanillaEggNBTEntityID(ItemStack item) {
        net.minecraft.server.v1_13_R1.ItemStack itemStack = null;
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
            // Try if the String could be an UUID
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
        if (itemInMainHand.getType() == Material.LEGACY_MONSTER_EGG) {
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
    public ItemStack getSpawnerItemInHand(Player player) {
        PlayerInventory inv = player.getInventory();
        ItemStack mainHand = inv.getItemInMainHand();
        ItemStack offHand = inv.getItemInOffHand();
        if ((mainHand.getType() == Material.LEGACY_MONSTER_EGG || mainHand.getType() == Material.SPAWNER)
                && (offHand.getType() == Material.LEGACY_MONSTER_EGG || offHand.getType() == Material.SPAWNER)) {
            return null; // not determinable
        } else if (mainHand.getType() == Material.LEGACY_MONSTER_EGG || mainHand.getType() == Material.SPAWNER) {
            return mainHand;
        } else if (offHand.getType() == Material.LEGACY_MONSTER_EGG || offHand.getType() == Material.SPAWNER) {
            return offHand;
        }
        return null;
    }

    @Override
    public void setSpawnerItemInHand(Player player, ItemStack newItem) {
        PlayerInventory inv = player.getInventory();
        ItemStack mainHand = inv.getItemInMainHand();
        ItemStack offHand = inv.getItemInOffHand();
        if ((mainHand.getType() == Material.LEGACY_MONSTER_EGG || mainHand.getType() == Material.SPAWNER)
                && (offHand.getType() == Material.LEGACY_MONSTER_EGG || offHand.getType() == Material.SPAWNER)) {
            return; // not determinable
        } else if (mainHand.getType() == Material.LEGACY_MONSTER_EGG || mainHand.getType() == Material.SPAWNER) {
            inv.setItemInMainHand(newItem);
        } else if (offHand.getType() == Material.LEGACY_MONSTER_EGG || offHand.getType() == Material.SPAWNER) {
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

}
