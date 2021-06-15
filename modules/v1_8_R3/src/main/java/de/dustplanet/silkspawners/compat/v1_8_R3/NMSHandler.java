package de.dustplanet.silkspawners.compat.v1_8_R3;

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
import org.bukkit.craftbukkit.v1_8_R3.CraftServer;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.block.CraftCreatureSpawner;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.inventory.ItemStack;
import org.spigotmc.SpigotWorldConfig;

import com.google.common.base.CaseFormat;
import com.mojang.authlib.GameProfile;

import de.dustplanet.silkspawners.compat.api.NMSProvider;
import net.minecraft.server.v1_8_R3.Entity;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.minecraft.server.v1_8_R3.EntityTypes;
import net.minecraft.server.v1_8_R3.Item;
import net.minecraft.server.v1_8_R3.MinecraftServer;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityHeadRotation;
import net.minecraft.server.v1_8_R3.PlayerInteractManager;
import net.minecraft.server.v1_8_R3.TileEntityMobSpawner;
import net.minecraft.server.v1_8_R3.World;
import net.minecraft.server.v1_8_R3.WorldServer;

public class NMSHandler implements NMSProvider {
    private Field tileField;
    private final SortedMap<String, Integer> entitiesMaps = new TreeMap<>();
    private final SortedMap<Integer, String> sortedMap = new TreeMap<>();

    public NMSHandler() {
        this(true);
    }

    public NMSHandler(final boolean checkForNerfFlags) {
        try {
            tileField = CraftCreatureSpawner.class.getDeclaredField("spawner");
            tileField.setAccessible(true);
        } catch (SecurityException | NoSuchFieldException e) {
            Bukkit.getLogger().warning("[SilkSpawners] Reflection failed: " + e.getMessage());
            e.printStackTrace();
        }

        final WorldServer handle = ((CraftWorld) Bukkit.getServer().getWorlds().get(0)).getHandle();

        if (checkForNerfFlags) {
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
            // TODO Needs 1.8 source
            // g.put(s, Integer.valueOf(i)); --> Name of ID
            final Field field = EntityTypes.class.getDeclaredField("g");
            field.setAccessible(true);
            @SuppressWarnings("unchecked")
            final Map<String, Integer> map = (Map<String, Integer>) field.get(null);
            // For each entry in our name -- ID map but it into the sortedMap
            for (final Map.Entry<String, Integer> entry : map.entrySet()) {
                sortedMap.put(entry.getValue(), entry.getKey());
                entitiesMaps.put(entry.getKey(), entry.getValue());
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
        Item.getById(SPAWNER_ID).c(1);
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

        net.minecraft.server.v1_8_R3.ItemStack itemStack = null;
        final CraftItemStack craftStack = CraftItemStack.asCraftCopy(item);
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
    public String getSilkSpawnersNBTEntityID(final ItemStack item) {
        net.minecraft.server.v1_8_R3.ItemStack itemStack = null;
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
        net.minecraft.server.v1_8_R3.ItemStack itemStack = null;
        final CraftItemStack craftStack = CraftItemStack.asCraftCopy(item);
        itemStack = CraftItemStack.asNMSCopy(craftStack);
        final NBTTagCompound tag = itemStack.getTag();

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
    public Block getSpawnerFacing(final Player player, final int distance) {
        final Block block = player.getTargetBlock((Set<Material>) null, distance);
        if (block == null || block.getType() != Material.MOB_SPAWNER) {
            return null;
        }
        return block;
    }

    @Override
    public ItemStack newEggItem(final String entityID, final int amount, final String displayName) {
        final ItemStack item = new ItemStack(Material.MONSTER_EGG, amount, this.entitiesMaps.get(entityID).shortValue());
        net.minecraft.server.v1_8_R3.ItemStack itemStack = null;
        final CraftItemStack craftStack = CraftItemStack.asCraftCopy(item);
        itemStack = CraftItemStack.asNMSCopy(craftStack);
        NBTTagCompound tag = itemStack.getTag();

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
        return player.getItemInHand();
    }

    @Override
    public void reduceEggs(final Player player) {
        final ItemStack eggs = player.getItemInHand();
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
    public ItemStack getSpawnerItemInHand(final Player player) {
        return player.getItemInHand();
    }

    @Override
    public void setSpawnerItemInHand(final Player player, final ItemStack newItem) {
        player.setItemInHand(newItem);
    }

    @Override
    public Collection<Material> getSpawnEggMaterials() {
        return Collections.singleton(Material.MONSTER_EGG);
    }

    @Override
    public int getIDForEntity(final String entityID) {
        return entitiesMaps.get(entityID);
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

    @Override
    public String getVanillaEggNBTEntityID(final ItemStack item) {
        final short numericalEntityID = item.getDurability();
        return getEntityFromNumericalID(numericalEntityID);
    }

    @Nullable
    private String getEntityFromNumericalID(final int numericalEntityID) {
        return sortedMap.get(numericalEntityID);
    }
}
