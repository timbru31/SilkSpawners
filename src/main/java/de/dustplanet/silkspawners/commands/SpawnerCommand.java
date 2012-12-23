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
import de.dustplanet.silkspawners.SilkUtil;

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

	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		if (!(sender instanceof Player)) {
			if (args.length == 0 || args.length == 1) {
				su.showAllCreatures(sender);
				return true;
			}
			if (args.length != 2) {
				sender.sendMessage(ChatColor.translateAlternateColorCodes('\u0026', plugin.localization.getString("usageSpawnerCommandCommandLine")));
				return true;
			}
			// Get strings
			String creatureString = args[0];
			String playerName = args[1];
			Player player = plugin.getServer().getPlayer(playerName);
			// Online check
			if (player == null) {
				sender.sendMessage(ChatColor.translateAlternateColorCodes('\u0026', plugin.localization.getString("playerOffline")));
				return true;
			}

			// Check if it's an egg or not
			boolean isEgg = isEgg(creatureString);
			if (isEgg) creatureString = creatureString.replaceFirst("egg$", "");

			// See if this is an unknown creature
			if (isUnkown(creatureString)) {
				sender.sendMessage(ChatColor.translateAlternateColorCodes('\u0026', plugin.localization.getString("unknownCreature").replace("%creature%", creatureString)));
				return true;
			}

			// entityID
			short entityID = su.name2Eid.get(creatureString);
			creatureString = su.getCreatureName(entityID);

			// Add items
			if (isEgg) {
				player.getInventory().addItem(su.newEggItem(entityID));
				sender.sendMessage(ChatColor.translateAlternateColorCodes('\u0026', plugin.localization.getString("addedEggOtherPlayer").replace("%creature%", creatureString)).replaceAll("%player%", player.getName()));
			}
			else {
				player.getInventory().addItem(su.newSpawnerItem(entityID, plugin.localization.getString("spawnerName")));
				sender.sendMessage(ChatColor.translateAlternateColorCodes('\u0026', plugin.localization.getString("addedSpawnerOtherPlayer").replace("%creature%", creatureString)).replaceAll("%player%", player.getName()));
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
			player.sendMessage(ChatColor.translateAlternateColorCodes('\u0026', plugin.localization.getString("getSpawnerType").replaceAll("%creature%", su.getCreatureName(entityID))));
		}
		// Set or get spawner
		else {
			// Get list of all creatures
			String creatureString = args[0];
			if (creatureString.equalsIgnoreCase("all") || creatureString.equalsIgnoreCase("list")) {
				su.showAllCreatures(sender);
				return true;
			}

			// Check for egg
			boolean isEgg = isEgg(creatureString);
			if (isEgg) creatureString = creatureString.replaceFirst("egg$", "");

			// See if this creature is known
			if (isUnkown(creatureString)) {
				player.sendMessage(ChatColor.translateAlternateColorCodes('\u0026', plugin.localization.getString("unknownCreature").replace("%creature%", creatureString)));
				return true;
			}
			
			// entityID
			short entityID = su.name2Eid.get(creatureString);
			creatureString = su.getCreatureName(entityID);
			String mobName = creatureString.toLowerCase();

			// Get the block
			Block block = su.getSpawnerFacing(player, plugin.config.getInt("spawnerCommandReachDistance", 6));

			// See if the block is a MobSpawner, then change it
			if (block != null && !isEgg) {
				if (!plugin.hasPermission(player, "silkspawners.changetype") && (!plugin.hasPermission(player, "silkspawners.changetype." + mobName) || !plugin.hasPermission(player, "silkspawners.changetype.*"))) {
					player.sendMessage(ChatColor.translateAlternateColorCodes('\u0026', plugin.localization.getString("noPermissionChangingSpawner")));
					return true;
				}
				su.setSpawnerType(block, entityID, player, ChatColor.translateAlternateColorCodes('\u0026', plugin.localization.getString("changingDeniedWorldGuard")));
				player.sendMessage(ChatColor.translateAlternateColorCodes('\u0026', plugin.localization.getString("changedSpawner").replaceAll("%creature%", creatureString)));
			}
			// Get free spawner item in hand
			else {
				// Check the item 
				ItemStack itemInHand = player.getItemInHand();

				// If it's a spawner change it.
				if (itemInHand != null && itemInHand.getType() == Material.MOB_SPAWNER) {
					if (!plugin.hasPermission(player, "silkspawners.changetype." + mobName) && !plugin.hasPermission(player, "silkspawners.changetype.*")) {
						player.sendMessage(ChatColor.translateAlternateColorCodes('\u0026', plugin.localization.getString("noPermissionChangingSpawner")));
						return true;
					}
					player.setItemInHand(su.setSpawnerType(itemInHand, entityID, plugin.localization.getString("spawnerName")));
					player.sendMessage(ChatColor.translateAlternateColorCodes('\u0026', plugin.localization.getString("changedSpawner").replaceAll("%creature%", creatureString)));
					return true;
				}
				// If it's a spawn egg change it.
				if (itemInHand != null && itemInHand.getType() == su.SPAWN_EGG) {
					if (!plugin.hasPermission(player, "silkspawners.changetypewithegg." + mobName) && !plugin.hasPermission(player, "silkspawners.changetypewithegg.*")) {
						player.sendMessage(ChatColor.translateAlternateColorCodes('\u0026', plugin.localization.getString("noPermissionChangingEgg")));
						return true;
					}
					su.setSpawnerType(itemInHand, entityID, plugin.localization.getString("spawnerName"));
					player.sendMessage(ChatColor.translateAlternateColorCodes('\u0026', plugin.localization.getString("changedEgg").replace("%creature%", creatureString)));
					return true;
				}

				// If emtpy, add a mob spawner or egg
				if ((!plugin.hasPermission(player, "silkspawners.freeitem." + mobName) && !plugin.hasPermission(player, "silkspawners.freeitem.*"))
						&& (!plugin.hasPermission(player, "silkspawners.freeitem.egg." + mobName) && !plugin.hasPermission(player, "silkspawners.freeitem.egg.*"))) {
					// Only viewing
					if (plugin.hasPermission(player, "silkspawners.viewtype")) {
						player.sendMessage(ChatColor.translateAlternateColorCodes('\u0026', plugin.localization.getString("lookAtSpawnerOrInHand")));
					}
					// Not even viewing allowed
					else player.sendMessage(ChatColor.translateAlternateColorCodes('\u0026', plugin.localization.getString("noPermission")));
					return true;
				}

				if (player.getItemInHand() != null && player.getItemInHand().getType() != Material.AIR) {
					player.sendMessage(ChatColor.translateAlternateColorCodes('\u0026', plugin.localization.getString("usageEggCommand")));
					return true;
				}

				// Add egg or spawner
				if (isEgg && (plugin.hasPermission(player, "silkspawners.freeitem.egg." + mobName) || plugin.hasPermission(player, "silkspawners.freeitem.egg.*"))) {
					player.setItemInHand(su.newEggItem(entityID));
					player.sendMessage(ChatColor.translateAlternateColorCodes('\u0026', plugin.localization.getString("addedEgg").replace("%creature%", creatureString)));
					return true;
				}
				if (plugin.hasPermission(player, "silkspawners.freeitem." + mobName) || plugin.hasPermission(player, "silkspawners.freeitem.*")) {
					player.setItemInHand(su.newSpawnerItem(entityID, plugin.localization.getString("spawnerName")));
					player.sendMessage(ChatColor.translateAlternateColorCodes('\u0026', plugin.localization.getString("addedSpawner").replace("%creature%", creatureString)));
					return true;
				}
				else {
					if (isEgg) player.sendMessage(ChatColor.translateAlternateColorCodes('\u0026', plugin.localization.getString("noPermissionFreeEgg")));
					else player.sendMessage(ChatColor.translateAlternateColorCodes('\u0026', plugin.localization.getString("noPermissionFreeSpawner")));
					return true;
				}
			}
		}
		return true;
	}

	private boolean isEgg(String creatureString) {
		if (creatureString.endsWith("egg")) return true;
		return false;
	}

	private boolean isUnkown(String creatureString) {
		if (!su.name2Eid.containsKey(creatureString)) return true;
		return false;
	}
}