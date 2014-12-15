package de.dustplanet.silkspawners.events;

import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Event called when a spawner is changed by SilkSpawners.
 *
 * @author (former) mushroomhostage
 * @author xGhOsTkiLLeRx
 */
public class SilkSpawnersSpawnerChangeEvent extends Event implements Cancellable {
    /**
     * Handlers list.
     */
    private static final HandlerList HANDLERS = new HandlerList();
    /**
     * Boolean state if the event is cancelled.
     */
    private boolean cancelled;
    // Our objects
    /**
     * Player who triggered the event.
     */
    private Player player;
    /**
     * new EntityID.
     */
    private short id;
    /**
     * current (old) EntityID of the spawner.
     */
    private final short oldID;
    /**
     * Block involved.
     */
    private Block block;
    /**
     * Spawner involved.
     */
    private CreatureSpawner spawner;

    /**
     * Amount of items being changed.
     */
    private int amount;

    /**
     * This constructor should not be used anymore,
     * because the new one carries information about the
     * current entityID of the spawner block or item.
     *
     * @deprecated use {@link #SilkSpawnersSpawnerChangeEvent(Player, Block, short, short, int)} instead.
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
        this.amount = 1;
    }

    /**
     * Constructor of the event. Please note that the amount is a fallback to 1
     * @deprecated use {@link #SilkSpawnersSpawnerChangeEvent(Player, Block, short, short, int)} instead.
     */
    @Deprecated
    public SilkSpawnersSpawnerChangeEvent(Player player, Block block, short id, short oldID) {
        this.player = player;
        this.block = block;
        if (block != null) {
            this.spawner = (CreatureSpawner) block.getState();
        }
        this.id = id;
        this.oldID = oldID;
        this.amount = 1;
    }

    /**
     * Constructor of the event.
     * @param player who issues the event
     * @param block is allowed to be null
     * @param id new ID
     * @param oldID of the spawner
     */
    public SilkSpawnersSpawnerChangeEvent(Player player, Block block, short id, short oldID, int amount) {
        this.player = player;
        this.block = block;
        if (block != null) {
            this.spawner = (CreatureSpawner) block.getState();
        }
        this.id = id;
        this.oldID = oldID;
        this.amount = amount;
    }

    /**
     * Determine if the event is cancelled or not.
     * @return yes or no
     */
    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    /**
     * Cancels the event.
     * @param cancel whether the event should be cancelled or not
     */
    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    /**
     * Get the player from this event.
     * @return the player object
     */
    public Player getPlayer() {
        return this.player;
    }

    /**
     * Get the block of this event.
     * @return the block - in this case a spawner; returns null when an egg is used
     */
    public Block getBlock() {
        return this.block;
    }

    /**
     * Get the creature spawner of this event.
     * @return the creature spawner; returns null when an egg is used
     */
    public CreatureSpawner getSpawner() {
        return this.spawner;
    }

    /**
     * Get the entity ID (mob to spawn) from this event.
     * @return the entity ID
     */
    public short getEntityID() {
        return this.id;
    }

    /**
     * Sets the entityID of the spawner.
     * @param id the new entityID
     */
    public void setEntityID(short id) {
        this.id = id;
    }

    /**
     * Gets the old entityID of the spawner (item or block).
     * May return 0 if the deprecated constructor is used!
     */
    public short getOldEntityID() {
        return this.oldID;
    }

    /**
     * Returns the HandlerList.
     * @return the HandlerList
     */
    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    /**
     * Returns the HanderList in a static way.
     * @return the HanderList
     */
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
