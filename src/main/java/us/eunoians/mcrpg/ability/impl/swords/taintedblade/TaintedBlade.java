package us.eunoians.mcrpg.ability.impl.swords.taintedblade;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.Ability;
import us.eunoians.mcrpg.ability.ConfigurableAbility;
import us.eunoians.mcrpg.ability.PotionEffectableAbility;
import us.eunoians.mcrpg.ability.ReadyableAbility;
import us.eunoians.mcrpg.ability.configurable.ConfigurableBaseActiveAbility;
import us.eunoians.mcrpg.ability.creation.AbilityCreationData;
import us.eunoians.mcrpg.annotation.AbilityIdentifier;
import us.eunoians.mcrpg.api.AbilityHolder;
import us.eunoians.mcrpg.api.error.AbilityConfigurationNotFoundException;
import us.eunoians.mcrpg.api.event.ability.swords.TaintedBladeActivateEvent;
import us.eunoians.mcrpg.util.configuration.FileManager;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This ability gives the user buffs along with a few debuffs for a few seconds for a spurt of strength to help end a fight
 *
 * @author DiamondDagger590
 */
@AbilityIdentifier(id = "tainted_blade", abilityCreationData = TaintedBladeCreationData.class)
public class TaintedBlade extends ConfigurableBaseActiveAbility implements PotionEffectableAbility {

    private final static Set<Material> ACTIVATION_MATERIALS = new HashSet<>();

    static {
        for (Material material : Material.values()) {
            if (material.toString().contains("_SWORD")) {
                ACTIVATION_MATERIALS.add(material);
            }
        }
    }

    /**
     * This assumes that the required extension of {@link AbilityCreationData}. Implementations of this will need
     * to sanitize the input.
     *
     * @param abilityCreationData The {@link AbilityCreationData} that is used to create this {@link Ability}
     */
    public TaintedBlade(@NotNull AbilityCreationData abilityCreationData) {
        super(abilityCreationData);

        if (abilityCreationData instanceof TaintedBladeCreationData) {
            TaintedBladeCreationData taintedBladeCreationData = (TaintedBladeCreationData) abilityCreationData;

            this.tier = taintedBladeCreationData.getTier();
            this.toggled = taintedBladeCreationData.isToggled();
            this.unlocked = taintedBladeCreationData.isUnlocked();
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

        Set<PotionEffect> potionEffects = getPotionEffects();

        TaintedBladeActivateEvent taintedBladeActivateEvent = new TaintedBladeActivateEvent(getAbilityHolder(), this, potionEffects, getCooldownDuration());
        Bukkit.getPluginManager().callEvent(taintedBladeActivateEvent);

        if (!taintedBladeActivateEvent.isCancelled()) {

            LivingEntity livingEntity = getAbilityHolder().getEntity();

            for (PotionEffect potionEffect : taintedBladeActivateEvent.getPotionEffects()) {
                livingEntity.addPotionEffect(potionEffect);
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
        return Collections.singletonList(new TaintedBladeListener());
    }

    /**
     * Gets the {@link Set} of {@link PotionEffect}s that should be given to either
     * the user of the {@link Ability} or the target of such {@link Ability}
     *
     * @return The {@link Set} of {@link PotionEffect}s
     */
    @Override
    public Set<PotionEffect> getPotionEffects() {

        ConfigurationSection configurationSection;

        try {
            configurationSection = getSpecificTierSection(getTier());
        } catch (AbilityConfigurationNotFoundException e) {
            e.printStackTrace();
            return new HashSet<>();
        }

        Set<PotionEffect> effects = new HashSet<>();

        effects.add(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, configurationSection.getInt("strength-duration", 3), 1));
        effects.add(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, configurationSection.getInt("resistance-duration", 3), 1));
        effects.add(new PotionEffect(PotionEffectType.SPEED, configurationSection.getInt("speed-duration", 3), 2));
        effects.add(new PotionEffect(PotionEffectType.HUNGER, configurationSection.getInt("hunger-duration", 3), 1));

        return effects;
    }

    /**
     * Handles parsing an {@link Event} to see if this ability should enter "ready" status.
     * <p>
     *
     * @param event The {@link Event} that needs to be parsed
     * @return {@code true} if the {@link ReadyableAbility} should enter "ready" status from this method call
     */
    @Override
    public boolean handleReadyAttempt(Event event) {

        if (!isReady() && event instanceof PlayerInteractEvent && ((PlayerInteractEvent) event).getItem() != null &&
                getActivatableMaterials().contains(((PlayerInteractEvent) event).getItem().getType())) {
            return true;
        }

        return false;
    }

    /**
     * Gets a {@link Set} of all {@link Material}s that can activate this {@link ReadyableAbility}
     *
     * @return A {@link Set} of all {@link Material}s that can activate this {@link ReadyableAbility}
     */
    @Override
    public Set<Material> getActivatableMaterials() {
        return ACTIVATION_MATERIALS;
    }

    /**
     * Checks to see if this {@link ReadyableAbility} can be set to a ready status by interacting with a block.
     * <p>
     * If this returns {@code false}, then {@link #isValidReadyableBlock(Block)}  will not be called.
     *
     * @return {@code true} if this ability can be ready'd from interacting with a {@link org.bukkit.block.Block}
     */
    @Override
    public boolean readyFromBlock() {
        return true;
    }

    /**
     * Checks to see if this {@link ReadyableAbility} can be set to a ready status by interacting with an entity.
     * <p>
     * If this returns {@code false}, then {@link #isValidReadyableEntity(Entity)}   will not be called.
     *
     * @return {@code true} if this ability can be ready'd from interacting with a {@link org.bukkit.entity.Entity}
     */
    @Override
    public boolean readyFromEntity() {
        return true;
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

        ConfigurationSection configurationSection = getAbilityConfigurationFile().getConfigurationSection("tainted-blade-config");

        if (configurationSection == null) {
            throw new AbilityConfigurationNotFoundException("Configuration section known as: 'tainted-blade-config' is missing from the " + FileManager.Files.SWORDS_CONFIG.getFileName() + " file.", getAbilityID());
        }
        return configurationSection;
    }
}
