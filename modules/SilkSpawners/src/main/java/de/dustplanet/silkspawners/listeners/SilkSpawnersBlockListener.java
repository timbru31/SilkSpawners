package de.dustplanet.silkspawners.listeners;

import java.util.HashMap;
import java.util.Locale;
import java.util.Random;

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
        final boolean isFakeEvent = !BlockBreakEvent.class.equals(event.getClass());
        if (isFakeEvent) {
            return;
        }

        final Block block = event.getBlock();
        final Player player = event.getPlayer();

        if (block.getType() != su.nmsProvider.getSpawnerMaterial()) {
            return;
        }

        if (!su.canBuildHere(player, block.getLocation())) {
            return;
        }

        String entityID = su.getSpawnerEntityID(block);

        final SilkSpawnersSpawnerBreakEvent breakEvent = new SilkSpawnersSpawnerBreakEvent(player, block, entityID);
        plugin.getServer().getPluginManager().callEvent(breakEvent);
        if (breakEvent.isCancelled()) {
            event.setCancelled(true);
            return;
        }

        entityID = su.getDisplayNameToMobID().get(breakEvent.getEntityID());

        plugin.informPlayer(player, ChatColor.translateAlternateColorCodes('\u0026', plugin.localization.getString("spawnerBroken"))
                .replace("%creature%", su.getCreatureName(entityID)));

        final ItemStack tool = su.nmsProvider.getItemInHand(player);
        final boolean validToolAndSilkTouch = su.isValidItemAndHasSilkTouch(tool);

        final World world = player.getWorld();

        final String mobName = su.getCreatureName(entityID).toLowerCase(Locale.ENGLISH).replace(" ", "");

        if (plugin.config.getBoolean("noDropsCreative", true) && player.getGameMode() == GameMode.CREATIVE) {
            return;
        }

        event.setExpToDrop(0);
        boolean mined = false;
        final boolean dropXPOnlyOnDestroy = plugin.config.getBoolean("dropXPOnlyOnDestroy", false);

        if (plugin.config.getBoolean("preventXPFarming", true) && block.hasMetadata("mined")) {
            mined = block.getMetadata("mined").get(0).asBoolean();
        }

        if (player.hasPermission("silkspawners.silkdrop." + mobName) || player.hasPermission("silkspawners.destroydrop." + mobName)) {
            final int addXP = plugin.config.getInt("destroyDropXP");
            // If we have more than 0 XP, drop them
            // either we drop XP for destroy and silktouch or only when
            // destroyed and we have no silktouch
            if (!mined && addXP != 0 && (!dropXPOnlyOnDestroy || !validToolAndSilkTouch)) {
                event.setExpToDrop(addXP);
                // check if we should flag spawners
                if (plugin.config.getBoolean("preventXPFarming", true)) {
                    block.setMetadata("mined", new FixedMetadataValue(plugin, true));
                }
            }
        }

        int randomNumber = rnd.nextInt(100);
        int dropChance = 0;

        if ((validToolAndSilkTouch && player.hasPermission("silkspawners.silkdrop." + mobName))
                || player.hasPermission("silkspawners.nosilk." + mobName)) {
            if (plugin.mobs.contains("creatures." + entityID + ".silkDropChance")) {
                dropChance = plugin.mobs.getInt("creatures." + entityID + ".silkDropChance", 100);
            } else {
                dropChance = plugin.config.getInt("silkDropChance", 100);
            }

            if (randomNumber < dropChance) {
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
                if (plugin.getConfig().getBoolean("dropSpawnerToInventory", false)) {
                    final HashMap<Integer, ItemStack> additionalItems = player.getInventory().addItem(spawnerItemStack);
                    if (!additionalItems.isEmpty()) {
                        for (final ItemStack itemStack : additionalItems.values()) {
                            world.dropItemNaturally(block.getLocation(), itemStack);
                        }
                    }
                } else {
                    world.dropItemNaturally(block.getLocation(), spawnerItemStack);
                }
            }
            return;
        }

        if (player.hasPermission("silkspawners.destroydrop." + mobName)) {
            if (plugin.config.getBoolean("destroyDropEgg", false)) {
                randomNumber = rnd.nextInt(100);
                if (plugin.mobs.contains("creatures." + entityID + ".eggDropChance")) {
                    dropChance = plugin.mobs.getInt("creatures." + entityID + ".eggDropChance", 100);
                } else {
                    dropChance = plugin.config.getInt("eggDropChance", 100);
                }
                if (randomNumber < dropChance) {
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
                if (randomNumber < dropChance) {
                    world.dropItem(block.getLocation(), new ItemStack(su.nmsProvider.getIronFenceMaterial(), dropBars));
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockPlace(final BlockPlaceEvent event) {
        if (event.isCancelled()) {
            return;
        }

        final boolean isFakeEvent = !BlockPlaceEvent.class.equals(event.getClass());
        if (isFakeEvent) {
            return;
        }

        final Block blockPlaced = event.getBlockPlaced();
        if (blockPlaced.getType() != su.nmsProvider.getSpawnerMaterial()) {
            return;
        }
        final Player player = event.getPlayer();
        if (!su.canBuildHere(player, blockPlaced.getLocation())) {
            return;
        }
        final ItemStack item = event.getItemInHand();
        String entityID = su.getStoredSpawnerItemEntityID(item);
        boolean defaultID = false;
        if (entityID == null) {
            defaultID = true;
            entityID = su.getDefaultEntityID();
        }

        final SilkSpawnersSpawnerPlaceEvent placeEvent = new SilkSpawnersSpawnerPlaceEvent(player, blockPlaced, entityID);
        plugin.getServer().getPluginManager().callEvent(placeEvent);

        if (placeEvent.isCancelled()) {
            event.setCancelled(true);
            return;
        }

        entityID = placeEvent.getEntityID();

        final String creatureName = su.getCreatureName(entityID);
        final String spawnerName = creatureName.toLowerCase(Locale.ENGLISH).replace(" ", "");

        if (!player.hasPermission("silkspawners.place." + spawnerName)) {
            event.setCancelled(true);
            su.sendMessage(player,
                    ChatColor
                            .translateAlternateColorCodes('\u0026',
                                    plugin.localization.getString("noPermissionPlace").replace("%ID%", entityID))
                            .replace("%creature%", creatureName));
            return;
        }

        if (defaultID) {
            plugin.informPlayer(player, ChatColor.translateAlternateColorCodes('\u0026', plugin.localization.getString("placingDefault")));
        } else {
            plugin.informPlayer(player, ChatColor.translateAlternateColorCodes('\u0026', plugin.localization.getString("spawnerPlaced"))
                    .replace("%creature%", su.getCreatureName(entityID)));
        }

        su.setSpawnerEntityID(blockPlaced, entityID);
    }
}
