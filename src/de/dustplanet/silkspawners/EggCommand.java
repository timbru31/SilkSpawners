package de.dustplanet.silkspawners;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class EggCommand implements CommandExecutor {

	private SilkUtil su;
	private SilkSpawners plugin;

	public EggCommand(SilkSpawners instance, SilkUtil util) {
		su = util;
		plugin = instance;
	}

	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {

		if (args.length == 0) {
			su.showAllCreatures(sender);
		}
		else {
			// Get list of all creatures
			String creatureString = args[0];
			if (creatureString.equalsIgnoreCase("all")) {
				su.showAllCreatures(sender);
				return true;
			}

			if (creatureString.endsWith("egg")) creatureString = creatureString.replaceFirst("egg$", "");
			
			// See if this creature is known
			if (isUnkown(creatureString)) {
				sender.sendMessage(ChatColor.RED + "Unrecognized creature " + ChatColor.YELLOW + creatureString);
				return true;
			}
			
			// entityID
			short entityID = su.name2Eid.get(creatureString);

			if (sender instanceof Player) {
				// We know it's safe
				Player player = (Player)sender;

				ItemStack itemInHand = player.getItemInHand();

				// If it's a spawner change it.
				if (itemInHand != null && itemInHand.getType() == su.SPAWN_EGG) {
					if (!plugin.hasPermission(player, "silkspawners.changetypewithegg")) {
						player.sendMessage(ChatColor.RED + "You do not have permission to change spawning eggs with /egg");
						return true;
					}
					su.setSpawnerType(itemInHand, entityID);
					sender.sendMessage(ChatColor.GREEN + "Successfully changed the spawning egg to a " + ChatColor.YELLOW + su.getCreatureName(entityID).toLowerCase() + " spawn egg");
					return true;
				}

				// If emtpy, add a mob spawner or egg
				if (!plugin.hasPermission(player, "silkspawners.freeitem.egg")) {
					sender.sendMessage(ChatColor.RED + "You do not have permission to spawn free eggs");
					return true;
				}

				if (player.getItemInHand() != null && player.getItemInHand().getType() != Material.AIR) {
					sender.sendMessage(ChatColor.RED + "To use this command, empty your hand (to get a free spawn egg) or have a spawn egg in your hand (to change the type)");
					return true;
				}

				// Add egg
				player.setItemInHand(su.newEggItem(entityID));
				sender.sendMessage(ChatColor.GREEN + "Successfully added a " + ChatColor.YELLOW + su.getCreatureName(entityID).toLowerCase() + " spawn egg" + ChatColor.GREEN + " to your inventory");
			}
			// Console MUST include a name
			else {
				if (args.length != 2) {
					sender.sendMessage(ChatColor.RED + "To use SilkSpawners from the command line use /egg [creature] [name]");
					return true;
				}
				String playerName = args[1];
				Player player = plugin.getServer().getPlayer(playerName);
				// Online check
				if (player == null) {
					sender.sendMessage(ChatColor.RED + "Sorry this player is offline!");
					return true;
				}
				// Add item
				player.getInventory().addItem(su.newEggItem(entityID));
				sender.sendMessage(ChatColor.GREEN + "Added a " + ChatColor.YELLOW + su.getCreatureName(entityID).toLowerCase() + " spawn egg " + ChatColor.GREEN + "to " + ChatColor.YELLOW + player.getName() + ChatColor.GREEN + "'s inventory");
			}
		}
		return true;
	}

	private boolean isUnkown(String creatureString) {
		if (!su.name2Eid.containsKey(creatureString)) return true;
		return false;
	}
}