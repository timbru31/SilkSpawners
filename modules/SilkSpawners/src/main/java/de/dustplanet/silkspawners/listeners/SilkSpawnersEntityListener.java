package de.dustplanet.silkspawners.listeners;

import java.util.Iterator;
import java.util.Random;
import java.util.logging.Level;

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
import org.bukkit.inventory.ItemStack;

import de.dustplanet.silkspawners.SilkSpawners;
import de.dustplanet.silkspawners.events.SilkSpawnersSpawnerExplodeEvent;
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
         * Skip if entity is not known or null or EnderDragon calls or this event tntExplosionChance/creeperExplosionChance is 0
         */
        final Entity entity = event.getEntity();
        final int tntExplosionChance = plugin.config.getInt("tntExplosionDropChance", 30);
        final int creeperExplosionChance = plugin.config.getInt("creeperExplosionDropChance", 100);
        if (event.getEntity() == null || entity instanceof EnderDragon || su.nmsProvider.isWindCharge(event.getEntity())
                || tntExplosionChance == 0 || creeperExplosionChance == 0) {
            plugin.getLogger().log(Level.FINE,
                    "Skipping handling of explosion event because abort criteria are met, tnt explosion chance is {0}, creeper explosion chance is {1}", tntExplosionChance, creeperExplosionChance);

            return;
        }

        Player sourcePlayer = null;
        if (plugin.config.getBoolean("permissionExplode", false) && entity instanceof TNTPrimed) {
            plugin.getLogger().fine("Checking for explosion permissing because igniter was TNT");
            final Entity igniter = ((TNTPrimed) entity).getSource();
            if (igniter != null && igniter instanceof Player) {
                sourcePlayer = (Player) igniter;
                plugin.getLogger().log(Level.FINE, "Player has destroydrop permission {0}",
                        sourcePlayer.hasPermission("silkspawners.explodedrop"));
                if (!sourcePlayer.hasPermission("silkspawners.explodedrop")) {
                    return;
                }
            }
        }

        for (final Iterator<Block> iterator = event.blockList().iterator(); iterator.hasNext();) {
            final Block block = iterator.next();
            if (block.getType() == su.nmsProvider.getSpawnerMaterial()) {
                plugin.getLogger().log(Level.FINE, "Calculating exploded spawner at {0}, {1}, {2}",
                        new Object[] { block.getX(), block.getY(), block.getZ() });

                plugin.getLogger().log(Level.FINE, "Explosion source is {0}", event.getEntity().getName());
                plugin.getLogger().log(Level.FINE, "Explosion source (EntityType) is {0}", event.getEntityType().name());

                final int randomNumber = rnd.nextInt(100);
                String entityID = su.getSpawnerEntityID(block);
                plugin.getLogger().log(Level.FINE, "Current entityID is {0}", entityID);

                if (entity instanceof TNTPrimed) { // if tnt is source of explosion
                    if (plugin.mobs.contains("creatures." + entityID + ".tntExplosionDropChance")) {
                        dropChance = plugin.mobs.getInt("creatures." + entityID + ".tntExplosionDropChance", 30);
                    } else {
                        dropChance = plugin.config.getInt("tntExplosionDropChance", 100);;
                    }
                } else { // if creeper is source of explosion
                    if (plugin.mobs.contains("creatures." + entityID + ".creeperExplosionDropChance")) {
                        dropChance = plugin.mobs.getInt("creatures." + entityID + ".creeperExplosionDropChance", 100);
                    } else {
                        dropChance = plugin.config.getInt("creeperExplosionDropChance", 100);;
                    }
                }
                plugin.getLogger().log(Level.FINE, "Current drop chance is {0}", dropChance);
                final SilkSpawnersSpawnerExplodeEvent explodeEvent = new SilkSpawnersSpawnerExplodeEvent(entity, sourcePlayer, block,
                        entityID, dropChance);
                plugin.getServer().getPluginManager().callEvent(explodeEvent);
                if (explodeEvent.isAllCancelled()) {
                    plugin.getLogger()
                            .fine("Skipping entity explode event because the the SilkSpawnersSpawnerExplodeEvent has all cancelled");
                    event.setCancelled(true);
                    return;
                }

                if (explodeEvent.isCancelled()) {
                    plugin.getLogger()
                            .fine("Skipping block destruction and drops because the the SilkSpawnersSpawnerExplodeEvent was cancelled");
                    iterator.remove();
                    continue;
                }

                entityID = explodeEvent.getEntityID();
                plugin.getLogger().log(Level.FINE, "New entityID is {0}", entityID);
                dropChance = explodeEvent.getDropChance();
                plugin.getLogger().log(Level.FINE, "New drop chance is {0}", dropChance);
                if (randomNumber < dropChance) {
                    final ItemStack explodeEventDrop = explodeEvent.getDrop();
                    ItemStack drops = null;
                    if (explodeEventDrop != null) {
                        plugin.getLogger().log(Level.FINE, "Setting custom drop: {0}x, {1}",
                                new Object[] { explodeEventDrop.getAmount(), explodeEventDrop.getType() });
                        drops = explodeEventDrop;
                    } else {
                        drops = su.newSpawnerItem(entityID, su.getCustomSpawnerName(entityID), 1, false);
                    }
                    if (drops != null) {
                        final World world = block.getWorld();
                        world.dropItemNaturally(block.getLocation(), drops);
                    }
                }
            }
        }
    }
}
