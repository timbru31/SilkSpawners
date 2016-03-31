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
 * Handles the commands.
 *
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
        switch (args.length) {
        case 1:
            switch (args[0].toLowerCase()) {
            case "help":
                handleHelp(sender);
                break;
            case "all":
            case "list":
                handleList(sender);
                break;
            case "reload":
            case "rl":
                handleReload(sender);
                break;
            case "view":
                handleView(sender);
                break;
            default:
                handleUnknownArgument(sender);
                break;
            }
            break;
        case 2:
            switch (args[0].toLowerCase()) {
            case "change":
            case "set":
                handleChange(sender, args[1]);
                break;
            default:
                handleUnknownArgument(sender);
                break;
            }
            break;
        case 3:
            switch (args[0].toLowerCase()) {
            case "give":
            case "add":
                handleGive(sender, args[1], args[2].toLowerCase(), null);
                break;
            default:
                handleUnknownArgument(sender);
                break;
            }
            break;
        case 4:
            switch (args[0].toLowerCase()) {
            case "give":
            case "add":
                handleGive(sender, args[1], args[2].toLowerCase(), args[3]);
                break;
            default:
                handleUnknownArgument(sender);
                break;
            }
            break;
        default:
            handleUnknownArgument(sender);
            break;
        }
        return true;
    }

    private void handleGive(CommandSender sender, String receiver, String mob, String amountString) {
        int amount = plugin.config.getInt("defaultAmountGive", 1);

        // Check given amount
        if (amountString != null && !amountString.isEmpty()) {
            amount = su.getNumber(amountString);
            if (amount == -1) {
                su.sendMessage(sender,
                        ChatColor.translateAlternateColorCodes('\u0026', plugin.localization.getString("useNumbers")));
                return;
            }
        }

        // Check player
        Player player = su.nmsProvider.getPlayer(receiver);
        // Online check
        if (player == null) {
            su.sendMessage(sender,
                    ChatColor.translateAlternateColorCodes('\u0026', plugin.localization.getString("playerOffline")));
            return;
        }

        // Check if it's an egg or not
        boolean isEgg = su.isEgg(mob);
        String egg = mob;
        if (isEgg) {
            egg = egg.replaceFirst("egg$", "");
        }

        if (isEgg) {
            handleGiveEgg(sender, player, egg, amount);
        } else {
            handleGiveSpawner(sender, player, mob, amount);
        }
    }

    private void handleGiveEgg(CommandSender sender, Player receiver, String mob, int amount) {
        short entityID = su.getNumber(mob);
        if (su.isUnkown(mob) && !plugin.config.getBoolean("ignoreCheckNumbers", false)
                || su.isUnkown(mob) && plugin.config.getBoolean("ignoreCheckNumbers", false) && entityID == -1) {
            su.sendMessage(sender,
                    ChatColor.translateAlternateColorCodes('\u0026', plugin.localization.getString("unknownCreature"))
                    .replace("%creature%", mob));
            return;
        }

        if (entityID == -1) {
            entityID = su.name2Eid.get(mob);
        }

        String creature = su.getCreatureName(entityID);
        // Filter spaces (like Zombie Pigman)
        String mobName = creature.toLowerCase().replace(" ", "");

        // Add egg
        if (sender.hasPermission("silkspawners.freeitemegg." + mobName)) {
            // Have space in inventory
            if (receiver.getInventory().firstEmpty() == -1) {
                su.sendMessage(sender,
                        ChatColor.translateAlternateColorCodes('\u0026', plugin.localization.getString("noFreeSlot")));
                return;
            }
            receiver.getInventory().addItem(su.newEggItem(entityID, su.eid2MobID.get(entityID), amount));
            if (sender instanceof Player) {
                Player pSender = (Player) sender;
                if (pSender.getUniqueId() == receiver.getUniqueId()) {
                    su.sendMessage(sender,
                            ChatColor.translateAlternateColorCodes('\u0026', plugin.localization.getString("addedEgg"))
                            .replace("%creature%", creature).replace("%amount%", Integer.toString(amount)));
                } else {
                    su.sendMessage(sender,
                            ChatColor
                            .translateAlternateColorCodes('\u0026', plugin.localization
                                    .getString("addedEggOtherPlayer").replace("%player%", receiver.getName()))
                            .replace("%creature%", creature).replace("%amount%", Integer.toString(amount)));
                }
            } else {
                su.sendMessage(sender,
                        ChatColor
                        .translateAlternateColorCodes('\u0026', plugin.localization
                                .getString("addedEggOtherPlayer").replace("%player%", receiver.getName()))
                        .replace("%creature%", creature).replace("%amount%", Integer.toString(amount)));
            }
            return;
        }
        su.sendMessage(sender,
                ChatColor.translateAlternateColorCodes('\u0026', plugin.localization.getString("noPermissionFreeEgg")));
    }

    private void handleGiveSpawner(CommandSender sender, Player receiver, String mob, int amount) {
        if (su.isUnkown(mob)) {
            su.sendMessage(sender,
                    ChatColor.translateAlternateColorCodes('\u0026', plugin.localization.getString("unknownCreature"))
                    .replace("%creature%", mob));
            return;
        }

        short entityID = su.name2Eid.get(mob);
        String creature = su.getCreatureName(entityID);
        // Filter spaces (like Zombie Pigman)
        String mobName = creature.toLowerCase().replace(" ", "");

        // Add spawner
        if (sender.hasPermission("silkspawners.freeitem." + mobName)) {
            // Have space in inventory
            if (receiver.getInventory().firstEmpty() == -1) {
                su.sendMessage(sender,
                        ChatColor.translateAlternateColorCodes('\u0026', plugin.localization.getString("noFreeSlot")));
                return;
            }
            receiver.getInventory().addItem(
                    su.newSpawnerItem(entityID, su.getCustomSpawnerName(su.eid2MobID.get(entityID)), amount, false));
            if (sender instanceof Player) {
                Player pSender = (Player) sender;
                if (pSender.getUniqueId() == receiver.getUniqueId()) {
                    su.sendMessage(sender, ChatColor
                            .translateAlternateColorCodes('\u0026', plugin.localization.getString("addedSpawner"))
                            .replace("%creature%", creature).replace("%amount%", Integer.toString(amount)));
                } else {
                    su.sendMessage(sender, ChatColor
                            .translateAlternateColorCodes('\u0026', plugin.localization
                                    .getString("addedSpawnerOtherPlayer").replace("%player%", receiver.getName()))
                            .replace("%creature%", creature).replace("%amount%", Integer.toString(amount)));
                }
            } else {
                su.sendMessage(sender,
                        ChatColor
                        .translateAlternateColorCodes('\u0026', plugin.localization
                                .getString("addedSpawnerOtherPlayer").replace("%player%", receiver.getName()))
                        .replace("%creature%", creature).replace("%amount%", Integer.toString(amount)));
            }
            return;
        }
        su.sendMessage(sender, ChatColor.translateAlternateColorCodes('\u0026',
                plugin.localization.getString("noPermissionFreeSpawner")));
    }

    private void handleChange(CommandSender sender, String newMob) {
        if (sender instanceof Player) {
            if (su.isUnkown(newMob)) {
                su.sendMessage(sender,
                        ChatColor.translateAlternateColorCodes('\u0026', plugin.localization.getString("unknownCreature"))
                        .replace("%creature%", newMob));
                return;
            }

            // entityID
            short entityID = su.name2Eid.get(newMob);
            String creature = su.getCreatureName(entityID);
            // Filter spaces (like Zombie Pigman)
            String mobName = creature.toLowerCase().replace(" ", "");

            Player player = (Player) sender;

            int distance = plugin.config.getInt("spawnerCommandReachDistance", 6);
            // If the distance is -1, return
            if (distance != -1) {
                // Get the block
                Block block = su.nmsProvider.getSpawnerFacing(player, distance);
                if (block != null) {
                    handleBlockChange(player, block, entityID, mobName);
                    return;
                }
            }

            Material itemInHand = player.getItemInHand().getType();
            if (itemInHand == Material.MOB_SPAWNER) {
                handleChangeSpawner(player, entityID, mobName);
            } else if (itemInHand == SilkUtil.SPAWN_EGG) {
                handleChangeEgg(player, entityID, mobName);
            } else {
                su.sendMessage(player,
                        ChatColor.translateAlternateColorCodes('\u0026', plugin.localization.getString("lookAtSpawner")));
            }
        } else {
            su.sendMessage(sender,
                    ChatColor.translateAlternateColorCodes('\u0026', plugin.localization.getString("noConsole")));
        }
    }

    private void handleBlockChange(Player player, Block block, short entityID, String mobName) {
        if (!player.hasPermission("silkspawners.changetype." + mobName)) {
            su.sendMessage(player, ChatColor.translateAlternateColorCodes('\u0026',
                    plugin.localization.getString("noPermissionChangingSpawner")));
            return;
        }
        // Call the event and maybe change things!
        SilkSpawnersSpawnerChangeEvent changeEvent = new SilkSpawnersSpawnerChangeEvent(player, block, entityID,
                su.getSpawnerEntityID(block), 1);
        plugin.getServer().getPluginManager().callEvent(changeEvent);
        // See if we need to stop
        if (changeEvent.isCancelled()) {
            return;
        }
        // Get the new ID (might be changed)
        short newEntityID = changeEvent.getEntityID();
        String newMob = su.getCreatureName(entityID);
        if (su.setSpawnerType(block, newEntityID, player, ChatColor.translateAlternateColorCodes('\u0026',
                plugin.localization.getString("changingDeniedWorldGuard")))) {
            su.sendMessage(player,
                    ChatColor.translateAlternateColorCodes('\u0026', plugin.localization.getString("changedSpawner"))
                    .replace("%creature%", newMob));
        }
    }

    private void handleChangeSpawner(Player player, short entityID, String mobName) {
        ItemStack itemInHand = player.getItemInHand();

        if (!player.hasPermission("silkspawners.changetype." + mobName)) {
            su.sendMessage(player, ChatColor.translateAlternateColorCodes('\u0026',
                    plugin.localization.getString("noPermissionChangingSpawner")));
            return;
        }

        // Call the event and maybe change things!
        SilkSpawnersSpawnerChangeEvent changeEvent = new SilkSpawnersSpawnerChangeEvent(player, null, entityID,
                su.getStoredSpawnerItemEntityID(itemInHand), itemInHand.getAmount());
        plugin.getServer().getPluginManager().callEvent(changeEvent);
        // See if we need to stop
        if (changeEvent.isCancelled()) {
            return;
        }

        // Get the new ID (might be changed)
        short newEntityID = changeEvent.getEntityID();
        String newMob = su.getCreatureName(entityID);
        player.setItemInHand(su.setSpawnerType(itemInHand, newEntityID, plugin.localization.getString("spawnerName")));
        su.sendMessage(player,
                ChatColor.translateAlternateColorCodes('\u0026', plugin.localization.getString("changedSpawner"))
                .replace("%creature%", newMob));
    }

    private void handleChangeEgg(Player player, short entityID, String mobName) {
        // If it's a spawn egg change it.
        ItemStack itemInHand = player.getItemInHand();

        if (!player.hasPermission("silkspawners.changetypewithegg." + mobName)) {
            su.sendMessage(player, ChatColor.translateAlternateColorCodes('\u0026',
                    plugin.localization.getString("noPermissionChangingEgg")));
            return;
        }

        // Call the event and maybe change things!
        SilkSpawnersSpawnerChangeEvent changeEvent = new SilkSpawnersSpawnerChangeEvent(player, null, entityID,
                su.getStoredSpawnerItemEntityID(itemInHand), itemInHand.getAmount());
        plugin.getServer().getPluginManager().callEvent(changeEvent);
        // See if we need to stop
        if (changeEvent.isCancelled()) {
            return;
        }

        // Get the new ID (might be changed)
        short newEntityID = changeEvent.getEntityID();
        String newMob = su.getCreatureName(entityID);
        su.setSpawnerType(itemInHand, newEntityID, plugin.localization.getString("spawnerName"));
        su.sendMessage(player, ChatColor.translateAlternateColorCodes('\u0026', plugin.localization.getString("changedEgg"))
                .replace("%creature%", newMob));
    }

    private void handleUnknownArgument(CommandSender sender) {
        su.sendMessage(sender,
                ChatColor.translateAlternateColorCodes('\u0026', plugin.localization.getString("unknownArgument")));
    }

    private void handleHelp(CommandSender sender) {
        for (int i = 1; i <= 7; i++) {
            su.sendMessage(sender,
                    ChatColor.translateAlternateColorCodes('\u0026', plugin.localization.getString("help" + i)));
        }
    }

    private void handleReload(CommandSender sender) {
        if (sender.hasPermission("silkspawners.reload")) {
            plugin.reloadConfigs();
            su.sendMessage(sender,
                    ChatColor.translateAlternateColorCodes('\u0026', plugin.localization.getString("configsReloaded")));
        } else {
            su.sendMessage(sender,
                    ChatColor.translateAlternateColorCodes('\u0026', plugin.localization.getString("noPermission")));
        }
    }

    private void handleList(CommandSender sender) {
        su.showAllCreatures(sender);
    }

    private void handleView(CommandSender sender) {
        if (sender instanceof Player) {
            // If the distance is -1, return
            int distance = plugin.config.getInt("spawnerCommandReachDistance", 6);
            if (distance == -1) {
                return;
            }
            // Get the block, returns null for non spawner blocks
            Player player = (Player) sender;
            Block block = su.nmsProvider.getSpawnerFacing(player, distance);
            if (block == null) {
                su.sendMessage(player, ChatColor.translateAlternateColorCodes('\u0026',
                        plugin.localization.getString("lookAtSpawner")));
                return;
            }
            short entityID = su.getSpawnerEntityID(block);
            if (player.hasPermission("silkspawners.viewtype")) {
                su.sendMessage(player, ChatColor
                        .translateAlternateColorCodes('\u0026', plugin.localization.getString("getSpawnerType"))
                        .replace("%creature%", su.getCreatureName(entityID)));
            } else {
                su.sendMessage(player, ChatColor.translateAlternateColorCodes('\u0026',
                        plugin.localization.getString("noPermissionViewType")));
            }
        } else {
            su.sendMessage(sender,
                    ChatColor.translateAlternateColorCodes('\u0026', plugin.localization.getString("noConsole")));
        }
    }
}
