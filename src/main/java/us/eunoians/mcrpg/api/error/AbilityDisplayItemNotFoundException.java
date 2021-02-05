package us.eunoians.mcrpg.api.error;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.ability.configurable.ConfigurableAbilityDisplayItem;

/**
 * This error is thrown whenever {@link ConfigurableAbilityDisplayItem#getDisplayItemSection()} attempts to return null
 *
 * @author DiamondDagger590
 */
public class AbilityDisplayItemNotFoundException extends AbilityConfigurationNotFoundException{

    public AbilityDisplayItemNotFoundException(@NotNull String reason, @NotNull NamespacedKey abilityKey) {
        super(reason, abilityKey);
    }
}
