package us.eunoians.mcrpg.event.event.ability.swords;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.impl.Ability;
import us.eunoians.mcrpg.ability.impl.swords.Bleed;
import us.eunoians.mcrpg.event.event.ability.AbilityEvent;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;

import java.util.Optional;

/**
 * This event activates whenever a {@link LivingEntity} is damaged by {@link Bleed}
 */
public class BleedDamageEvent extends AbilityEvent implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private static final Ability BLEED = McRPG.getInstance().getAbilityRegistry().getRegisteredAbility(Bleed.BLEED_KEY);

    private final Optional<AbilityHolder> bleedUser;
    private final Entity damagedEntity;
    private double damage;
    private boolean ignoreArmor;
    private boolean cancelled = false;

    public BleedDamageEvent(@NotNull Entity damagedEntity, double damage, boolean ignoreArmor) {
        super(BLEED);
        this.bleedUser = Optional.empty();
        this.damagedEntity = damagedEntity;
        this.damage = Math.max(0.0, damage);
        this.ignoreArmor = ignoreArmor;
    }

    public BleedDamageEvent(@NotNull AbilityHolder bleedUser, @NotNull Entity damagedEntity, double damage, boolean ignoreArmor) {
        super(BLEED);
        this.bleedUser = Optional.of(bleedUser);
        this.damagedEntity = damagedEntity;
        this.damage = Math.max(0.0, damage);
        this.ignoreArmor = ignoreArmor;
    }

    public BleedDamageEvent(@NotNull Optional<AbilityHolder> bleedUser, @NotNull Entity damagedEntity, double damage, boolean ignoreArmor) {
        super(BLEED);
        this.bleedUser = bleedUser;
        this.damagedEntity = damagedEntity;
        this.damage = Math.max(0.0, damage);
        this.ignoreArmor = ignoreArmor;
    }

    @Override
    @NotNull
    public Bleed getAbility() {
        return (Bleed) super.getAbility();
    }

    /**
     * Gets an {@link Optional} that will either be empty or will contain the {@link AbilityHolder}
     * that triggered the bleed.
     * <p>
     * The instance when this would be null is if bleed activated on an entity through a plugin calling
     * {@link us.eunoians.mcrpg.ability.impl.swords.bleed.BleedManager#startBleeding(LivingEntity)}.
     *
     * @return Gets an {@link Optional} that will either be empty or will contain the {@link AbilityHolder}
     * that triggered the bleed.
     */
    @NotNull
    public Optional<AbilityHolder> getBleedUser() {
        return bleedUser;
    }

    /**
     * Get the {@link Entity} affected by bleed
     *
     * @return The {@link Entity} affected by bleed
     */
    @NotNull
    public Entity getDamagedEntity() {
        return damagedEntity;
    }

    /**
     * Gets the amount of damage to deal per bleed tick
     *
     * @return The amount of damage to deal per bleed tick
     */
    public double getDamage() {
        return damage;
    }

    /**
     * Sets the amount of damage to deal per bleed tick
     *
     * @param damage The amount of damage to deal per bleed tick. Must be a number 0 or greater
     */
    public void setDamage(double damage) {
        this.damage = Math.max(0.0, damage);
    }

    /**
     * Checks to see if this instance of bleeding will ignore armor
     *
     * @return {@code true} if this instance of bleeding ignores armor
     */
    public boolean isDamageIgnoringArmor() {
        return ignoreArmor;
    }

    /**
     * Sets if this instance of bleeding will ignore armor
     *
     * @param ignoreArmor The new state of ignoring armor for bleed damage for this isntance of bleed
     */
    public void setDamageIgnoreArmor(boolean ignoreArmor) {
        this.ignoreArmor = ignoreArmor;
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
