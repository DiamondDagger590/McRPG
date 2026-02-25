package us.eunoians.mcrpg.quest.board.distribution;

import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Immutable snapshot of contribution data for a given scope, passed to each
 * {@link RewardDistributionType#resolve} call. This is a pure data record with
 * no Bukkit dependency.
 *
 * @param contributions per-player contribution amounts for the relevant scope
 * @param totalProgress the sum of all contributions (or total objective progress
 *                      if untracked contributions exist)
 * @param groupMembers  the full set of group members (e.g., land trusted players),
 *                      used by the {@code MEMBERSHIP} distribution type.
 *                      For solo quests, this is just the single player.
 */
public record ContributionSnapshot(
        @NotNull Map<UUID, Long> contributions,
        long totalProgress,
        @NotNull Set<UUID> groupMembers
) {
    public ContributionSnapshot {
        contributions = Map.copyOf(contributions);
        groupMembers = Set.copyOf(groupMembers);
    }
}
