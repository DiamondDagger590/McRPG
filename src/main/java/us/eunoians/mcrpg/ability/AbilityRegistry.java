package us.eunoians.mcrpg.ability;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.api.event.ability.AbilityRegisterEvent;
import us.eunoians.mcrpg.api.event.ability.AbilityUnregisterEvent;
import us.eunoians.mcrpg.util.pair.ImmutablePair;
import us.eunoians.mcrpg.util.pair.Pair;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * The central ability registry for McRPG.
 * <p>
 * This is where abilities will be registered and unregistered, as well the central location for
 * the creation of {@link AbilityData} for {@link us.eunoians.mcrpg.entity.AbilityHolder ability holders}.
 */
public class AbilityRegistry {

    private final McRPG mcRPG;
    private final Map<NamespacedKey, Ability> abilities;
    //TODO find a new home for these two
    private final Map<NamespacedKey, EntityAlliedFunction> entityAlliedFunctions;
    private final Map<NamespacedKey, AlliedAttackCheckFunction> alliedAttackCheckFunctions;

    public AbilityRegistry(@NotNull McRPG mcRPG) {
        this.mcRPG = mcRPG;
        abilities = new HashMap<>();
        entityAlliedFunctions = new HashMap<>();
        alliedAttackCheckFunctions = new HashMap<>();
    }

    /**
     * Registers the {@link Ability} for use in McRPG. This includes internal storage and registering it as a
     * listener via {@link org.bukkit.plugin.PluginManager#registerEvents(Listener, Plugin)}.
     * <p>
     * Once registered, developers can create instances of {@link AbilityData} for the {@link Ability}
     * that can then be used by {@link us.eunoians.mcrpg.entity.AbilityHolder ability holders}.
     * <p>
     * This method also calls a {@link AbilityRegisterEvent} after the ability has been registered.
     *
     * @param ability The {@link Ability} to register
     */
    public void registerAbility(@NotNull Ability ability) {
        abilities.put(ability.getAbilityKey(), ability);
        Bukkit.getPluginManager().registerEvents(ability, mcRPG);

        Bukkit.getPluginManager().callEvent(new AbilityRegisterEvent(ability));
    }

    /**
     * Checks to see if the provided {@link Ability} is registered by using {@link Ability#getAbilityKey()}
     * and calling {@link #isAbilityRegistered(NamespacedKey)}.
     *
     * @param ability The {@link Ability} to check
     * @return {@code true} if the provided {@link Ability} is registered or {@code false} otherwise.
     */
    public boolean isAbilityRegistered(@NotNull Ability ability) {
        return isAbilityRegistered(ability.getAbilityKey());
    }

    /**
     * Checks to see if the provided {@link NamespacedKey} matches an {@link Ability} that is
     * currently registered.
     *
     * @param abilityKey The {@link NamespacedKey} to check
     * @return {@code true} if the provided {@link NamespacedKey} matches a registered {@link Ability} or {@code false} otherwise.
     */
    public boolean isAbilityRegistered(@NotNull NamespacedKey abilityKey) {
        return abilities.containsKey(abilityKey);
    }

    /**
     * Unregisters the provided {@link Ability} from McRPG by calling {@link #unregisterAbility(NamespacedKey)} using the
     * {@link NamespacedKey} from {@link Ability#getAbilityKey()}. This process includes removing it from local storage
     * and unregistering any listeners that the {@link Ability} contains, provided the {@link Ability} was registered already.
     * <p>
     * Note that this does not remove {@link AbilityData} from existing {@link us.eunoians.mcrpg.entity.AbilityHolder ability holders},
     * and will still allow the ability to be saved and exist in loadouts. This just prevents the ability from being loaded for future holders
     * and the ability will not be able to activate at all.
     * <p>
     * This method will also result in the calling of an {@link AbilityUnregisterEvent} after the unregistration
     * has finished ONLY if the ability was registered in the first place.
     *
     * @param ability The {@link Ability} to unregister
     */
    public void unregisterAbility(@NotNull Ability ability) {
        unregisterAbility(ability.getAbilityKey());
    }

    /**
     * Unregisters the provided {@link NamespacedKey} from McRPG. This process includes removing it from local storage
     * and unregistering any listeners that the {@link Ability} associated with this {@link NamespacedKey} contains,
     * provided the {@link Ability} was registered already.
     * <p>
     * Note that this does not remove {@link AbilityData} from existing {@link us.eunoians.mcrpg.entity.AbilityHolder ability holders},
     * and will still allow the ability to be saved and exist in loadouts. This just prevents the ability from being loaded for future holders
     * and the ability will not be able to activate at all.
     * <p>
     * This method will also result in the calling of an {@link AbilityUnregisterEvent} after the unregistration
     * has finished ONLY if the ability was registered in the first place.
     *
     * @param abilityKey The {@link NamespacedKey} to unregister
     */
    public void unregisterAbility(@NotNull NamespacedKey abilityKey) {
        Ability ability = abilities.remove(abilityKey);

        if (ability != null) {
            HandlerList.unregisterAll(ability);
            Bukkit.getPluginManager().callEvent(new AbilityUnregisterEvent(ability));
        }
    }

