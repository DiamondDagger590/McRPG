package us.eunoians.mcrpg.quest.board.distribution;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.quest.reward.QuestRewardType;
import us.eunoians.mcrpg.util.McRPGMethods;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies {@link RewardDistributionGranter} isolates a throwing reward so the remaining rewards for
 * the same online player are still granted.
 */
public class RewardDistributionGranterFaultIsolationTest extends McRPGBaseTest {

    @Test
    @DisplayName("a throwing reward does not abort the remaining rewards for an online player")
    void grant_isolatesThrowingReward_forOnlinePlayer() {
        Player player = server.addPlayer();
        RewardDistributionGranter granter = new RewardDistributionGranter(McRPG.getInstance());

        RecordingReward good = new RecordingReward(new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "good"));
        ThrowingReward bad = new ThrowingReward(new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "bad"));

        // The throwing reward is granted first so that, without isolation, the good reward would be skipped.
        Map<UUID, List<QuestRewardType>> resolved = Map.of(player.getUniqueId(), List.of(bad, good));

        granter.grant(resolved, new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "test_quest"));

        assertTrue(good.wasGranted(), "The reward after the throwing one must still be granted");
    }

    /** Reward that records whether its grant ran. */
    private static class RecordingReward implements QuestRewardType {
        private final NamespacedKey key;
        private boolean granted;

        RecordingReward(@NotNull NamespacedKey key) {
            this.key = key;
        }

        boolean wasGranted() {
            return granted;
        }

        @Override
        public @NotNull NamespacedKey getKey() {
            return key;
        }

        @Override
        public @NotNull QuestRewardType parseConfig(@NotNull Section section) {
            return this;
        }

        @Override
        public void grant(@NotNull Player player) {
            granted = true;
        }

        @Override
        public @NotNull Map<String, Object> serializeConfig() {
            return Map.of();
        }

        @Override
        public @NotNull QuestRewardType fromSerializedConfig(@NotNull Map<String, Object> config) {
            return this;
        }

        @Override
        public @NotNull Optional<NamespacedKey> getExpansionKey() {
            return Optional.empty();
        }
    }

    /** Reward whose grant always throws. */
    private static class ThrowingReward extends RecordingReward {
        ThrowingReward(@NotNull NamespacedKey key) {
            super(key);
        }

        @Override
        public void grant(@NotNull Player player) {
            throw new IllegalStateException("boom");
        }
    }
}
