package us.eunoians.mcrpg.api.error;

import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.ability.EnableableAbility;

/**
 * This error is thrown whenever {@link EnableableAbility#getEnabledSection()} attempts to return null
 *
 * @author DiamondDagger590
 */
public class EnabledAbilityConfigurationNotFoundException extends AbilityConfigurationNotFoundException{

    public EnabledAbilityConfigurationNotFoundException(@NotNull String reason) {
        super(reason);
    }
}
