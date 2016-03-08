package de.dustplanet.silkspawners.listeners;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import de.dustplanet.silkspawners.SilkSpawners;
import de.dustplanet.util.SilkUtil;

/**
 * To show a chat message that a player clicked on an mob spawner.
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
    public void onItemCraft(CraftItemEvent event) {
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

        ItemStack result = event.getCurrentItem();

        for (ItemStack itemStack : event.getInventory().getContents()) {
            if (itemStack.getType() == SilkUtil.SPAWN_EGG && itemStack.getDurability() == 0) {
                plugin.getServer().getLogger().warning("Found vanilla spawn egg in crafting recipe of a spawner. Please use SilkSpawners to receive spawn eggs instead!");
                short entityID = su.getStoredEggEntityID(itemStack);
                String mobID = su.eid2MobID.get(entityID);
                result = su.newSpawnerItem(entityID, su.getCustomSpawnerName(mobID), result.getAmount(), true);
                event.setCurrentItem(result);
            }
        }

        // Variables
        short entityID = su.getStoredSpawnerItemEntityID(result);
        // Pig here again
        if (entityID == 0 || !su.knownEids.contains(entityID)) {
            entityID = su.getDefaultEntityID();
        }
        String creatureName = su.getCreatureName(entityID);

        String spawnerName = creatureName.toLowerCase().replace(" ", "");
        if (!player.hasPermission("silkspawners.craft." + spawnerName)) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.translateAlternateColorCodes('\u0026', plugin.localization.getString("noPermissionCraft").replace("%ID%", Short.toString(entityID))).replace("%creature%", spawnerName));
            return;
        }
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
            entityID = su.getDefaultEntityID();
        }
        String creatureName = su.getCreatureName(entityID);

        // If we should notify and the item is a MobSpawner and we have a player
        // here who has the permission
        if (plugin.config.getBoolean("notifyOnClick") && player.hasPermission("silkspawners.info")) {
            // Player
            su.notify(player, creatureName, entityID);
        }
    }
}
