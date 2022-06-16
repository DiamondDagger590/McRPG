package us.eunoians.mcrpg.entity;

import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Tracks all entities that are ability holders for the server.
 * <p>
 * {@link AbilityHolder AbilityHolders} stored in this tracker may be more
 * than just an {@link AbilityHolder}, such as an {@link us.eunoians.mcrpg.entity.holder.SkillHolder} in the
 * instance of a tracked {@link us.eunoians.mcrpg.entity.player.McRPGPlayer}.
 */
public class AbilityHolderTracker {

    private final McRPG mcRPG;

    private final Map<UUID, AbilityHolder> entityMap = new HashMap<>();

    public AbilityHolderTracker(@NotNull McRPG mcRPG) {
        this.mcRPG = mcRPG;
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
        return Optional.ofNullable(entityMap.get(uuid));
    }

    /**
     * Tracks the provided {@link AbilityHolder}.
     *
     * @param abilityHolder The {@link AbilityHolder} to track.
     */
    public void trackAbilityHolder(@NotNull AbilityHolder abilityHolder) {
        entityMap.put(abilityHolder.getUUID(), abilityHolder);
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
        return entityMap.containsKey(uuid);
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
        return Optional.ofNullable(entityMap.remove(uuid));
    }

}
