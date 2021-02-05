package us.eunoians.mcrpg.ability.configurable;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.ability.ConfigurableAbility;
import us.eunoians.mcrpg.ability.UnlockableAbility;
import us.eunoians.mcrpg.api.error.UnlockableAbilityConfigurationNotFoundException;

/**
 * This interface represents an {@link UnlockableAbility} that pulls the level that should be unlocked from a config
 *
 * @author DiamondDagger590
 */
public interface ConfigurableUnlockableAbility extends ConfigurableAbility, UnlockableAbility {

    /**
     * Gets the {@link ConfigurationSection} containing unlock level information for the ability
     *
     * @return The {@link ConfigurationSection} that contains unlock level information for the ability
     * @throws UnlockableAbilityConfigurationNotFoundException if there is a null {@link ConfigurationSection} returned
     */
    @NotNull
    public default ConfigurationSection getUnlockSection() throws UnlockableAbilityConfigurationNotFoundException {

        ConfigurationSection configurationSection = getAbilityConfigurationFile().getConfigurationSection("unlock-level-for-ability");

        if (configurationSection == null) {
            throw new UnlockableAbilityConfigurationNotFoundException("Configuration section known as: 'unlock-level-for-ability' is missing from the " + this.getAbilityConfigurationFile().getName() + " file.", getAbilityID());
        }
        return configurationSection;
    }

    /**
     * Gets the level at which this {@link UnlockableAbility} is automatically unlocked
     *
     * @return A positive zero-exclusive that is the level at which this {@link UnlockableAbility} is unlocked
     * @throws UnlockableAbilityConfigurationNotFoundException If this is an instance of {@link ConfigurableUnlockableAbility} and
     *                                                         the {@link ConfigurationSection} returns {@code null}.
     */
    @Override
    public default int getUnlockLevel() throws UnlockableAbilityConfigurationNotFoundException{
        return getUnlockSection().getInt(getAbilityID().getKey(), 0);
    }
}
