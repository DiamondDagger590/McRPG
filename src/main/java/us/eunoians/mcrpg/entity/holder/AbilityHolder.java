package us.eunoians.mcrpg.entity.holder;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.ability.Ability;
import us.eunoians.mcrpg.ability.attribute.AbilityAttribute;

import java.util.*;

//TODO javadoc
public class AbilityHolder {

    private final UUID uuid;
    private final Set<NamespacedKey> availableAbilities;
    private final Map<NamespacedKey, AbilityHolderAttributeRecord> abilityAttributeMap;

    public AbilityHolder(@NotNull UUID uuid) {
        this.uuid = uuid;
        this.availableAbilities = new HashSet<>();
        this.abilityAttributeMap = new HashMap<>();
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

    @NotNull
    public Optional<AbilityHolderAttributeRecord> getAbilityAttributes(@NotNull Ability ability) {
        return getAbilityAttributes(ability.getAbilityKey());
    }

    @NotNull
    public Optional<AbilityHolderAttributeRecord> getAbilityAttributes(@NotNull NamespacedKey namespacedKey) {
        return Optional.ofNullable(abilityAttributeMap.get(namespacedKey));
    }

    public void updateAbilityAttribute(@NotNull Ability ability, @NotNull AbilityAttribute<?> abilityAttribute) {
        updateAbilityAttribute(ability.getAbilityKey(), abilityAttribute);
    }

    public void updateAbilityAttribute(@NotNull NamespacedKey abilityKey, @NotNull AbilityAttribute<?> abilityAttribute) {

        AbilityHolderAttributeRecord abilityHolderAttributeRecord = abilityAttributeMap.containsKey(abilityKey)
                ? abilityAttributeMap.get(abilityKey)
                : new AbilityHolderAttributeRecord(new HashMap<>());

        abilityHolderAttributeRecord.abilityAttributesMap().put(abilityAttribute.getNamespacedKey(), abilityAttribute);
        abilityAttributeMap.put(abilityKey, abilityHolderAttributeRecord);
    }

}
