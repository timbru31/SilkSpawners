package de.dustplanet.silkspawners.listeners;

import java.util.Random;

import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;

import de.dustplanet.silkspawners.SilkSpawners;
import de.dustplanet.util.SilkUtil;

/**
 * Handle the explosion of a spawner.
 *
 * @author (former) mushroomhostage
 * @author xGhOsTkiLLeRx
 */

public class SilkSpawnersEntityListener implements Listener {
    private final SilkSpawners plugin;
    private final SilkUtil su;
    private final Random rnd;

    public SilkSpawnersEntityListener(final SilkSpawners instance, final SilkUtil util) {
        plugin = instance;
        su = util;
        rnd = new Random();
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntiyExplode(final EntityExplodeEvent event) {
        /*
         * Skip if entity is not known or null or EnderDragon calls or this event explosionChance is 0
         */
        final Entity entity = event.getEntity();
        if (event.getEntity() == null || entity instanceof EnderDragon || plugin.config.getInt("explosionDropChance", 30) == 0) {
            return;
        }

        boolean drop = true;
        if (plugin.config.getBoolean("permissionExplode", false) && entity instanceof TNTPrimed) {
            final Entity igniter = ((TNTPrimed) entity).getSource();
            if (igniter != null && igniter instanceof Player) {
                final Player sourcePlayer = (Player) igniter;
                drop = sourcePlayer.hasPermission("silkspawners.explodedrop");
            }
        }

        // Check if a spawner block is on the list
        if (drop) {
            for (final Block block : event.blockList()) {
                // We have a spawner
                if (block.getType() == su.nmsProvider.getSpawnerMaterial()) {
                    // Roll the dice
                    final int randomNumber = rnd.nextInt(100);
                    final String entityID = su.getSpawnerEntityID(block);
                    // Check if we should drop a block
                    int dropChance = 0;
                    if (plugin.mobs.contains("creatures." + entityID + ".explosionDropChance")) {
                        dropChance = plugin.mobs.getInt("creatures." + entityID + ".explosionDropChance", 100);
                    } else {
                        dropChance = plugin.config.getInt("explosionDropChance", 100);
                    }
                    if (randomNumber < dropChance) {
                        final World world = block.getWorld();
                        world.dropItemNaturally(block.getLocation(),
                                su.newSpawnerItem(entityID, su.getCustomSpawnerName(entityID), 1, false));
                    }
                }
            }
        }
    }
}
