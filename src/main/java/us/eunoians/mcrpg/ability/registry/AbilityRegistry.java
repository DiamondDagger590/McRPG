package us.eunoians.mcrpg.ability.registry;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.Ability;
import us.eunoians.mcrpg.ability.BaseAbility;
import us.eunoians.mcrpg.ability.creation.AbilityCreationData;

import java.util.HashMap;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

/**
 * The central {@link AbilityRegistry} for all {@link us.eunoians.mcrpg.McRPG} abilities!
 *
 * @author OxKitsune
 */
public class AbilityRegistry {

    /**
     * A map that contains all registered abilities.
     */
    private final HashMap<NamespacedKey, Function<AbilityCreationData, ? extends BaseAbility>> registeredAbilities;

    /**
     * Construct a new {@link AbilityRegistry}
     */
    public AbilityRegistry() {
        this.registeredAbilities = new HashMap<>();
    }

    /**
     * Register an ability to the {@link AbilityRegistry}.
     *
     * @param key         the id of the ability
     * @param constructor the implementation of the ability itself.
     */
    public void registerAbility(@NotNull NamespacedKey key, @NotNull Function<AbilityCreationData, ? extends BaseAbility> constructor) {
        if (getAbility(key).isPresent()) {
            throw new IllegalArgumentException("An ability with id: \"" + key.toString() + "\" is already registered!");
        }
        registeredAbilities.put(key, constructor);
    }

    /**
     * Get the registered {@link Ability} using the ability id (as {@link NamespacedKey} key).
     *
     * @param abilityKey the key of the skill
     * @return an {@link Optional} containing the {@link Ability}
     */
    public Optional<Function<AbilityCreationData, ? extends BaseAbility>> getAbility(@NotNull NamespacedKey abilityKey) {
        if (!registeredAbilities.containsKey(abilityKey)) {
            return Optional.empty();
        }
        return Optional.of(registeredAbilities.get(abilityKey));
    }

    /**
     * Create a new {@link Ability} instance using the specified {@link AbilityCreationData}.
     *
     * @param abilityKey   the id of the ability
     * @param creationData the creation data for the ability
     * @return the instantiated {@link Ability}
     * @throws IllegalArgumentException whenever the specified {@code abilityKey} isn't a valid ability!
     */
    @NotNull
    public <T extends BaseAbility> T createAbility(@NotNull NamespacedKey abilityKey, @NotNull AbilityCreationData creationData) {
        Function<AbilityCreationData, ? extends BaseAbility> constructor = getAbility(abilityKey).orElse(null);
        if (constructor == null) {
            throw new IllegalArgumentException("An ability with id: " + abilityKey.toString() + " doesn't exist!");
        }

        // This is kind of disgusting, need to figure out a way to prevent this
        T ability = (T) constructor.apply(creationData);
        ability.registerListeners(McRPG.getInstance());
        return ability;
    }

    /**
     * Gets the {@link Set} of all registered {@link NamespacedKey}s
     *
     * @return The {@link Set} of all registered {@link NamespacedKey}s
     */
    @NotNull
    public Set<NamespacedKey> getAllRegisteredAbilityKeys() {
        return this.registeredAbilities.keySet();
    }
}
