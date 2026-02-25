package us.eunoians.mcrpg.quest.board.distribution;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.expansion.content.McRPGContent;

import java.util.Set;
import java.util.UUID;

/**
 * Defines a pluggable reward distribution strategy. Given a contribution snapshot and
 * tier configuration, returns the set of player UUIDs who qualify for that tier's rewards.
 * <p>
 * Implementations are stateless — the same instance is shared across all evaluations.
 * Built-in types: {@code mcrpg:top_players}, {@code mcrpg:contribution_threshold},
 * {@code mcrpg:participated}, {@code mcrpg:membership}.
 * <p>
 * Third-party plugins can register custom types via the
 * {@link RewardDistributionTypeRegistry} and the content expansion system.
 */
public interface RewardDistributionType extends McRPGContent {

    /**
     * Gets the unique key identifying this distribution type.
     *
     * @return the namespaced key for this type
     */
    @NotNull
    NamespacedKey getKey();

    /**
     * Resolves the set of player UUIDs that qualify for rewards under this
     * distribution type, given the contribution snapshot and tier config.
     *
     * @param snapshot the contribution data for the relevant scope
     * @param tier     the tier configuration (contains type-specific params)
     * @return the set of qualifying player UUIDs
     */
    @NotNull
    Set<UUID> resolve(@NotNull ContributionSnapshot snapshot,
                      @NotNull DistributionTierConfig tier);
}
