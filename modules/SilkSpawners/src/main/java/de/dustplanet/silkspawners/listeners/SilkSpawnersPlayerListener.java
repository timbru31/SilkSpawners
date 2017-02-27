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
import us.forseth11.feudal.core.Feudal;
import us.forseth11.feudal.kingdoms.Kingdom;
import us.forseth11.feudal.kingdoms.Land;
import us.forseth11.feudal.user.User;

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
        if (event.getPlayer().getInventory().getItem(event.getNewSlot()) != null
                && event.getPlayer().getInventory().getItem(event.getNewSlot()).getType().equals(Material.MOB_SPAWNER)
                && plugin.config.getBoolean("notifyOnHold")
                && event.getPlayer().hasPermission("silkspawners.info")) {

            // Get ID
            short entityID = su
                    .getStoredSpawnerItemEntityID(event.getPlayer().getInventory().getItem(event.getNewSlot()));
            // Check for unknown/invalid ID
            if (entityID == 0 || !su.knownEids.contains(entityID)) {
                entityID = su.getDefaultEntityID();
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
            short entityID = su.getStoredEggEntityID(item);
            // Clicked spawner with monster egg to change type
            if (block != null && block.getType() == Material.MOB_SPAWNER) {
                Action action = event.getAction();
                if (action != Action.LEFT_CLICK_BLOCK && action != Action.RIGHT_CLICK_BLOCK) {
                    return;
                }

                if (action != Action.RIGHT_CLICK_BLOCK && plugin.config.getBoolean("disableChangeTypeWithEgg", false)) {
                    return;
                }

                // WorldGuard region protection
                if (!su.canBuildHere(player, block.getLocation())) {
                    return;
                }

                // Mob
                String mobName = su.getCreatureName(entityID).toLowerCase().replace(" ", "");

                if (!player.hasPermission("silkspawners.changetypewithegg." + mobName)) {
                    su.sendMessage(player, ChatColor.translateAlternateColorCodes('\u0026',
                            plugin.localization.getString("noPermissionChangingWithEggs")));
                    event.setCancelled(true);
                    return;
                }

                if (plugin.config.getBoolean("factionsSupport", false) && su.isPluginEnabled("Factions")) {
                    try {
                        MPlayer mp = MPlayer.get(player);
                        Faction blockFaction = BoardColl.get().getFactionAt(PS.valueOf(block.getLocation()));
                        if (!blockFaction.isNone() && !mp.isInOwnTerritory()) {
                            event.setCancelled(true);
                            su.sendMessage(player, ChatColor
                                    .translateAlternateColorCodes('\u0026', plugin.localization.getString("changingDeniedFactions")));
                            return;
                        }
                    } catch (NoClassDefFoundError e) {
                        // Try for legacy 1.6 factions, e.g. FactionsUUID
                        FPlayers fPlayers = FPlayers.getInstance();
                        FPlayer fPlayer = fPlayers.getByPlayer(player);
                        Board board = Board.getInstance();
                        com.massivecraft.factions.Faction blockFaction = board.getFactionAt(new FLocation(block.getLocation()));
                        if (!blockFaction.isWilderness() && !fPlayer.isInOwnTerritory()) {
                            event.setCancelled(true);
                            su.sendMessage(player, ChatColor
                                    .translateAlternateColorCodes('\u0026', plugin.localization.getString("changingDeniedFactions")));
                            return;
                        }
                    }
                }
                if (plugin.config.getBoolean("feudalSupport", false) && su.isPluginEnabled("Feudal")) {
                    Land blockLand = new Land(block.getLocation());
                    Kingdom blockKingdom = Feudal.getLandKingdom(blockLand);
                    User user = Feudal.getUser(player.getUniqueId().toString());
                    if (blockKingdom != null && !blockKingdom.isMember(user.getUUID())) {
                        event.setCancelled(true);
                        su.sendMessage(player, ChatColor
                                .translateAlternateColorCodes('\u0026', plugin.localization.getString("changingDeniedFeudal")));
                        return;
                    }
                }

                // Call the event and maybe change things!
                SilkSpawnersSpawnerChangeEvent changeEvent = new SilkSpawnersSpawnerChangeEvent(player, block, entityID,
                        su.getSpawnerEntityID(block), 1);
                plugin.getServer().getPluginManager().callEvent(changeEvent);
                // See if we need to stop
                if (changeEvent.isCancelled()) {
                    event.setCancelled(true);
                    return;
                }
                // Get the new ID (might be changed)
                entityID = changeEvent.getEntityID();

                su.setSpawnerType(block, entityID, player, ChatColor.translateAlternateColorCodes('\u0026',
                        plugin.localization.getString("changingDeniedWorldGuard")));
                su.sendMessage(player, ChatColor
                        .translateAlternateColorCodes('\u0026', plugin.localization.getString("changedSpawner"))
                        .replace("%creature%", su.getCreatureName(entityID)));

                // Consume egg
                if (plugin.config.getBoolean("consumeEgg", true)) {
                    su.nmsProvider.reduceEggs(player);
                    // Prevent normal eggs reducing
                    event.setCancelled(true);
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
                            su.nmsProvider.reduceEggs(player);
                        }
                    } else {
                        su.sendMessage(player, ChatColor.translateAlternateColorCodes('\u0026',
                                plugin.localization.getString("noSpawnerHere")));
                    }
                    event.setCancelled(true);
                } else if (plugin.config.getBoolean("spawnEggOverride", false)) {
                    // Disabled by default, since it is dangerous
                    // Name
                    String mobID = su.eid2MobID.get(entityID);
                    // Are we allowed to spawn?
                    boolean allowed = plugin.config.getBoolean("spawnEggOverrideSpawnDefault", true);
                    if (mobID != null) {
                        allowed = plugin.mobs.getBoolean("creatures." + mobID + ".enableSpawnEggOverrideAllowSpawn",
                                allowed);
                    }
                    // Deny spawning
                    if (!allowed) {
                        su.sendMessage(player, ChatColor
                                .translateAlternateColorCodes('\u0026',
                                        plugin.localization.getString("spawningDenied").replace("%ID%",
                                                Short.toString(entityID)))
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
                                    plugin.localization.getString("spawning").replace("%ID%",
                                            Short.toString(entityID)))
                            .replace("%creature%", su.getCreatureName(entityID)));

                    // Spawn on top of targeted block
                    Location location = block.getLocation().add(0, 1, 0);
                    double x = location.getX();
                    double y = location.getY();
                    double z = location.getZ();

                    // We can spawn using the direct method from EntityTypes
                    su.nmsProvider.spawnEntity(player.getWorld(), entityID, x, y, z);

                    su.nmsProvider.reduceEggs(player);

                    // Prevent normal spawning
                    event.setCancelled(true);
                }
            }
        }
    }
}
