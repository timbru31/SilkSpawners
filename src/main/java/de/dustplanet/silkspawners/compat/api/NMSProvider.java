package de.dustplanet.silkspawners.compat.api;

import java.util.SortedMap;

import org.bukkit.World;
import org.bukkit.block.BlockState;

public interface NMSProvider {
    
    public void spawnEntity(World world, short entityID, double x, double y, double z);
    public SortedMap<Integer, String> rawEntityMap();
    public String getMobNameOfSpawner(BlockState blockState);
    public boolean setMobNameOfSpawner(BlockState blockState, String mobID);
    public void setSpawnersUnstackable();
}
