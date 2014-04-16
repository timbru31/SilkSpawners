package de.dustplanet.silkspawners.commands;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import de.dustplanet.util.SilkUtil;

/**
 * Handle the tab completion list
 *
 * @author (former) mushroomhostage
 * @author xGhOsTkiLLeRx
 */

public class SilkSpawnersTabCompleter implements TabCompleter {
    private SilkUtil su;

    public SilkSpawnersTabCompleter(SilkUtil util) {
	su = util;
    }

    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
	// Long enough!
	if (args.length > 0) {
	    ArrayList<String> results = new ArrayList<String>();
	    // Use ONLY lowercase
	    String argument = args[0].toLowerCase();
	    // Add list and all
	    if (("list").startsWith(argument)) {
		results.add("list");
	    }
	    if (("all").startsWith(argument)) {
		results.add("all");
	    }
	    if (("reload").startsWith(argument)) {
		results.add("reload");
	    }
	    // Noting entered, suggest list!
	    if (argument.equalsIgnoreCase("")) {
		results.add("list");
		results.add("all");
		results.add("reload");
	    }
	    for (String displayName : su.eid2DisplayName.values()) {
		// Lowercase, too
		displayName = displayName.toLowerCase();
		// Add the string if our name starts with the argument
		if (displayName.startsWith(argument)) {
		    results.add(displayName);
		}
	    }
	    // Return the list
	    return results;
	}
	return null;
    }
}
