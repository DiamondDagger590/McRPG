package us.eunoians.mcrpg.quest.board.distribution.builtin;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.expansion.McRPGExpansion;
import us.eunoians.mcrpg.quest.board.distribution.ContributionSnapshot;
import us.eunoians.mcrpg.quest.board.distribution.DistributionTierConfig;
import us.eunoians.mcrpg.quest.board.distribution.RewardDistributionType;
import us.eunoians.mcrpg.util.McRPGMethods;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Returns all players who are members of the group (e.g., land members) at the time of
 * completion, regardless of contribution. Uses the {@code groupMembers} set from the
 * {@link ContributionSnapshot}.
 */
public final class MembershipDistributionType implements RewardDistributionType {

    public static final NamespacedKey KEY = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "membership");

    @NotNull
    @Override
    public NamespacedKey getKey() {
        return KEY;
    }

    @NotNull
    @Override
    public Set<UUID> resolve(@NotNull ContributionSnapshot snapshot,
                             @NotNull DistributionTierConfig tier) {
        return Set.copyOf(snapshot.groupMembers());
    }

    @NotNull
    @Override
    public Optional<NamespacedKey> getExpansionKey() {
        return Optional.of(McRPGExpansion.EXPANSION_KEY);
    }
}
