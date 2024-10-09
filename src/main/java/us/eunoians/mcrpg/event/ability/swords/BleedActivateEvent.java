package us.eunoians.mcrpg.event.ability.swords;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.impl.Ability;
import us.eunoians.mcrpg.ability.impl.swords.Bleed;
import us.eunoians.mcrpg.event.ability.AbilityActivateEvent;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;

/**
 * This event is called whenever {@link Bleed} activates for an {@link AbilityHolder}.
 */
public class BleedActivateEvent extends AbilityActivateEvent implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private static final Ability BLEED = McRPG.getInstance().getAbilityRegistry().getRegisteredAbility(Bleed.BLEED_KEY);

    private final LivingEntity entity;
    private int bleedCycles;
    private double bleedDamage;
    private boolean cancelled = false;

    public BleedActivateEvent(@NotNull AbilityHolder abilityHolder, @NotNull LivingEntity entity, int bleedCycles, double bleedDamage) {
        super(abilityHolder, BLEED);
        this.entity = entity;
        this.bleedCycles = Math.max(1, bleedCycles);
        this.bleedDamage = Math.max(1, bleedDamage);
    }

    @Override
    @NotNull
    public Bleed getAbility() {
        return (Bleed) super.getAbility();
    }

    /**
     * Gets the {@link LivingEntity} that is bleeding due to this event
     *
     * @return The {@link LivingEntity} that is bleeding due to this event
     */
    @NotNull
    public LivingEntity getBleedingEntity() {
        return entity;
    }

    /**
     * Gets the amount of times that bleed will damage the {@link #getBleedingEntity()}
     *
     * @return The amount of times that bleed will damage the {@link #getBleedingEntity()}
     */
    public int getBleedCycles() {
        return bleedCycles;
    }

    /**
     * Sets the amount of times that bleed will damage the {@link #getBleedingEntity()}
     *
     * @param bleedCycles The new amount of times that bleed will damage the {@link #getBleedingEntity()}
     */
    public void setBleedCycles(int bleedCycles) {
        this.bleedCycles = Math.max(1, bleedCycles);
    }

    /**
     * Get the amount of damage to deal (minimum amount is 1).
     *
     * @return The amount of damage to deal (minimum amount is 1).
     */
    public double getBleedDamage() {
        return bleedDamage;
    }

    /**
     * Sets the amount of damage to deal with bleed (minimum amount is 1).
     *
     * @param bleedDamage The new amount of damage to deal with bleed.
     */
    public void setBleedDamage(double bleedDamage) {
        this.bleedDamage = Math.max(1, bleedDamage);
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
