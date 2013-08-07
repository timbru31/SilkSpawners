package de.dustplanet.silkspawners.listeners;

import java.util.Random;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.EnderDragon;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;
import de.dustplanet.silkspawners.SilkSpawners;
import de.dustplanet.util.SilkUtil;

/**
 * Handle the explosion of a spawner
 * 
 * @author (former) mushroomhostage
 * @author xGhOsTkiLLeRx
 */

public class SilkSpawnersEntityListener implements Listener {
    private SilkSpawners plugin;
    private SilkUtil su;
    private Random rnd;

    public SilkSpawnersEntityListener(SilkSpawners instance, SilkUtil util) {
	plugin = instance;
	su = util;
	rnd = new Random();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntiyExplode(EntityExplodeEvent event) {
	/* Skip if
	 * event is cancelled
	 * entity is not known or null
	 * EnderDragon calls this event
	 * explosionChance is 0
	 */
	if (event.isCancelled() || event.getEntity() == null || event.getEntity() instanceof EnderDragon || plugin.config.getInt("explosionDropChance") == 0) {
	    return;
	}

	// Check if a spawner block is on the list
	for (Block b : event.blockList()) {
	    // We have a spawner
	    if (b.getType() == Material.MOB_SPAWNER) {
		// Roll the dice
		int randomNumber = rnd.nextInt(100);
		// Check if we should drop a block
		if (randomNumber < plugin.config.getInt("explosionDropChance")) {
		    World world = b.getWorld();
		    // Drop a spawner (first get the entityID from the block and then make a new spawner item)
		    world.dropItemNaturally(b.getLocation(), su.newSpawnerItem(su.getSpawnerEntityID(b), plugin.localization.getString("spawnerName")));
		}
	    }
	}
    }
}