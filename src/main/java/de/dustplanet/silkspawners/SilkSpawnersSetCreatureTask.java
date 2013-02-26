package de.dustplanet.silkspawners;

import org.bukkit.block.Block;
import de.dustplanet.util.SilkUtil;

/**
 * Workaround! :(
 * @author (former) mushroomhostage
 * @author xGhOsTkiLLeRx
 */

public class SilkSpawnersSetCreatureTask implements Runnable {
	private SilkUtil su;
	private short entityID;
	private Block block;

	public SilkSpawnersSetCreatureTask(short entityID, Block block, SilkUtil su) {
		this.entityID = entityID;
		this.block = block;
		this.su = su;
	}

	public void run() {
		su.setSpawnerEntityID(block, entityID);
	}
}