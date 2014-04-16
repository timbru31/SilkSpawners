package de.dustplanet.silkspawners.commands;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import de.dustplanet.silkspawners.SilkSpawners;
import de.dustplanet.silkspawners.events.SilkSpawnersSpawnerChangeEvent;
import de.dustplanet.util.SilkUtil;

/**
 * Handles the command /egg
 *
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
	} else {
	    // Get list of all creatures
	    String creatureString = args[0].toLowerCase();
	    if (creatureString.equalsIgnoreCase("all") || creatureString.equalsIgnoreCase("list")) {
		su.showAllCreatures(sender);
		return true;
	    } else if (creatureString.equalsIgnoreCase("reload") || creatureString.equalsIgnoreCase("rl")) {
		if (!(sender instanceof Player) || plugin.hasPermission((Player) sender, "silkspawners.reload")) {
		    plugin.reloadConfigs();
		    sender.sendMessage(ChatColor.translateAlternateColorCodes('\u0026', plugin.localization.getString("configsReloaded")));
		} else {
		    sender.sendMessage(ChatColor.translateAlternateColorCodes('\u0026', plugin.localization.getString("noPermission")));
		}
		return true;
	    }

	    // Since egg is obsolete, just remove it then
	    if (creatureString.endsWith("egg")) {
		creatureString = creatureString.replaceFirst("egg$", "");
	    }

	    // See if this creature is known
	    if ((su.isUnkown(creatureString) && !plugin.config.getBoolean("ignoreCheckNumbers", false))
		    || (su.isUnkown(creatureString) && plugin.config.getBoolean("ignoreCheckNumbers", false) && su.getNumber(creatureString) == -1)) {
		sender.sendMessage(ChatColor.translateAlternateColorCodes('\u0026', plugin.localization.getString("unknownCreature")).replace("%creature%", creatureString));
		return true;
	    }

	    // entityID
	    short entityID = 0;
	    if (su.getNumber(creatureString) == -1) {
		entityID = su.name2Eid.get(creatureString);
	    } else {
		entityID = su.getNumber(creatureString);
	    }

	    creatureString = su.getCreatureName(entityID);
	    // Filter spaces (like Zombie Pigman)
	    String mobName = creatureString.toLowerCase().replace(" ", "");

	    if (sender instanceof Player) {
		// We know it's safe
		Player player = (Player) sender;
		ItemStack itemInHand = player.getItemInHand();
		// If it's a spawn egg change it.
		if (itemInHand != null && itemInHand.getType() == su.SPAWN_EGG) {
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

		// If empty add a egg
		// No permission
		if (!plugin.hasPermission(player, "silkspawners.freeitemegg." + mobName) && !plugin.hasPermission(player, "silkspawners.freeitemegg.*")) {
		    player.sendMessage(ChatColor.translateAlternateColorCodes('\u0026', plugin.localization.getString("noPermissionFreeEgg")));
		    return true;
		}
		// Make hand free
		if (player.getItemInHand() != null && player.getItemInHand().getType() != Material.AIR) {
		    player.sendMessage(ChatColor.translateAlternateColorCodes('\u0026', plugin.localization.getString("usageEggCommand")));
		    return true;
		}

		// Amount
		int amount = 1;
		if (args.length > 1) {
		    try {
			amount = Integer.valueOf(args[1]);
		    } catch (NumberFormatException e) {
			player.sendMessage(ChatColor.translateAlternateColorCodes('\u0026', plugin.localization.getString("useNumbers")));
			return true;
		    }
		}

		// Add egg
		player.setItemInHand(su.newEggItem(entityID, amount));
		player.sendMessage(ChatColor.translateAlternateColorCodes('\u0026', plugin.localization.getString("addedEgg")).replace("%creature%", creatureString));
	    }
	    // Console MUST include a name
	    else {
		// /egg creature name
		if (args.length != 3) {
		    sender.sendMessage(ChatColor.translateAlternateColorCodes('\u0026', plugin.localization.getString("usageEggCommandCommandLine")));
		    return true;
		}

		// Amount
		int amount = 1;
		if (args.length > 1) {
		    try {
			amount = Integer.valueOf(args[1]);
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
		// Add item
		player.getInventory().addItem(su.newEggItem(entityID, amount));
		sender.sendMessage(ChatColor.translateAlternateColorCodes('\u0026', plugin.localization.getString("addedEggOtherPlayer").replace("%player%", player.getName())).replace("%creature%", creatureString));
	    }
	}
	return true;
    }
}
