package de.dustplanet.silkspawners.events;

import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.Player;

public interface ISilkSpawnersEvent {

    String getEntityID();

    void setEntityID(String entityID);

    Player getPlayer();

    Block getBlock();

    CreatureSpawner getSpawner();
}
