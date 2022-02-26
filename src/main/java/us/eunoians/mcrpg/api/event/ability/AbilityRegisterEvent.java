package us.eunoians.mcrpg.api.event.ability;

import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.ability.Ability;

/**
 * This event is called whenever an {@link Ability} is registered to McRPG by using
 * {@link us.eunoians.mcrpg.ability.AbilityRegistry#registerAbility(Ability)}.
 */
public class AbilityRegisterEvent extends AbilityEvent {

    private static final HandlerList handlers = new HandlerList();

    public AbilityRegisterEvent(@NotNull Ability ability){
        super(ability);
    }

    @Override
    @NotNull
    public HandlerList getHandlers(){
        return handlers;
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return handlers;
    }
}
