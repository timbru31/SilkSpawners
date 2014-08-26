package de.dustplanet.silkspawners.commands;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import de.dustplanet.silkspawners.SilkSpawners;
import de.dustplanet.silkspawners.events.SilkSpawnersSpawnerChangeEvent;
import de.dustplanet.util.SilkUtil;

/**
 * Handles the command /spawner
 * @author (former) mushroomhostage
 * @author xGhOsTkiLLeRx
 */

public class SpawnerCommand implements CommandExecutor {
    private SilkUtil su;
    private SilkSpawners plugin;

    public SpawnerCommand(SilkSpawners instance, SilkUtil util) {
        su = util;
        plugin = instance;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        // Case console
        if (!(sender instanceof Player)) {
            // Not enough arguments -> list
            if (args.length == 0) {
                su.showAllCreatures(sender);
                return true;
            }
            if (args[0].equalsIgnoreCase("reload") || args[0].equalsIgnoreCase("rl")) {
                plugin.reloadConfigs();
                sender.sendMessage(ChatColor.translateAlternateColorCodes('\u0026', plugin.localization.getString("configsReloaded")));
                return true;
            } else if  (args.length < 3) {
                su.showAllCreatures(sender);
                return true;
            }
            // We need exactly 3 arguments (creature, amount and player)
            if (args.length != 3) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('\u0026', plugin.localization.getString("usageSpawnerCommandCommandLine")));
                return true;
            }
            // Get strings
            String creatureString = args[0].toLowerCase();
            int amount = 1;
            if (args.length > 1) {
                try {
                    amount = Integer.parseInt(args[1]);
                } catch (NumberFormatException e) {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('\u0026', plugin.localization.getString("useNumbers")));
                    return true;
                }
            }
            String playerName = args[2];
            Player player = su.getPlayer(playerName);
            // Online check
            if (player == null) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('\u0026', plugin.localization.getString("playerOffline")));
                return true;
            }

            // Check if it's an egg or not
            boolean isEgg = su.isEgg(creatureString);
            if (isEgg) {
                creatureString = creatureString.replaceFirst("egg$", "");
            }

            // See if this is an unknown creature
            if (su.isUnkown(creatureString)) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('\u0026', plugin.localization.getString("unknownCreature")).replace("%creature%", creatureString));
                return true;
            }

            // entityID
            short entityID = su.name2Eid.get(creatureString);
            creatureString = su.getCreatureName(entityID);

            // Add egg
            if (isEgg) {
                player.getInventory().addItem(su.newEggItem(entityID, amount));
                sender.sendMessage(ChatColor.translateAlternateColorCodes('\u0026', plugin.localization.getString("addedEggOtherPlayer").replace("%player%", player.getName())).replace("%creature%", creatureString));
            } else {
                // Add spawner
                player.getInventory().addItem(su.newSpawnerItem(entityID, su.getCustomSpawnerName(su.eid2MobID.get(entityID)), amount));
                sender.sendMessage(ChatColor.translateAlternateColorCodes('\u0026', plugin.localization.getString("addedSpawnerOtherPlayer").replace("%player%", player.getName())).replace("%creature%", creatureString));
            }
            return true;
        }

        // We know it's safe
        Player player = (Player) sender;

        // Get information about the spawner
        if (args.length == 0) {
            // Get spawner type
            if (!plugin.hasPermission(player, "silkspawners.viewtype")) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('\u0026', plugin.localization.getString("noPermissionViewType")));
                return true;
            }
            // Get the block, returns null for non spawner blocks
            Block block = su.getSpawnerFacing(player, plugin.config.getInt("spawnerCommandReachDistance", 6));
            if (block == null) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('\u0026', plugin.localization.getString("lookAtSpawner")));
                return true;
            }
            short entityID = su.getSpawnerEntityID(block);
            player.sendMessage(ChatColor.translateAlternateColorCodes('\u0026', plugin.localization.getString("getSpawnerType")).replace("%creature%", su.getCreatureName(entityID)));
        } else {
            // Set or get spawner
            // Get list of all creatures
            String creatureString = args[0].toLowerCase();
            if (creatureString.equalsIgnoreCase("all") || creatureString.equalsIgnoreCase("list")) {
                su.showAllCreatures(sender);
                return true;
            } else if (creatureString.equalsIgnoreCase("reload") || creatureString.equalsIgnoreCase("rl")) {
                if (plugin.hasPermission(player, "silkspawners.reload")) {
                    plugin.reloadConfigs();
                    player.sendMessage(ChatColor.translateAlternateColorCodes('\u0026', plugin.localization.getString("configsReloaded")));
                } else {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('\u0026', plugin.localization.getString("noPermission")));
                }
                return true;
            }

            // Check for egg
            boolean isEgg = su.isEgg(creatureString);
            if (isEgg) {
                creatureString = creatureString.replaceFirst("egg$", "");
            }

            // See if this creature is known
            if (su.isUnkown(creatureString)) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('\u0026', plugin.localization.getString("unknownCreature")).replace("%creature%", creatureString));
                return true;
            }

            // entityID
            short entityID = su.name2Eid.get(creatureString);
            creatureString = su.getCreatureName(entityID);
            // Filter spaces (like Zombie Pigman)
            String mobName = creatureString.toLowerCase().replace(" ", "");

            // Get the block
            Block block = su.getSpawnerFacing(player, plugin.config.getInt("spawnerCommandReachDistance", 6));

            // See if the block is a MobSpawner, then change it
            if (block != null && !isEgg) {
                if (!plugin.hasPermission(player, "silkspawners.changetype." + mobName) && !plugin.hasPermission(player, "silkspawners.changetype.*")) {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('\u0026', plugin.localization.getString("noPermissionChangingSpawner")));
                    return true;
                }
                // Call the event and maybe change things!
                SilkSpawnersSpawnerChangeEvent changeEvent = new SilkSpawnersSpawnerChangeEvent(player, block, entityID, su.getSpawnerEntityID(block));
                plugin.getServer().getPluginManager().callEvent(changeEvent);
                // See if we need to stop
                if (changeEvent.isCancelled()) {
                    return true;
                }
                // Get the new ID (might be changed)
                entityID = changeEvent.getEntityID();
                creatureString = su.getCreatureName(entityID);
                su.setSpawnerType(block, entityID, player, ChatColor.translateAlternateColorCodes('\u0026', plugin.localization.getString("changingDeniedWorldGuard")));
                player.sendMessage(ChatColor.translateAlternateColorCodes('\u0026', plugin.localization.getString("changedSpawner")).replace("%creature%", creatureString));
            } else {
                // Get free spawner item in hand
                // Check the item
                ItemStack itemInHand = player.getItemInHand();
                if (itemInHand != null) {
                    // If it's a spawner change it.
                    if (itemInHand.getType() == Material.MOB_SPAWNER) {
                        if (!plugin.hasPermission(player, "silkspawners.changetype." + mobName) && !plugin.hasPermission(player, "silkspawners.changetype.*")) {
                            player.sendMessage(ChatColor.translateAlternateColorCodes('\u0026', plugin.localization.getString("noPermissionChangingSpawner")));
                            return true;
                        }
                        // Call the event and maybe change things!
                        SilkSpawnersSpawnerChangeEvent changeEvent = new SilkSpawnersSpawnerChangeEvent(player, null, entityID, su.getStoredSpawnerItemEntityID(itemInHand));
                        plugin.getServer().getPluginManager().callEvent(changeEvent);
                        // See if we need to stop
                        if (changeEvent.isCancelled()) {
                            return true;
                        }
                        // Get the new ID (might be changed)
                        entityID = changeEvent.getEntityID();
                        creatureString = su.getCreatureName(entityID);
                        player.setItemInHand(su.setSpawnerType(itemInHand, entityID, plugin.localization.getString("spawnerName")));
                        player.sendMessage(ChatColor.translateAlternateColorCodes('\u0026', plugin.localization.getString("changedSpawner")).replace("%creature%", creatureString));
                        return true;
                    }
                    // If it's a spawn egg change it.
                    if (itemInHand.getType() == su.spawn_egg) {
                        if (!plugin.hasPermission(player, "silkspawners.changetypewithegg." + mobName) && !plugin.hasPermission(player, "silkspawners.changetypewithegg.*")) {
                            player.sendMessage(ChatColor.translateAlternateColorCodes('\u0026', plugin.localization.getString("noPermissionChangingEgg")));
                            return true;
                        }
                        // Call the event and maybe change things!
                        SilkSpawnersSpawnerChangeEvent changeEvent = new SilkSpawnersSpawnerChangeEvent(player, null, entityID, su.getStoredSpawnerItemEntityID(itemInHand));
                        plugin.getServer().getPluginManager().callEvent(changeEvent);
                        // See if we need to stop
                        if (changeEvent.isCancelled()) {
                            return true;
                        }
                        // Get the new ID (might be changed)
                        entityID = changeEvent.getEntityID();
                        creatureString = su.getCreatureName(entityID);
                        su.setSpawnerType(itemInHand, entityID, plugin.localization.getString("spawnerName"));
                        player.sendMessage(ChatColor.translateAlternateColorCodes('\u0026', plugin.localization.getString("changedEgg")).replace("%creature%", creatureString));
                        return true;
                    }
                }

                // If empty, add a mob spawner or egg
                if (!plugin.hasPermission(player, "silkspawners.freeitem." + mobName) && !plugin.hasPermission(player, "silkspawners.freeitem.*")
                        && !plugin.hasPermission(player, "silkspawners.freeitemegg." + mobName) && !plugin.hasPermission(player, "silkspawners.freeitemegg.*")) {
                    // Only viewing
                    if (plugin.hasPermission(player, "silkspawners.viewtype")) {
                        player.sendMessage(ChatColor.translateAlternateColorCodes('\u0026', plugin.localization.getString("lookAtSpawnerOrInHand")));
                    } else {
                        // Not even viewing allowed
                        player.sendMessage(ChatColor.translateAlternateColorCodes('\u0026', plugin.localization.getString("noPermission")));
                    }
                    return true;
                }

                if (player.getItemInHand() != null && player.getItemInHand().getType() != Material.AIR) {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('\u0026', plugin.localization.getString("usageEggCommand")));
                    return true;
                }

                int amount = 1;
                if (args.length > 1) {
                    try {
                        amount = Integer.parseInt(args[1]);
                    } catch (NumberFormatException e) {
                        player.sendMessage(ChatColor.translateAlternateColorCodes('\u0026', plugin.localization.getString("useNumbers")));
                        return true;
                    }
                }

                // Add egg or spawner
                if (isEgg && (plugin.hasPermission(player, "silkspawners.freeitemegg." + mobName) || plugin.hasPermission(player, "silkspawners.freeitemegg.*"))) {
                    player.setItemInHand(su.newEggItem(entityID, amount));
                    player.sendMessage(ChatColor.translateAlternateColorCodes('\u0026', plugin.localization.getString("addedEgg")).replace("%creature%", creatureString));
                    return true;
                }
                if (plugin.hasPermission(player, "silkspawners.freeitem." + mobName) || plugin.hasPermission(player, "silkspawners.freeitem.*")) {
                    player.setItemInHand(su.newSpawnerItem(entityID, su.getCustomSpawnerName(su.eid2MobID.get(entityID)), amount));
                    player.sendMessage(ChatColor.translateAlternateColorCodes('\u0026', plugin.localization.getString("addedSpawner")).replace("%creature%", creatureString));
                    return true;
                }
                if (isEgg) {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('\u0026', plugin.localization.getString("noPermissionFreeEgg")));
                } else {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('\u0026', plugin.localization.getString("noPermissionFreeSpawner")));
                }
                return true;
            }
        }
        return true;
    }
}
