package de.dustplanet.silkspawners.compat.v1_8_R3;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.block.CraftCreatureSpawner;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.inventory.ItemStack;

import de.dustplanet.silkspawners.compat.api.NMSProvider;
import net.minecraft.server.v1_8_R3.Entity;
import net.minecraft.server.v1_8_R3.EntityTypes;
import net.minecraft.server.v1_8_R3.Item;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import net.minecraft.server.v1_8_R3.TileEntityMobSpawner;
import net.minecraft.server.v1_8_R3.World;

public class NMSHandler implements NMSProvider {
    private Field tileField;

    public NMSHandler() {
        try {
            // Get the spawner field
            // TODO Needs 1.8 source
            tileField = CraftCreatureSpawner.class.getDeclaredField("spawner");
            tileField.setAccessible(true);
        } catch (SecurityException | NoSuchFieldException e) {
            Bukkit.getServer().getLogger().info("Reflection failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void spawnEntity(org.bukkit.World w, short entityID, double x, double y, double z) {
        // TODO Needs 1.8 source
        World world = ((CraftWorld) w).getHandle();
        Entity entity = EntityTypes.a(entityID, world);
        // Should actually never happen since the method above
        // contains a null check, too
        if (entity == null) {
            Bukkit.getLogger().warning("Failed to spawn, falling through. You should report this (entity == null)!");
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
            // TODO Needs 1.8 source
            // g.put(s, Integer.valueOf(i)); --> Name of ID
            Field field = EntityTypes.class.getDeclaredField("g");
            field.setAccessible(true);
            @SuppressWarnings("unchecked")
            Map<String, Integer> map = (Map<String, Integer>) field.get(null);
            // For each entry in our name -- ID map but it into the sortedMap
            for (Map.Entry<String, Integer> entry : map.entrySet()) {
                sortedMap.put(entry.getValue(), entry.getKey());
            }
        } catch (SecurityException | NoSuchFieldException | IllegalArgumentException | IllegalAccessException e) {
            Bukkit.getServer().getLogger().severe("Failed to dump entity map: " + e.getMessage());
            e.printStackTrace();
        }
        return sortedMap;
    }

    @Override
    public String getMobNameOfSpawner(BlockState blockState) {
        // Get our spawner;
        CraftCreatureSpawner spawner = (CraftCreatureSpawner) blockState;
        // Get the mob ID ourselves if we can
        try {
            TileEntityMobSpawner tile = (TileEntityMobSpawner) tileField.get(spawner);
            // Get the name from the field of our spawner
            return tile.getSpawner().getMobName();
        } catch (IllegalArgumentException | IllegalAccessException e) {
            Bukkit.getServer().getLogger().info("Reflection failed: " + e.getMessage());
            e.printStackTrace();
        }
        return "";
    }

    @Override
    public void setSpawnersUnstackable() {
        // http://forums.bukkit.org/threads/setting-max-stack-size.66364/
        Item.getById(52).c(1);
    }

    @Override
    public boolean setMobNameOfSpawner(BlockState blockState, String mobID) {
        // Get out spawner;
        CraftCreatureSpawner spawner = (CraftCreatureSpawner) blockState;

        try {
            // Refer to the NMS TileEntityMobSpawner and change the name, see
            // TODO Needs 1.8 source
            TileEntityMobSpawner tile = (TileEntityMobSpawner) tileField.get(spawner);
            // Changes as of 1.7.10
            // TODO Needs 1.8 source
            tile.getSpawner().setMobName(mobID);
            return true;
        } catch (IllegalArgumentException | IllegalAccessException e) {
            Bukkit.getServer().getLogger().info("Reflection failed: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public org.bukkit.entity.Entity getTNTSource(TNTPrimed tnt) {
        return tnt.getSource();
    }

    @Override
    public ItemStack setNBTEntityID(ItemStack item, short entityID, String entity) {
        net.minecraft.server.v1_8_R3.ItemStack itemStack = null;
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

        // Check for Vanilla key
        if (!tag.hasKey("BlockEntityTag")) {
            tag.set("BlockEntityTag", new NBTTagCompound());
        }
        tag = tag.getCompound("SilkSpawners");
        tag.setShort("entityID", entityID);

        tag = itemStack.getTag().getCompound("BlockEntityTag");
        tag.setString("EntityId", entity);

        return CraftItemStack.asCraftMirror(itemStack);
    }

    @Override
    public short getSilkSpawnersNBTEntityID(ItemStack item) {
        net.minecraft.server.v1_8_R3.ItemStack itemStack = null;
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
        net.minecraft.server.v1_8_R3.ItemStack itemStack = null;
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
     * @param player the player
     * @param distance the reach distance
     * @return the found block or null
     */
    @Override
    public Block getSpawnerFacing(Player player, int distance) {
        Block block = player.getTargetBlock((Set<Material>) null, distance);
        if (block == null || block.getType() != Material.MOB_SPAWNER) {
            return null;
        }
        return block;
    }

    @Override
    public Collection<? extends Player> getOnlinePlayers() {
        return Bukkit.getOnlinePlayers();
    }

    @Override
    public ItemStack newEggItem(short entityID, String entity, int amount) {
        return new ItemStack(Material.MONSTER_EGG, amount, entityID);
    }

    @Override
    public String getVanillaEggNBTEntityID(ItemStack item) {
        // EntityTag.id for eggs was added in 1.9 only
        return null;
    }
}
