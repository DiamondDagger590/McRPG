package us.eunoians.mcrpg.event.event.entity;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.ability.ready.ReadyData;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;

/**
 * This event is fired whenever an {@link AbilityHolder} is no longer
 * ready.
 */
public class AbilityHolderUnreadyEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    private final AbilityHolder abilityHolder;
    private final ReadyData readyData;
    private final boolean autoExpire;

    public AbilityHolderUnreadyEvent(@NotNull AbilityHolder abilityHolder, @NotNull ReadyData readyData, boolean autoExpire) {
        this.abilityHolder = abilityHolder;
        this.readyData = readyData;
        this.autoExpire = autoExpire;
    }

    /**
     * Gets the {@link AbilityHolder} who is no longer ready.
     *
     * @return The {@link AbilityHolder} who is no longer ready.
     */
    @NotNull
    public AbilityHolder getAbilityHolder() {
        return abilityHolder;
    }

    /**
     * Checks to see if the ready status auto expired or was manually removed.
     *
     * @return {@code true} if the ready status auto expired, {@code false} if it
     * was manually removed.
     */
    public boolean didReadyAutoExpire() {
        return autoExpire;
    }

    /**
     * Gets the {@link ReadyData} that is being removed for the holder.
     *
     * @return The {@link ReadyData} that is being removed for the holder.
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