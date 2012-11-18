package de.dustplanet.silkspawners;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SilkSpawnersCommands implements CommandExecutor {
	
	private SilkUtil su;
	private SilkSpawners plugin;
	
	public SilkSpawnersCommands(SilkSpawners instance, SilkUtil util) {
		su = util;
		plugin = instance;
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		// No console, since we need a block or inventory of a player
		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.RED + "Sorry, but you can use SilkSpawners only ingame!");
			return true;
		}

		Player player = (Player)sender;

		if (args.length == 0) {
			// Get spawner type
			if (!plugin.hasPermission(player, "silkspawners.viewtype")) {
				sender.sendMessage("You do not have permission to view the spawner type");
				return true;
			}
			// Get the block, returns null for non spawner blocks
			Block block = su.getSpawnerFacing(player, plugin.getConfig().getInt("spawnerCommandReachDistance", 6));
			if (block == null) {
				sender.sendMessage("You must be looking directly at a spawner to use this command");
				return true;
			}

			try {
				short entityID = su.getSpawnerEntityID(block);

				sender.sendMessage(su.getCreatureName(entityID) + " spawner");
			} catch (Exception e) {
				plugin.informPlayer(player, "Failed to identify spawner: " + e);
			}

		} else {
			// Set or get spawner

			Block block = su.getSpawnerFacing(player, plugin.getConfig().getInt("spawnerCommandReachDistance", 6));

			String creatureString = args[0];
			if (creatureString.equalsIgnoreCase("all")) {
				// Get list of all creatures..anyone can do this
				su.showAllCreatures(player);
				return true;
			}

			boolean isEgg = false;

			if (creatureString.endsWith("egg")) {
				isEgg = true;
				creatureString = creatureString.replaceFirst("egg$", "");
			}

			if (!su.name2Eid.containsKey(creatureString)) {
				player.sendMessage("Unrecognized creature "+creatureString);
				return true;
			}

			short entityID = su.name2Eid.get(creatureString);

			if (block != null && !isEgg) {
				if (!plugin.hasPermission(player, "silkspawners.changetype")) {
					player.sendMessage("You do not have permission to change spawners with /spawner");
					return true;
				}

				su.setSpawnerType(block, entityID, player);
			} else {
				// Get free spawner item in hand
				if (!plugin.hasPermission(player, "silkspawners.freeitem")) {
					if (plugin.hasPermission(player, "silkspawners.viewtype")) {
						sender.sendMessage("You must be looking directly at a spawner to use this command");
					} else {
						sender.sendMessage("You do not have permission to use this command");
					}
					return true;
				}

				if (player.getItemInHand() != null && player.getItemInHand().getType() != Material.AIR) {
					sender.sendMessage("To use this command, empty your hand (to get a free spawner item) or point at an existing spawner (to change the spawner type)");
					return true;
				}

				if (isEgg) {
					player.setItemInHand(su.newEggItem(entityID));
					sender.sendMessage(su.getCreatureName(entityID) + " spawn egg");
				} else {
					player.setItemInHand(su.newSpawnerItem(entityID));
					sender.sendMessage(su.getCreatureName(entityID) + " spawner");
				}
			}
		}

		return true;
	}

}
