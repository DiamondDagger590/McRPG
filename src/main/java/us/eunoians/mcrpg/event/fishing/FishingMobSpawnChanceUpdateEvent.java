package us.eunoians.mcrpg.event.fishing;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Fired when a player's fishing mob spawn chance is about to change.
 * <p>
 * Cancellable — if cancelled, the chance remains unchanged. Third-party
 * plugins can also modify the new chance via {@link #setNewChance(double)}.
 */
public class FishingMobSpawnChanceUpdateEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    private final Player player;
    private final double oldChance;
    private double newChance;
    private final Location hookLocation;
    private boolean cancelled = false;

    /**
     * Creates a new spawn chance update event.
     *
     * @param player       the player whose chance is changing
     * @param oldChance    the previous spawn chance value
     * @param newChance    the proposed new spawn chance value
     * @param hookLocation the current fishing hook location
     */
    public FishingMobSpawnChanceUpdateEvent(@NotNull Player player, double oldChance,
                                             double newChance, @NotNull Location hookLocation) {
        this.player = player;
        this.oldChance = oldChance;
        this.newChance = newChance;
        this.hookLocation = hookLocation;
    }

    /**
     * Gets the player whose spawn chance is changing.
     *
     * @return the player
     */
    @NotNull
    public Player getPlayer() {
        return player;
    }

    /**
     * Gets the spawn chance before this update.
     *
     * @return the old chance value
     */
    public double getOldChance() {
        return oldChance;
    }

    /**
     * Gets the proposed new spawn chance.
     *
     * @return the new chance value
     */
    public double getNewChance() {
        return newChance;
    }

    /**
     * Sets the new spawn chance. Third-party plugins can use this to
     * modify the chance update.
     *
     * @param newChance the modified new chance value
     */
    public void setNewChance(double newChance) {
        this.newChance = newChance;
    }

    /**
     * Gets the fishing hook location at the time of this chance update.
     *
     * @return the hook location
     */
    @NotNull
    public Location getHookLocation() {
        return hookLocation;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    @Override
    @NotNull
    public HandlerList getHandlers() {
        return handlers;
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return handlers;
    }
}
