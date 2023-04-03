package us.eunoians.mcrpg.entity.holder;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.ability.Ability;
import us.eunoians.mcrpg.ability.AbilityData;

import java.util.*;

/**
 * A "holder" is a representation of an {@link org.bukkit.entity.Entity} for McRPG.
 * <p>
 * An AbilityHolder is the basic level of holders, where an entity that is this holder type
 * can have {@link Ability abilities} associated with them. This is designed to allow for non-player
 * entities to have abilities, which wasn't possible in the previous iteration of this plugin.
 * <p>
 * To take things further, there are the classes {@link LoadoutHolder} and {@link SkillHolder}. A {@link SkillHolder}
 * is one that has a levelable skill which usually can unlock more abilities. There is also {@link LoadoutHolder}
 * which restricts the abilities that an entity can use. By nature, an AbilityHolder can use any number of abilities
 * that they have. If an ability holder is also a loadout holder, then they are restricted to using only the abilities in
 * their loadout.
 */
public class AbilityHolder {

    private final UUID uuid;
    private final Set<NamespacedKey> availableAbilities;
    private final Map<NamespacedKey, AbilityData> abilityDataMap;

    public AbilityHolder(@NotNull UUID uuid) {
        this.uuid = uuid;
        this.availableAbilities = new HashSet<>();
        this.abilityDataMap = new HashMap<>();
    }

    /**
     * Gets the {@link UUID} of this holder
     *
     * @return The {@link UUID} of this holder
     */
    @NotNull
    public UUID getUUID() {
        return uuid;
    }

    /**
     * Adds the {@link Ability} as an available ability for this holder to use.
     *
     * @param ability The {@link Ability} to add as an available ability for this holder to use.
     */
    public void addAvailableAbility(@NotNull Ability ability) {
        addAvailableAbility(ability.getAbilityKey());
    }

    /**
     * Adds the {@link NamespacedKey} as an available ability for this holder to use
     *
     * @param abilityKey The {@link Ability} to add as an available ability for this holder to use.
     */
    public void addAvailableAbility(@NotNull NamespacedKey abilityKey) {
        availableAbilities.add(abilityKey);
    }

    /**
     * Removes the {@link Ability} so it is no longer an available ability for this holder to use.
     *
     * @param ability The {@link Ability} to remove so that it is no longer an available ability for this
     *                holder to use.
     */
    public void removeAvailableAbility(@NotNull Ability ability) {
        removeAvailableAbility(ability.getAbilityKey());
    }

    /**
     * Removes the {@link NamespacedKey} so it is no longer an available ability for this holder to use.
     *
     * @param abilityKey The {@link NamespacedKey} to remove so that it is no longer an available ability for this
     *                   holder to use.
     */
    public void removeAvailableAbility(@NotNull NamespacedKey abilityKey) {
        availableAbilities.remove(abilityKey);
    }

    /**
     * Checks to see if the provided {@link Ability} is available for this holder to use.
     *
     * @param ability The {@link Ability} to check
     * @return {@code true} if the provided {@link Ability} is available for this holder to use
     */
    public boolean isAbilityAvailable(@NotNull Ability ability) {
        return isAbilityAvailable(ability.getAbilityKey());
    }

    /**
     * Checks to see if the provided {@link NamespacedKey} is available for this holder to use.
     *
     * @param abilityKey The {@link NamespacedKey} to check
     * @return {@code true} if the provided {@link NamespacedKey} is available for this holder to use
     */
    public boolean isAbilityAvailable(@NotNull NamespacedKey abilityKey) {
        return availableAbilities.contains(abilityKey);
    }

    /**
     * Checks to see if this ability holder has {@link AbilityData} that matches
     * the provided {@link Ability}
     *
     * @param ability The {@link Ability} to check for
     * @return {@code true} if the provided {@link Ability} has {@link AbilityData}
     * associated with it for this holder.
     */
    public boolean hasAbilityData(@NotNull Ability ability) {
        return hasAbilityData(ability.getAbilityKey());
    }

    /**
     * Checks to see if this ability holder has {@link AbilityData} that matches
     * the provided {@link NamespacedKey}
     *
     * @param namespacedKey The {@link NamespacedKey} to check for
     * @return {@code true} if the provided {@link NamespacedKey} has {@link AbilityData}
     * associated with it for this holder.
     */
    public boolean hasAbilityData(@NotNull NamespacedKey namespacedKey) {
        return abilityDataMap.containsKey(namespacedKey);
    }

    /**
     * Gets the {@link AbilityData} associated with the provided {@link Ability}
     *
     * @param ability The {@link Ability} to get the associated {@link AbilityData} for
     * @return An {@link Optional} that will be empty if no match is found, or will contain
     * the {@link AbilityData} that is associated with the provided {@link Ability}
     */
    @NotNull
    public Optional<AbilityData> getAbilityData(@NotNull Ability ability) {
        return getAbilityData(ability.getAbilityKey());
    }

    /**
     * Gets the {@link AbilityData} associated with the provided {@link NamespacedKey}
     *
     * @param namespacedKey The {@link NamespacedKey} to get the associated {@link AbilityData} for
     * @return An {@link Optional} that will be empty if no match is found, or will contain
     * the {@link AbilityData} that is associated with the provided {@link NamespacedKey}
     */
    @NotNull
    public Optional<AbilityData> getAbilityData(@NotNull NamespacedKey namespacedKey) {
        return Optional.ofNullable(abilityDataMap.get(namespacedKey));
    }

    /**
     * Adds the provided {@link AbilityData} to this ability holder
     *
     * @param abilityData The {@link AbilityData} to add to this ability holder
     */
    public void addAbilityData(@NotNull AbilityData abilityData) {
        abilityDataMap.put(abilityData.getAbilityKey(), abilityData);
    }

    /**
     * Removes the {@link AbilityData} for the associated {@link Ability} provided
     * that there is any stored in this holder.
     *
     * @param ability The {@link Ability} to remove the associated {@link AbilityData}
     *                for.
     */
    public void removeAbilityData(@NotNull Ability ability) {
        removeAbilityData(ability.getAbilityKey());
    }

    /**
     * Removes the {@link AbilityData} for the associated {@link NamespacedKey} provided
     * that there is any stored in this holder.
     *
     * @param namespacedKey The {@link NamespacedKey} to remove the associated {@link AbilityData}
     *                      for.
     */
    public void removeAbilityData(@NotNull NamespacedKey namespacedKey) {
        abilityDataMap.remove(namespacedKey);
    }
}
