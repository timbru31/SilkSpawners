package de.dustplanet.silkspawners.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import de.dustplanet.util.SilkUtil;

/**
 * Handle the tab completion list.
 *
 * @author (former) mushroomhostage
 * @author xGhOsTkiLLeRx
 */

public class SilkSpawnersTabCompleter implements TabCompleter {
    private String[] commands = { "add", "all", "change", "give", "help", "list", "reload", "rl", "set", "view" };
    private SilkUtil su;

    public SilkSpawnersTabCompleter(SilkUtil util) {
        su = util;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        ArrayList<String> results = new ArrayList<>();
        if (args.length == 1) {
            String command = args[0].toLowerCase(Locale.ENGLISH);
            return addCommands(command);
        } else if (args.length == 2 && (args[0].equalsIgnoreCase("change") || args[0].equalsIgnoreCase("set"))) {
            String mob = args[1].toLowerCase(Locale.ENGLISH);
            results.addAll(addMobs(mob));
        } else if (args.length == 2 && (args[0].equalsIgnoreCase("give") || args[0].equalsIgnoreCase("add"))) {
            String player = args[1].toLowerCase(Locale.ENGLISH);
            results.addAll(addPlayers(player));
        } else if (args.length == 3 && (args[0].equalsIgnoreCase("give") || args[0].equalsIgnoreCase("add"))) {
            String mob = args[2].toLowerCase(Locale.ENGLISH);
            results.addAll(addMobs(mob));
        }
        return results;
    }

    private ArrayList<String> addCommands(String cmd) {
        ArrayList<String> results = new ArrayList<>();
        for (String command : commands) {
            // Add the string if our name starts with the argument
            if (command.startsWith(cmd)) {
                results.add(command);
            }
        }
        return results;
    }

    private ArrayList<String> addMobs(String mob) {
        ArrayList<String> results = new ArrayList<>();
        for (String displayName : su.name2Eid.keySet()) {
            // Lowercase, too and strip spaces
            displayName = displayName.toLowerCase().replace(" ", "");
            // Add the string if our name starts with the argument
            if (displayName.startsWith(mob)) {
                results.add(displayName);
            }
        }
        return results;
    }

    private ArrayList<String> addPlayers(String playerString) {
        ArrayList<String> results = new ArrayList<>();
        for (Player player : su.nmsProvider.getOnlinePlayers()) {
            // Lowercase, too and strip spaces
            String displayName = player.getName().toLowerCase().replace(" ", "");
            // Add the string if our name starts with the argument
            if (displayName.startsWith(playerString)) {
                results.add(player.getName());
            }
        }
        return results;
    }
}
