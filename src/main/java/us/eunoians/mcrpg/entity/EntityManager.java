package us.eunoians.mcrpg.entity;

import com.diamonddagger590.mccore.pair.ImmutablePair;
import com.diamonddagger590.mccore.pair.Pair;
import com.diamonddagger590.mccore.registry.manager.Manager;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.entity.check.AlliedAttackCheck;
import us.eunoians.mcrpg.entity.check.EntityAlliedCheck;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;
import us.eunoians.mcrpg.entity.holder.QuestHolder;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Tracks all entities that are different kinds of holders and manages entity relationship
 * checks such as alliance and attack eligibility.
 * <p>
 * {@link AbilityHolder AbilityHolders} stored in this tracker may be more
 * than just an {@link AbilityHolder}, such as an {@link us.eunoians.mcrpg.entity.holder.SkillHolder} in the
 * instance of a tracked {@link us.eunoians.mcrpg.entity.player.McRPGPlayer}.
 */
public class EntityManager extends Manager<McRPG> {

    private final Map<UUID, AbilityHolder> abilityHolderMap;
    private final Map<UUID, QuestHolder> questHolderMap;
    private final Map<NamespacedKey, EntityAlliedCheck> entityAlliedFunctions;
    private final Map<NamespacedKey, AlliedAttackCheck> alliedAttackCheckFunctions;

    public EntityManager(@NotNull McRPG mcRPG) {
        super(mcRPG);
        this.abilityHolderMap = new HashMap<>();
        this.questHolderMap = new HashMap<>();
        this.entityAlliedFunctions = new HashMap<>();
        this.alliedAttackCheckFunctions = new HashMap<>();
    }

    /**
     * Gets an {@link Optional} containing the {@link AbilityHolder} associated with the
     * provided {@link UUID}
     *
     * @param uuid The {@link UUID} to get the associated {@link AbilityHolder} for
     * @return An {@link Optional} containing the {@link AbilityHolder} associated with the provided
     * {@link UUID} or an empty {@link Optional} if there is no associated {@link AbilityHolder}.
     */
    @NotNull
    public Optional<AbilityHolder> getAbilityHolder(@NotNull UUID uuid) {
        return Optional.ofNullable(abilityHolderMap.get(uuid));
    }

    /**
     * Tracks the provided {@link AbilityHolder}.
     *
     * @param abilityHolder The {@link AbilityHolder} to track.
     */
    public void trackAbilityHolder(@NotNull AbilityHolder abilityHolder) {
        abilityHolderMap.put(abilityHolder.getUUID(), abilityHolder);
    }

    /**
     * Checks to see if the provided {@link AbilityHolder} is currently tracked.
     *
     * @param abilityHolder The {@link AbilityHolder} to check.
     * @return {@code true} if the provided {@link AbilityHolder} is currently tracked.
     */
    public boolean isAbilityHolderTracked(@NotNull AbilityHolder abilityHolder) {
        return isAbilityHolderTracked(abilityHolder.getUUID());
    }

    /**
     * Checks to see if the provided {@link UUID} has an associated {@link AbilityHolder}
     * that is currently tracked.
     *
     * @param uuid The {@link UUID} to check
     * @return {@code true} if the provided {@link UUID} has an associated {@link AbilityHolder}
     * that is currently tracked.
     */
    public boolean isAbilityHolderTracked(@NotNull UUID uuid) {
        return abilityHolderMap.containsKey(uuid);
    }

    /**
     * Removes the {@link AbilityHolder} associated with the provided {@link UUID} provided an instance
     * was being tracked.
     *
     * @param uuid The {@link UUID} to remove the associated {@link AbilityHolder} for.
     * @return An {@link Optional} containing either the removed {@link AbilityHolder} or an empty {@link Optional}
     * if there was no {@link AbilityHolder} associated with the provided {@link UUID}.
     */
    @NotNull
    public Optional<AbilityHolder> removeAbilityHolder(@NotNull UUID uuid) {
        return Optional.ofNullable(abilityHolderMap.remove(uuid));
    }

