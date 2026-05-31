package us.eunoians.mcrpg.event.ability.guardian;

import org.bukkit.entity.Entity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.Ability;
import us.eunoians.mcrpg.ability.impl.guardian.PhaseShift;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;
import us.eunoians.mcrpg.event.ability.AbilityActivateEvent;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;

/**
 * This event is fired whenever {@link PhaseShift} activates.
 */
public class PhaseShiftActivateEvent extends AbilityActivateEvent implements Cancellable {

    private static final Ability PHASE_SHIFT = McRPG.getInstance().registryAccess().registry(McRPGRegistryKey.ABILITY).getRegisteredAbility(PhaseShift.PHASE_SHIFT_KEY);

    private static final HandlerList HANDLERS = new HandlerList();

    private final Entity target;
    private boolean cancelled = false;

    /**
     * Constructs a new {@link PhaseShiftActivateEvent}.
     *
     * @param abilityHolder The {@link AbilityHolder} activating the ability
     * @param target        The {@link Entity} being targeted by the phase shift
     */
    public PhaseShiftActivateEvent(@NotNull AbilityHolder abilityHolder, @NotNull Entity target) {
        super(abilityHolder, PHASE_SHIFT);
        this.target = target;
    }

    @Override
    @NotNull
    public PhaseShift getAbility() {
        return (PhaseShift) super.getAbility();
    }

    /**
     * Gets the {@link Entity} being targeted by the phase shift.
     *
     * @return The {@link Entity} being targeted
     */
    @NotNull
    public Entity getTarget() {
        return target;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    /**
     * Required by Bukkit to locate the handler list for this event class.
     *
     * @return The {@link HandlerList} for this event
     */
    @NotNull
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
