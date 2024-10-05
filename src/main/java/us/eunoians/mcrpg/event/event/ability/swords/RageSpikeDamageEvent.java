package us.eunoians.mcrpg.event.event.ability.swords;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.impl.Ability;
import us.eunoians.mcrpg.ability.impl.swords.RageSpike;
import us.eunoians.mcrpg.event.event.ability.AbilityEvent;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;

/**
 * This event is called whenever a {@link LivingEntity} would be damaged
 * by an {@link AbilityHolder} using {@link RageSpike}.
 */
public class RageSpikeDamageEvent extends AbilityEvent implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private static final Ability RAGE_SPIKE = McRPG.getInstance().getAbilityRegistry().getRegisteredAbility(RageSpike.RAGE_SPIKE_KEY);

    private boolean cancelled = false;
    private final AbilityHolder abilityHolder;
    private final LivingEntity damagedEntity;
    private double damage;

    public RageSpikeDamageEvent(@NotNull AbilityHolder abilityHolder, @NotNull LivingEntity damagedEntity, double damage) {
        super(RAGE_SPIKE);
        this.abilityHolder = abilityHolder;
        this.damagedEntity = damagedEntity;
        this.damage = Math.max(0, damage);
    }

    /**
     * Gets the {@link AbilityHolder} that is using {@link RageSpike}.
     * @return The {@link AbilityHolder} that is using {@link RageSpike}.
     */
    @NotNull
    public AbilityHolder getAbilityHolder() {
        return abilityHolder;
    }

    /**
     * Gets the {@link LivingEntity} that is being damaged.
     * @return The {@link LivingEntity} that is being damaged.
     */
    @NotNull
    public LivingEntity getDamagedEntity() {
        return damagedEntity;
    }

    /**
     * Gets the amount of damage to deal.
     * @return The amount of damage to deal.
     */
    public double getDamage() {
        return damage;
    }

    /**
     * Sets the amount of damage to deal.
     * @param damage The amount of damage to deal.
     */
    public void setDamage(double damage) {
        this.damage = Math.max(0, damage);
    }

    @NotNull
    @Override
    public RageSpike getAbility() {
        return (RageSpike) super.getAbility();
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
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