    @NotNull
    public Optional<QuestHolder> getQuestHolder(@NotNull UUID uuid) {
        return Optional.ofNullable(questHolderMap.get(uuid));
    }

    public void trackQuestHolder(@NotNull QuestHolder questHolder) {
        questHolderMap.put(questHolder.getUUID(), questHolder);
    }

    public boolean isQuestHolderTracked(@NotNull QuestHolder questHolder) {
        return isQuestHolderTracked(questHolder.getUUID());
    }

    public boolean isQuestHolderTracked(@NotNull UUID uuid) {
        return questHolderMap.containsKey(uuid);
    }

    @NotNull
    public Optional<QuestHolder> removeQuestHolder(@NotNull UUID uuid) {
        return Optional.ofNullable(questHolderMap.remove(uuid));
    }

    /**
     * Register the provided {@link EntityAlliedCheck} to be checked when {@link #areEntitiesAllied(Entity, Entity)} is called.
     * <p>
     * This also registers {@link AlliedAttackCheck#DEFAULT_ALLIED_ATTACK_CHECK_FUNCTION} as the default function to prevent allies
     * from attacking each other. This behavior can be overridden by calling {@link #registerAlliedAttackCheckFunction(NamespacedKey, AlliedAttackCheck)} with
     * a different implementation.
     *
     * @param namespacedKey        The {@link NamespacedKey} to register this {@link EntityAlliedCheck} against.
     * @param entityAlliedFunction The {@link EntityAlliedCheck} to register.
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
     * @param entity1       The first {@link Entity} to check.
     * @param entity2       The second {@link Entity} to check.
     * @param namespacedKey The {@link NamespacedKey} of the allied function to check.
     * @return {@code true} if the two {@link Entity entities} are considered allies by the registered {@link EntityAlliedCheck}.
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
     * @param entity1 The first {@link Entity} to check.
     * @param entity2 The second {@link Entity} to check.
     * @return {@code true} if the two {@link Entity entities} are considered allies by any registered {@link EntityAlliedCheck EntityAlliedFunctions}.
     */
    public Pair<Boolean, Optional<NamespacedKey>> areEntitiesAllied(@NotNull Entity entity1, @NotNull Entity entity2) {
        for (NamespacedKey namespacedKey : entityAlliedFunctions.keySet()) {
            if (areEntitiesAllied(entity1, entity2, namespacedKey)) {
                return ImmutablePair.of(true, Optional.of(namespacedKey));
            }
        }
        return ImmutablePair.of(false, Optional.empty());
    }

    /**
     * Register the provided {@link AlliedAttackCheck} to be checked when {@link #areEntitiesAllied(Entity, Entity)} is called.
     *
     * @param namespacedKey             The {@link NamespacedKey} to register this {@link AlliedAttackCheck} against.
     * @param alliedAttackCheckFunction The {@link AlliedAttackCheck} to register.
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
     * @param entity1 The first {@link Entity} to check.
     * @param entity2 The second {@link Entity} to check.
     * @return {@code true} if the two {@link Entity entities} are considered allies by any registered {@link AlliedAttackCheck EntityAlliedFunctions}.
     */
    public Pair<Boolean, Optional<NamespacedKey>> shouldAlliesBeUnableToDamage(@NotNull Entity entity1, @NotNull Entity entity2) {
        for (NamespacedKey namespacedKey : alliedAttackCheckFunctions.keySet()) {
            AlliedAttackCheck alliedAttackCheckFunction = alliedAttackCheckFunctions.get(namespacedKey);
            if (areEntitiesAllied(entity1, entity2, namespacedKey) && alliedAttackCheckFunction.shouldBeUnableToDamage(entity1, entity2)) {
                return ImmutablePair.of(true, Optional.of(namespacedKey));
            }
        }
        return ImmutablePair.of(false, Optional.empty());
    }
}
