package us.eunoians.mcrpg.event.ability.swords;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.impl.Ability;
import us.eunoians.mcrpg.ability.impl.swords.SerratedStrikes;
import us.eunoians.mcrpg.event.ability.AbilityActivateEvent;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;

/**
 * This event is fired whenever an {@link AbilityHolder} activates {@link SerratedStrikes}.
 */
public class SerratedStrikesActivateEvent extends AbilityActivateEvent implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private static final Ability SERRATED_STRIKES = McRPG.getInstance().getAbilityRegistry().getRegisteredAbility(SerratedStrikes.SERRATED_STRIKES_KEY);

    private final LivingEntity livingEntity;
    private int duration;
    private boolean cancelled = false;

    public SerratedStrikesActivateEvent(@NotNull AbilityHolder abilityHolder, @NotNull LivingEntity livingEntity, int duration) {
        super(abilityHolder, SERRATED_STRIKES);
        this.livingEntity = livingEntity;
        this.duration = Math.max(0, duration);
    }

    @NotNull
    @Override
    public SerratedStrikes getAbility() {
        return (SerratedStrikes) super.getAbility();
    }

    @NotNull
    public LivingEntity getLivingEntity() {
        return livingEntity;
    }

    /**
     * Gets the duration of {@link SerratedStrikes}.
     *
     * @return The duration of {@link SerratedStrikes}.
     */
    public int getDuration() {
        return duration;
    }

    /**
     * Sets the duration for {@link SerratedStrikes}.
     *
     * @param duration The new duration
     */
    public void setDuration(int duration) {
        this.duration = Math.max(0, duration);
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
