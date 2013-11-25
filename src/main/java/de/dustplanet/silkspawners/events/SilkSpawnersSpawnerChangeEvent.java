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
    private final short oldID;
    private Block block;
    private CreatureSpawner spawner;

    /**
     * This constructor should not be used anymore,
     * because the new one carries information about the
     * current entityID of the spawner block or item
     *
     * @deprecated use {@link #SilkSpawnersSpawnerChangeEvent(Player, Block, short, short)} instead.  
     */
    @Deprecated
    public SilkSpawnersSpawnerChangeEvent(Player player, Block block, short id) {
	this.player = player;
	this.block = block;
	if (block != null) {
	    this.spawner = (CreatureSpawner) block.getState();
	}
	this.id = id;
	this.oldID = 0;
    }
    
    /**
     * Constructor of the event
     * @param player who issues the event
     * @param block is allowed to be null
     * @param id new ID
     * @param oldID of the spawner
     */
    public SilkSpawnersSpawnerChangeEvent(Player player, Block block, short id, short oldID) {
	this.player = player;
	this.block = block;
	if (block != null) {
	    this.spawner = (CreatureSpawner) block.getState();
	}
	this.id = id;
	this.oldID = oldID;
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
     * @param cancel whether the event should be cancelled or not
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
     * @param id the new entityID
     */
    public void setEntityID(short id) {
	this.id = id;
    }
    
    /**
     * Gets the old entityID of the spawner (item or block)
     * May return 0 if the deprecated constructor is used!
     */
    public short getOldEntityID() {
	return this.oldID;
    }

    public HandlerList getHandlers() {
	return handlers;
    }

    public static HandlerList getHandlerList() {
	return handlers;
    }
}