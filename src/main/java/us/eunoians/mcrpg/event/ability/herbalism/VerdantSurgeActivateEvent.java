package us.eunoians.mcrpg.event.ability.herbalism;

import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.Ability;
import us.eunoians.mcrpg.ability.impl.herbalism.VerdantSurge;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;
import us.eunoians.mcrpg.event.ability.AbilityActivateEvent;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;

/**
 * This event is fired whenever {@link VerdantSurge} activates, allowing configuration of the number of pulses
 * emitted by the activation along with the maximum radius of each pulse.
 */
public class VerdantSurgeActivateEvent extends AbilityActivateEvent implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private static final Ability VERDANT_SURGE = McRPG.getInstance().registryAccess().registry(McRPGRegistryKey.ABILITY).getRegisteredAbility(VerdantSurge.VERDANT_SURGE_KEY);

    private int pulseCount;
    private double maxPulseRadius;
    private boolean cancelled;

    public VerdantSurgeActivateEvent(@NotNull AbilityHolder abilityHolder, int pulseCount, double maxPulseRadius) {
        super(abilityHolder, VERDANT_SURGE);
        this.pulseCount = Math.max(0, pulseCount);
        this.maxPulseRadius = Math.max(0, maxPulseRadius);
        this.cancelled = false;
    }

    /**
     * Retrieves the number of pulses that will be emitted with the activation of the Verdant Surge ability.
     *
     * @return The number of pulses that will be emitted. This value is guaranteed to be non-negative.
     */
    public int getPulseCount() {
        return pulseCount;
    }

    /**
     * Retrieves the maximum pulse radius (in blocks) that a given pulse can travel.
     *
     * @return The maximum radius within which the Verdant Surge pulses can spread. This value is guaranteed to be non-negative.
     */
    public double getMaxPulseRadius() {
        return maxPulseRadius;
    }

    /**
     * Sets the number of pulses that will be emitted during the activation of the Verdant Surge ability.
     * If the provided pulse count is negative, the value is clamped to 0.
     *
     * @param pulseCount The desired number of pulses to set. A non-negative value is expected.
     */
    public void setPulseCount(int pulseCount) {
        this.pulseCount = Math.max(0, pulseCount);
    }

    /**
     * Sets the maximum pulse radius for the Verdant Surge ability. The pulse radius determines the
     * maximum distance (in blocks) that a pulse can travel during activation. If the specified radius
     * is negative, it will be clamped to 0 to ensure a non-negative value.
     *
     * @param maxPulseRadius The desired maximum pulse radius (in blocks). A non-negative value is expected.
     */
    public void setMaxPulseRadius(double maxPulseRadius) {
        this.maxPulseRadius = Math.max(0, maxPulseRadius);
    }

    @NotNull
    @Override
    public VerdantSurge getAbility() {
        return (VerdantSurge) super.getAbility();
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
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }
}
