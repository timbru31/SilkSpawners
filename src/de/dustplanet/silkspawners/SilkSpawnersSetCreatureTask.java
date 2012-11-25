<<<<<<< HEAD
package de.dustplanet.silkspawners;

import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class SilkSpawnersSetCreatureTask  implements Runnable {
	private SilkUtil su;
	private short entityID;
	private Block block;
	private SilkSpawners plugin;
	private Player player;

	public SilkSpawnersSetCreatureTask(short entityID, Block block, SilkSpawners plugin, Player player, SilkUtil su) {
		this.entityID = entityID;
		this.block = block;
		this.plugin = plugin;
		this.player = player;
		this.su = su;
	}

	public void run() {
		try {
			su.setSpawnerEntityID(block, entityID);
		} catch (Exception e) {
			plugin.informPlayer(player, ChatColor.RED + "Please report this! Failed to set type: " + ChatColor.YELLOW + e);
			e.printStackTrace();
		}
	}

}
=======
package de.dustplanet.silkspawners;

import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class SilkSpawnersSetCreatureTask  implements Runnable {
	private SilkUtil su;
	private short entityID;
	private Block block;
	private SilkSpawners plugin;
	private Player player;

	public SilkSpawnersSetCreatureTask(short entityID, Block block, SilkSpawners plugin, Player player, SilkUtil su) {
		this.entityID = entityID;
		this.block = block;
		this.plugin = plugin;
		this.player = player;
		this.su = su;
	}

	public void run() {
		try {
			su.setSpawnerEntityID(block, entityID);
		} catch (Exception e) {
			plugin.informPlayer(player, ChatColor.RED + "Please report this! Failed to set type: " + ChatColor.YELLOW + e);
			e.printStackTrace();
		}
	}

}
>>>>>>> 2d075b373d988112ba7579def46a8ca199ecf026
