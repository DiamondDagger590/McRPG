package us.eunoians.mcrpg.ability;

import us.eunoians.mcrpg.api.error.EnabledAbilityConfigurationNotFoundException;

/**
 * This interface offers a way to abstractly get the enabled portion of an ability config in order to prevent
 * repetitive code in abilities.
 *
 * @author DiamondDagger590
 */
public interface EnableableAbility {

    /**
     * Gets if this {@link EnableableAbility} is currently enabled
     *
     * @return {@code true} if this {@link EnableableAbility} is currently enabled
     * @throws EnabledAbilityConfigurationNotFoundException if this is an instance of {@link us.eunoians.mcrpg.ability.configurable.ConfigurableEnableableAbility}
     * and the configuration section is null.
     */
    public boolean isEnabled() throws EnabledAbilityConfigurationNotFoundException;
}
