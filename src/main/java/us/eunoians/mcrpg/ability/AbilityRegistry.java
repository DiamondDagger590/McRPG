package us.eunoians.mcrpg.ability;

import com.diamonddagger590.mccore.pair.ImmutablePair;
import com.diamonddagger590.mccore.pair.Pair;
import com.google.common.collect.ImmutableSet;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.check.AlliedAttackCheck;
import us.eunoians.mcrpg.ability.check.EntityAlliedCheck;
import us.eunoians.mcrpg.api.event.ability.AbilityRegisterEvent;
import us.eunoians.mcrpg.api.event.ability.AbilityUnregisterEvent;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;
import us.eunoians.mcrpg.exception.ability.AbilityNotRegisteredException;
import us.eunoians.mcrpg.skill.Skill;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * The central ability registry for McRPG.
 * <p>
 * This is where abilities will be registered and unregistered, as well the central location for
 * the creation of {@link AbilityData} for {@link AbilityHolder ability holders}.
 */
public class AbilityRegistry {

    private final McRPG mcRPG;
    private final Map<NamespacedKey, Ability> abilities;
    private final Map<NamespacedKey, Set<NamespacedKey>> abilitiesWithSkills;
    private final Set<NamespacedKey> abilitiesWithoutSkills;
    //TODO find a new home for these two
    private final Map<NamespacedKey, EntityAlliedCheck> entityAlliedFunctions;
    private final Map<NamespacedKey, AlliedAttackCheck> alliedAttackCheckFunctions;

    public AbilityRegistry(@NotNull McRPG mcRPG) {
        this.mcRPG = mcRPG;
        abilities = new HashMap<>();
        abilitiesWithSkills = new HashMap<>();
        abilitiesWithoutSkills = new HashSet<>();
        entityAlliedFunctions = new HashMap<>();
        alliedAttackCheckFunctions = new HashMap<>();
    }

    /**
     * Registers the {@link Ability} for use in McRPG. This includes internal storage and registering it as a
     * listener via {@link org.bukkit.plugin.PluginManager#registerEvents(Listener, Plugin)}.
     * <p>
     * Once registered, developers can create instances of {@link AbilityData} for the {@link Ability}
     * that can then be used by {@link AbilityHolder ability holders}.
     * <p>
     * This method also calls a {@link AbilityRegisterEvent} after the ability has been registered.
     *
     * @param ability The {@link Ability} to register
     */
    public void registerAbility(@NotNull Ability ability) {
        NamespacedKey abilityKey = ability.getAbilityKey();
        abilities.put(abilityKey, ability);

        if (ability.belongsToSkill() && ability.getSkill().isPresent()) {
            NamespacedKey skillKey = ability.getSkill().get();

            Set<NamespacedKey> abilities = abilitiesWithSkills.getOrDefault(skillKey, new HashSet<>());
            abilities.add(abilityKey);
            abilitiesWithSkills.put(skillKey, abilities);
        } else {
            abilitiesWithoutSkills.add(abilityKey);
        }
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
     * Note that this does not remove {@link AbilityData} from existing {@link AbilityHolder ability holders},
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
     * Note that this does not remove {@link AbilityData} from existing {@link AbilityHolder ability holders},
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

        if (ability == null) {
            return;
        }

        if (ability.belongsToSkill() && ability.getSkill().isPresent()) {
            NamespacedKey skillKey = ability.getSkill().get();

            if (abilitiesWithSkills.containsKey(skillKey)) {
                Set<NamespacedKey> abilities = abilitiesWithSkills.get(skillKey);
                abilities.remove(abilityKey);
                if (abilities.isEmpty()) {
                    abilitiesWithSkills.remove(skillKey);
                }
            }
        } else {
            abilitiesWithoutSkills.remove(abilityKey);
        }

        Bukkit.getPluginManager().callEvent(new AbilityUnregisterEvent(ability));

    }

