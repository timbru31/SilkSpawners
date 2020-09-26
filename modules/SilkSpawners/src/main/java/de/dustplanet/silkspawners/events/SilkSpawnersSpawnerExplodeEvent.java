package de.dustplanet.silkspawners.events;

import javax.annotation.Nullable;

import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

/**
 * Event called when a spawner is exploded and drop chances are handled by SilkSpawners.
 *
 * @author timbru31
 */
public class SilkSpawnersSpawnerExplodeEvent extends Event implements Cancellable, ISilkSpawnersEvent {
    /**
     * Handlers list.
     */
    private static final HandlerList HANDLERS = new HandlerList();

    /**
     * Boolean state if the event is cancelled.
     */
    private boolean cancelled;

    /**
     * Player who triggered the event. Possibly null
     */
    @Nullable
    private final Player player;

    /**
     * new Entity.
     */
    private String entityID;

    /**
     * the current dropChance
     */
    private int dropChance;

    /**
     * Block involved.
     */
    private final Block block;

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
     *
     * @param player who issues the event, can be null
     * @param block is allowed to be null
     * @param entityID new entity ID
     * @param dropChance the current dropChance
     */
    public SilkSpawnersSpawnerExplodeEvent(@Nullable final Player player, final Block block, final String entityID, final int dropChance) {
        this.player = player;
        this.block = block;
        this.dropChance = dropChance;
        if (block != null) {
            this.spawner = (CreatureSpawner) block.getState();
        }
        this.entityID = entityID;
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
     * Gets the overridden ItemStack.
     *
     * @return the overridden ItemStack to drop if set or null
     */
    public ItemStack getDrop() {
        return drop;
    }

    /**
     * Sets the ItemStack to drop.
     *
     * @param drop the ItemStack to drop
     */
    public void setDrop(final ItemStack drop) {
        this.drop = drop;
    }

    /**
     * Gets the current drop chance.
     *
     * @return the drop chance
     */
    public int getDropChance() {
        return dropChance;
    }

    /**
     * Sets the new drop chance in percent.
     *
     * @param dropChance the overriden drop chance
     */
    public void setDropChance(final int dropChance) {
        this.dropChance = dropChance;
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
