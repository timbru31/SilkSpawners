package de.dustplanet.silkspawners.compat.api;

import java.util.Collection;
import java.util.SortedMap;

import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

public interface NMSProvider {
    void spawnEntity(World world, short entityID, double x, double y, double z);
    SortedMap<Integer, String> rawEntityMap();
    String getMobNameOfSpawner(BlockState blockState);
    boolean setMobNameOfSpawner(BlockState blockState, String mobID);
    void setSpawnersUnstackable();
    Entity getTNTSource(TNTPrimed tnt);
    ItemStack setNBTEntityID(ItemStack item, short entityID, String entity);
    short getSilkSpawnersNBTEntityID(ItemStack item);
    String getVanillaNBTEntityID(ItemStack item);
    Block getSpawnerFacing(Player player, int distance);
    Collection<? extends Player> getOnlinePlayers();
    ItemStack newEggItem(short entityID, String entity, int amount);
    String getVanillaEggNBTEntityID(ItemStack item);
    void displayBossBar(String title, String colorName, String styleName, Player player, Plugin plugin, int period);
}
