package de.dustplanet.silkspawners.listeners;

import java.util.Locale;

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

import com.massivecraft.factions.Board;
import com.massivecraft.factions.FLocation;
import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.entity.BoardColl;
import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.entity.MPlayer;
import com.massivecraft.massivecore.ps.PS;

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
    private final SilkSpawners plugin;
    private final SilkUtil su;

    public SilkSpawnersPlayerListener(final SilkSpawners instance, final SilkUtil util) {
        plugin = instance;
        su = util;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerHoldItem(final PlayerItemHeldEvent event) {
        // Check if we should notify the player. The second condition is the
        // permission and that the slot isn't null and the item is a mob spawner
        if (event.getPlayer().getInventory().getItem(event.getNewSlot()) != null
                && event.getPlayer().getInventory().getItem(event.getNewSlot()).getType() == su.nmsProvider.getSpawnerMaterial()
                && plugin.config.getBoolean("notifyOnHold") && event.getPlayer().hasPermission("silkspawners.info")) {

            // Get ID
            String entityID = su.getStoredSpawnerItemEntityID(event.getPlayer().getInventory().getItem(event.getNewSlot()));
            // Check for unknown/invalid ID
            if (entityID == null) {
                entityID = su.getDefaultEntityID();
            }
            // Get the name from the entityID
            final String spawnerName = su.getCreatureName(entityID);
            final Player player = event.getPlayer();
            su.notify(player, spawnerName);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerInteract(final PlayerInteractEvent event) {
        if (!event.hasItem() || !event.hasBlock()) {
            return;
        }
        final ItemStack item = event.getItem();
        final Block block = event.getClickedBlock();
        if (block == null) {
            return;
        }
        final Player player = event.getPlayer();
        // If we use a spawn egg
        if (item != null && su.nmsProvider.getSpawnEggMaterials().contains(item.getType())) {
            // Get the entityID
            String entityID = su.getStoredEggEntityID(item);
            final boolean disableChangeTypeWithEgg = plugin.config.getBoolean("disableChangeTypeWithEgg", false);

            // Clicked spawner with monster egg to change type
            if (block.getType() == su.nmsProvider.getSpawnerMaterial()) {
                final Action action = event.getAction();
                if (action != Action.LEFT_CLICK_BLOCK && action != Action.RIGHT_CLICK_BLOCK) {
                    return;
                }

                if (!disableChangeTypeWithEgg) {
                    if (!su.canBuildHere(player, block.getLocation())) {
                        su.sendMessage(player, ChatColor.translateAlternateColorCodes('\u0026',
                                plugin.localization.getString("changingDeniedWorldGuard")));
                        return;
                    }

                    if (!checkIfFactionsPermitsBlockInteractions(player, block)) {
                        event.setCancelled(true);
                        return;
                    }

                    // Mob
                    final String mobName = su.getCreatureName(entityID).toLowerCase(Locale.ENGLISH).replace(" ", "");

                    if (!player.hasPermission("silkspawners.changetypewithegg." + mobName)) {
                        su.sendMessage(player, ChatColor.translateAlternateColorCodes('\u0026',
                                plugin.localization.getString("noPermissionChangingWithEggs")));
                        event.setCancelled(true);
                        return;
                    }

                    // Call the event and maybe change things!
                    final SilkSpawnersSpawnerChangeEvent changeEvent = new SilkSpawnersSpawnerChangeEvent(player, block, entityID,
                            su.getSpawnerEntityID(block), 1);
                    plugin.getServer().getPluginManager().callEvent(changeEvent);
                    // See if we need to stop
                    if (changeEvent.isCancelled()) {
                        event.setCancelled(true);
                        return;
                    }
                    // Get the new ID (might be changed)
                    entityID = changeEvent.getEntityID();

                    su.setSpawnerType(block, entityID, player,
                            ChatColor.translateAlternateColorCodes('\u0026', plugin.localization.getString("changingDeniedWorldGuard")));
                    su.sendMessage(player, ChatColor.translateAlternateColorCodes('\u0026', plugin.localization.getString("changedSpawner"))
                            .replace("%creature%", su.getCreatureName(entityID)));

                    // Consume egg
                    if (plugin.config.getBoolean("consumeEgg", true)) {
                        su.nmsProvider.reduceEggs(player);
                        // Prevent normal eggs reducing
                        event.setCancelled(true);
                    }
                } else {
                    event.setCancelled(true);
                }
            }
            // Normal spawning
            if (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR) {
                if (plugin.config.getBoolean("spawnEggToSpawner", false)) {
                    if (!checkIfFactionsPermitsBlockInteractions(player, block)) {
                        event.setCancelled(true);
                        return;
                    }

                    final Block targetBlock = block.getRelative(BlockFace.UP);
                    // Check if block above is air
                    if (targetBlock.getType() == Material.AIR) {
                        targetBlock.setType(su.nmsProvider.getSpawnerMaterial());
                        su.setSpawnerEntityID(targetBlock, entityID);
                        // Prevent mob spawning
                        // Should we consume the egg?
                        if (plugin.config.getBoolean("consumeEgg", true)) {
                            su.nmsProvider.reduceEggs(player);
                        }
                    } else {
                        su.sendMessage(player,
                                ChatColor.translateAlternateColorCodes('\u0026', plugin.localization.getString("noSpawnerHere")));
                    }
                    event.setCancelled(true);
                } else if (plugin.config.getBoolean("spawnEggOverride", false)) {
                    boolean allowed = plugin.config.getBoolean("spawnEggOverrideSpawnDefault", true);
                    if (entityID != null) {
                        allowed = plugin.mobs.getBoolean("creatures." + entityID + ".enableSpawnEggOverrideAllowSpawn", allowed);
                    }
                    // Deny spawning
                    if (!allowed) {
                        su.sendMessage(player,
                                ChatColor
                                        .translateAlternateColorCodes('\u0026',
                                                plugin.localization.getString("spawningDenied").replace("%ID%", entityID))
                                        .replace("%creature%", su.getCreatureName(entityID)));
                        event.setCancelled(true);
                        return;
                    }
                    // Bukkit doesn't allow us to spawn wither or dragons and so
                    // on. NMS here we go!
                    // https://github.com/Bukkit/CraftBukkit/blob/master/src/main/java/net/minecraft/server/ItemMonsterEgg.java#L22

                    // Notify
                    plugin.informPlayer(player,
                            ChatColor
                                    .translateAlternateColorCodes('\u0026',
                                            plugin.localization.getString("spawning").replace("%ID%", entityID))
                                    .replace("%creature%", su.getCreatureName(entityID)));

                    // Spawn on top of targeted block
                    final Location location = block.getLocation().add(0, 1, 0);
                    final double x = location.getX();
                    final double y = location.getY();
                    final double z = location.getZ();

                    // We can spawn using the direct method from EntityTypes
                    su.nmsProvider.spawnEntity(player.getWorld(), entityID, x, y, z);

                    su.nmsProvider.reduceEggs(player);

                    // Prevent normal spawning
                    event.setCancelled(true);
                }
            }
        }
    }

    public boolean checkIfFactionsPermitsBlockInteractions(final Player player, final Block block) {
        if (plugin.config.getBoolean("factionsSupport", false) && su.isPluginEnabled("Factions")) {
            try {
                final MPlayer mp = MPlayer.get(player);
                final Faction blockFaction = BoardColl.get().getFactionAt(PS.valueOf(block.getLocation()));
                if (!blockFaction.isNone() && !mp.isInOwnTerritory()) {
                    su.sendMessage(player,
                            ChatColor.translateAlternateColorCodes('\u0026', plugin.localization.getString("changingDeniedFactions")));
                    return false;
                }
            } catch (@SuppressWarnings("unused") final NoClassDefFoundError e) {
                // Try for legacy 1.6 factions, e.g. FactionsUUID
                final FPlayers fPlayers = FPlayers.getInstance();
                final FPlayer fPlayer = fPlayers.getByPlayer(player);
                final Board board = Board.getInstance();
                final com.massivecraft.factions.Faction blockFaction = board.getFactionAt(new FLocation(block.getLocation()));
                if (!blockFaction.isWilderness() && !fPlayer.isInOwnTerritory()) {
                    su.sendMessage(player,
                            ChatColor.translateAlternateColorCodes('\u0026', plugin.localization.getString("changingDeniedFactions")));
                    return false;
                }
            }
        }
        return true;
    }
}
