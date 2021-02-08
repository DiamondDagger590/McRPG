package us.eunoians.mcrpg.ability.impl.swords.deeperwound;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.LivingEntity;
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
import us.eunoians.mcrpg.api.event.ability.swords.DeeperWoundActivateEvent;
import us.eunoians.mcrpg.util.configuration.FileManager;

import java.util.Collections;
import java.util.List;

/**
 * This ability extends the duration of {@link us.eunoians.mcrpg.ability.impl.swords.bleed.Bleed}
 *
 * @author DiamondDagger590
 */
@AbilityIdentifier(id = "deeper_wound", abilityCreationData = DeeperWoundCreationData.class)
public class DeeperWound extends ConfigurableBaseAbility {

    /**
     * This assumes that the required extension of {@link AbilityCreationData}. Implementations of this will need
     * to sanitize the input.
     *
     * @param abilityCreationData The {@link AbilityCreationData} that is used to create this {@link Ability}
     */
    public DeeperWound(@NotNull AbilityCreationData abilityCreationData) {
        super(abilityCreationData);

        if(abilityCreationData instanceof DeeperWoundCreationData){
            DeeperWoundCreationData deeperWoundCreationData = (DeeperWoundCreationData) abilityCreationData;

            this.tier = deeperWoundCreationData.getTier();
            this.toggled = deeperWoundCreationData.isToggled();
            this.unlocked = deeperWoundCreationData.isUnlocked();
        }
    }

    /**
     * Gets the {@link DeeperWoundCreationData} that creates this {@link Ability}.
     *
     * @return The {@link DeeperWoundCreationData} that creates this {@link Ability}
     */
    @Override
    public @NotNull DeeperWoundCreationData getAbilityCreationData() {
        return (DeeperWoundCreationData) super.getAbilityCreationData();
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
     * @param activator    The {@link LivingEntity} that is activating this {@link Ability}
     * @param optionalData Any objects that should be passed in. It is up to the implementation of the
     *                     ability to sanitize this input but this is here as there is no way to allow a
     *                     generic activation method without providing access for all types of ability
     */
    @Override
    public void activate(AbilityHolder activator, Object... optionalData) {

        if(optionalData.length > 0 && optionalData[0] instanceof BleedActivateEvent) {

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
                int extraCycles = configurationSection.getInt("extra-cycles", 3);

                DeeperWoundActivateEvent deeperWoundActivateEvent = new DeeperWoundActivateEvent(bleedActivateEvent.getAbilityHolder(), this, bleedActivateEvent.getAmountOfCycles() + extraCycles, bleedActivateEvent);
                Bukkit.getPluginManager().callEvent(deeperWoundActivateEvent);

                if (!deeperWoundActivateEvent.isCancelled()) {
                    bleedActivateEvent.setAmountOfCycles(deeperWoundActivateEvent.getAmountOfCyclesToSet());
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
        return Collections.singletonList(new DeeperWoundListener());
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

        ConfigurationSection configurationSection = getAbilityConfigurationFile().getConfigurationSection("deeper-wound-config");

        if (configurationSection == null) {
            throw new AbilityConfigurationNotFoundException("Configuration section known as: 'deeper-wound-config' is missing from the " + FileManager.Files.SWORDS_CONFIG.getFileName() + " file.", getAbilityID());
        }
        return configurationSection;
    }
}
