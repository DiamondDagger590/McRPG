package us.eunoians.mcrpg.api.error;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.ability.configurable.ConfigurableUnlockableAbility;

/**
 * This error is thrown whenever {@link ConfigurableUnlockableAbility#getUnlockSection()} attempts to return null
 *
 * @author DiamondDagger590
 */
public class UnlockableAbilityConfigurationNotFoundException extends AbilityConfigurationNotFoundException{

    public UnlockableAbilityConfigurationNotFoundException(@NotNull String reason, @NotNull NamespacedKey abilityKey) {
        super(reason, abilityKey);
    }
}
