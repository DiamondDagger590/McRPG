package us.eunoians.mcrpg.event.event.ability.swords;

import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.impl.Ability;
import us.eunoians.mcrpg.ability.impl.swords.RageSpike;
import us.eunoians.mcrpg.event.event.ability.AbilityActivateEvent;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;

/**
 * This event is fired whenever {@link RageSpike} activates.
 */
public class RageSpikeActivateEvent extends AbilityActivateEvent implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private static final Ability RAGE_SPIKE = McRPG.getInstance().getAbilityRegistry().getRegisteredAbility(RageSpike.RAGE_SPIKE_KEY);

    private boolean cancelled = false;

    public RageSpikeActivateEvent(@NotNull AbilityHolder abilityHolder) {
        super(abilityHolder, RAGE_SPIKE);
    }

    @Override
    @NotNull
    public RageSpike getAbility() {
        return (RageSpike) super.getAbility();
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
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
