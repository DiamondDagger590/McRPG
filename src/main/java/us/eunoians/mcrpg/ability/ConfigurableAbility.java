package us.eunoians.mcrpg.ability;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.api.error.AbilityConfigurationNotFoundException;

/**
 * This interface represents an {@link Ability} that requires some sort of configuration support
 *
 * @author DiamondDagger590
 */
public interface ConfigurableAbility extends Ability {

    /**
     * Gets the {@link FileConfiguration} that is used to configure this {@link ConfigurableAbility}
     *
     * @return The {@link FileConfiguration} that is used to configure this {@link ConfigurableAbility}
     */
    @NotNull
    public FileConfiguration getAbilityConfigurationFile();

    /**
     * Gets the exact {@link ConfigurationSection} that is used to configure this {@link ConfigurableAbility}.
     *
     * @return The exact {@link ConfigurationSection} that is used to configure this {@link ConfigurableAbility}.
     * @throws AbilityConfigurationNotFoundException Whenever the {@link ConfigurationSection} pulled is null
     */
    @NotNull
    public ConfigurationSection getAbilityConfigurationSection() throws AbilityConfigurationNotFoundException;
}
