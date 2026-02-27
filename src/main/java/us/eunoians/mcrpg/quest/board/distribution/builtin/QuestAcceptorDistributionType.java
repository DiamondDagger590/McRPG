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
 * A distribution type that resolves exclusively to the player who accepted the scoped quest.
 * Designed for "leader bonus" rewards — powerful items or privileges that go to the player
 * who initiated the quest on behalf of their group.
 * <p>
 * For non-scoped quests (where acceptor UUID is null), resolves to an empty set.
 */
public final class QuestAcceptorDistributionType implements RewardDistributionType {

    public static final NamespacedKey KEY = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "quest_acceptor");

    @NotNull
    @Override
    public NamespacedKey getKey() {
        return KEY;
    }

    @NotNull
    @Override
    public Set<UUID> resolve(@NotNull ContributionSnapshot snapshot,
                             @NotNull DistributionTierConfig tier) {
        UUID acceptor = snapshot.questAcceptorUUID();
        if (acceptor == null) {
            return Set.of();
        }
        return Set.of(acceptor);
    }

    @NotNull
    @Override
    public Optional<NamespacedKey> getExpansionKey() {
        return Optional.of(McRPGExpansion.EXPANSION_KEY);
    }
}
