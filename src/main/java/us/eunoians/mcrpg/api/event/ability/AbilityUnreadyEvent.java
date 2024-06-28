package us.eunoians.mcrpg.api.event.ability;

import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.ability.impl.Ability;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;

public class AbilityUnreadyEvent extends AbilityEvent {

    private static final HandlerList handlers = new HandlerList();

    private final AbilityHolder abilityHolder;
    private final boolean autoExpire;

    public AbilityUnreadyEvent(@NotNull AbilityHolder abilityHolder, @NotNull Ability ability, boolean autoExpire) {
        super(ability);
        this.abilityHolder = abilityHolder;
        this.autoExpire = autoExpire;
    }

    @NotNull
    public AbilityHolder getAbilityHolder() {
        return abilityHolder;
    }

    public boolean didReadyAutoExpire() {
        return autoExpire;
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