    /**
     * Gets an {@link Ability} instance of the provided {@link NamespacedKey}.
     * <p>
     * This method will throw a {@link AbilityNotRegisteredException} if the {@link #isAbilityRegistered(NamespacedKey)}
     * returns false for the provided {@link NamespacedKey}.
     *
     * @param abilityKey The {@link NamespacedKey} to get the {@link Ability} instance of
     * @return The {@link Ability} instance of the provided {@link NamespacedKey}
     */
    @NotNull
    public Ability getRegisteredAbility(@NotNull NamespacedKey abilityKey) {
        if (!isAbilityRegistered(abilityKey)) {
            throw new AbilityNotRegisteredException(abilityKey);
        }

        return abilities.get(abilityKey);
    }

    /**
     * Checks to see if the provided {@link Skill} has any {@link Ability Abilities} associated with it.
     *
     * @param skill The {@link Skill} to check
     * @return {@code true} if the provided {@link Skill} has any {@link Ability Abilities} associated with it.
     */
    public boolean doesSkillHaveAbilities(@NotNull Skill skill) {
        return doesSkillHaveAbilities(skill.getSkillKey());
    }

    /**
     * Checks to see if the {@link Skill} associated with the provided {@link NamespacedKey} has any
     * {@link Ability Abilities} associated wit hit.
     *
     * @param skillKey The {@link NamespacedKey} to check
     * @return {@code true} if the provided {@link NamespacedKey} has any {@link Ability Abilities} associated with it.
     */
    public boolean doesSkillHaveAbilities(@NotNull NamespacedKey skillKey) {
        return abilitiesWithSkills.containsKey(skillKey);
    }

    /**
     * Gets an {@link ImmutableSet} of {@link NamespacedKey NamespacedKeys} representing all {@link Ability Abilities}
     * belonging to the provided {@link Skill}.
     *
     * @param skill The {@link Skill} to get the {@link Ability Abilities} for
     * @return An {@link ImmutableSet} of {@link NamespacedKey NamespacedKeys} representing all {@link Ability Abilities}
     * belonging to the provided {@link Skill}.
     */
    @NotNull
    public Set<NamespacedKey> getAbilitiesBelongingToSkill(@NotNull Skill skill) {
        return getAbilitiesBelongingToSkill(skill.getSkillKey());
    }

    /**
     * Gets an {@link ImmutableSet} of {@link NamespacedKey NamespacedKeys} representing all {@link Ability Abilities}
     * belonging to the {@link Skill} associated with the provided {@link NamespacedKey}.
     *
     * @param skillKey The {@link NamespacedKey} to get the {@link Ability Abilities} for
     * @return An {@link ImmutableSet} of {@link NamespacedKey NamespacedKeys} representing all {@link Ability Abilities}
     * belonging to the provided {@link NamespacedKey}.
     */
    @NotNull
    public Set<NamespacedKey> getAbilitiesBelongingToSkill(@NotNull NamespacedKey skillKey) {
        return abilitiesWithSkills.containsKey(skillKey) ? ImmutableSet.copyOf(abilitiesWithSkills.get(skillKey)) : ImmutableSet.of();
    }

    /**
     * Gets an {@link ImmutableSet} of all {@link NamespacedKey NamespacedKeys} belonging to {@link Ability Abilities} that don't
     * have any skills associated with them.
     *
     * @return An {@link ImmutableSet} of all {@link NamespacedKey NamespacedKeys} belonging to {@link Ability Abilities} that don't
     * have any skills associated with them.
     */
    @NotNull
    public Set<NamespacedKey> getAbilitiesWithoutSkills() {
        return ImmutableSet.copyOf(abilitiesWithoutSkills);
    }

    /**
     * Gets an {@link ImmutableSet} of all {@link NamespacedKey NamespacedKeys} belonging to all registered {@link Ability Abilities}.
     *
     * @return An {@link ImmutableSet} of all {@link NamespacedKey NamespacedKeys} belonging to all registered {@link Ability Abilities}.
     */
    @NotNull
    public Set<NamespacedKey> getAllAbilities() {
        return ImmutableSet.copyOf(abilities.keySet());
    }

