package de.dustplanet.silkspawners.listeners;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;

import de.dustplanet.silkspawners.SilkSpawners;
import de.dustplanet.silkspawners.events.SilkSpawnersSpawnerChangeEvent;
import de.dustplanet.util.SilkUtil;

/**
 * To show a chat message that a player is holding a mob spawner and it's type.
 *
 * @author (former) mushroomhostage
 * @author xGhOsTkiLLeRx
 */

public class SilkSpawnersPlayerListener implements Listener {
    private SilkSpawners plugin;
    private SilkUtil su;

    public SilkSpawnersPlayerListener(SilkSpawners instance, SilkUtil util) {
        plugin = instance;
        su = util;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerHoldItem(PlayerItemHeldEvent event) {
        // Check if we should notify the player. The second condition is the
        // permission and that the slot isn't null and the item is a mob spawner
        if (plugin.config.getBoolean("notifyOnHold")
                && plugin.hasPermission(event.getPlayer(), "silkspawners.info")
                && event.getPlayer().getInventory().getItem(event.getNewSlot()) != null
                && event.getPlayer().getInventory().getItem(event.getNewSlot()).getType().equals(Material.MOB_SPAWNER)) {

            // Get ID
            short entityID = su.getStoredSpawnerItemEntityID(event.getPlayer().getInventory().getItem(event.getNewSlot()));
            // Check for unknown/invalid ID
            if (entityID == 0 || !su.knownEids.contains(entityID)) {
                entityID = su.defaultEntityID;
            }
            // Get the name from the entityID
            String spawnerName = su.getCreatureName(entityID);
            Player player = event.getPlayer();
            su.notify(player, spawnerName, entityID);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!event.hasItem() || !event.hasBlock()) {
            return;
        }
        ItemStack item = event.getItem();
        Block block = event.getClickedBlock();
        Player player = event.getPlayer();
        // If we use a spawn egg
        if (item != null && item.getType() == SilkUtil.SPAWN_EGG) {
            // Get the entityID
            short entityID = item.getDurability();
            // Clicked spawner with monster egg to change type
            if (event.getAction() == Action.LEFT_CLICK_BLOCK && block != null && block.getType() == Material.MOB_SPAWNER) {
                // WorldGuard region protection
                if (!su.canBuildHere(player, block.getLocation())) {
                    return;
                }

                // Mob
                String mobName = su.getCreatureName(entityID).toLowerCase().replace(" ", "");

                if (!plugin.hasPermission(player, "silkspawners.changetypewithegg." + mobName)
                        && !plugin.hasPermission(player, "silkspawners.changetypewithegg.*")) {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('\u0026', plugin.localization .getString("noPermissionChangingWithEggs")));
                    return;
                }

                // Call the event and maybe change things!
                SilkSpawnersSpawnerChangeEvent changeEvent = new SilkSpawnersSpawnerChangeEvent(player, block, entityID, su.getSpawnerEntityID(block));
                plugin.getServer().getPluginManager().callEvent(changeEvent);
                // See if we need to stop
                if (changeEvent.isCancelled()) {
                    return;
                }
                // Get the new ID (might be changed)
                entityID = changeEvent.getEntityID();

                su.setSpawnerType(block, entityID, player, ChatColor.translateAlternateColorCodes('\u0026', plugin.localization.getString("changingDeniedWorldGuard")));
                player.sendMessage(ChatColor.translateAlternateColorCodes('\u0026', plugin.localization.getString("changedSpawner")).replace("%creature%", su.getCreatureName(entityID)));

                // Consume egg
                if (plugin.config.getBoolean("consumeEgg", true)) {
                    su.reduceEggs(player);
                }
                // Normal spawning
            } else if (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR) {
                if (plugin.config.getBoolean("spawnEggToSpawner", false)) {
                    Block targetBlock = block.getRelative(BlockFace.UP);
                    // Check if block above is air
                    if (targetBlock.getType() == Material.AIR) {
                        targetBlock.setType(Material.MOB_SPAWNER);
                        su.setSpawnerEntityID(targetBlock, entityID);
                        // Prevent mob spawning
                        // Should we consume the egg?
                        if (plugin.config.getBoolean("consumeEgg", true)) {
                            su.reduceEggs(player);
                        }
                    } else {
                        player.sendMessage(ChatColor.translateAlternateColorCodes('\u0026', plugin.localization.getString("noSpawnerHere")));
                    }
                    event.setCancelled(true);
                } else if (plugin.config.getBoolean("spawnEggOverride", false)) {
                    // Disabled by default, since it is dangerous
                    // Name
                    String mobID = su.eid2MobID.get(entityID);
                    // Are we allowed to spawn?
                    boolean allowed = plugin.config.getBoolean("spawnEggOverrideSpawnDefault", true);
                    if (mobID != null) {
                        allowed = plugin.mobs.getBoolean("creatures." + mobID
                                + ".enableSpawnEggOverrideAllowSpawn", allowed);
                    }
                    // Deny spawning
                    if (!allowed) {
                        player.sendMessage(ChatColor.translateAlternateColorCodes('\u0026',
                                plugin.localization
                                .getString("spawningDenied")
                                .replace("%ID%", Short.toString(entityID)))
                                .replace("%creature%", su.getCreatureName(entityID)));
                        event.setCancelled(true);
                        return;
                    }
                    // Bukkit doesn't allow us to spawn wither or dragons and so
                    // on. NMS here we go!
                    // https://github.com/Bukkit/CraftBukkit/blob/master/src/main/java/net/minecraft/server/ItemMonsterEgg.java#L22

                    // Notify
                    plugin.informPlayer(player, ChatColor.translateAlternateColorCodes('\u0026',
                            plugin.localization.getString("spawning")
                            .replace("%ID%", Short.toString(entityID)))
                            .replace("%creature%", su.getCreatureName(entityID)));

                    // Spawn on top of targeted block
                    Location location = block.getLocation().add(0, 1, 0);
                    double x = location.getX(), y = location.getY(), z = location.getZ();

                    // We can spawn using the direct method from EntityTypes
                    su.nmsProvider.spawnEntity(player.getWorld(), entityID, x, y, z);

                    su.reduceEggs(player);

                    // Prevent normal spawning
                    event.setCancelled(true);
                }
            }
        }
    }
}
