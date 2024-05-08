package us.eunoians.mcrpg.api.event.ability.swords;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.Ability;
import us.eunoians.mcrpg.ability.impl.swords.EnhancedBleed;
import us.eunoians.mcrpg.api.event.ability.AbilityActivateEvent;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;

/**
 * This event is called whenever {@link EnhancedBleed} activates
 */
public class EnhancedBleedActivateEvent extends AbilityActivateEvent implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private static final Ability ENHANCED_BLEED = McRPG.getInstance().getAbilityRegistry().getRegisteredAbility(EnhancedBleed.ENHANCED_BLEED_KEY);

    private final LivingEntity entity;
    private double additionalBleedDamage;
    private boolean cancelled = false;

    public EnhancedBleedActivateEvent(@NotNull AbilityHolder abilityHolder, @NotNull LivingEntity entity, double additionalBleedDamage) {
        super(abilityHolder, ENHANCED_BLEED);
        this.entity = entity;
        this.additionalBleedDamage = Math.max(0, additionalBleedDamage);
    }

    @Override
    @NotNull
    public EnhancedBleed getAbility() {
        return (EnhancedBleed) super.getAbility();
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
     * Gets the amount of additional damage to inflict to the {@link #getBleedingEntity()}
     * each bleed tick
     *
     * @return The amount of additional damage to inflict to the {@link #getBleedingEntity()}
     * each bleed tick
     */
    public double getAdditionalBleedDamage() {
        return additionalBleedDamage;
    }

    /**
     * Sets the amount of additional damage to inflict to the {@link #getBleedingEntity()}
     * each bleed tick
     *
     * @param additionalBleedDamage The amount of additional damage to inflict to the {@link #getBleedingEntity()}
     *                              each bleed tick
     */
    public void setAdditionalBleedDamage(double additionalBleedDamage) {
        this.additionalBleedDamage = Math.max(0, additionalBleedDamage);
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
