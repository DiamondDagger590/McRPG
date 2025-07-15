package us.eunoians.mcrpg.exception.ability;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.ability.Ability;

/**
 * An exception that is thrown whenever an {@link Ability} is referenced without
 * being properly registered in the {@link us.eunoians.mcrpg.ability.AbilityRegistry}
 */
public class AbilityNotRegisteredException extends RuntimeException {

    private final NamespacedKey abilityKey;

    public AbilityNotRegisteredException(@NotNull NamespacedKey abilityKey) {
        this.abilityKey = abilityKey;
    }

    /**
     * The {@link NamespacedKey} that was used but wasn't registered
     *
     * @return The {@link NamespacedKey} that was used but wasn't registered.
     */
    @NotNull
    public NamespacedKey getAbilityKey() {
        return abilityKey;
    }

    @Override
    public String getMessage() {
        return String.format("Ability with key %s was referenced but is not registered.", getAbilityKey().getKey());
    }
}
