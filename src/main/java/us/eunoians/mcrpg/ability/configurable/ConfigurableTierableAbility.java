package us.eunoians.mcrpg.ability.configurable;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.ability.ConfigurableAbility;
import us.eunoians.mcrpg.ability.TierableAbility;
import us.eunoians.mcrpg.api.Methods;
import us.eunoians.mcrpg.api.error.AbilityConfigurationNotFoundException;
import us.eunoians.mcrpg.api.error.TierableAbilityConfigurationNotFoundException;

/**
 * This interface represents an ability that pulls tier information from a config file.
 *
 * @author DiamondDagger590
 */
public interface ConfigurableTierableAbility extends ConfigurableAbility, TierableAbility {

    /**
     * Gets the {@link ConfigurationSection} containing information about when tiers are available for the ability
     *
     * @return The {@link ConfigurationSection} that contains information about when tiers are available for the ability
     * @throws AbilityConfigurationNotFoundException if there is a null {@link ConfigurationSection} returned
     */
    @NotNull
    public default ConfigurationSection getTierUpgradeSection() throws AbilityConfigurationNotFoundException {

        ConfigurationSection configurationSection = getAbilityConfigurationSection().getConfigurationSection("tier-upgrade");

        if (configurationSection == null) {
            throw new TierableAbilityConfigurationNotFoundException("Configuration section known as: 'tier-upgrade' is missing from the " + this.getAbilityConfigurationFile().getName() + " file.", getAbilityID());
        }
        return configurationSection;
    }

    /**
     * Gets the {@link ConfigurationSection} containing unlock level information for the ability
     *
     * @param tier A positive zero exclusive tier at which to get the information for
     * @return The {@link ConfigurationSection} that contains unlock level information for the ability
     * @throws AbilityConfigurationNotFoundException if there is a null {@link ConfigurationSection} returned
     */
    @NotNull
    public default ConfigurationSection getSpecificTierSection(int tier) throws AbilityConfigurationNotFoundException {

        String tierName = "tier-" + Methods.convertToNumeral(Math.max(1, tier));
        ConfigurationSection configurationSection = getAbilityConfigurationSection().getConfigurationSection(tierName);

        if (configurationSection == null) {
            throw new TierableAbilityConfigurationNotFoundException("Configuration section known as: '" + tierName +  "' is missing from the " + this.getAbilityConfigurationFile().getName() + " file.", getAbilityID());
        }
        return configurationSection;
    }

    /**
     * Gets the level at which the provided tier is unlocked at
     *
     * @param tier A positive zero exclusive tier at which to get the unlock level for
     * @return The level at which the tier becomes available or {@code -1} if missing from config.
     * @throws AbilityConfigurationNotFoundException if there is a null {@link ConfigurationSection} returned
     */
    @Override
    public default int getTierUnlockLevel(int tier) throws AbilityConfigurationNotFoundException {
        return getTierUpgradeSection().getInt("tier-" + Methods.convertToNumeral(Math.max(1, tier)), -1);
    }
}
