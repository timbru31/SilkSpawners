package de.dustplanet.silkspawners.commands;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import de.dustplanet.silkspawners.SilkSpawners;
import de.dustplanet.silkspawners.SilkUtil;

/**
 * Handles the command /egg
 * @author (former) mushroomhostage
 * @author xGhOsTkiLLeRx
 */

public class EggCommand implements CommandExecutor {
	private SilkUtil su;
	private SilkSpawners plugin;

	public EggCommand(SilkSpawners instance, SilkUtil util) {
		su = util;
		plugin = instance;
	}

	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		// No arguments
		if (args.length == 0) {
			su.showAllCreatures(sender);
		}
		else {
			// Get list of all creatures
			String creatureString = args[0];
			if (creatureString.equalsIgnoreCase("all") || creatureString.equalsIgnoreCase("list")) {
				su.showAllCreatures(sender);
				return true;
			}

			// Since egg is obsolete, just remove it then
			if (creatureString.endsWith("egg")) creatureString = creatureString.replaceFirst("egg$", "");

			// See if this creature is known
			if (isUnkown(creatureString)) {
				sender.sendMessage(ChatColor.translateAlternateColorCodes('\u0026', plugin.localization.getString("unknownCreature").replace("%creature%", creatureString)));
				return true;
			}

			// entityID
			short entityID = su.name2Eid.get(creatureString);
			creatureString = su.getCreatureName(entityID);

			if (sender instanceof Player) {
				// We know it's safe
				Player player = (Player)sender;
				ItemStack itemInHand = player.getItemInHand();
				// If it's a spawn egg change it.
				if (itemInHand != null && itemInHand.getType() == su.SPAWN_EGG) {
					if (!plugin.hasPermission(player, "silkspawners.changetypewithegg")) {
						player.sendMessage(ChatColor.translateAlternateColorCodes('\u0026', plugin.localization.getString("noPermissionChangingEgg")));
						return true;
					}
					su.setSpawnerType(itemInHand, entityID, plugin.localization.getString("spawnerName"));
					player.sendMessage(ChatColor.translateAlternateColorCodes('\u0026', plugin.localization.getString("changedEgg").replace("%creature%", creatureString)));
					return true;
				}

				// If empty add a egg
				// No permission
				if (!plugin.hasPermission(player, "silkspawners.freeitem.egg")) {
					player.sendMessage(ChatColor.translateAlternateColorCodes('\u0026', plugin.localization.getString("noPermissionFreeEgg")));
					return true;
				}
				// Make hand free
				if (player.getItemInHand() != null && player.getItemInHand().getType() != Material.AIR) {
					player.sendMessage(ChatColor.translateAlternateColorCodes('\u0026', plugin.localization.getString("usageEggCommand")));
					return true;
				}

				// Add egg
				player.setItemInHand(su.newEggItem(entityID));
				player.sendMessage(ChatColor.translateAlternateColorCodes('\u0026', plugin.localization.getString("addedEgg").replace("%creature%", creatureString)));
			}
			// Console MUST include a name
			else {
				// /egg creature name
				if (args.length != 2) {
					sender.sendMessage(ChatColor.translateAlternateColorCodes('\u0026', plugin.localization.getString("usageEggCommandCommandLine")));
					return true;
				}
				String playerName = args[1];
				Player player = plugin.getServer().getPlayer(playerName);
				// Online check
				if (player == null) {
					sender.sendMessage(ChatColor.translateAlternateColorCodes('\u0026', plugin.localization.getString("playerOffline")));
					return true;
				}
				// Add item
				player.getInventory().addItem(su.newEggItem(entityID));
				sender.sendMessage(ChatColor.translateAlternateColorCodes('\u0026', plugin.localization.getString("addedEggOtherPlayer").replace("%creature%", creatureString)).replaceAll("%player%", player.getName()));
			}
		}
		return true;
	}

	private boolean isUnkown(String creatureString) {
		if (!su.name2Eid.containsKey(creatureString)) return true;
		return false;
	}
}