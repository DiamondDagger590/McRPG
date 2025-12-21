package us.eunoians.mcrpg.event.ability.herbalism;

import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.Ability;
import us.eunoians.mcrpg.ability.impl.herbalism.MassHarvest;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;
import us.eunoians.mcrpg.event.ability.AbilityActivateEvent;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;

/**
 * This event is fired whenever {@link MassHarvest} activates, allowing configuration of the maximum radius of the pulse.
 */
public class MassHarvestActivateEvent extends AbilityActivateEvent implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private static final Ability MASS_HARVEST = McRPG.getInstance().registryAccess().registry(McRPGRegistryKey.ABILITY)
            .getRegisteredAbility(MassHarvest.MASS_HARVEST_KEY);

    private double maxPulseRadius;
    private boolean cancelled;

    public MassHarvestActivateEvent(@NotNull AbilityHolder abilityHolder, double maxPulseRadius) {
        super(abilityHolder, MASS_HARVEST);
        this.maxPulseRadius = Math.max(0, maxPulseRadius);
        this.cancelled = false;
    }

    /**
     * Retrieves the maximum pulse radius (in blocks) that a given pulse can travel.
     *
     * @return The maximum radius within which the Mass Harvest pulses can spread. This value is guaranteed to be non-negative.
     */
    public double getMaxPulseRadius() {
        return maxPulseRadius;
    }

    /**
     * Sets the maximum pulse radius for the Mass Harvest ability. The pulse radius determines the
     * maximum distance (in blocks) that a pulse can travel during activation. If the specified radius
     * is negative, it will be clamped to 0 to ensure a non-negative value.
     *
     * @param maxPulseRadius The desired maximum pulse radius (in blocks). A non-negative value is expected.
     */
    public void setMaxPulseRadius(int maxPulseRadius) {
        this.maxPulseRadius = Math.max(0, maxPulseRadius);
    }

    @NotNull
    @Override
    public MassHarvest getAbility() {
        return (MassHarvest) super.getAbility();
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
