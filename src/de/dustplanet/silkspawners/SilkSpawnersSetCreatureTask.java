package de.dustplanet.silkspawners;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class SilkSpawnersSetCreatureTask  implements Runnable {
    short entityID;
    Block block;
    SilkSpawners plugin;
    Player player;

    public SilkSpawnersSetCreatureTask(short entityID, Block block, SilkSpawners plugin, Player player) {
        this.entityID = entityID;
        this.block = block;
        this.plugin = plugin;
        this.player = player;
    }

    public void run() {
        try {
            plugin.setSpawnerEntityID(block, entityID);
        } catch (Exception e) {
            plugin.informPlayer(player, "Failed to set type: " + e);
        }
    }

}
