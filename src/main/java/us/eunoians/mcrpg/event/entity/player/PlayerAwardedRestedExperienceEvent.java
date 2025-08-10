package us.eunoians.mcrpg.event.entity.player;

import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;

/**
 * This event is fired whenever a player is awarded rested experience (usually via
 * {@link us.eunoians.mcrpg.skill.experience.rested.RestedExperienceManager}).
 */
public class PlayerAwardedRestedExperienceEvent extends McRPGPlayerEvent implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled = false;
    private float restedExperience;
    private final double maxAccumulation;

    public PlayerAwardedRestedExperienceEvent(@NotNull McRPGPlayer mcRPGPlayer, float restedExperience, double maxAccumulation) {
        super(mcRPGPlayer);
        this.restedExperience = Math.max(restedExperience, 0);
        this.maxAccumulation = Math.max(maxAccumulation, 0);
    }

    /**
     * Gets the amount of rested experience being awarded to the player.
     *
     * @return The amount of rested experience to be awarded.
     */
    public float getRestedExperience() {
        return restedExperience;
    }

    /**
     * Sets the amount of rested experience to be awarded to the player. This value will be clamped
     * between 0 and {@link #getMaxAccumulation()}.
     *
     * @param restedExperience The amount of rested experience to be awarded.
     */
    public void setRestedExperience(double restedExperience) {
        this.restedExperience = (float) Math.clamp(restedExperience, 0, maxAccumulation);
    }

    /**
     * Gets the maximum amount of rested experience the player is allowed to accumulate.
     *
     * @return the maximum amount of rested experience the player is allowed to accumulate.
     */
    public double getMaxAccumulation() {
        return maxAccumulation;
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
