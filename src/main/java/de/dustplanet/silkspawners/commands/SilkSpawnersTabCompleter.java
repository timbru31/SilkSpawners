package de.dustplanet.silkspawners.commands;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import de.dustplanet.util.SilkUtil;

public class SilkSpawnersTabCompleter implements TabCompleter {
	private SilkUtil su;
	
	public SilkSpawnersTabCompleter(SilkUtil util) {
		su = util;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		// Long enough!
		if (args.length > 0) {
			ArrayList<String> results = new ArrayList<String>();
			// Use ONLY lowercase
			String argument = args[0].toLowerCase();
			for (String displayName: su.eid2DisplayName.values()) {
				// Lowercase, too
				displayName = displayName.toLowerCase();
				if (displayName.startsWith(argument)) results.add(displayName);
			}
			return results;
		}
		return null;
	}
}