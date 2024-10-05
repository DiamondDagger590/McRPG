package us.eunoians.mcrpg.event.event.entity;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.ability.ready.ReadyData;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;

/**
 * This event is fired whenever an {@link AbilityHolder} becomes
 * "ready" to activate an ability.
 */
public class AbilityHolderReadyEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    private final AbilityHolder abilityHolder;
    private final ReadyData readyData;

    public AbilityHolderReadyEvent(@NotNull AbilityHolder abilityHolder, @NotNull ReadyData readyData) {
        this.abilityHolder = abilityHolder;
        this.readyData = readyData;
    }

    /**
     * Gets the {@link AbilityHolder} that is being set as ready.
     *
     * @return The {@link AbilityHolder} that is being set as ready.
     */
    @NotNull
    public AbilityHolder getAbilityHolder() {
        return abilityHolder;
    }

    /**
     * Gets the {@link ReadyData} that is being used.
     *
     * @return The {@link ReadyData} that is being used.
     */
    @NotNull
    public ReadyData getReadyData() {
        return readyData;
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
