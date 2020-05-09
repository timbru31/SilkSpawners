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
    private String[] commands = { "add", "all", "change", "give", "help", "list", "reload", "rl", "set", "view", "info" };
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
        } else if (args.length == 2 && ("change".equalsIgnoreCase(args[0]) || "set".equalsIgnoreCase(args[0]))) {
            String mob = args[1].toLowerCase(Locale.ENGLISH);
            results.addAll(addMobs(mob));
        } else if (args.length == 2 && ("give".equalsIgnoreCase(args[0]) || "add".equalsIgnoreCase(args[0]))) {
            String player = args[1].toLowerCase(Locale.ENGLISH);
            results.addAll(addPlayers(player));
        } else if (args.length == 3 && ("give".equalsIgnoreCase(args[0]) || "add".equalsIgnoreCase(args[0]))) {
            String mob = args[2].toLowerCase(Locale.ENGLISH);
            results.addAll(addMobs(mob));
        } else if (args.length == 2 && ("selfget".equalsIgnoreCase(args[0]) || "i".equalsIgnoreCase(args[0]))) {
            String mob = args[1].toLowerCase(Locale.ENGLISH);
            results.addAll(addMobs(mob));
        }
        return results;
    }

    private ArrayList<String> addCommands(String cmd) {
        ArrayList<String> results = new ArrayList<>();
        for (String command : commands) {
            if (command.startsWith(cmd)) {
                results.add(command);
            }
        }
        return results;
    }

    private ArrayList<String> addMobs(String mob) {
        ArrayList<String> results = new ArrayList<>();
        for (String displayName : su.getDisplayNameToMobID().keySet()) {
            displayName = displayName.toLowerCase(Locale.ENGLISH).replace(" ", "");
            if (displayName.startsWith(mob)) {
                results.add(displayName);
            }
        }
        return results;
    }

    private ArrayList<String> addPlayers(String playerString) {
        ArrayList<String> results = new ArrayList<>();
        for (Player player : su.nmsProvider.getOnlinePlayers()) {
            String displayName = player.getName().toLowerCase(Locale.ENGLISH).replace(" ", "");
            if (displayName.startsWith(playerString)) {
                results.add(player.getName());
            }
        }
        return results;
    }
}
