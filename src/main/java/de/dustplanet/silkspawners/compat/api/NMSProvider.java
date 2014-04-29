package de.dustplanet.silkspawners.compat.api;

import java.util.SortedMap;

import org.bukkit.World;
import org.bukkit.block.BlockState;

public interface NMSProvider {
    
    void spawnEntity(World world, short entityID, double x, double y, double z);
    SortedMap<Integer, String> rawEntityMap();
    String getMobNameOfSpawner(BlockState blockState);
    boolean setMobNameOfSpawner(BlockState blockState, String mobID);
    void setSpawnersUnstackable();
}
