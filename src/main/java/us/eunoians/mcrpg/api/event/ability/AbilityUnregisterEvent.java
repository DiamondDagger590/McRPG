package us.eunoians.mcrpg.api.event.ability;

import org.bukkit.NamespacedKey;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.ability.impl.Ability;

/**
 * This event is called whenever an {@link Ability} is unregistered from McRPG by using
 * {@link us.eunoians.mcrpg.ability.AbilityRegistry#unregisterAbility(NamespacedKey)}.
 */
public class AbilityUnregisterEvent extends AbilityEvent {

    private static final HandlerList handlers = new HandlerList();

    public AbilityUnregisterEvent(@NotNull Ability ability) {
        super(ability);
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
