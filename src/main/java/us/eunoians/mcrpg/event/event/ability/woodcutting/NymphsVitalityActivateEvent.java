package us.eunoians.mcrpg.event.event.ability.woodcutting;

import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.impl.Ability;
import us.eunoians.mcrpg.ability.impl.woodcutting.NymphsVitality;
import us.eunoians.mcrpg.event.event.ability.AbilityActivateEvent;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;

/**
 * This event is fired whenever {@link NymphsVitality} activates.
 */
public class NymphsVitalityActivateEvent extends AbilityActivateEvent implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private static final Ability NYMPHS_VITALITY = McRPG.getInstance().getAbilityRegistry().getRegisteredAbility(NymphsVitality.NYMPHS_VITALITY_KEY);
    private boolean cancelled = false;

    public NymphsVitalityActivateEvent(@NotNull AbilityHolder abilityHolder) {
        super(abilityHolder, NYMPHS_VITALITY);
    }

    @NotNull
    @Override
    public NymphsVitality getAbility() {
        return (NymphsVitality) super.getAbility();
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
