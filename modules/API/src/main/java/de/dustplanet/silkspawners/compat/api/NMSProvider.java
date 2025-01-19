package de.dustplanet.silkspawners.compat.api;

import java.util.Collection;
import java.util.List;
import java.util.SortedMap;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import com.google.common.base.CaseFormat;

public interface NMSProvider {
    static final Pattern LOWER_CAMEL_REGEX = Pattern.compile("([a-z]+[A-Z]+\\w+)+");
    static final Pattern UPPER_CAMEL_REGEX = Pattern.compile("([A-Z]+[a-z]+\\w+)+");
    public static final int SPAWNER_ID = 52;
    public static final String NAMESPACED_SPAWNER_ID = "spawner";

    void spawnEntity(World world, String entityID, double x, double y, double z, Player player);

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

    default String getOtherPluginsNBTEntityID(@SuppressWarnings("unused") final ItemStack item) {
        return null;
    }

    Block getSpawnerFacing(Player player, int distance);

    default Collection<? extends Player> getOnlinePlayers() {
        return Bukkit.getOnlinePlayers();
    }

    @Deprecated
    default ItemStack newEggItem(final String entityID, final int amount) {
        return newEggItem(entityID, amount, null);
    }

    ItemStack newEggItem(String entityID, int amount, String displayName);

    String getVanillaEggNBTEntityID(ItemStack item);

    @SuppressWarnings("unused")
    default void displayBossBar(final String title, final String colorName, final String styleName, final Player player,
            final Plugin plugin, final int period) {
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
        final Collection<Material> spawnEggs = this.getSpawnEggMaterials();
        if (spawnEggs.size() > 1) {
            throw new UnsupportedOperationException(
                    "Spawn egg is not determinable because there is more than one material, please use getSpawnEggMaterials() for v1.13+");
        }
        return spawnEggs.iterator().next();
    }

    Collection<Material> getSpawnEggMaterials();

    // Only required for MC 1.8
    default int getIDForEntity(@SuppressWarnings("unused") final String entityID) {
        return 0;
    }

    default CaseFormat caseFormatOf(final String s) {
        if (s.contains("_")) {
            if (s.toUpperCase().equals(s)) {
                return CaseFormat.UPPER_UNDERSCORE;
            }
            if (s.toLowerCase().equals(s)) {
                return CaseFormat.LOWER_UNDERSCORE;
            }
        } else if (s.contains("-")) {
            if (s.toLowerCase().equals(s)) {
                return CaseFormat.LOWER_HYPHEN;
            }
        } else {
            if (Character.isLowerCase(s.charAt(0))) {
                if (LOWER_CAMEL_REGEX.matcher(s).matches()) {
                    return CaseFormat.LOWER_CAMEL;
                }
            } else {
                if (UPPER_CAMEL_REGEX.matcher(s).matches()) {
                    return CaseFormat.UPPER_CAMEL;
                }
            }
        }

        return CaseFormat.LOWER_UNDERSCORE;
    }

    Player loadPlayer(OfflinePlayer offline);

    default boolean isWindCharge(@SuppressWarnings("unused") final Entity entity) {
        return false;
    }
}
