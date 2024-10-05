package us.eunoians.mcrpg.event.event.ability.mining;

import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.impl.Ability;
import us.eunoians.mcrpg.ability.impl.mining.ItsATriple;
import us.eunoians.mcrpg.event.event.ability.AbilityActivateEvent;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;

/**
 * This event fires whenever {@link ItsATriple} activates, which changes the drops for {@link us.eunoians.mcrpg.ability.impl.mining.ExtraOre} from
 * 2 to 3.
 */
public class ItsATripleActivateEvent extends AbilityActivateEvent implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private static final Ability ITS_A_TRIPLE = McRPG.getInstance().getAbilityRegistry().getRegisteredAbility(ItsATriple.ITS_A_TRIPLE_KEY);
    private boolean cancelled = false;

    public ItsATripleActivateEvent(@NotNull AbilityHolder abilityHolder) {
        super(abilityHolder, ITS_A_TRIPLE);
    }

    @NotNull
    @Override
    public ItsATriple getAbility() {
        return (ItsATriple) super.getAbility();
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
