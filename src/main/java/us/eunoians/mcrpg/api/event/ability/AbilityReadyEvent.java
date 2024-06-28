package us.eunoians.mcrpg.api.event.ability;

import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.ability.impl.Ability;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;

public class AbilityReadyEvent extends AbilityEvent {

    private static final HandlerList handlers = new HandlerList();

    private final AbilityHolder abilityHolder;

    public AbilityReadyEvent(@NotNull AbilityHolder abilityHolder, @NotNull Ability ability) {
        super(ability);
        this.abilityHolder = abilityHolder;
    }

    @NotNull
    public AbilityHolder getAbilityHolder() {
        return abilityHolder;
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
