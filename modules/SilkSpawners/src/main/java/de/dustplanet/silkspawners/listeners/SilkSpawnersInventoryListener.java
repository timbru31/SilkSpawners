package de.dustplanet.silkspawners.listeners;

import java.util.Locale;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
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
    private final SilkSpawners plugin;
    private final SilkUtil su;

    public SilkSpawnersInventoryListener(final SilkSpawners instance, final SilkUtil util) {
        plugin = instance;
        su = util;
    }

    @SuppressWarnings("deprecation")
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPrepareItemCraftEvent(final PrepareItemCraftEvent event) {
        if (event.getRecipe() == null || event.getRecipe().getResult() == null) {
            return;
        }

        if (event.getRecipe().getResult().getType() != su.nmsProvider.getSpawnerMaterial()) {
            return;
        }

        ItemStack result = event.getRecipe().getResult();

        for (final ItemStack itemStack : event.getInventory().getContents()) {
            if (su.nmsProvider.getSpawnEggMaterials().contains(itemStack.getType()) && itemStack.getDurability() == 0) {
                final String entityID = su.getStoredEggEntityID(itemStack);
                result = su.newSpawnerItem(entityID, su.getCustomSpawnerName(entityID), result.getAmount(), true);
                event.getInventory().setResult(result);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onItemCraft(final CraftItemEvent event) {
        if (event == null || event.getCurrentItem() == null || event.getWhoClicked() == null) {
            return;
        }

        if (event.getCurrentItem().getType() != su.nmsProvider.getSpawnerMaterial()) {
            return;
        }

        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        final Player player = (Player) event.getWhoClicked();

        String entityID = su.getStoredSpawnerItemEntityID(event.getCurrentItem());
        if (entityID == null) {
            entityID = su.getDefaultEntityID();
        }
        final String creatureName = su.getCreatureName(entityID);

        final String spawnerName = creatureName.toLowerCase(Locale.ENGLISH).replace(" ", "");
        if (!su.hasPermission(player, "silkspawners.craft.", entityID)) {
            event.setCancelled(true);
            su.sendMessage(player,
                    ChatColor
                            .translateAlternateColorCodes('\u0026',
                                    plugin.localization.getString("noPermissionCraft").replace("%ID%", entityID))
                            .replace("%creature%", spawnerName));
            return;
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryClick(final InventoryClickEvent event) {
        if (event == null || event.getCurrentItem() == null || event.getWhoClicked() == null) {
            return;
        }

        if (event.getCurrentItem().getType() != su.nmsProvider.getSpawnerMaterial()) {
            return;
        }

        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        final Player player = (Player) event.getWhoClicked();

        String entityID = su.getStoredSpawnerItemEntityID(event.getCurrentItem());

        if (entityID == null) {
            entityID = su.getDefaultEntityID();
        }
        final String creatureName = su.getCreatureName(entityID);

        if (plugin.config.getBoolean("notifyOnClick") && player.hasPermission("silkspawners.info")) {
            su.notify(player, creatureName);
        }
    }
}
