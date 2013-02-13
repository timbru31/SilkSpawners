package de.dustplanet.silkspawners.events;

import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class SilkSpawnersSpawnerChangeEvent extends Event implements Cancellable {
	private static final HandlerList handlers = new HandlerList();
	private boolean cancelled;
	// Our objects
	private Player player;
	private short id;
	private Block block;
	private CreatureSpawner spawner;

	public SilkSpawnersSpawnerChangeEvent(Player player, Block block, short id) {
		this.player = player;
		this.block = block;
		if (block != null) this.spawner = (CreatureSpawner) block.getState();
		this.id = id;
	}
	
	/**
	 * Determine if the event is cancelled or not
	 * @return yes or no
	 */
	public boolean isCancelled() {
		return this.cancelled;
	}
	
	/**
	 * Cancel the event
	 * @param wheter the event should be cancelled or not
	 */
	public void setCancelled(boolean cancel) {
		this.cancelled = cancel;
	}

	/**
	 * Get the player from this event
	 * @return the player object
	 */
	public Player getPlayer() {
		return this.player;
	}
	
	/**
	 * Get the block of this event
	 * @return the block - in this case a spawner; returns null when an egg is used
	 */
	public Block getBlock() {
		return this.block;
	}
	
	/**
	 * Get the creature spawner of this event
	 * @return the creature spawner; returns null when an egg is used
	 */
	public CreatureSpawner getSpawner() {
		return this.spawner;
	}
	
	/**
	 * Get the entity ID (mob to spawn) from this event
	 * @return the entity ID
	 */
	public short getEntityID() {
		return this.id;
	}
	
	/**
	 * Sets the entityID of the spawner
	 * @param the new entityID
	 */
	public void setEntityID(short id) {
		this.id = id;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
}