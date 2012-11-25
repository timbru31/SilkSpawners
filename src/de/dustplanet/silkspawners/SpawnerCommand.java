package de.dustplanet.silkspawners;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

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
				sender.sendMessage(ChatColor.RED + "To use SilkSpawners from the command line use /spawner [creature]|[creature]egg [name]");
				return true;
			}
			// Get strings
			String creatureString = args[0];
			String playerName = args[1];
			Player player = plugin.getServer().getPlayer(playerName);
			// Online check
			if (player == null) {
				sender.sendMessage(ChatColor.RED + "Sorry this player is offline!");
				return true;
			}
			
			// Check if it's an egg or not
			boolean isEgg = isEgg(creatureString);
			if (isEgg) creatureString = creatureString.replaceFirst("egg$", "");

			// See if this is an unknown creature
			if (isUnkown(creatureString)) {
				sender.sendMessage(ChatColor.RED + "Unrecognized creature " + ChatColor.YELLOW + creatureString);
				return true;
			}

			// entityID
			short entityID = su.name2Eid.get(creatureString);

			// Add items
			if (isEgg) {
				player.getInventory().addItem(su.newEggItem(entityID));
				sender.sendMessage(ChatColor.GREEN + "Added a " + ChatColor.YELLOW + su.getCreatureName(entityID).toLowerCase() + " spawn egg " + ChatColor.GREEN + "to " + ChatColor.YELLOW + player.getName() + ChatColor.GREEN + "'s inventory");
			}
			else {
				player.getInventory().addItem(su.newSpawnerItem(entityID));
				sender.sendMessage(ChatColor.GREEN + "Added a " + ChatColor.YELLOW + su.getCreatureName(entityID).toLowerCase() + " spawner " + ChatColor.GREEN + "to " + ChatColor.YELLOW + player.getName() + ChatColor.GREEN + "'s inventory");
			}
			return true;
		}

		// We know it's safe
		Player player = (Player)sender;

		// Get information about the spawner
		if (args.length == 0) {
			// Get spawner type
			if (!plugin.hasPermission(player, "silkspawners.viewtype")) {
				sender.sendMessage(ChatColor.RED + "You do not have permission to view the spawner type");
				return true;
			}
			// Get the block, returns null for non spawner blocks
			Block block = su.getSpawnerFacing(player, plugin.getConfig().getInt("spawnerCommandReachDistance", 6));
			if (block == null) {
				sender.sendMessage(ChatColor.RED + "You must be looking directly at a spawner to use this command");
				return true;
			}
			short entityID = su.getSpawnerEntityID(block);
			sender.sendMessage(ChatColor.GREEN + "This is a " + su.getCreatureName(entityID).toLowerCase() + " spawner");
		}
		// Set or get spawner
		else {
			// Get list of all creatures
			String creatureString = args[0];
			if (creatureString.equalsIgnoreCase("all")) {
				su.showAllCreatures(sender);
				return true;
			}

			// Check for egg
			boolean isEgg = isEgg(creatureString);
			if (isEgg) creatureString = creatureString.replaceFirst("egg$", "");
			
			// See if this creature is known
			if (isUnkown(creatureString)) {
				sender.sendMessage(ChatColor.RED + "Unrecognized creature " + ChatColor.YELLOW + creatureString);
				return true;
			}
			
			// entityID
			short entityID = su.name2Eid.get(creatureString);

			// Get the block
			Block block = su.getSpawnerFacing(player, plugin.getConfig().getInt("spawnerCommandReachDistance", 6));

			// See if the block is a MobSpawner, then change it
			if (block != null && !isEgg) {
				if (!plugin.hasPermission(player, "silkspawners.changetype")) {
					player.sendMessage(ChatColor.RED + "You do not have permission to change spawners with /spawner");
					return true;
				}
				su.setSpawnerType(block, entityID, player);
				sender.sendMessage(ChatColor.GREEN + "Successfully changed the spawner to a " + ChatColor.YELLOW + su.getCreatureName(entityID).toLowerCase() + " spawner");
			}
			// Get free spawner item in hand
			else {
				// Check the item 
				ItemStack itemInHand = player.getItemInHand();

				// If it's a spawner change it.
				if (itemInHand != null && itemInHand.getType() == Material.MOB_SPAWNER) {
					if (!plugin.hasPermission(player, "silkspawners.changetype")) {
						player.sendMessage(ChatColor.RED + "You do not have permission to change spawners with /spawner");
						return true;
					}
					su.setSpawnerType(itemInHand, entityID);
					sender.sendMessage(ChatColor.GREEN + "Successfully changed the spawner to a " + ChatColor.YELLOW + su.getCreatureName(entityID).toLowerCase() + " spawner");
					return true;
				}

				// If emtpy, add a mob spawner or egg
				if (!plugin.hasPermission(player, "silkspawners.freeitem") && !plugin.hasPermission(player, "silkspawners.freeitem.egg")) {
					// Only viewing
					if (plugin.hasPermission(player, "silkspawners.viewtype")) {
						sender.sendMessage(ChatColor.RED + "You must be looking directly at a spawner or have a spawner in your hand to use this command");
					}
					// Not even viewing allowed
					else sender.sendMessage(ChatColor.RED + "You do not have permission to use this command");
					return true;
				}

				if (player.getItemInHand() != null && player.getItemInHand().getType() != Material.AIR) {
					sender.sendMessage(ChatColor.RED + "To use this command, empty your hand (to get a free spawner item), point at an existing spawner or have a spawner in your hand (to change the spawner type)");
					return true;
				}

				// Add egg or spawner
				if (isEgg && plugin.hasPermission(player, "silkspawners.freeitem.egg")) {
					player.setItemInHand(su.newEggItem(entityID));
					sender.sendMessage(ChatColor.GREEN + "Successfully added a " + ChatColor.YELLOW + su.getCreatureName(entityID).toLowerCase() + " spawn egg" + ChatColor.GREEN + " to your inventory");
					return true;
				}
				if (plugin.hasPermission(player, "silkspawners.freeitem")) {
					player.setItemInHand(su.newSpawnerItem(entityID));
					sender.sendMessage(ChatColor.GREEN + "Successfully added a " + ChatColor.YELLOW + su.getCreatureName(entityID).toLowerCase() + " spawner" + ChatColor.GREEN + " to your inventory");
					return true;
				}
				else {
					if (isEgg) sender.sendMessage(ChatColor.RED + "You are not allowed to get a free egg!");
					else sender.sendMessage(ChatColor.RED + "You are not allowed to get a spawner!");
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
