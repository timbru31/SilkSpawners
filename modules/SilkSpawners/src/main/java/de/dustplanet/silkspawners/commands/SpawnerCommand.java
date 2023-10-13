package de.dustplanet.silkspawners.commands;

import java.util.HashMap;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
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
    private final SilkUtil su;
    private final SilkSpawners plugin;

    public SpawnerCommand(final SilkSpawners instance, final SilkUtil util) {
        su = util;
        plugin = instance;
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command cmd, final String commandLabel, final String[] args) {
        switch (args.length) {
            case 1:
                switch (args[0].toLowerCase(Locale.ENGLISH)) {
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
                    case "info":
                    case "view":
                        handleView(sender);
                        break;
                    default:
                        handleUnknownArgument(sender);
                        break;
                }
                break;
            case 2:
                switch (args[0].toLowerCase(Locale.ENGLISH)) {
                    case "change":
                    case "set":
                        handleChange(sender, args[1]);
                        break;
                    case "selfget":
                    case "i":
                        handleGive(sender, sender.getName(), args[1], null);
                        break;
                    default:
                        handleUnknownArgument(sender);
                        break;
                }
                break;
            case 3:
                switch (args[0].toLowerCase(Locale.ENGLISH)) {
                    case "give":
                    case "add":
                        handleGive(sender, args[1], args[2].toLowerCase(Locale.ENGLISH), null);
                        break;
                    case "selfget":
                    case "i":
                        handleGive(sender, sender.getName(), args[1], args[2]);
                        break;
                    default:
                        handleUnknownArgument(sender);
                        break;
                }
                break;
            case 4:
                switch (args[0].toLowerCase(Locale.ENGLISH)) {
                    case "give":
                    case "add":
                        handleGive(sender, args[1], args[2].toLowerCase(Locale.ENGLISH), args[3]);
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

    @SuppressWarnings("deprecation")
    private void handleGive(final CommandSender sender, final String receiver, final String mob, final String amountString) {
        int amount = plugin.config.getInt("defaultAmountGive", 1);
        boolean saveData = false;

        // Check given amount
        if (StringUtils.isNotBlank(amountString)) {
            amount = su.getNumber(amountString);
            if (amount == -1) {
                su.sendMessage(sender, ChatColor.translateAlternateColorCodes('\u0026', plugin.localization.getString("useNumbers")));
                return;
            }
        }

        // Check player
        Player player = su.nmsProvider.getPlayer(receiver);
        // Online check
        if (player == null) {
            player = this.su.nmsProvider.loadPlayer(Bukkit.getOfflinePlayer(receiver));
            if (player == null) {
                su.sendMessage(sender, ChatColor.translateAlternateColorCodes('\u0026', plugin.localization.getString("playerOffline")));
                return;
            }
            saveData = true;
        }

        // Check if it's an egg or not
        final boolean isEgg = su.isEgg(mob);
        String egg = mob;
        if (isEgg) {
            egg = egg.replaceFirst("egg$", "");
        }

        if (isEgg) {
            handleGiveEgg(sender, player, egg, amount, saveData);
        } else {
            handleGiveSpawner(sender, player, mob, amount, saveData);
        }
    }

    private void handleGiveEgg(final CommandSender sender, final Player receiver, final String mob, final int amount,
            final boolean saveData) {
        if (su.isUnknown(mob)) {
            su.sendMessage(sender, ChatColor.translateAlternateColorCodes('\u0026', plugin.localization.getString("unknownCreature"))
                    .replace("%creature%", mob));
            return;
        }
        final String entityID = su.getDisplayNameToMobID().get(mob);
        final String creature = su.getCreatureName(entityID);

        // Add egg
        if (su.hasPermission(sender, "silkspawners.freeitemegg.", entityID)) {
            final ItemStack eggItemStack = su.newEggItem(entityID, amount, su.getCreatureEggName(entityID));
            final HashMap<Integer, ItemStack> leftOvers = receiver.getInventory().addItem(eggItemStack);
            if (!leftOvers.values().isEmpty()) {
                if (plugin.getConfig().getBoolean("spillSpawnersFromCommands", false)) {
                    final World world = receiver.getWorld();
                    world.dropItemNaturally(receiver.getLocation(), eggItemStack);
                    su.sendMessage(sender,
                            ChatColor.translateAlternateColorCodes('\u0026', plugin.localization.getString("noFreeSlotDroppedGround")));
                    return;
                }
                su.sendMessage(sender, ChatColor.translateAlternateColorCodes('\u0026', plugin.localization.getString("noFreeSlot")));
                return;
            }
            if (saveData) {
                receiver.saveData();
            }
            if (sender instanceof Player) {
                final Player pSender = (Player) sender;
                if (pSender.getUniqueId() == receiver.getUniqueId()) {
                    su.sendMessage(sender, ChatColor.translateAlternateColorCodes('\u0026', plugin.localization.getString("addedEgg"))
                            .replace("%creature%", creature).replace("%amount%", Integer.toString(amount)));
                } else {
                    su.sendMessage(sender,
                            ChatColor
                                    .translateAlternateColorCodes('\u0026',
                                            plugin.localization.getString("addedEggOtherPlayer").replace("%player%", receiver.getName()))
                                    .replace("%creature%", creature).replace("%amount%", Integer.toString(amount)));
                }
            } else {
                su.sendMessage(sender,
                        ChatColor
                                .translateAlternateColorCodes('\u0026',
                                        plugin.localization.getString("addedEggOtherPlayer").replace("%player%", receiver.getName()))
                                .replace("%creature%", creature).replace("%amount%", Integer.toString(amount)));
            }
            return;
        }
        su.sendMessage(sender, ChatColor.translateAlternateColorCodes('\u0026', plugin.localization.getString("noPermissionFreeEgg")));

    }

    private void handleGiveSpawner(final CommandSender sender, final Player receiver, final String mob, final int amount,
            final boolean saveData) {
        if (su.isUnknown(mob)) {
            su.sendMessage(sender, ChatColor.translateAlternateColorCodes('\u0026', plugin.localization.getString("unknownCreature"))
                    .replace("%creature%", mob));
            return;
        }

        final String entityID = su.getDisplayNameToMobID().get(mob);
        final String creature = su.getCreatureName(entityID);

        // Add spawner
        if (su.hasPermission(sender, "silkspawners.freeitem.", entityID)) {
            final ItemStack spawnerItemStack = su.newSpawnerItem(entityID, su.getCustomSpawnerName(entityID), amount, false);
            final HashMap<Integer, ItemStack> leftOvers = receiver.getInventory().addItem(spawnerItemStack);
            if (!leftOvers.values().isEmpty()) {
                if (plugin.getConfig().getBoolean("spillSpawnersFromCommands", false)) {
                    final World world = receiver.getWorld();
                    world.dropItemNaturally(receiver.getLocation(), spawnerItemStack);
                    su.sendMessage(sender,
                            ChatColor.translateAlternateColorCodes('\u0026', plugin.localization.getString("noFreeSlotDroppedGround")));
                    return;
                }
                su.sendMessage(sender, ChatColor.translateAlternateColorCodes('\u0026', plugin.localization.getString("noFreeSlot")));
                return;
            }
            if (saveData) {
                receiver.saveData();
            }
            if (sender instanceof Player) {
                final Player pSender = (Player) sender;
                if (pSender.getUniqueId() == receiver.getUniqueId()) {
                    su.sendMessage(sender, ChatColor.translateAlternateColorCodes('\u0026', plugin.localization.getString("addedSpawner"))
                            .replace("%creature%", creature).replace("%amount%", Integer.toString(amount)));
                } else {
                    su.sendMessage(sender,
                            ChatColor
                                    .translateAlternateColorCodes('\u0026',
                                            plugin.localization.getString("addedSpawnerOtherPlayer").replace("%player%",
                                                    receiver.getName()))
                                    .replace("%creature%", creature).replace("%amount%", Integer.toString(amount)));
                }
            } else {
                su.sendMessage(sender,
                        ChatColor
                                .translateAlternateColorCodes('\u0026',
                                        plugin.localization.getString("addedSpawnerOtherPlayer").replace("%player%", receiver.getName()))
                                .replace("%creature%", creature).replace("%amount%", Integer.toString(amount)));
            }
            return;
        }
        su.sendMessage(sender, ChatColor.translateAlternateColorCodes('\u0026', plugin.localization.getString("noPermissionFreeSpawner")));

    }

    private void handleChange(final CommandSender sender, final String newMob) {
        if (sender instanceof Player) {
            if (su.isUnknown(newMob)) {
                su.sendMessage(sender, ChatColor.translateAlternateColorCodes('\u0026', plugin.localization.getString("unknownCreature"))
                        .replace("%creature%", newMob));
                return;
            }

            final String entityID = su.getDisplayNameToMobID().get(newMob);

            final Player player = (Player) sender;

            final int distance = plugin.config.getInt("spawnerCommandReachDistance", 6);
            // If the distance is -1, return
            if (distance != -1) {
                // Get the block
                final Block block = su.nmsProvider.getSpawnerFacing(player, distance);
                if (block != null) {
                    handleBlockChange(player, block, entityID);
                    return;
                }
            }

            final ItemStack itemInHand = su.nmsProvider.getSpawnerItemInHand(player);
            Material itemMaterial;
            try {
                itemMaterial = itemInHand.getType();
            } catch (@SuppressWarnings("unused") final NullPointerException e) {
                itemMaterial = null;
            }

            if (itemMaterial != null && itemMaterial == su.nmsProvider.getSpawnerMaterial()) {
                handleChangeSpawner(player, entityID, itemInHand);
            } else if (itemMaterial != null && su.nmsProvider.getSpawnEggMaterials().contains(itemMaterial)) {
                handleChangeEgg(player, entityID, itemInHand);
            } else {
                su.sendMessage(player,
                        ChatColor.translateAlternateColorCodes('\u0026', plugin.localization.getString("spawnerNotDeterminable")));
            }
        } else {
            su.sendMessage(sender, ChatColor.translateAlternateColorCodes('\u0026', plugin.localization.getString("noConsole")));
        }

    }

    private void handleBlockChange(final Player player, final Block block, final String entityID) {
        if (!su.hasPermission(player, "silkspawners.changetype.", entityID)) {
            su.sendMessage(player,
                    ChatColor.translateAlternateColorCodes('\u0026', plugin.localization.getString("noPermissionChangingSpawner")));
            return;
        }
        // Call the event and maybe change things!
        final SilkSpawnersSpawnerChangeEvent changeEvent = new SilkSpawnersSpawnerChangeEvent(player, block, entityID,
                su.getSpawnerEntityID(block), 1);
        plugin.getServer().getPluginManager().callEvent(changeEvent);
        // See if we need to stop
        if (changeEvent.isCancelled()) {
            return;
        }
        // Get the new ID (might be changed)
        final String newEntityID = changeEvent.getEntityID();
        final String newMob = su.getCreatureName(entityID);
        if (su.setSpawnerType(block, newEntityID, player,
                ChatColor.translateAlternateColorCodes('\u0026', plugin.localization.getString("changingDeniedWorldGuard")))) {
            su.sendMessage(player, ChatColor.translateAlternateColorCodes('\u0026', plugin.localization.getString("changedSpawner"))
                    .replace("%creature%", newMob));
        }

    }

    private void handleChangeSpawner(final Player player, final String entityID, final ItemStack itemInHand) {
        if (!su.hasPermission(player, "silkspawners.changetype.", entityID)) {
            su.sendMessage(player,
                    ChatColor.translateAlternateColorCodes('\u0026', plugin.localization.getString("noPermissionChangingSpawner")));
            return;
        }

        // Call the event and maybe change things!
        final SilkSpawnersSpawnerChangeEvent changeEvent = new SilkSpawnersSpawnerChangeEvent(player, null, entityID,
                su.getStoredSpawnerItemEntityID(itemInHand), itemInHand.getAmount());
        plugin.getServer().getPluginManager().callEvent(changeEvent);
        // See if we need to stop
        if (changeEvent.isCancelled()) {
            return;
        }

        // Get the new ID (might be changed)
        final String newEntityID = changeEvent.getEntityID();
        final String newMob = su.getCreatureName(entityID);
        final ItemStack newItem = su.setSpawnerType(itemInHand, newEntityID, plugin.localization.getString("spawnerName"));
        su.nmsProvider.setSpawnerItemInHand(player, newItem);
        su.sendMessage(player, ChatColor.translateAlternateColorCodes('\u0026', plugin.localization.getString("changedSpawner"))
                .replace("%creature%", newMob));

    }

    private void handleChangeEgg(final Player player, final String entityID, final ItemStack itemInHand) {
        if (!su.hasPermission(player, "silkspawners.changetype.", entityID)) {
            su.sendMessage(player,
                    ChatColor.translateAlternateColorCodes('\u0026', plugin.localization.getString("noPermissionChangingEgg")));
            return;
        }

        // Call the event and maybe change things!
        final SilkSpawnersSpawnerChangeEvent changeEvent = new SilkSpawnersSpawnerChangeEvent(player, null, entityID,
                su.getStoredSpawnerItemEntityID(itemInHand), itemInHand.getAmount());
        plugin.getServer().getPluginManager().callEvent(changeEvent);
        // See if we need to stop
        if (changeEvent.isCancelled()) {
            return;
        }

        // Get the new ID (might be changed)
        final String newEntityID = changeEvent.getEntityID();
        final String newMob = su.getCreatureName(entityID);
        final ItemStack newItem = su.setSpawnerType(itemInHand, newEntityID, plugin.localization.getString("spawnerName"));
        su.nmsProvider.setSpawnerItemInHand(player, newItem);
        su.sendMessage(player, ChatColor.translateAlternateColorCodes('\u0026', plugin.localization.getString("changedEgg"))
                .replace("%creature%", newMob));

    }

    private void handleUnknownArgument(final CommandSender sender) {
        su.sendMessage(sender, ChatColor.translateAlternateColorCodes('\u0026', plugin.localization.getString("unknownArgument")));
    }

    private void handleHelp(final CommandSender sender) {
        if (sender.hasPermission("silkspawners.help")) {
            final String message = ChatColor.translateAlternateColorCodes('\u0026',
                    plugin.localization.getString("help").replace("%version%", plugin.getDescription().getVersion()));
            su.sendMessage(sender, message);
        } else {
            su.sendMessage(sender, ChatColor.translateAlternateColorCodes('\u0026', plugin.localization.getString("noPermission")));
        }
    }

    private void handleReload(final CommandSender sender) {
        if (sender.hasPermission("silkspawners.reload")) {
            plugin.reloadConfigs();
            su.sendMessage(sender, ChatColor.translateAlternateColorCodes('\u0026', plugin.localization.getString("configsReloaded")));
        } else {
            su.sendMessage(sender, ChatColor.translateAlternateColorCodes('\u0026', plugin.localization.getString("noPermission")));
        }
    }

    private void handleList(final CommandSender sender) {
        su.showAllCreatures(sender);
    }

    private void handleView(final CommandSender sender) {
        if (sender instanceof Player) {
            // If the distance is -1, return
            final int distance = plugin.config.getInt("spawnerCommandReachDistance", 6);
            if (distance == -1) {
                return;
            }
            // Get the block, returns null for non spawner blocks
            final Player player = (Player) sender;
            final Block block = su.nmsProvider.getSpawnerFacing(player, distance);
            if (block == null) {
                su.sendMessage(player, ChatColor.translateAlternateColorCodes('\u0026', plugin.localization.getString("lookAtSpawner")));
                return;
            }
            final String entityID = su.getSpawnerEntityID(block);
            if (player.hasPermission("silkspawners.viewtype")) {
                su.sendMessage(player, ChatColor.translateAlternateColorCodes('\u0026', plugin.localization.getString("getSpawnerType"))
                        .replace("%creature%", su.getCreatureName(entityID)));
            } else {
                su.sendMessage(player,
                        ChatColor.translateAlternateColorCodes('\u0026', plugin.localization.getString("noPermissionViewType")));
            }
        } else {
            su.sendMessage(sender, ChatColor.translateAlternateColorCodes('\u0026', plugin.localization.getString("noConsole")));
        }
    }
}
