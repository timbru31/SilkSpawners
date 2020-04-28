package de.dustplanet.silkspawners.compat.api;

import java.util.Collection;
import java.util.List;
import java.util.SortedMap;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

public interface NMSProvider {

    public static final int SPAWNER_ID = 52;
    public static final String NAMESPACED_SPAWNER_ID = "spawner";

    void spawnEntity(World world, String entityID, double x, double y, double z);

    default List<String> rawEntityMap() {
        return null;
    }

    default SortedMap<Integer, String> legacyRawEntityMap() {
        return null;
    }

    String getMobNameOfSpawner(BlockState blockState);

    boolean setMobNameOfSpawner(BlockState blockState, String entityID);

    void setSpawnersUnstackable();

    ItemStack setNBTEntityID(ItemStack item, String entityID);

    String getSilkSpawnersNBTEntityID(ItemStack item);

    String getVanillaNBTEntityID(ItemStack item);

    Block getSpawnerFacing(Player player, int distance);

    default Collection<? extends Player> getOnlinePlayers() {
        return Bukkit.getOnlinePlayers();
    }

    @Deprecated
    default ItemStack newEggItem(String entityID, int amount) {
        return newEggItem(entityID, amount, null);
    }

    ItemStack newEggItem(String entityID, int amount, String displayName);

    @SuppressWarnings("unused")
    default String getVanillaEggNBTEntityID(ItemStack item) {
        return null;
    }

    @SuppressWarnings("unused")
    default void displayBossBar(String title, String colorName, String styleName, Player player, Plugin plugin, int period) {
        return;
    }

    ItemStack getItemInHand(Player player);

    ItemStack getSpawnerItemInHand(Player player);

    void setSpawnerItemInHand(Player player, ItemStack newItem);

    void reduceEggs(Player player);

    Player getPlayer(String playerUUIDOrName);

    default Material getSpawnerMaterial() {
        return Material.SPAWNER;
    }

    default Material getIronFenceMaterial() {
        return Material.IRON_BARS;
    }

    @Deprecated
    default Material getSpawnEggMaterial() {
        Collection<Material> spawnEggs = this.getSpawnEggMaterials();
        if (spawnEggs.size() > 1) {
            throw new UnsupportedOperationException(
                    "Spawn egg is not determinable because there is more than one material, please use getSpawnEggMaterials() for v1.13+");
        }
        return spawnEggs.iterator().next();
    }

    Collection<Material> getSpawnEggMaterials();

    // Only required for MC 1.8
    default int getIDForEntity(@SuppressWarnings("unused") String entityID) {
        return 0;
    }
}
