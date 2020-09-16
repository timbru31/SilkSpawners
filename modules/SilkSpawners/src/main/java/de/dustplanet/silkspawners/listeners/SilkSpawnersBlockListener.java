package de.dustplanet.silkspawners.listeners;

import java.util.HashMap;
import java.util.Random;
import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
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
import de.dustplanet.silkspawners.events.SilkSpawnersSpawnerBreakEvent;
import de.dustplanet.silkspawners.events.SilkSpawnersSpawnerPlaceEvent;
import de.dustplanet.util.SilkUtil;

/**
 * Handle the placement and breaking of a spawner.
 *
 * @author (former) mushroomhostage
 * @author xGhOsTkiLLeRx
 */

public class SilkSpawnersBlockListener implements Listener {
    private final SilkSpawners plugin;
    private final SilkUtil su;
    private final Random rnd;

    public SilkSpawnersBlockListener(final SilkSpawners instance, final SilkUtil util) {
        plugin = instance;
        su = util;
        rnd = new Random();
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(final BlockBreakEvent event) {
        plugin.getLogger().fine("Handling a block break event");
        final boolean isFakeEvent = !BlockBreakEvent.class.equals(event.getClass());
        if (isFakeEvent) {
            plugin.getLogger().fine("Skipping block break event because the event is fake");
            return;
        }

        final Block block = event.getBlock();
        final Player player = event.getPlayer();

        if (block.getType() != su.nmsProvider.getSpawnerMaterial()) {
            plugin.getLogger().fine("Skipping block break event because the block is not a spawner");
            return;
        }

        if (!su.canBuildHere(player, block.getLocation())) {
            plugin.getLogger().fine("Skipping block break event because the player can't build here");
            return;
        }

        String entityID = su.getSpawnerEntityID(block);
        plugin.getLogger().log(Level.FINE, "The stored entity of the block is {0}", entityID);

        final SilkSpawnersSpawnerBreakEvent breakEvent = new SilkSpawnersSpawnerBreakEvent(player, block, entityID);
        plugin.getServer().getPluginManager().callEvent(breakEvent);
        if (breakEvent.isCancelled()) {
            plugin.getLogger().fine("Skipping block break event because the the SilkSpawnersBreakEvent was cancelled");
            event.setCancelled(true);
            return;
        }

        entityID = su.getDisplayNameToMobID().get(breakEvent.getEntityID());
        plugin.getLogger().log(Level.FINE, "The stored entity of the block is {0}", entityID);

        plugin.informPlayer(player, ChatColor.translateAlternateColorCodes('\u0026', plugin.localization.getString("spawnerBroken"))
                .replace("%creature%", su.getCreatureName(entityID)));

        final ItemStack tool = su.nmsProvider.getItemInHand(player);
        final boolean validToolAndSilkTouch = su.isValidItemAndHasSilkTouch(tool);

        final World world = player.getWorld();

        if (plugin.config.getBoolean("noDropsCreative", true) && player.getGameMode() == GameMode.CREATIVE) {
            plugin.getLogger().fine("Skipping block break event because the game mode is creative and noDropsCreative is true");
            return;
        }

        event.setExpToDrop(0);
        boolean mined = false;
        final boolean dropXPOnlyOnDestroy = plugin.config.getBoolean("dropXPOnlyOnDestroy", false);

        if (plugin.config.getBoolean("preventXPFarming", true) && block.hasMetadata("mined")) {
            mined = block.getMetadata("mined").get(0).asBoolean();
            plugin.getLogger().fine("Checking mined flag of the block");
        }

        if (su.hasPermission(player, "silkspawners.silkdrop.", entityID)
                || su.hasPermission(player, "silkspawners.destroydrop.", entityID)) {
            final int addXP = plugin.config.getInt("destroyDropXP");
            // If we have more than 0 XP, drop them
            // either we drop XP for destroy and silktouch or only when
            // destroyed and we have no silktouch
            if (!mined && addXP != 0 && (!dropXPOnlyOnDestroy || !validToolAndSilkTouch)) {
                event.setExpToDrop(addXP);
                // check if we should flag spawners
                if (plugin.config.getBoolean("preventXPFarming", true)) {
                    block.setMetadata("mined", new FixedMetadataValue(plugin, true));
                    plugin.getLogger().fine("Setting mined flag of the placed block");
                }
            }
        }

        int randomNumber = rnd.nextInt(100);
        int dropChance = 0;

        if ((validToolAndSilkTouch && su.hasPermission(player, "silkspawners.silkdrop.", entityID))
                || su.hasPermission(player, "silkspawners.nosilk.", entityID)) {
            plugin.getLogger().log(Level.FINE, "Player has silkdrop permission {0}",
                    su.hasPermission(player, "silkspawners.silkdrop.", entityID));
            plugin.getLogger().log(Level.FINE, "Player has nosilk permission {0}",
                    su.hasPermission(player, "silkspawners.nosilk.", entityID));
            if (plugin.mobs.contains("creatures." + entityID + ".silkDropChance")) {
                dropChance = plugin.mobs.getInt("creatures." + entityID + ".silkDropChance", 100);
            } else {
                dropChance = plugin.config.getInt("silkDropChance", 100);
            }

            plugin.getLogger().log(Level.FINE, "Drop chance is {0}", dropChance);

            if (randomNumber < dropChance) {
                plugin.getLogger().fine("Dice rolled, proceed");
                final ItemStack breakEventDrop = breakEvent.getDrop();
                ItemStack spawnerItemStack = null;
                if (breakEventDrop != null) {
                    spawnerItemStack = breakEventDrop;
                } else {
                    int amount = 1;
                    if (plugin.mobs.contains("creatures." + entityID + ".dropAmount")) {
                        amount = plugin.mobs.getInt("creatures." + entityID + ".dropAmount", 1);
                    } else {
                        amount = plugin.config.getInt("dropAmount", 1);
                    }
                    spawnerItemStack = su.newSpawnerItem(entityID, su.getCustomSpawnerName(entityID), amount, false);
                }
                if (spawnerItemStack == null) {
                    plugin.getLogger().warning("Skipping dropping of spawner, since item is null");
                    return;
                }
                plugin.getLogger().log(Level.FINE, "Dropping {0} of {1} ",
                        new Object[] { spawnerItemStack.getAmount(), spawnerItemStack.getType() });

                if (plugin.getConfig().getBoolean("dropSpawnerToInventory", false)) {
                    plugin.getLogger().fine("Dropping into the inventory");

                    final HashMap<Integer, ItemStack> additionalItems = player.getInventory().addItem(spawnerItemStack);
                    if (!additionalItems.isEmpty()) {
                        plugin.getLogger().fine("Inventory is full, dropping the rest naturally on the ground");

                        for (final ItemStack itemStack : additionalItems.values()) {
                            world.dropItemNaturally(block.getLocation(), itemStack);
                        }
                    }
                } else {
                    plugin.getLogger().fine("Dropping naturally on the ground");

                    world.dropItemNaturally(block.getLocation(), spawnerItemStack);
                }
            }
            return;
        }

        if (su.hasPermission(player, "silkspawners.destroydrop.", entityID)) {
            plugin.getLogger().fine("Player has destroydrop, checking spawn eggs and iron bars drops");

            if (plugin.config.getBoolean("destroyDropEgg", false)) {
                randomNumber = rnd.nextInt(100);
                if (plugin.mobs.contains("creatures." + entityID + ".eggDropChance")) {
                    dropChance = plugin.mobs.getInt("creatures." + entityID + ".eggDropChance", 100);
                } else {
                    dropChance = plugin.config.getInt("eggDropChance", 100);
                }
                plugin.getLogger().log(Level.FINE, "Spawn egg drop chance is", dropChance);

                if (randomNumber < dropChance) {
                    plugin.getLogger().fine("Dropping a spawn egg on the ground");

                    world.dropItemNaturally(block.getLocation(), su.newEggItem(entityID, 1, su.getCreatureEggName(entityID)));
                }
            }

            final int dropBars = plugin.config.getInt("destroyDropBars", 0);
            if (dropBars != 0) {
                randomNumber = rnd.nextInt(100);
                if (plugin.mobs.contains("creatures." + entityID + ".destroyDropChance")) {
                    dropChance = plugin.mobs.getInt("creatures." + entityID + ".destroyDropChance", 100);
                } else {
                    dropChance = plugin.config.getInt("destroyDropChance", 100);
                }
                plugin.getLogger().log(Level.FINE, "Iron bars drop chance is", dropChance);
                if (randomNumber < dropChance) {
                    plugin.getLogger().fine("Dropping a iron bars on the ground");
                    world.dropItem(block.getLocation(), new ItemStack(su.nmsProvider.getIronFenceMaterial(), dropBars));
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockPlace(final BlockPlaceEvent event) {
        plugin.getLogger().fine("Handling a block place event");
        if (event.isCancelled()) {
            plugin.getLogger().fine("Returning because the event is already cancelled");
            return;
        }

        final boolean isFakeEvent = !BlockPlaceEvent.class.equals(event.getClass());
        if (isFakeEvent) {
            plugin.getLogger().fine("Skipping block place event because the event is fake");
            return;
        }

        final Block blockPlaced = event.getBlockPlaced();
        if (blockPlaced.getType() != su.nmsProvider.getSpawnerMaterial()) {
            plugin.getLogger().fine("Skipping block place event because the placed block is not a spawner");
            return;
        }
        final Player player = event.getPlayer();
        if (!su.canBuildHere(player, blockPlaced.getLocation())) {
            plugin.getLogger().fine("Skipping block break event because the player can't build here");
            return;
        }
        final ItemStack item = event.getItemInHand();
        String entityID = su.getStoredSpawnerItemEntityID(item);
        plugin.getLogger().log(Level.FINE, "The stored entity of the item is {0}", entityID);
        boolean defaultID = false;
        if (entityID == null) {
            defaultID = true;
            entityID = su.getDefaultEntityID();
            plugin.getLogger().fine("Stored entity was null, setting default");
        }

        final SilkSpawnersSpawnerPlaceEvent placeEvent = new SilkSpawnersSpawnerPlaceEvent(player, blockPlaced, entityID);
        plugin.getServer().getPluginManager().callEvent(placeEvent);

        if (placeEvent.isCancelled()) {
            plugin.getLogger().fine("Skipping block place event because the SilkSpawnersPlaceEvent was cancelled");
            event.setCancelled(true);
            return;
        }

        entityID = placeEvent.getEntityID();
        plugin.getLogger().log(Level.FINE, "The stored entity of the item is {0}", entityID);

        final String creatureName = su.getCreatureName(entityID);

        if (!su.hasPermission(player, "silkspawners.place.", entityID)) {
            event.setCancelled(true);
            su.sendMessage(player,
                    ChatColor
                            .translateAlternateColorCodes('\u0026',
                                    plugin.localization.getString("noPermissionPlace").replace("%ID%", entityID))
                            .replace("%creature%", creatureName));
            plugin.getLogger().fine("Skipping block place event because the player is missing the permission");

            return;
        }

        if (defaultID) {
            plugin.informPlayer(player, ChatColor.translateAlternateColorCodes('\u0026', plugin.localization.getString("placingDefault")));
        } else {
            plugin.informPlayer(player, ChatColor.translateAlternateColorCodes('\u0026', plugin.localization.getString("spawnerPlaced"))
                    .replace("%creature%", su.getCreatureName(entityID)));
        }

        su.setSpawnerEntityID(blockPlaced, entityID);
        plugin.getLogger().log(Level.FINE, "Changing placed down item entity ID to {0}", entityID);
    }
}
