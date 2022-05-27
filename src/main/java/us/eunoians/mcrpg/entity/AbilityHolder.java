package us.eunoians.mcrpg.entity;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.ability.Ability;
import us.eunoians.mcrpg.ability.attribute.AbilityAttribute;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class AbilityHolder {

    private final UUID uuid;
    private final Map<NamespacedKey, AbilityHolderAttributeRecord> abilityAttributeMap;

    public AbilityHolder(@NotNull UUID uuid) {
        this.uuid = uuid;
        this.abilityAttributeMap = new HashMap<>();
    }

    @NotNull
    public UUID getUUID() {
        return uuid;
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
