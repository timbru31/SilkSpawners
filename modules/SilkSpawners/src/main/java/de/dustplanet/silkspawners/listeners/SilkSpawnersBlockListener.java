package de.dustplanet.silkspawners.listeners;

import java.util.HashMap;
import java.util.Random;

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
    private SilkSpawners plugin;
    private SilkUtil su;
    private Random rnd;

    public SilkSpawnersBlockListener(SilkSpawners instance, SilkUtil util) {
        plugin = instance;
        su = util;
        rnd = new Random();
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.isCancelled()) {
            return;
        }

        boolean isFakeEvent = !BlockBreakEvent.class.equals(event.getClass());
        if (isFakeEvent) {
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

        // Call the event and maybe change things!
        SilkSpawnersSpawnerBreakEvent breakEvent = new SilkSpawnersSpawnerBreakEvent(player, block, entityID);
        plugin.getServer().getPluginManager().callEvent(breakEvent);
        // See if we need to stop
        if (breakEvent.isCancelled()) {
            event.setCancelled(true);
            return;
        }
        // Get the new ID (might be changed)
        entityID = breakEvent.getEntityID();

        // Message the player about the broken spawner
        plugin.informPlayer(player, ChatColor.translateAlternateColorCodes('\u0026', plugin.localization.getString("spawnerBroken")).replace("%creature%", su.getCreatureName(entityID)));

        // If using silk touch, drop spawner itself
        ItemStack tool = su.nmsProvider.getItemInHand(player);
        // Check for SilkTocuh level
        boolean validToolAndSilkTouch = su.isValidItemAndHasSilkTouch(tool);

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
        // assume not mined
        boolean mined = false;
        // drop XP only when destroyed and not silk picked
        boolean dropXPOnlyOnDestroy = plugin.config.getBoolean("dropXPOnlyOnDestroy", false);

        if (plugin.config.getBoolean("preventXPFarming", true) && block.hasMetadata("mined")) {
            mined = block.getMetadata("mined").get(0).asBoolean();
        }

        // Drop maybe some XP
        if (player.hasPermission("silkspawners.silkdrop." + mobName) || player.hasPermission("silkspawners.destroydrop." + mobName)) {
            int addXP = plugin.config.getInt("destroyDropXP");
            // If we have more than 0 XP, drop them
            // either we drop XP for destroy and silktouch or only when
            // destroyed and we have no silktouch
            if (!mined && addXP != 0 && (!dropXPOnlyOnDestroy || !validToolAndSilkTouch && dropXPOnlyOnDestroy)) {
                event.setExpToDrop(addXP);
                // check if we should flag spawners
                if (plugin.config.getBoolean("preventXPFarming", true)) {
                    block.setMetadata("mined", new FixedMetadataValue(plugin, true));
                }
            }
        }

        // random drop chance
        String mobID = su.eid2MobID.get(entityID);
        int randomNumber = rnd.nextInt(100);
        int dropChance = 0;

        // silk touch
        if (validToolAndSilkTouch && player.hasPermission("silkspawners.silkdrop." + mobName)) {
            // Calculate drop chance
            if (plugin.mobs.contains("creatures." + mobID + ".silkDropChance")) {
                dropChance = plugin.mobs.getInt("creatures." + mobID + ".silkDropChance", 100);
            } else {
                dropChance = plugin.config.getInt("silkDropChance", 100);
            }
            // Drop spawner
            if (randomNumber < dropChance) {
                // Check if dropped ItemStack has been overridden
                ItemStack breakEventDrop = breakEvent.getDrop();
                ItemStack spawnerItemStack = null;
                if (breakEventDrop != null) {
                    spawnerItemStack = breakEventDrop;
                } else {
                    int amount = 1;
                    if (plugin.mobs.contains("creatures." + mobID + ".dropAmount")) {
                        amount = plugin.mobs.getInt("creatures." + mobID + ".dropAmount", 1);
                    } else {
                        amount = plugin.config.getInt("dropAmount", 1);
                    }
                    spawnerItemStack = su.newSpawnerItem(entityID, su.getCustomSpawnerName(su.eid2MobID.get(entityID)), amount, false);
                }
                if (spawnerItemStack == null) {
                    plugin.getLogger().warning("Skipping dropping of spawner, since item is null");
                    return;
                }
                if (plugin.getConfig().getBoolean("dropSpawnerToInventory", false)) {
                    HashMap<Integer, ItemStack> additionalItems = player.getInventory().addItem(spawnerItemStack);
                    if (!additionalItems.isEmpty()) {
                        for (ItemStack itemStack : additionalItems.values()) {
                            world.dropItemNaturally(block.getLocation(), itemStack);
                        }
                    }
                } else {
                    world.dropItemNaturally(block.getLocation(), spawnerItemStack);
                }
            }
            return;
        }

        // no silk touch
        if (player.hasPermission("silkspawners.destroydrop." + mobName)) {
            if (plugin.config.getBoolean("destroyDropEgg", false)) {
                // Calculate drop chance
                randomNumber = rnd.nextInt(100);
                if (plugin.mobs.contains("creatures." + mobID + ".eggDropChance")) {
                    dropChance = plugin.mobs.getInt("creatures." + mobID + ".eggDropChance", 100);
                } else {
                    dropChance = plugin.config.getInt("eggDropChance", 100);
                }
                if (randomNumber < dropChance) {
                    // Drop egg
                    world.dropItemNaturally(block.getLocation(), su.newEggItem(entityID, su.eid2MobID.get(entityID), 1));
                }
            }
            // Drop iron bars (or not)
            int dropBars = plugin.config.getInt("destroyDropBars", 0);
            if (dropBars != 0) {
                // Calculate drop chance
                randomNumber = rnd.nextInt(100);
                if (plugin.mobs.contains("creatures." + mobID + ".destroyDropChance")) {
                    dropChance = plugin.mobs.getInt("creatures." + mobID + ".destroyDropChance", 100);
                } else {
                    dropChance = plugin.config.getInt("destroyDropChance", 100);
                }
                if (randomNumber < dropChance) {
                    world.dropItem(block.getLocation(), new ItemStack(Material.IRON_FENCE, dropBars));
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (event.isCancelled()) {
            return;
        }

        boolean isFakeEvent = !BlockPlaceEvent.class.equals(event.getClass());
        if (isFakeEvent) {
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
        boolean defaultID = false;
        // 0 or unknown then fallback
        if (entityID == 0 || !su.knownEids.contains(entityID)) {
            // Default
            defaultID = true;
            entityID = su.getDefaultEntityID();
        }

        // Call the event and maybe change things!
        SilkSpawnersSpawnerPlaceEvent placeEvent = new SilkSpawnersSpawnerPlaceEvent(player, blockPlaced, entityID);
        plugin.getServer().getPluginManager().callEvent(placeEvent);
        // See if we need to stop
        if (placeEvent.isCancelled()) {
            return;
        }
        // Get the new ID (might be changed)
        entityID = placeEvent.getEntityID();

        // Names
        String creatureName = su.getCreatureName(entityID);
        String spawnerName = creatureName.toLowerCase().replace(" ", "");

        // Check for place permission
        if (!player.hasPermission("silkspawners.place." + spawnerName)) {
            event.setCancelled(true);
            su.sendMessage(player, ChatColor.translateAlternateColorCodes('\u0026', plugin.localization.getString("noPermissionPlace").replace("%ID%", Short.toString(entityID))).replace("%creature%", creatureName));
            return;
        }

        // Message default
        if (defaultID) {
            plugin.informPlayer(player, ChatColor.translateAlternateColorCodes('\u0026', plugin.localization.getString("placingDefault")));
        } else {
            // Else message the type
            plugin.informPlayer(player, ChatColor.translateAlternateColorCodes('\u0026', plugin.localization.getString("spawnerPlaced")).replace("%creature%", su.getCreatureName(entityID)));
        }

        su.setSpawnerEntityID(blockPlaced, entityID);
    }
}
