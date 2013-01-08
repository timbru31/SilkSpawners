package de.dustplanet.silkspawners;

import org.bukkit.block.Block;
import de.dustplanet.util.SilkUtil;

/**
 * Workaround! :(
 * @author (former) mushroomhostage
 * @author xGhOsTkiLLeRx
 */

public class SilkSpawnersSetCreatureTask  implements Runnable {
	private SilkUtil su;
	private short entityID;
	private Block block;
	private SilkSpawners plugin;

	public SilkSpawnersSetCreatureTask(short entityID, Block block, SilkSpawners plugin, SilkUtil su) {
		this.entityID = entityID;
		this.block = block;
		this.plugin = plugin;
		this.su = su;
	}

	public void run() {
		try {
			su.setSpawnerEntityID(block, entityID);
			//block.setMetadata("xp", new FixedMetadataValue(plugin, "This spawner should not drop XP anymore!"));
		} catch (Exception e) {
			plugin.getLogger().warning("Please report this! Failed to set type: " + e.getMessage());
			e.printStackTrace();
		}
	}
}