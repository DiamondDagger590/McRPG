package us.eunoians.mcrpg.ability.impl.swords.bleed;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.*;
import us.eunoians.mcrpg.ability.configurable.ConfigurableAbilityDisplayItem;
import us.eunoians.mcrpg.ability.configurable.ConfigurableEnableableAbility;
import us.eunoians.mcrpg.ability.creation.AbilityCreationData;
import us.eunoians.mcrpg.annotation.AbilityIdentifier;
import us.eunoians.mcrpg.api.AbilityHolder;
import us.eunoians.mcrpg.api.error.AbilityConfigurationNotFoundException;
import us.eunoians.mcrpg.api.event.ability.swords.BleedActivateEvent;
import us.eunoians.mcrpg.api.manager.BleedManager;
import us.eunoians.mcrpg.util.configuration.FileManager;
import us.eunoians.mcrpg.util.parser.Parser;

import java.util.Collections;
import java.util.List;

/**
 * This ability is an {@link DefaultAbility} that activates when a {@link org.bukkit.entity.LivingEntity} attacks
 * another {@link org.bukkit.entity.LivingEntity}. This {@link Ability} will deal damage over time with a few modifiers
 */
@AbilityIdentifier(id = "bleed", abilityCreationData = BleedCreationData.class)
public class Bleed extends BaseAbility implements DefaultAbility, ToggleableAbility, ConfigurableEnableableAbility,
        ConfigurableAbilityDisplayItem {

    private static final String BLEED_CHANCE_EQUATION_KEY = "bleed-chance-activation";
    private static final String BLEED_FREQUENCY_KEY = "frequency";
    private static final String BLEED_FREQUENCY_TICKS_KEY = "frequency-ticks";
    private static final String BLEED_CYCLES_KEY = "cycles";
    private static final String BLEED_DAMAGE_KEY = "base-damage";

    /**
     * Represents whether the ability is toggled on or off
     */
    private boolean toggled = false;

    /**
     * This assumes that you will be passing in {@link BleedCreationData} and will attempt sanitization.
     *
     * @param abilityCreationData The {@link BleedCreationData} is used to create this {@link Ability}
     */
    public Bleed(@NotNull AbilityCreationData abilityCreationData) {
        super(abilityCreationData);

        if (abilityCreationData instanceof BleedCreationData) {
            this.toggled = ((BleedCreationData) abilityCreationData).isToggled();
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
    public List<Listener> createListeners() {
        return Collections.singletonList(new BleedListener());
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
     * Gets the {@link BleedCreationData} that creates this {@link Ability}.
     *
     * @return The {@link BleedCreationData} that creates this {@link Ability}
     */
    @Override
    public @NotNull BleedCreationData getAbilityCreationData() {
        return (BleedCreationData) super.getAbilityCreationData();
    }

    /**
     * @param activator    The {@link LivingEntity} that is activating this {@link Ability}
     * @param optionalData Any objects that should be passed in. It is up to the implementation of the
     *                     ability to sanitize this input but this is here as there is no way to allow a
     *                     generic activation method without providing access for all types of ability
     */
    @Override
    public void activate(AbilityHolder activator, Object... optionalData) {

        if (optionalData.length > 0 && optionalData[0] instanceof LivingEntity) {
            LivingEntity target = (LivingEntity) optionalData[0];

            int frequencyInTicks;
            int cycles;
            int damagePerCycle;

            try {
                ConfigurationSection abilityConfiguration = getAbilityConfigurationSection();
                frequencyInTicks = abilityConfiguration.contains(BLEED_FREQUENCY_KEY) ? abilityConfiguration.getInt(BLEED_FREQUENCY_KEY) * 20 : abilityConfiguration.getInt(BLEED_FREQUENCY_TICKS_KEY, 40);
                cycles = abilityConfiguration.getInt(BLEED_CYCLES_KEY, 3);
                damagePerCycle = abilityConfiguration.getInt(BLEED_DAMAGE_KEY, 1);

            } catch (AbilityConfigurationNotFoundException e) {
                e.printStackTrace();
                return;
            }

            BleedManager bleedManager = McRPG.getInstance().getBleedManager();

            BleedActivateEvent bleedActivateEvent = new BleedActivateEvent(activator, this, target,
                    frequencyInTicks, damagePerCycle, cycles, false, 0);

            Bukkit.getPluginManager().callEvent(bleedActivateEvent);
            if (!bleedActivateEvent.isCancelled()) {
                bleedManager.startBleed(activator, target, bleedActivateEvent.getCycleFrequencyInTicks(), bleedActivateEvent.getDamagePerCycle(),
                        bleedActivateEvent.getAmountOfCycles(), bleedActivateEvent.isRestoreHealth(), bleedActivateEvent.getHealthToRestore());
            }
        }
    }

    /**
     * Gets the {@link Parser} that represents the equation needed to activate this ability.
     * <p>
     * Provided that there is an invalid equation offered in the configuration file, the equation will
     * always result in 0.
     *
     * @return The {@link Parser} that represents the equation needed to activate this ability
     */
    @Override
    public @NotNull Parser getActivationEquation() {

        try {
            return new Parser(getAbilityConfigurationSection().getString(BLEED_CHANCE_EQUATION_KEY));
        } catch (AbilityConfigurationNotFoundException e) {
            e.printStackTrace();
            return new Parser("");
        }
    }

    /**
     * This method checks to see if the {@link ToggleableAbility} is currently toggled on
     *
     * @return True if the {@link ToggleableAbility} is currently toggled on
     */
    @Override
    public boolean isToggled() {
        return this.toggled;
    }

    /**
     * This method inverts the current toggled state of the ability and returns the result.
     * <p>
     * This is more of a lazy way of calling {@link #setToggled(boolean)} without also needing to call
     * {@link #isToggled()} to invert
     *
     * @return The stored result of the inverted version of {@link #isToggled()}
     */
    @Override
    public boolean toggle() {
        this.toggled = !this.toggled;
        return isToggled();
    }

    /**
     * This method sets the toggled status of the ability
     *
     * @param toggled True if the ability should be toggled on
     */
    @Override
    public void setToggled(boolean toggled) {
        this.toggled = toggled;
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

        ConfigurationSection configurationSection = getAbilityConfigurationFile().getConfigurationSection("bleed-config");

        if (configurationSection == null) {
            throw new AbilityConfigurationNotFoundException("Configuration section known as: 'bleed-config' is missing from the " + FileManager.Files.SWORDS_CONFIG.getFileName() + " file.", getAbilityID());
        }
        return configurationSection;
    }
}
