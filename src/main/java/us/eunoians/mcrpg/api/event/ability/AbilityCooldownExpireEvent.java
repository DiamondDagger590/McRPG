package us.eunoians.mcrpg.api.event.ability;

import org.bukkit.NamespacedKey;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.ability.impl.Ability;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;

/**
 * An event that gets thrown whenever an {@link Ability}'s cooldown
 * expires for a given {@link AbilityHolder}.
 * <p>
 * This event is only called when the cooldown expires naturally, not through
 * manual means such as {@link AbilityHolder#cleanupHolder()}.
 */
public class AbilityCooldownExpireEvent extends AbilityEvent {

    private static final HandlerList handlers = new HandlerList();

    private final AbilityHolder abilityHolder;

    public AbilityCooldownExpireEvent(@NotNull AbilityHolder abilityHolder, @NotNull Ability ability) {
        super(ability);
        this.abilityHolder = abilityHolder;
    }

    /**
     * Gets the {@link AbilityHolder} that had their cooldown expire.
     *
     * @return The {@link AbilityHolder} that had their cooldown expire.
     */
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
