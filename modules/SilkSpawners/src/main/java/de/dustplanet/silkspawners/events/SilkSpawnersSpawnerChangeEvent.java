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
public class SilkSpawnersSpawnerChangeEvent extends Event implements Cancellable, ISilkSpawnersEvent {
    /**
     * Handlers list.
     */
    private static final HandlerList HANDLERS = new HandlerList();

    /**
     * Boolean state if the event is cancelled.
     */
    private boolean cancelled;

    /**
     * Player who triggered the event.
     */
    private final Player player;

    /**
     * new entity ID.
     */
    private String entityID;

    /**
     * current (old) entity ID of the spawner.
     */
    private final String oldEntityID;

    /**
     * Block involved.
     */
    private final Block block;

    /**
     * Spawner involved.
     */
    private CreatureSpawner spawner;

    /**
     * Amount of items being changed.
     */
    private int amount;

    /**
     * Constructor of the event.
     *
     * @param player who issues the event
     * @param block is allowed to be null
     * @param entityID new entity ID
     * @param oldEntityID of the spawner
     * @param amount of items being changed
     */
    public SilkSpawnersSpawnerChangeEvent(final Player player, final Block block, final String entityID, final String oldEntityID,
            final int amount) {
        this.player = player;
        this.block = block;
        if (block != null) {
            this.spawner = (CreatureSpawner) block.getState();
        }
        this.entityID = entityID;
        this.oldEntityID = oldEntityID;
        this.amount = amount;
    }

    /**
     * Determine if the event is cancelled or not.
     *
     * @return yes or no
     */
    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    /**
     * Cancels the event.
     *
     * @param cancel whether the event should be cancelled or not
     */
    @Override
    public void setCancelled(final boolean cancel) {
        this.cancelled = cancel;
    }

    /**
     * Get the player from this event.
     *
     * @return the player object
     */
    @Override
    public Player getPlayer() {
        return this.player;
    }

    /**
     * Get the block of this event.
     *
     * @return the block - in this case a spawner; returns null when an egg is used
     */
    @Override
    public Block getBlock() {
        return this.block;
    }

    /**
     * Get the creature spawner of this event.
     *
     * @return the creature spawner; returns null when an egg is used
     */
    @Override
    public CreatureSpawner getSpawner() {
        return this.spawner;
    }

    /**
     * Get the entity ID (mob to spawn) from this event.
     *
     * @return the entity ID
     */
    @Override
    public String getEntityID() {
        return this.entityID;
    }

    /**
     * Sets the entity ID of the spawner.
     *
     * @param entityID the new entity ID
     */
    @Override
    public void setEntityID(final String entityID) {
        this.entityID = entityID;
    }

    /**
     * Gets the old entity ID of the spawner (item or block).
     *
     * @return the old entity ID
     */
    public String getOldEntityID() {
        return this.oldEntityID;
    }

    /**
     * Gets the amount of the ItemStack.
     *
     * @return the amount
     */
    public int getAmount() {
        return amount;
    }

    /**
     * Sets the amount of the ItemStack.
     *
     * @param amount of the ItemStack
     */
    public void setAmount(final int amount) {
        this.amount = amount;
    }

    /**
     * Returns the HandlerList.
     *
     * @return the HandlerList
     */
    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    /**
     * Returns the HanderList in a static way.
     *
     * @return the HanderList
     */
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
