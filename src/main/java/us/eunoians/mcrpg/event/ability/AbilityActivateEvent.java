package us.eunoians.mcrpg.event.ability;

import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.ability.Ability;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;

/**
 * This event is called whenever an {@link Ability} activates.
 * <p>
 * Provides a {@link HandlerList} so listeners can subscribe at this abstract level
 * and receive all concrete ability activation events polymorphically.
 */
public abstract class AbilityActivateEvent extends AbilityEvent {

    private static final HandlerList HANDLERS = new HandlerList();

    private final AbilityHolder abilityHolder;

    public AbilityActivateEvent(@NotNull AbilityHolder abilityHolder, @NotNull Ability ability) {
        super(ability);
        this.abilityHolder = abilityHolder;
    }

    /**
     * Gets the {@link AbilityHolder} that activated the {@link #getAbility() Ability}
     *
     * @return The {@link AbilityHolder} that activated the {@link #getAbility() Ability}
     */
    @NotNull
    public AbilityHolder getAbilityHolder() {
        return abilityHolder;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    /**
     * Required by Bukkit to locate the handler list for this event class when
     * a listener registers for {@link AbilityActivateEvent}.
     *
     * @return The shared {@link HandlerList} for this event.
     */
    @NotNull
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
