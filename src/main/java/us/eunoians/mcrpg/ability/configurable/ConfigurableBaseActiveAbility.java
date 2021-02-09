package us.eunoians.mcrpg.ability.configurable;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.ability.Ability;
import us.eunoians.mcrpg.ability.ActiveAbility;
import us.eunoians.mcrpg.ability.CooldownableAbility;
import us.eunoians.mcrpg.ability.ReadyableAbility;
import us.eunoians.mcrpg.ability.creation.AbilityCreationData;
import us.eunoians.mcrpg.api.error.AbilityConfigurationNotFoundException;
import us.eunoians.mcrpg.util.configuration.FileManager;

/**
 * This class couples implementation of {@link ConfigurableBaseAbility} with {@link us.eunoians.mcrpg.ability.ActiveAbility} and
 * {@link us.eunoians.mcrpg.ability.ReadyableAbility} as a generic way for McRPG classes to use these repeated interfaces as most behaviour
 * is the same for abilities that are active and readyable so this prevents repeat code.
 *
 * @author DiamondDagger590
 */
public abstract class ConfigurableBaseActiveAbility extends ConfigurableBaseAbility implements ActiveAbility, ReadyableAbility, CooldownableAbility {

    protected boolean ready = false;

    /**
     * This assumes that the required extension of {@link AbilityCreationData}. Implementations of this will need
     * to sanitize the input.
     *
     * @param abilityCreationData The {@link AbilityCreationData} that is used to create this {@link Ability}
     */
    public ConfigurableBaseActiveAbility(@NotNull AbilityCreationData abilityCreationData) {
        super(abilityCreationData);
    }

    /**
     * Gets the amount of time in seconds that this {@link CooldownableAbility} should be on cooldown for after activation
     *
     * @return The positive zero exclusive amount of time in seconds this {@link CooldownableAbility} should be on cooldown for after activation.
     */
    @Override
    public int getCooldownDuration() {

        ConfigurationSection configurationSection;

        try {
            configurationSection = getSpecificTierSection(getTier());
        } catch (AbilityConfigurationNotFoundException e) {
            e.printStackTrace();
            return 180;
        }

        return configurationSection.getInt("cooldown", 180);
    }

    /**
     * Gets the amount of seconds that the "ready" status should last for this ability
     *
     * @return The amount of seconds that the "ready" status should last for this ability
     */
    @Override
    public int getReadyDurationSeconds() {

        ConfigurationSection configurationSection;

        try {
            configurationSection = getSpecificTierSection(getTier());
        } catch (AbilityConfigurationNotFoundException e) {
            e.printStackTrace();
            return (FileManager.Files.CONFIG.getFile().contains("player-configuration.ready-duration") ? FileManager.Files.CONFIG.getFile().getInt("player-configuration.ready-duration") : 2);
        }

        //Written like this to prioritize ability specific ready durations over the global config
        return configurationSection.contains("ready-duration") ? configurationSection.getInt("ready-duration") :
                (FileManager.Files.CONFIG.getFile().contains("player-configuration.ready-duration") ? FileManager.Files.CONFIG.getFile().getInt("player-configuration.ready-duration") : 2);
    }

    /**
     * Checks to see if this ability is currently in a ready status
     *
     * @return {@code true} if this ability is currently in a ready status
     */
    @Override
    public boolean isReady() {
        return this.ready;
    }

    /**
     * Sets if this ability is currently in a ready status or not
     *
     * @param ready If this ability should be in a ready state or note
     */
    @Override
    public void setReady(boolean ready) {
        this.ready = ready;
    }
}
