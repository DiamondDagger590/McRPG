package us.eunoians.mcrpg.api.event.ability.swords;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.Ability;
import us.eunoians.mcrpg.ability.impl.swords.DeeperWound;
import us.eunoians.mcrpg.api.event.ability.AbilityActivateEvent;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;

/**
 * This event is called whenever {@link DeeperWound} activates.
 */
public class DeeperWoundActivateEvent extends AbilityActivateEvent implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private static final Ability DEEPER_WOUND = McRPG.getInstance().getAbilityRegistry().getRegisteredAbility(DeeperWound.DEEPER_WOUND_KEY);

    private final LivingEntity entity;
    private int additionalBleedCycles;
    private boolean cancelled = false;

    public DeeperWoundActivateEvent(@NotNull AbilityHolder abilityHolder, @NotNull LivingEntity entity, int additionalBleedCycles) {
        super(abilityHolder, DEEPER_WOUND);
        this.entity = entity;
        this.additionalBleedCycles = Math.max(0, additionalBleedCycles);
    }

    @Override
    @NotNull
    public DeeperWound getAbility() {
        return (DeeperWound) super.getAbility();
    }

    /**
     * Gets the {@link LivingEntity} that is currently bleeding
     *
     * @return The {@link LivingEntity} that is currently bleeding
     */
    @NotNull
    public LivingEntity getBleedingEntity() {
        return entity;
    }

    /**
     * Gets the amount of additional times that bleed should damage the {@link #getBleedingEntity()}
     *
     * @return The amount of additional times that bleed should damage the {@link #getBleedingEntity()}
     */
    public int getAdditionalBleedCycles() {
        return additionalBleedCycles;
    }

    /**
     * Sets the amount of additional times that bleed should damage the {@link #getBleedingEntity()}
     *
     * @param additionalBleedCycles The amount of additional times that bleed should damage the {@link #getBleedingEntity()}
     */
    public void setAdditionalBleedCycles(int additionalBleedCycles) {
        this.additionalBleedCycles = Math.max(0, additionalBleedCycles);
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
