package us.eunoians.mcrpg.api.error;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.ability.configurable.ConfigurableEnableableAbility;

/**
 * This error is thrown whenever {@link ConfigurableEnableableAbility#getEnabledSection()} attempts to return null
 *
 * @author DiamondDagger590
 */
public class EnabledAbilityConfigurationNotFoundException extends AbilityConfigurationNotFoundException{

    public EnabledAbilityConfigurationNotFoundException(@NotNull String reason, @NotNull NamespacedKey abilityKey) {
        super(reason, abilityKey);
    }
}
