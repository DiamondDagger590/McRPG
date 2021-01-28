package us.eunoians.mcrpg.api.event.ability;

import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.ability.Ability;
import us.eunoians.mcrpg.ability.CooldownableAbility;
import us.eunoians.mcrpg.api.AbilityHolder;

/**
 * This event is a specific child of {@link AbilityActivateEvent} that allows for configurable cooldown times via
 * events while still also allowing automatic cooldown integration with {@link us.eunoians.mcrpg.ability.listener.CooldownableAbilityListener}.
 *
 * @author DiamondDagger590
 */
public abstract class CooldownableAbilityActivateEvent extends AbilityActivateEvent {

    private int cooldownSeconds;

    /**
     * @param abilityHolder    The {@link AbilityHolder} that is activating the event
     * @param ability          The {@link Ability} being activated
     * @param abilityEventType The {@link AbilityEventType} that specifies the generic reason the event was called
     */
    public CooldownableAbilityActivateEvent(@NotNull AbilityHolder abilityHolder, @NotNull CooldownableAbility ability, @NotNull AbilityEventType abilityEventType, int cooldownSeconds) {
        super(abilityHolder, ability, abilityEventType);
        this.cooldownSeconds = cooldownSeconds;
    }

    /**
     * The {@link Ability} that is being activated by this event
     *
     * @return The {@link Ability} that is being activated by this event
     */
    @Override
    public @NotNull CooldownableAbility getAbility() {
        return (CooldownableAbility) super.getAbility();
    }

    /**
     * Gets the amount of cooldown seconds to place the {@link CooldownableAbility} on provided this event is uncancelled
     *
     * @return A positive zero exclusive amount of cooldown seconds to place the {@link CooldownableAbility} on provided this event is uncancelled.
     */
    public int getCooldownSeconds() {
        return cooldownSeconds;
    }

    /**
     * Sets the amount of cooldown seconds to place the {@link CooldownableAbility} on provided this event is uncancelled
     *
     * @param cooldownSeconds A positive non-zero amount of cooldown seconds to place the {@link CooldownableAbility} on provided this event in uncancelled
     */
    public void setCooldownSeconds(int cooldownSeconds) {
        this.cooldownSeconds = Math.max(1, cooldownSeconds);
    }
}