    /**
     * Register the provided {@link EntityAlliedFunction} to be checked when {@link #areEntitiesAllied(Entity, Entity)} is called.
     * <p>
     * This also registers {@link AlliedAttackCheckFunction#DEFAULT_ALLIED_ATTACK_CHECK_FUNCTION} as the default function to prevent allies
     * from attacking each other. This behavior can be overridden by calling {@link #registerAlliedAttackCheckFunction(NamespacedKey, AlliedAttackCheckFunction)} with
     * a different implementation.
     *
     * @param namespacedKey        The {@link NamespacedKey} to register this {@link EntityAlliedFunction} against
     * @param entityAlliedFunction The {@link EntityAlliedFunction} to register
     */
    public void registerEntityAlliedFunction(@NotNull NamespacedKey namespacedKey, @NotNull EntityAlliedFunction entityAlliedFunction) {
        entityAlliedFunctions.put(namespacedKey, entityAlliedFunction);
        registerAlliedAttackCheckFunction(namespacedKey, AlliedAttackCheckFunction.DEFAULT_ALLIED_ATTACK_CHECK_FUNCTION);
    }

    /**
     * Checks to see if the two provided {@link Entity entities} are allies or not using registered {@link EntityAlliedFunction EntityAlliedFunctions}.
     * <p>
     * This allows 3rd party plugins to anonymously register handling for their specific definition of what an "ally" is.
     * <p>
     * The order of the two entities should not matter as well.
     *
     * @param entity1 The first {@link Entity} to check
     * @param entity2 The second {@link Entity} to check
     * @return {@code true} if the two {@link Entity entities} are considered allies by any registered {@link EntityAlliedFunction EntityAlliedFunctions}.
     */
    public boolean areEntitiesAllied(@NotNull Entity entity1, @NotNull Entity entity2, @NotNull NamespacedKey namespacedKey) {

        if (entityAlliedFunctions.containsKey(namespacedKey)) {
            return entityAlliedFunctions.get(namespacedKey).areAllies(entity1, entity2);
        }

        return false;
    }

    /**
     * Checks to see if the two provided {@link Entity entities} are allies or not using registered {@link EntityAlliedFunction EntityAlliedFunctions}.
     * <p>
     * This allows 3rd party plugins to anonymously register handling for their specific definition of what an "ally" is.
     * <p>
     * The order of the two entities should not matter as well.
     *
     * @param entity1 The first {@link Entity} to check
     * @param entity2 The second {@link Entity} to check
     * @return {@code true} if the two {@link Entity entities} are considered allies by any registered {@link EntityAlliedFunction EntityAlliedFunctions}.
     */
    public Pair<Boolean, Optional<NamespacedKey>> areEntitiesAllied(@NotNull Entity entity1, @NotNull Entity entity2) {

        for (NamespacedKey namespacedKey : entityAlliedFunctions.keySet()) {

            //We don't care about any others, something considers them allies so stop early
            if (areEntitiesAllied(entity1, entity2, namespacedKey)) {
                return ImmutablePair.of(true, Optional.of(namespacedKey));
            }
        }

        return ImmutablePair.of(false, Optional.empty());
    }

    /**
     * Register the provided {@link AlliedAttackCheckFunction} to be checked when {@link #areEntitiesAllied(Entity, Entity)} is called
     *
     * @param alliedAttackCheckFunction The {@link AlliedAttackCheckFunction} to register
     */
    public void registerAlliedAttackCheckFunction(@NotNull NamespacedKey namespacedKey, @NotNull AlliedAttackCheckFunction alliedAttackCheckFunction) {
        alliedAttackCheckFunctions.put(namespacedKey, alliedAttackCheckFunction);
    }

    /**
     * Checks to see if the two provided {@link Entity entities} should be unable to attack each other, assuming {@link #areEntitiesAllied(Entity, Entity)} returns
     * {@code true}. This first checks that {@link #areEntitiesAllied(Entity, Entity)} returns {@code true} before proceeding to check the matching {@link AlliedAttackCheckFunction}.
     * <p>
     * This allows 3rd party plugins to anonymously register handling for their specific definition for when allies should be unable to attack each other.
     * <p>
     * The order of the two entities should not matter as well.
     *
     * @param entity1 The first {@link Entity} to check
     * @param entity2 The second {@link Entity} to check
     * @return {@code true} if the two {@link Entity entities} are considered allies by any registered {@link AlliedAttackCheckFunction EntityAlliedFunctions}.
     */
    public Pair<Boolean, Optional<NamespacedKey>> shouldAlliesBeUnableToDamage(@NotNull Entity entity1, @NotNull Entity entity2) {

        for (NamespacedKey namespacedKey : alliedAttackCheckFunctions.keySet()) {

            AlliedAttackCheckFunction alliedAttackCheckFunction = alliedAttackCheckFunctions.get(namespacedKey);

            //Require the entities to currently be allies and them to be unable to damage each other
            if (areEntitiesAllied(entity1, entity2, namespacedKey) && alliedAttackCheckFunction.shouldBeUnableToDamage(entity1, entity2)) {
                return ImmutablePair.of(true, Optional.of(namespacedKey));
            }
        }

        return ImmutablePair.of(false, Optional.empty());
    }
}
