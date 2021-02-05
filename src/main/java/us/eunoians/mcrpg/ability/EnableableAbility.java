package us.eunoians.mcrpg.ability;

import org.bukkit.configuration.ConfigurationSection;
import us.eunoians.mcrpg.api.error.EnabledAbilityConfigurationNotFoundException;

/**
 * This interface offers a way to abstractly get the enabled portion of an ability config in order to prevent
 * repetitive code in abilities.
 *
 * @author DiamondDagger590
 */
public interface EnableableAbility extends ConfigurableAbility {

    /**
     * Gets the {@link ConfigurationSection} containing enabled information for the ability
     *
     * @return The {@link ConfigurationSection} that contains enabled information for the ability
     * @throws EnabledAbilityConfigurationNotFoundException if there is a null {@link ConfigurationSection} returned
     */
    public default ConfigurationSection getEnabledSection() throws EnabledAbilityConfigurationNotFoundException {

        ConfigurationSection configurationSection = getAbilityConfigurationFile().getConfigurationSection("enabled-abilities");

        if (configurationSection == null) {
            throw new EnabledAbilityConfigurationNotFoundException("Configuration section known as: 'enabled-abilities' is missing from the " + this.getAbilityConfigurationFile().getName() + " file.");
        }
        return configurationSection;
    }

    /**
     * Gets if this {@link EnableableAbility} is currently enabled
     *
     * @return {@code true} if this {@link EnableableAbility} is currently enabled
     */
    public boolean isEnabled() throws EnabledAbilityConfigurationNotFoundException;
}
