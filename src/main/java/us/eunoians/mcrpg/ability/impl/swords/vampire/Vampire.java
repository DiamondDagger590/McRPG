package us.eunoians.mcrpg.ability.impl.swords.vampire;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.Ability;
import us.eunoians.mcrpg.ability.ConfigurableAbility;
import us.eunoians.mcrpg.ability.configurable.ConfigurableBaseAbility;
import us.eunoians.mcrpg.ability.creation.AbilityCreationData;
import us.eunoians.mcrpg.annotation.AbilityIdentifier;
import us.eunoians.mcrpg.api.AbilityHolder;
import us.eunoians.mcrpg.api.Methods;
import us.eunoians.mcrpg.api.error.AbilityConfigurationNotFoundException;
import us.eunoians.mcrpg.api.event.ability.swords.BleedActivateEvent;
import us.eunoians.mcrpg.api.event.ability.swords.VampireActivateEvent;
import us.eunoians.mcrpg.util.configuration.FileManager;

import java.util.Collections;
import java.util.List;

/**
 * This ability restores health each bleed cycle to the {@link AbilityHolder} that caused the {@link us.eunoians.mcrpg.ability.impl.swords.bleed.Bleed}
 *
 * @author DiamondDagger590
 */
@AbilityIdentifier(id = "vampire", abilityCreationData = VampireCreationData.class)
public class Vampire extends ConfigurableBaseAbility {

    /**
     * This assumes that the required extension of {@link AbilityCreationData}. Implementations of this will need
     * to sanitize the input.
     *
     * @param abilityCreationData The {@link AbilityCreationData} that is used to create this {@link Ability}
     */
    public Vampire(@NotNull AbilityCreationData abilityCreationData) {
        super(abilityCreationData);

        if (abilityCreationData instanceof VampireCreationData) {

            VampireCreationData vampireCreationData = (VampireCreationData) abilityCreationData;

            this.tier = vampireCreationData.getTier();
            this.unlocked = vampireCreationData.isUnlocked();
            this.toggled = vampireCreationData.isToggled();
        }
    }

    /**
     * Gets the {@link NamespacedKey} that this {@link Ability} belongs to
     *
     * @return The {@link NamespacedKey} that this {@link Ability} belongs to
     */
    @Override
    public @NotNull NamespacedKey getSkill() {
        return McRPG.getNamespacedKey("swords");
    }

    /**
     * @param activator    The {@link AbilityHolder} that is activating this {@link Ability}
     * @param optionalData Any objects that should be passed in. It is up to the implementation of the
     *                     ability to sanitize this input but this is here as there is no way to allow a
     *                     generic activation method without providing access for all types of ability
     */
    @Override
    public void activate(AbilityHolder activator, Object... optionalData) {

        if (optionalData.length > 0 && optionalData[0] instanceof BleedActivateEvent) {
            BleedActivateEvent bleedActivateEvent = (BleedActivateEvent) optionalData[0];
            ConfigurationSection configurationSection;

            try {
                configurationSection = getSpecificTierSection(getTier());
            } catch (AbilityConfigurationNotFoundException e) {
                e.printStackTrace();
                return;
            }

            double activationChance = configurationSection.getDouble("activation-chance");

            if (Methods.calculateChance(activationChance)) {
                int amountToHeal = configurationSection.getInt("amount-to-heal", 3);

                VampireActivateEvent vampireActivateEvent = new VampireActivateEvent(bleedActivateEvent.getAbilityHolder(), this, amountToHeal, bleedActivateEvent);
                Bukkit.getPluginManager().callEvent(vampireActivateEvent);

                if (!vampireActivateEvent.isCancelled()) {
                    bleedActivateEvent.setHealthToRestore(vampireActivateEvent.getAmountToHeal());
                }
            }
        }
    }

    /**
     * Abstract method that can be used to create listeners for this specific ability.
     * Note: This should only return a {@link List} of {@link Listener} objects. These shouldn't be registered yet!
     * This will be done automatically.
     *
     * @return a list of listeners for this {@link Ability}
     */
    @Override
    protected List<Listener> createListeners() {
        return Collections.singletonList(new VampireListener());
    }

    /**
     * Gets the {@link FileConfiguration} that is used to configure this {@link ConfigurableAbility}
     *
     * @return The {@link FileConfiguration} that is used to configure this {@link ConfigurableAbility}
     */
    @Override
    public @NotNull FileConfiguration getAbilityConfigurationFile() {
        return McRPG.getInstance().getFileManager().getFile(FileManager.Files.SWORDS_CONFIG);
    }

    /**
     * Gets the exact {@link ConfigurationSection} that is used to configure this {@link ConfigurableAbility}.
     *
     * @return The exact {@link ConfigurationSection} that is used to configure this {@link ConfigurableAbility}.
     */
    @Override
    public @NotNull ConfigurationSection getAbilityConfigurationSection() throws AbilityConfigurationNotFoundException {

        ConfigurationSection configurationSection = getAbilityConfigurationFile().getConfigurationSection("vampire-config");

        if (configurationSection == null) {
            throw new AbilityConfigurationNotFoundException("Configuration section known as: 'vampire-config' is missing from the " + FileManager.Files.SWORDS_CONFIG.getFileName() + " file.", getAbilityID());
        }
        return configurationSection;
    }
}
