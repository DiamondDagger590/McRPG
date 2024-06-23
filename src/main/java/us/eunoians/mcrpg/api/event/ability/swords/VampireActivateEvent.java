package us.eunoians.mcrpg.api.event.ability.swords;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.impl.Ability;
import us.eunoians.mcrpg.ability.impl.swords.Vampire;
import us.eunoians.mcrpg.api.event.ability.AbilityActivateEvent;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;

/**
 * This event gets called whenever {@link Vampire} activates
 */
public class VampireActivateEvent extends AbilityActivateEvent implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private static final Ability VAMPIRE = McRPG.getInstance().getAbilityRegistry().getRegisteredAbility(Vampire.VAMPIRE_KEY);

    private final LivingEntity entity;
    private double amountToHeal;
    private boolean cancelled = false;

    public VampireActivateEvent(@NotNull AbilityHolder abilityHolder, @NotNull LivingEntity entity, double amountToHeal) {
        super(abilityHolder, VAMPIRE);
        this.entity = entity;
        this.amountToHeal = Math.max(0, amountToHeal);
    }

    @NotNull
    @Override
    public Vampire getAbility() {
        return (Vampire) super.getAbility();
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
     * Sets the amount of health to restore to the {@link #getAbilityHolder() AbilityHolder} per
     * bleed tick
     *
     * @param amountToHeal The amount of health to restore to the {@link #getAbilityHolder() AbilityHolder} per
     *                     bleed tick
     */
    public void setAmountToHeal(double amountToHeal) {
        this.amountToHeal = Math.max(0, amountToHeal);
    }

    /**
     * Gets the amount of health to restore to the {@link #getAbilityHolder() AbilityHolder} per
     * bleed tick
     *
     * @return The amount of health to restore to the {@link #getAbilityHolder() AbilityHolder} per
     * bleed tick
     */
    public double getAmountToHeal() {
        return amountToHeal;
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
