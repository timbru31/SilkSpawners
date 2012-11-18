package de.dustplanet.silkspawners;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.getspout.spoutapi.player.SpoutPlayer;

public class SilkSpawnersInventoryListener implements Listener {
	private SilkSpawners plugin;
	public SilkSpawnersInventoryListener(SilkSpawners plugin) {
		this.plugin = plugin;
	}
	
	/**
	 * To show a chat message that a player clicked on an mob spawner
	 * @param event
	 * @author (former) Chris Churchwell (thedudeguy)
	 * @author xGhOsTkiLLeRx
	 */
	
	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		// Null checks, somehow erros appeared...
		if (event.getCurrentItem() == null || event.getWhoClicked() == null) return;
		// If we should notify and the item is a mobspawner and we have a player here who has the permission
		if (plugin.getConfig().getBoolean("notifyOnClick") && event.getCurrentItem().getType().equals(Material.MOB_SPAWNER) && event.getWhoClicked() instanceof Player && plugin.hasPermission((Player)event.getWhoClicked(), "silkspawners.info")) {
			// TODO Static
			// Don't spam with pigs
			if (SilkSpawners.getStoredSpawnerItemEntityID(event.getCurrentItem()) == 0 && plugin.defaultEntityID == 0) return;

			short entityID = SilkSpawners.getStoredSpawnerItemEntityID(event.getCurrentItem());
			if (entityID == 0) entityID = plugin.defaultEntityID;

			String spawnerName = plugin.getCreatureName(entityID);

			if (plugin.spoutEnabled && ((SpoutPlayer)event.getWhoClicked()).isSpoutCraftEnabled()) {
				((SpoutPlayer)event.getWhoClicked()).sendNotification("Monster Spawner", spawnerName, Material.MOB_SPAWNER);
			} else {
				Player player = (Player)event.getWhoClicked();
				player.sendMessage(" ");
				player.sendMessage("-- Monster Spawner --");
				player.sendMessage("-- Type: " + spawnerName);
			}
		}

	}
}
