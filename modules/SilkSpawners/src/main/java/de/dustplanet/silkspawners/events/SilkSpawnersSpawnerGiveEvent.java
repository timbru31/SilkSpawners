package de.dustplanet.silkspawners.events;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Event called when spawners or spawn eggs are given via the SilkSpawners give command.
 *
 * @author timbru31
 */
public class SilkSpawnersSpawnerGiveEvent extends Event implements Cancellable {
    /**
     * Handlers list.
     */
    private static final HandlerList HANDLERS = new HandlerList();

    /**
     * Boolean state if the event is cancelled.
     */
    private boolean cancelled;

    /**
     * Sender who issued the give command, might be the console.
     */
    private final CommandSender sender;

    /**
     * Player who receives the items.
     */
    private final Player receiver;

    /**
     * Entity ID (spawner type) being given.
     */
    private String entityID;

    /**
     * Amount of items being given.
     */
    private int amount;

    /**
     * Boolean state if a spawn egg instead of a spawner is given.
     */
    private final boolean egg;

    /**
     * Constructor of the event.
     *
     * @param sender who issues the event, might be the console
     * @param receiver who receives the items
     * @param entityID of the spawner or egg being given
     * @param amount of items being given
     * @param egg whether a spawn egg instead of a spawner is given
     */
    public SilkSpawnersSpawnerGiveEvent(final CommandSender sender, final Player receiver, final String entityID, final int amount,
            final boolean egg) {
        this.sender = sender;
        this.receiver = receiver;
        this.entityID = entityID;
        this.amount = amount;
        this.egg = egg;
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
     * Get the sender of this event.
     *
     * @return the sender, might be the console
     */
    public CommandSender getSender() {
        return this.sender;
    }

    /**
     * Get the receiver of the items.
     *
     * @return the player receiving the items
     */
    public Player getReceiver() {
        return this.receiver;
    }

    /**
     * Get the entity ID (mob to spawn) from this event.
     *
     * @return the entity ID
     */
    public String getEntityID() {
        return this.entityID;
    }

    /**
     * Sets the entity ID of the spawner or egg being given.
     *
     * @param entityID the new entity ID
     */
    public void setEntityID(final String entityID) {
        this.entityID = entityID;
    }

    /**
     * Gets the amount of the ItemStack.
     *
     * @return the amount
     */
    public int getAmount() {
        return this.amount;
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
     * Determine if a spawn egg instead of a spawner is given.
     *
     * @return yes or no
     */
    public boolean isEgg() {
        return this.egg;
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
