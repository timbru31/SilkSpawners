package de.dustplanet.silkspawners.listeners;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;

import de.dustplanet.silkspawners.SilkSpawners;
import de.dustplanet.util.SilkUtil;

/**
 * To show a chat message that a player clicked on an mob spawner
 *
 * @author (former) mushroomhostage
 * @author xGhOsTkiLLeRx
 */

public class SilkSpawnersInventoryListener implements Listener {
    private SilkSpawners plugin;
    private SilkUtil su;

    public SilkSpawnersInventoryListener(SilkSpawners instance, SilkUtil util) {
	plugin = instance;
	su = util;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
	// Null checks, somehow errors appeared...
	if (event == null || event.getCurrentItem() == null
		|| event.getWhoClicked() == null) {
	    return;
	}

	// Check for MobSpawner
	if (event.getCurrentItem().getType() != Material.MOB_SPAWNER) {
	    return;
	}

	// Player
	if (!(event.getWhoClicked() instanceof Player)) {
	    return;
	}

	Player player = (Player) event.getWhoClicked();

	// Variables
	short entityID = su.getStoredSpawnerItemEntityID(event.getCurrentItem());
	// Pig here again
	if (entityID == 0 || !su.knownEids.contains(entityID)) {
	    entityID = su.defaultEntityID;
	}
	String creatureName = su.getCreatureName(entityID);

	/*
	 * Crafting
	 */
	if (event.getSlotType() == InventoryType.SlotType.RESULT) {
	    String spawnerName = creatureName.toLowerCase().replace(" ", "");
	    if (!plugin.hasPermission(player, "silkspawners.craft.*") && !plugin.hasPermission(player, "silkspawners.craft." + spawnerName)) {
		event.setResult(Result.DENY);
		event.setCancelled(true);
		player.sendMessage(ChatColor.translateAlternateColorCodes('\u0026', plugin.localization.getString("noPermissionCraft").replace("%ID%", Short.toString(entityID))).replace("%creature%", spawnerName));
		return;
	    }
	}

	// If we should notify and the item is a MobSpawner and we have a player
	// here who has the permission
	if (plugin.config.getBoolean("notifyOnClick") && plugin.hasPermission(player, "silkspawners.info")) {
	    // Player
	    su.notify(player, creatureName, entityID);
	}
    }
}
