package us.eunoians.mcrpg.api.error;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.ability.ConfigurableAbility;

/**
 * This error is thrown whenever an {@link us.eunoians.mcrpg.ability.ConfigurableAbility} gets a null response for
 * {@link ConfigurableAbility#getAbilityConfigurationSection()}.
 *
 * @author DiamondDagger590
 */
public class AbilityConfigurationNotFoundException extends McRPGException {

    @NotNull
    private final NamespacedKey abilityKey;

    public AbilityConfigurationNotFoundException(@NotNull String reason, @NotNull NamespacedKey abilityKey) {
        super(reason);
        this.abilityKey = abilityKey;
    }

    /**
     * Gets the {@link NamespacedKey} that represents the {@link us.eunoians.mcrpg.ability.Ability} that caused
     * this error
     *
     * @return The {@link NamespacedKey} that represents the {@link us.eunoians.mcrpg.ability.Ability} that caused this
     * error
     */
    @NotNull
    public NamespacedKey getAbilityKey() {
        return abilityKey;
    }
}
