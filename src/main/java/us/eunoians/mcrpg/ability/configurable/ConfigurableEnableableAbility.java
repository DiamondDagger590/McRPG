package us.eunoians.mcrpg.ability.configurable;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.ability.ConfigurableAbility;
import us.eunoians.mcrpg.ability.EnableableAbility;
import us.eunoians.mcrpg.api.error.EnabledAbilityConfigurationNotFoundException;

/**
 * This interface represents an {@link EnableableAbility} that should have it's enabled status pulled
 * from a config file.
 *
 * @author DiamondDagger590
 */
public interface ConfigurableEnableableAbility extends EnableableAbility, ConfigurableAbility {

    /**
     * Gets the {@link ConfigurationSection} containing enabled information for the ability
     *
     * @return The {@link ConfigurationSection} that contains enabled information for the ability
     * @throws EnabledAbilityConfigurationNotFoundException if there is a null {@link ConfigurationSection} returned
     */
    @NotNull
    public default ConfigurationSection getEnabledSection() throws EnabledAbilityConfigurationNotFoundException {

        ConfigurationSection configurationSection = getAbilityConfigurationFile().getConfigurationSection("enabled-abilities");

        if (configurationSection == null) {
            throw new EnabledAbilityConfigurationNotFoundException("Configuration section known as: 'enabled-abilities' is missing from the " + this.getAbilityConfigurationFile().getName() + " file.", getAbilityID());
        }
        return configurationSection;
    }

    /**
     * Gets if this {@link EnableableAbility} is currently enabled
     *
     * @return {@code true} if this {@link EnableableAbility} is currently enabled
     * @throws EnabledAbilityConfigurationNotFoundException if this is an instance of {@link ConfigurableEnableableAbility}
     *                                                      and the configuration section is null.
     */
    @Override
    public default boolean isEnabled() throws EnabledAbilityConfigurationNotFoundException{
        return getEnabledSection().getBoolean(getAbilityID().getKey());
    }
}
