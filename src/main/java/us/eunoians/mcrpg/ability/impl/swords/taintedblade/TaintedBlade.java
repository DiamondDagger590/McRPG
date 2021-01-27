package us.eunoians.mcrpg.ability.impl.swords.taintedblade;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.potion.PotionEffect;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.Ability;
import us.eunoians.mcrpg.ability.ActiveAbility;
import us.eunoians.mcrpg.ability.BaseAbility;
import us.eunoians.mcrpg.ability.PotionEffectableAbility;
import us.eunoians.mcrpg.ability.ReadyableAbility;
import us.eunoians.mcrpg.ability.TierableAbility;
import us.eunoians.mcrpg.ability.ToggleableAbility;
import us.eunoians.mcrpg.ability.UnlockableAbility;
import us.eunoians.mcrpg.ability.creation.AbilityCreationData;
import us.eunoians.mcrpg.api.AbilityHolder;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TaintedBlade extends BaseAbility implements UnlockableAbility, ToggleableAbility,
        TierableAbility, ReadyableAbility, ActiveAbility, PotionEffectableAbility {

    private final static Set<Material> ACTIVATION_MATERIALS = new HashSet<>();

    static {
        for (Material material : Material.values()) {
            if (material.toString().contains("_SWORD")) {
                ACTIVATION_MATERIALS.add(material);
            }
        }
    }

    private int tier = 0;
    private boolean toggled = false;
    private boolean unlocked = false;
    private boolean ready = false;

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
        //TODO
        return null;
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
     * Gets the amount of seconds that the "ready" status should last for this ability
     *
     * @return The amount of seconds that the "ready" status should last for this ability
     */
    @Override
    public int getReadyDurationSeconds() {
        return 5;
    }

    /**
     * Gets the tier of this {@link Ability}.
     * <p>
     * A tier of 0 represents an ability that is currently not unlocked but this should be checked by
     * {@link UnlockableAbility#isUnlocked()}.
     *
     * @return A positive zero inclusive number representing the current tier of this {@link TierableAbility}.
     */
    @Override
    public int getTier() {
        return this.tier;
    }

    /**
     * Sets the tier of this {@link TierableAbility}.
     * <p>
     * This should only accept positive zero inclusive numbers and should sanitize for them.
     *
     * @param tier A positive zero inclusive number representing the new tier of this {@link TierableAbility}
     */
    @Override
    public void setTier(int tier) {
        this.tier = tier;
    }

    /**
     * Gets the {@link ConfigurationSection} that belongs to this ability
     *
     * @param tier The tier at which to get the {@link ConfigurationSection} for
     * @return Either the {@link ConfigurationSection} mapped to the provided tier or {@code null} if invalid
     */
    @Override
    public @Nullable ConfigurationSection getTierConfigSection(int tier) {
        return null;
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
        return this.toggled;
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
     * Checks to see if the {@link UnlockableAbility} is currently unlocked or not.
     *
     * @return {@code true} if this {@link UnlockableAbility} is currently unlocked.
     */
    @Override
    public boolean isUnlocked() {
        return this.unlocked;
    }

    /**
     * Sets if this {@link UnlockableAbility} is currently unlocked or not.
     *
     * @param unlocked If this {@link UnlockableAbility} is currently unlocked or not.
     */
    @Override
    public void setUnlocked(boolean unlocked) {
        this.unlocked = unlocked;
    }
}
