package de.dustplanet.silkspawners;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.getspout.spoutapi.player.SpoutPlayer;

public class SilkSpawnersInventoryListener implements Listener {
	private SilkSpawners plugin;
	private SilkUtil su;
	
	public SilkSpawnersInventoryListener(SilkSpawners instance, SilkUtil util) {
		plugin = instance;
		su = util;
	}
	
	/**
	 * To show a chat message that a player clicked on an mob spawner
	 * @author (former) mushroomhostage
	 * @author xGhOsTkiLLeRx
	 */
	
	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		// Null checks, somehow erros appeared...
		if (event == null || event.getCurrentItem() == null || event.getWhoClicked() == null) return;
		// If we should notify and the item is a mobspawner and we have a player here who has the permission
		if (plugin.getConfig().getBoolean("notifyOnClick") && event.getCurrentItem().getType().equals(Material.MOB_SPAWNER) && event.getWhoClicked() instanceof Player && plugin.hasPermission((Player) event.getWhoClicked(), "silkspawners.info")) {
			// Don't spam with pigs
			if (su.getStoredSpawnerItemEntityID(event.getCurrentItem()) == 0 && su.defaultEntityID == 0) return;
			// Get the entity ID
			short entityID = su.getStoredSpawnerItemEntityID(event.getCurrentItem());
			// Pig here again
			if (entityID == 0) entityID = su.defaultEntityID;
			// Get the name of the creature
			String spawnerName = su.getCreatureName(entityID);
			// Player
			Player player = (Player) event.getWhoClicked();
			// If we use Spout & the player Spoutcraft, send a notification (achievement like)
			if (plugin.spoutEnabled && ((SpoutPlayer) player).isSpoutCraftEnabled()) {
				((SpoutPlayer) player).sendNotification("Monster Spawner", spawnerName, Material.MOB_SPAWNER);
			}
			else {
				player.sendMessage(" ");
				player.sendMessage("-- Monster Spawner --");
				player.sendMessage("-- Type: " + spawnerName);
				player.sendMessage("-- EntityID: " + entityID);
			}
		}

	}
}
