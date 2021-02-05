package us.eunoians.mcrpg.api.error;

import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.ability.ConfigurableAbility;

/**
 * This error is thrown whenever an {@link us.eunoians.mcrpg.ability.ConfigurableAbility} gets a null response for
 * {@link ConfigurableAbility#getAbilityConfigurationSection()}.
 *
 * @author DiamondDagger590
 */
public class AbilityConfigurationNotFoundException extends McRPGException {

    public AbilityConfigurationNotFoundException(@NotNull String reason) {
        super(reason);
    }
}
