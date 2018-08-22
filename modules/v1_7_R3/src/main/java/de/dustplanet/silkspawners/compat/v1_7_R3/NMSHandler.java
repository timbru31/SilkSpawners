package de.dustplanet.silkspawners.compat.v1_7_R3;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nullable;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.craftbukkit.v1_7_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_7_R3.block.CraftCreatureSpawner;
import org.bukkit.craftbukkit.v1_7_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.inventory.ItemStack;

import de.dustplanet.silkspawners.compat.api.NMSProvider;
import net.minecraft.server.v1_7_R3.Entity;
import net.minecraft.server.v1_7_R3.EntityTypes;
import net.minecraft.server.v1_7_R3.Item;
import net.minecraft.server.v1_7_R3.NBTTagCompound;
import net.minecraft.server.v1_7_R3.RegistryMaterials;
import net.minecraft.server.v1_7_R3.TileEntityMobSpawner;
import net.minecraft.server.v1_7_R3.World;

public class NMSHandler implements NMSProvider {
    private Field tileField;

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
    public void spawnEntity(org.bukkit.World w, String entityID, double x, double y, double z) {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setString("id", entityID);

        World world = ((CraftWorld) w).getHandle();
        Entity entity = EntityTypes.a(tag, world);

        if (entity == null) {
            Bukkit.getLogger().warning("[SilkSpawners] Failed to spawn, falling through. You should report this (entity == null)!");
            return;
        }

        entity.setPositionRotation(x, y, z, world.random.nextFloat() * 360.0f, 0.0f);
        world.addEntity(entity, SpawnReason.SPAWNER_EGG);
    }

    @Override
    public List<String> rawEntityMap() {
        List<String> entities = new ArrayList<>();
        try {
            Field field = EntityTypes.class.getDeclaredField("g");
            field.setAccessible(true);
            @SuppressWarnings("unchecked")
            Map<String, Integer> map = (Map<String, Integer>) field.get(null);
            for (String entity : map.keySet()) {
                entities.add(entity);
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
            return tile.a().getMobName();
        } catch (IllegalArgumentException | IllegalAccessException e) {
            Bukkit.getLogger().warning("[SilkSpawners] Reflection failed: " + e.getMessage());
            e.printStackTrace();
        }
        return "";
    }

    @Override
    public void setSpawnersUnstackable() {
        // https://bukkit.org/threads/66364/
        try {
            // Get the new registry HashMp from the Item class
            Field registryField = Item.class.getDeclaredField("REGISTRY");
            registryField.setAccessible(true);
            RegistryMaterials registry = (RegistryMaterials) registryField.get(null);
            // Get entry of the spawner
            Object spawnerEntry = registry.a(52);
            // Set maxStackSize "e(int maxStackSize)"
            Field maxStackSize = Item.class.getDeclaredField("maxStackSize");
            maxStackSize.setAccessible(true);
            maxStackSize.setInt(spawnerEntry, 1);
            // Cleanup
            registryField.setAccessible(false);
            maxStackSize.setAccessible(false);
        } catch (SecurityException | IllegalArgumentException | IllegalAccessException | NoSuchFieldException e) {
            Bukkit.getLogger().warning("[SilkSpawners] Failed to set max stack size, ignoring spawnersUnstackable: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public boolean setMobNameOfSpawner(BlockState blockState, String mobID) {
        CraftCreatureSpawner spawner = (CraftCreatureSpawner) blockState;

        try {
            TileEntityMobSpawner tile = (TileEntityMobSpawner) tileField.get(spawner);
            tile.a().a(mobID);
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

        net.minecraft.server.v1_7_R3.ItemStack itemStack = null;
        CraftItemStack craftStack = CraftItemStack.asCraftCopy(item);
        itemStack = CraftItemStack.asNMSCopy(craftStack);
        NBTTagCompound tag = itemStack.getTag();

        if (tag == null) {
            tag = new NBTTagCompound();
            itemStack.setTag(tag);
        }

        if (!tag.hasKey("SilkSpawners")) {
            tag.set("SilkSpawners", new NBTTagCompound());
        }

        tag.getCompound("SilkSpawners").setString("entity", entity);

        if (!tag.hasKey("BlockEntityTag")) {
            tag.set("BlockEntityTag", new NBTTagCompound());
        }

        tag.getCompound("BlockEntityTag").setString("EntityId", entity);

        return CraftItemStack.asCraftMirror(itemStack);
    }

    @Override
    @Nullable
    public String getSilkSpawnersNBTEntityID(ItemStack item) {
        net.minecraft.server.v1_7_R3.ItemStack itemStack = null;
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
        net.minecraft.server.v1_7_R3.ItemStack itemStack = null;
        CraftItemStack craftStack = CraftItemStack.asCraftCopy(item);
        itemStack = CraftItemStack.asNMSCopy(craftStack);
        NBTTagCompound tag = itemStack.getTag();

        if (tag == null || !tag.hasKey("BlockEntityTag")) {
            return null;
        }
        return tag.getCompound("BlockEntityTag").getString("EntityId");
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
        @SuppressWarnings("deprecation")
        Block block = player.getTargetBlock(null, distance);
        if (block == null || block.getType() != Material.MOB_SPAWNER) {
            return null;
        }
        return block;
    }

    @Override
    public ItemStack newEggItem(String entity, int amount) {
        return new ItemStack(Material.MONSTER_EGG, amount);
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
        return player.getItemInHand();
    }

    @Override
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

    @Override
    public ItemStack getSpawnerItemInHand(Player player) {
        return player.getItemInHand();
    }

    @Override
    public void setSpawnerItemInHand(Player player, ItemStack newItem) {
        player.setItemInHand(newItem);
    }
}
