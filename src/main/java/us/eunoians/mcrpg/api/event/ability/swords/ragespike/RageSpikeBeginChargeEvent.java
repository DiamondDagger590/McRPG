package us.eunoians.mcrpg.api.event.ability.swords.ragespike;

import org.bukkit.event.Cancellable;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.ability.impl.swords.ragespike.RageSpike;
import us.eunoians.mcrpg.api.AbilityHolder;

/**
 * This event is called whenever an {@link us.eunoians.mcrpg.player.McRPGPlayer} begins charging {@link RageSpike}.
 *
 * @author DiamondDagger590
 */
public class RageSpikeBeginChargeEvent extends RageSpikeNonActivationEvent implements Cancellable {

    private boolean cancelled;
    private double chargeSeconds;

    public RageSpikeBeginChargeEvent(@NotNull AbilityHolder abilityHolder, @NotNull RageSpike rageSpike, double chargeSeconds) {
        super(abilityHolder, rageSpike);
        this.chargeSeconds = Math.max(0.001, chargeSeconds);
    }

    /**
     * Gets the amount of seconds to charge {@link RageSpike} for
     *
     * @return A positive non-zero amount of seconds to charge {@link RageSpike} for
     */
    public double getChargeSeconds() {
        return chargeSeconds;
    }

    /**
     * Sets the amount of seconds needed to charge {@link RageSpike} for
     *
     * @param chargeSeconds A positive non-zero new amount of seconds needed to charge {@link RageSpike} for
     */
    public void setChargeSeconds(int chargeSeconds) {
        this.chargeSeconds = Math.max(0.001, chargeSeconds);
    }

    /**
     * Gets the cancellation state of this event. A cancelled event will not
     * be executed in the server, but will still pass to other plugins
     *
     * @return true if this event is cancelled
     */
    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    /**
     * Sets the cancellation state of this event. A cancelled event will not
     * be executed in the server, but will still pass to other plugins.
     *
     * @param cancel true if you wish to cancel this event
     */
    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }
}
