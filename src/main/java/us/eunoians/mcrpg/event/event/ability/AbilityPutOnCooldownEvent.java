package us.eunoians.mcrpg.event.event.ability;

import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.ability.impl.CooldownableAbility;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;

/**
 * This event is thrown whenever an {@link CooldownableAbility} gets put
 * on cooldown for an {@link AbilityHolder}.
 */
public class AbilityPutOnCooldownEvent extends AbilityEvent {

    private static final HandlerList handlers = new HandlerList();

    private final AbilityHolder abilityHolder;
    private long cooldown;

    public AbilityPutOnCooldownEvent(@NotNull AbilityHolder abilityHolder, @NotNull CooldownableAbility ability, long cooldown) {
        super(ability);
        this.abilityHolder = abilityHolder;
        this.cooldown = Math.max(0, cooldown);
    }

    @NotNull
    @Override
    public CooldownableAbility getAbility() {
        return (CooldownableAbility) super.getAbility();
    }

    /**
     * Gets the {@link AbilityHolder} that is being put on cooldown.
     *
     * @return The {@link AbilityHolder} that is being put on cooldown.
     */
    @NotNull
    public AbilityHolder getAbilityHolder() {
        return abilityHolder;
    }

    /**
     * Gets the duration in seconds that the {@link CooldownableAbility} is on cooldown
     * for.
     *
     * @return The cooldown duration in seconds.
     */
    public long getCooldown() {
        return cooldown;
    }

    /**
     * Sets the duration in seconds that the {@link CooldownableAbility} will be on cooldown for.
     *
     * @param cooldown The new cooldown duration in seconds.
     */
    public void setCooldown(long cooldown) {
        this.cooldown = Math.max(0, cooldown);
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
