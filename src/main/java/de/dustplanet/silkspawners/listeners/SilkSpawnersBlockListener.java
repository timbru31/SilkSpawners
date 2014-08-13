package de.dustplanet.silkspawners.listeners;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import de.dustplanet.silkspawners.SilkSpawners;
import de.dustplanet.util.SilkUtil;

/**
 * Handle the placement and breaking of a spawner
 *
 * @author (former) mushroomhostage
 * @author xGhOsTkiLLeRx
 */

public class SilkSpawnersBlockListener implements Listener {
    private SilkSpawners plugin;
    private SilkUtil su;

    public SilkSpawnersBlockListener(SilkSpawners instance, SilkUtil util) {
        plugin = instance;
        su = util;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.isCancelled()) {
            return;
        }
        Block block = event.getBlock();
        Player player = event.getPlayer();

        // We just want the mob spawner events
        if (block.getType() != Material.MOB_SPAWNER) {
            return;
        }

        // We can't build here? Return then
        if (!su.canBuildHere(player, block.getLocation())) {
            return;
        }

        // Get the entityID from the spawner
        short entityID = su.getSpawnerEntityID(block);

        // Message the player about the broken spawner
        plugin.informPlayer(player, ChatColor.translateAlternateColorCodes('\u0026', plugin.localization.getString("spawnerBroken")).replace("%creature%", su.getCreatureName(entityID)));

        // If using silk touch, drop spawner itself
        ItemStack tool = player.getItemInHand();
        // Check for SilkTocuh level
        boolean silkTouch = su.hasSilkTouch(tool);

        // Get the world to drop in
        World world = player.getWorld();

        // Mob
        String mobName = su.getCreatureName(entityID).toLowerCase().replace(" ", "");

        // No drops in creative
        if (plugin.config.getBoolean("noDropsCreative", true) && player.getGameMode() == GameMode.CREATIVE) {
            return;
        }

        // Prevent XP farming/duping
        event.setExpToDrop(0);
        boolean mined = false;
        
        if (plugin.config.getBoolean("preventXPFarming", true) && block.hasMetadata("mined")) {
            mined = block.getMetadata("mined").get(0).asBoolean();
        }

        if (plugin.hasPermission(player, "silkspawners.silkdrop." + mobName)
                || plugin.hasPermission(player, "silkspawners.silkdrop.*")
                || plugin.hasPermission(player, "silkspawners.destroydrop." + mobName)
                || plugin.hasPermission(player, "silkspawners.destroydrop.*")) {
            // If we have more than 0 XP, drop them
            int addXP = plugin.config.getInt("destroyDropXP");
            if (!mined && addXP != 0) {
                event.setExpToDrop(addXP);
                // check if we should flag spawners
                if (plugin.config.getBoolean("preventXPFarming", true)) {
                    block.setMetadata("mined", new FixedMetadataValue(plugin, true));
                }
            }
        }

        // Case 1 -> silk touch
        if (silkTouch && (plugin.hasPermission(player, "silkspawners.silkdrop." + mobName)
                || plugin.hasPermission(player, "silkspawners.silkdrop.*"))) {
            // Drop spawner
            world.dropItemNaturally(block.getLocation(), su.newSpawnerItem(entityID, su.getCustomSpawnerName(su.eid2MobID.get(entityID)), 1));
            return;
        }

        // Case 2 -> no silk touch
        if (plugin.hasPermission(player, "silkspawners.destroydrop." + mobName)
                || plugin.hasPermission(player, "silkspawners.destroydrop.*")) {
            if (plugin.config.getBoolean("destroyDropEgg")) {
                // Drop egg
                world.dropItemNaturally(block.getLocation(), su.newEggItem(entityID));
            }
            // Drop iron bars (or not)
            int dropBars = plugin.config.getInt("destroyDropBars");
            if (dropBars != 0) {
                world.dropItem(block.getLocation(), new ItemStack(Material.IRON_FENCE, dropBars));
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (event.isCancelled()) {
            return;
        }
        Block blockPlaced = event.getBlockPlaced();
        // Just mob spawner events
        if (blockPlaced.getType() != Material.MOB_SPAWNER) {
            return;
        }
        Player player = event.getPlayer();
        // If the player can't build here, return
        if (!su.canBuildHere(player, blockPlaced.getLocation())) {
            return;
        }
        // Get the item
        ItemStack item = event.getItemInHand();
        // Get data from item
        short entityID = su.getStoredSpawnerItemEntityID(item);
        // 0 or unknown then fallback
        if (entityID == 0 || !su.knownEids.contains(entityID)) {
            // Default
            entityID = su.defaultEntityID;
        }

        // Names
        String creatureName = su.getCreatureName(entityID).toLowerCase();
        String spawnerName = creatureName.replace(" ", "");

        // Check for place permission
        if (!plugin.hasPermission(player, "silkspawners.place.*")
                && !plugin.hasPermission(player, "silkspawners.place." + spawnerName)) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.translateAlternateColorCodes('\u0026', plugin.localization.getString("noPermissionPlace").replace("%ID%", Short.toString(entityID))).replace("%creature%", spawnerName));
            return;
        }

        // Message default
        if (entityID == su.defaultEntityID) {
            plugin.informPlayer(player, ChatColor.translateAlternateColorCodes('\u0026', plugin.localization.getString("placingDefault")));
        } else {
            // Else message the type
            plugin.informPlayer(player, ChatColor.translateAlternateColorCodes('\u0026', plugin.localization.getString("spawnerPlaced")).replace("%creature%", su.getCreatureName(entityID)));
        }

        su.setSpawnerEntityID(blockPlaced, entityID);
    }
}
