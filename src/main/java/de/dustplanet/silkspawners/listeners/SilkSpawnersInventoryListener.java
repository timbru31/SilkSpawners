package de.dustplanet.silkspawners.listeners;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import de.dustplanet.silkspawners.SilkSpawners;
import de.dustplanet.util.SilkUtil;

/**
 * To show a chat message that a player clicked on an mob spawner
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
	
	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		// Null checks, somehow errors appeared...
		if (event == null || event.getCurrentItem() == null || event.getWhoClicked() == null) {
			return;
		}
		// If we should notify and the item is a mobspawner and we have a player here who has the permission
		if (plugin.config.getBoolean("notifyOnClick") && event.getCurrentItem().getType().equals(Material.MOB_SPAWNER) && event.getWhoClicked() instanceof Player && plugin.hasPermission((Player) event.getWhoClicked(), "silkspawners.info")) {
			// Get the entity ID
			short entityID = su.getStoredSpawnerItemEntityID(event.getCurrentItem());
			// Pig here again
			if (entityID == 0 || !su.knownEids.contains(entityID)) {
				entityID = su.defaultEntityID;
			}
			// Get the name of the creature
			String spawnerName = su.getCreatureName(entityID);
			// Player
			Player player = (Player) event.getWhoClicked();
			su.notify(player, spawnerName, entityID);
		}
	}
}