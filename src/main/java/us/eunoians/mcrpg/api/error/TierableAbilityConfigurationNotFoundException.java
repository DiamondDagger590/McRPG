package us.eunoians.mcrpg.api.error;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.ability.configurable.ConfigurableTierableAbility;

/**
 * This error is thrown whenever {@link ConfigurableTierableAbility#getTierUpgradeSection()} or {@link ConfigurableTierableAbility#getSpecificTierSection()}
 * attempts to return null
 *
 * @author DiamondDagger590
 */
public class TierableAbilityConfigurationNotFoundException  extends AbilityConfigurationNotFoundException {

    public TierableAbilityConfigurationNotFoundException(@NotNull String reason, @NotNull NamespacedKey abilityKey) {
        super(reason, abilityKey);
    }
}