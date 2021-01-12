package us.eunoians.mcrpg.api.event.swords;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageEvent;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.ability.impl.swords.bleed.Bleed;

/**
 * This event is a custom {@link EntityDamageEvent} that gets called to
 * handle damage dealt by the {@link Bleed} ability.
 *
 * @author DiamondDagger590
 */
public class BleedDamageEvent extends EntityDamageEvent {

    /**
     * The {@link LivingEntity} that caused Bleed to occur
     */
    @NotNull
    private final LivingEntity inflicter;

    /**
     * If this Bleed cycle should heal the {@link #getInflicter()}
     */
    private boolean restoreHealth;

    /**
     * How much health should be restored to the {@link #getInflicter()} if
     * {@link #isRestoreHealth()} is true
     */
    private int healthToRestore;

    public BleedDamageEvent(@NotNull LivingEntity inflicter, @NotNull LivingEntity damagee, double damage, boolean restoreHealth,
                            int healthToRestore) {
        super(damagee, DamageCause.CUSTOM, Math.max(damage, 0));

        this.inflicter = inflicter;
        this.restoreHealth = restoreHealth;
        this.healthToRestore = Math.max(healthToRestore, 0);
    }

    /**
     * Gets the {@link LivingEntity} that caused the Bleed to occur
     *
     * @return The {@link LivingEntity} that caused the Bleed to occur
     */
    @NotNull
    public LivingEntity getInflicter() {
        return inflicter;
    }

    /**
     * Gets if this cycle should restore health or not
     *
     * @return {@code true} if this cycle should restore health or not
     */
    public boolean isRestoreHealth() {
        return restoreHealth;
    }

    /**
     * Returns a positive zero inclusive number representing the amount to heal the {@link #getInflicter()}
     * by for this bleed cycle
     *
     * @return A positive zero inclusive number representing the amount to heal the {@link #getInflicter()}
     * by for this bleed cycle
     */
    public int getHealthToRestore() {
        return healthToRestore;
    }

    /**
     * Sets if this bleed cycle should restore health or not
     *
     * @param restoreHealth If this bleed cycle should restore health or not
     */
    public void setRestoreHealth(boolean restoreHealth) {
        this.restoreHealth = restoreHealth;
    }

    /**
     * Sets the amount of health to be restored if {@link #isRestoreHealth()} is true
     * @param healthToRestore A positive zero inclusive number representing the health to be restored
     *                        if {@link #isRestoreHealth()} is true
     */
    public void setHealthToRestore(int healthToRestore) {
        this.healthToRestore = Math.max(healthToRestore, 0);
    }
}
