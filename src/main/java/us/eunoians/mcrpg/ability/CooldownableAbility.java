package us.eunoians.mcrpg.ability;

import us.eunoians.mcrpg.api.event.ability.CooldownableAbilityActivateEvent;

/**
 * This interface represents an {@link Ability} that should be put on cooldown after activation.
 * <p>
 * Handling of the cooldown system is fully automated provided that implementation meets two requirements.
 * <p>
 * 1) The {@link Ability} implementation extends this ({@link CooldownableAbility}) interface
 * <p>
 * 2) There is an {@link CooldownableAbilityActivateEvent} called for this {@link CooldownableAbility}s activation
 * that adheres to the Bukkit/Spigot event calling order.
 * <p>
 * If both of these conditions are true, then McRPG will automatically handle putting abilities on cooldown (with lunar support!!!)
 * and cancelling {@link CooldownableAbilityActivateEvent} calls if the {@link CooldownableAbility} is on cooldown.
 * <p>
 * This means that the implementor doesn't need to care about any cooldown related information besides providing implementation to the
 * methods provided in this interface.
 *
 * @author DiamondDagger590
 */
public interface CooldownableAbility extends Ability {

    /**
     * Gets the amount of time in seconds that this {@link CooldownableAbility} should be on cooldown for after activation
     * @return The postivie zero exclusive amount of time in seconds this {@link CooldownableAbility} should be on cooldown for after activation.
     */
    public int getCooldownDuration();
}
