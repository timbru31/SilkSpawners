package de.dustplanet.silkspawners.events;

import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

/**
 * Event called when a spawner is broken by SilkSpawners.
 *
 * @author xGhOsTkiLLeRx
 */
public class SilkSpawnersSpawnerBreakEvent extends Event implements Cancellable {
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
    private Player player;

    /**
     * new EntityID.
     */
    private short id;

    /**
     * Block involved.
     */
    private Block block;

    /**
     * Spawner involved.
     */
    private CreatureSpawner spawner;

    /**
     * Overridden ItemStack that should instead be dropped.
     */
    private ItemStack drop;

    /**
     * Constructor of the event.
     * @param player who issues the event
     * @param block is allowed to be null
     * @param id new ID
     */
    public SilkSpawnersSpawnerBreakEvent(Player player, Block block, short id) {
        this.player = player;
        this.block = block;
        if (block != null) {
            this.spawner = (CreatureSpawner) block.getState();
        }
        this.id = id;
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
     * Gets the overridden ItemStack.
     * @return the overridden ItemStack to drop if set or null
     */
    public ItemStack getDrop() {
        return drop;
    }

    /**
     * Sets the ItemStack to drop.
     * @param drop the ItemStack to drop
     */
    public void setDrop(ItemStack drop) {
        this.drop = drop;
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