    /**
     * Register the provided {@link EntityAlliedCheck} to be checked when {@link #areEntitiesAllied(Entity, Entity)} is called.
     * <p>
     * This also registers {@link AlliedAttackCheck#DEFAULT_ALLIED_ATTACK_CHECK_FUNCTION} as the default function to prevent allies
     * from attacking each other. This behavior can be overridden by calling {@link #registerAlliedAttackCheckFunction(NamespacedKey, AlliedAttackCheck)} with
     * a different implementation.
     *
     * @param namespacedKey        The {@link NamespacedKey} to register this {@link EntityAlliedCheck} against
     * @param entityAlliedFunction The {@link EntityAlliedCheck} to register
     */
    public void registerEntityAlliedFunction(@NotNull NamespacedKey namespacedKey, @NotNull EntityAlliedCheck entityAlliedFunction) {
        entityAlliedFunctions.put(namespacedKey, entityAlliedFunction);
        registerAlliedAttackCheckFunction(namespacedKey, AlliedAttackCheck.DEFAULT_ALLIED_ATTACK_CHECK_FUNCTION);
    }

    /**
     * Checks to see if the two provided {@link Entity entities} are allies or not using registered {@link EntityAlliedCheck EntityAlliedFunctions}.
     * <p>
     * This allows 3rd party plugins to anonymously register handling for their specific definition of what an "ally" is.
     * <p>
     * The order of the two entities should not matter as well.
     *
     * @param entity1       The first {@link Entity} to check
     * @param entity2       The second {@link Entity} to check
     * @param namespacedKey The {@link NamespacedKey} of the allied function to check
     * @return {@code true} if the two {@link Entity entities} are considered allies by any registered {@link EntityAlliedCheck EntityAlliedFunctions}.
     */
    public boolean areEntitiesAllied(@NotNull Entity entity1, @NotNull Entity entity2, @NotNull NamespacedKey namespacedKey) {

        if (entityAlliedFunctions.containsKey(namespacedKey)) {
            return entityAlliedFunctions.get(namespacedKey).areAllies(entity1, entity2);
        }

        return false;
    }

    /**
     * Checks to see if the two provided {@link Entity entities} are allies or not using registered {@link EntityAlliedCheck EntityAlliedFunctions}.
     * <p>
     * This allows 3rd party plugins to anonymously register handling for their specific definition of what an "ally" is.
     * <p>
     * The order of the two entities should not matter as well.
     *
     * @param entity1 The first {@link Entity} to check
     * @param entity2 The second {@link Entity} to check
     * @return {@code true} if the two {@link Entity entities} are considered allies by any registered {@link EntityAlliedCheck EntityAlliedFunctions}.
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
     * Register the provided {@link AlliedAttackCheck} to be checked when {@link #areEntitiesAllied(Entity, Entity)} is called
     *
     * @param alliedAttackCheckFunction The {@link AlliedAttackCheck} to register
     */
    public void registerAlliedAttackCheckFunction(@NotNull NamespacedKey namespacedKey, @NotNull AlliedAttackCheck alliedAttackCheckFunction) {
        alliedAttackCheckFunctions.put(namespacedKey, alliedAttackCheckFunction);
    }

    /**
     * Checks to see if the two provided {@link Entity entities} should be unable to attack each other, assuming {@link #areEntitiesAllied(Entity, Entity)} returns
     * {@code true}. This first checks that {@link #areEntitiesAllied(Entity, Entity)} returns {@code true} before proceeding to check the matching {@link AlliedAttackCheck}.
     * <p>
     * This allows 3rd party plugins to anonymously register handling for their specific definition for when allies should be unable to attack each other.
     * <p>
     * The order of the two entities should not matter as well.
     *
     * @param entity1 The first {@link Entity} to check
     * @param entity2 The second {@link Entity} to check
     * @return {@code true} if the two {@link Entity entities} are considered allies by any registered {@link AlliedAttackCheck EntityAlliedFunctions}.
     */
    public Pair<Boolean, Optional<NamespacedKey>> shouldAlliesBeUnableToDamage(@NotNull Entity entity1, @NotNull Entity entity2) {

        for (NamespacedKey namespacedKey : alliedAttackCheckFunctions.keySet()) {

            AlliedAttackCheck alliedAttackCheckFunction = alliedAttackCheckFunctions.get(namespacedKey);

            //Require the entities to currently be allies and them to be unable to damage each other
            if (areEntitiesAllied(entity1, entity2, namespacedKey) && alliedAttackCheckFunction.shouldBeUnableToDamage(entity1, entity2)) {
                return ImmutablePair.of(true, Optional.of(namespacedKey));
            }
        }

        return ImmutablePair.of(false, Optional.empty());
    }
}
