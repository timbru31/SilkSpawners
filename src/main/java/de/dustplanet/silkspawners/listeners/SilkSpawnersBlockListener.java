package de.dustplanet.silkspawners.listeners;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import de.dustplanet.silkspawners.SilkSpawners;
import de.dustplanet.silkspawners.SilkSpawnersSetCreatureTask;
import de.dustplanet.util.SilkUtil;

/**
 * Handle the placement, breaking and explosion of a spawner
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

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
	Block block = event.getBlock();
	Player player = event.getPlayer();
	// To prevent silk touch exploit by breaking blocks holding spawner
	// (since abuses Silk Touch enchantment), cancel event
	// NOTE: No longer necessary, I leave it in here for compatibility with
	// old spawners
	ItemStack heldItem = player.getItemInHand();
	if (heldItem != null && heldItem.getType() == Material.MOB_SPAWNER && plugin.config.getBoolean("denyBreakHoldingSpawner", true)) {
	    event.setCancelled(true);
	    return;
	}

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

	// mcMMO sends its own FakeBlockBreakEvent with super breaker ability,
	// causing us to think
	// the spawner is broken when it isn't, allowing for duping:
	// http://www.youtube.com/watch?v=GGlyZmph8NM
	// Ignore these events if configured
	// NOTE: Researches say it's fixed, just in case
	if (event.getClass() != BlockBreakEvent.class && plugin.config.getBoolean("ignoreFakeBreakEvents", true)) {
	    return;
	}

	// Message the player about the broken spawner
	plugin.informPlayer(player, ChatColor.translateAlternateColorCodes('\u0026',
		plugin.localization.getString("spawnerBroken").replaceAll("%creature%", su.getCreatureName(entityID))));

	// If using silk touch, drop spawner itself
	ItemStack tool = player.getItemInHand();
	// Check for SilkTocuh level
	boolean silkTouch = hasSilkTouch(tool);

	// Get the world to drop in
	World world = player.getWorld();

	// Mob
	String mobName = su.getCreatureName(entityID).toLowerCase().replaceAll(" ", "");

	// No drops in creative
	if (plugin.config.getBoolean("noDropsCreative", true) && player.getGameMode() == GameMode.CREATIVE) {
	    return;
	}

	// Prevent XP farming/duping
	event.setExpToDrop(0);
	if (plugin.hasPermission(player, "silkspawners.silkdrop." + mobName)
		|| plugin.hasPermission(player, "silkspawners.silkdrop.*")
		|| plugin.hasPermission(player, "silkspawners.destroydrop." + mobName)
		|| plugin.hasPermission(player, "silkspawners.destroydrop.*")) {
	    // If we have more than 0 XP, drop them
	    int addXP = plugin.config.getInt("destroyDropXP");
	    if (addXP != 0) {
		event.setExpToDrop(addXP);
	    }
	}

	// Case 1 -> silk touch
	if (silkTouch && (plugin.hasPermission(player, "silkspawners.silkdrop." + mobName)
		|| plugin.hasPermission(player, "silkspawners.silkdrop.*"))) {
	    // Drop spawner
	    world.dropItemNaturally(block.getLocation(), su.newSpawnerItem(entityID, plugin.localization.getString("spawnerName")));
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

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPlace(final BlockPlaceEvent event) {
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
	String spawnerName = creatureName.replaceAll(" ", "");

	// Check for place permission
	if (!plugin.hasPermission(player, "silkspawners.place.*")
		&& !plugin.hasPermission(player, "silkspawners.place." + spawnerName)) {
	    event.setCancelled(true);
	    player.sendMessage(ChatColor.translateAlternateColorCodes('\u0026', plugin.localization.getString("noPermissionPlace").replaceAll("%creature%", spawnerName).replaceAll("%ID%", Short.toString(entityID))));
	    return;
	}

	// Message default
	if (entityID == su.defaultEntityID) {
	    plugin.informPlayer(player, ChatColor.translateAlternateColorCodes('\u0026', plugin.localization.getString("placingDefault")));
	}
	// Else message the type
	else {
	    plugin.informPlayer(player, ChatColor.translateAlternateColorCodes('\u0026', plugin.localization.getString("spawnerPlaced").replaceAll("%creature%", su.getCreatureName(entityID))));
	}

	su.setSpawnerEntityID(blockPlaced, entityID);
	// Bukkit 1.1-R3 regressed from 1.1-R1, ignores block state update on
	// onBlockPlace
	// TODO: file or find bug about this, get it fixed so can remove this
	// lame workaround
	// Still on 1.4 -> Ticket
	// https://bukkit.atlassian.net/browse/BUKKIT-2974
	plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new SilkSpawnersSetCreatureTask(entityID, blockPlaced, su), 1);
    }

    // Checks if the given ItemStack has got the SilkTouch
    private boolean hasSilkTouch(ItemStack tool) {
	int minLevel = plugin.config.getInt("minSilkTouchLevel", 1);
	// Always have it
	if (minLevel == 0) {
	    return true;
	}
	// No silk touch fists..
	if (tool == null) {
	    return false;
	}

	// This check isn't actually necessary, since containsEnchantment just
	// checks level>0,
	// but is kept here for clarity, and in case Bukkit allows level-0
	// enchantments like vanilla
	if (!tool.containsEnchantment(Enchantment.SILK_TOUCH)) {
	    return false;
	}
	// Return if the level is enough
	return tool.getEnchantmentLevel(Enchantment.SILK_TOUCH) >= minLevel;
    }
}