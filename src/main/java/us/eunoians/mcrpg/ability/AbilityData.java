package us.eunoians.mcrpg.ability;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.ability.attribute.AbilityAttribute;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class AbilityData {

    private final NamespacedKey abilityKey;
    private final Map<NamespacedKey, AbilityAttribute<?>> abilityAttributes;

    AbilityData(@NotNull NamespacedKey abilityKey, @NotNull AbilityAttribute<?>... abilityAttributes){
        this.abilityKey = abilityKey;
        this.abilityAttributes = new HashMap<>();

        for(AbilityAttribute<?> abilityAttribute : abilityAttributes){
            addAttribute(abilityAttribute);
        }
    }

    @NotNull
    public NamespacedKey getAbilityKey(){
        return abilityKey;
    }

    public boolean doesAbilityHaveAttribute(@NotNull AbilityAttribute<?> abilityAttribute){
        return doesAbilityHaveAttribute(abilityAttribute.getNamespacedKey());
    }

    public boolean doesAbilityHaveAttribute(@NotNull NamespacedKey namespacedKey){
        return abilityAttributes.containsKey(namespacedKey);
    }

    @NotNull
    public Optional<AbilityAttribute<?>> getAbilityAttribute(@NotNull NamespacedKey namespacedKey){
        return Optional.ofNullable(abilityAttributes.get(namespacedKey));
    }

    public void addAttribute(@NotNull AbilityAttribute<?> abilityAttribute){
        abilityAttributes.put(abilityAttribute.getNamespacedKey(), abilityAttribute);
    }

    public void removeAttribute(@NotNull AbilityAttribute<?> abilityAttribute){
        removeAttribute(abilityAttribute.getNamespacedKey());
    }

    public void removeAttribute(@NotNull NamespacedKey namespacedKey){
        abilityAttributes.remove(namespacedKey);
    }
}
