package us.eunoians.mcrpg.quest.board.distribution;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.quest.reward.QuestRewardType;
import us.eunoians.mcrpg.util.McRPGMethods;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RewardDistributionGranterTest extends McRPGBaseTest {

    private static final NamespacedKey QUEST_KEY = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "test_quest");

    static class TrackingRewardType implements QuestRewardType {

        static final NamespacedKey KEY = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "tracking_reward");
        static final List<UUID> GRANTED_TO = new ArrayList<>();

        private final long amount;

        TrackingRewardType(long amount) {
            this.amount = amount;
        }

        @Override
        public @org.jetbrains.annotations.NotNull NamespacedKey getKey() {
            return KEY;
        }

        @Override
        public @org.jetbrains.annotations.NotNull QuestRewardType parseConfig(@org.jetbrains.annotations.NotNull Section section) {
            return this;
        }

        @Override
        public void grant(@org.jetbrains.annotations.NotNull Player player) {
            GRANTED_TO.add(player.getUniqueId());
        }

        @Override
        public @org.jetbrains.annotations.NotNull Map<String, Object> serializeConfig() {
            return Map.of("amount", amount);
        }

        @Override
        public @org.jetbrains.annotations.NotNull QuestRewardType fromSerializedConfig(@org.jetbrains.annotations.NotNull Map<String, Object> config) {
            return this;
        }

        @Override
        public @org.jetbrains.annotations.NotNull Optional<NamespacedKey> getExpansionKey() {
            return Optional.empty();
        }

        @Override
        public @org.jetbrains.annotations.NotNull QuestRewardType withAmountMultiplier(double multiplier) {
            return new TrackingRewardType(Math.max(1, (long) (amount * multiplier)));
        }
    }

    @DisplayName("Online player receives reward immediately via grant()")
    @Test
    void onlinePlayer_rewardGrantedImmediately() {
        TrackingRewardType.GRANTED_TO.clear();
        PlayerMock player = server.addPlayer();
        UUID playerUUID = player.getUniqueId();

        var reward = new TrackingRewardType(100);
        Map<UUID, List<QuestRewardType>> resolved = Map.of(playerUUID, List.of(reward));

        RewardDistributionGranter.grant(resolved, QUEST_KEY);

        assertEquals(1, TrackingRewardType.GRANTED_TO.size());
        assertEquals(playerUUID, TrackingRewardType.GRANTED_TO.get(0));
    }

    @DisplayName("Empty rewards map results in no-op")
    @Test
    void emptyRewardsMap_noOp() {
        TrackingRewardType.GRANTED_TO.clear();
        Map<UUID, List<QuestRewardType>> resolved = Map.of();

        RewardDistributionGranter.grant(resolved, QUEST_KEY);

        assertTrue(TrackingRewardType.GRANTED_TO.isEmpty());
    }

    @DisplayName("Player with empty reward list results in no grant call")
    @Test
    void playerWithEmptyRewardList_noGrant() {
        TrackingRewardType.GRANTED_TO.clear();
        PlayerMock player = server.addPlayer();

        Map<UUID, List<QuestRewardType>> resolved = Map.of(player.getUniqueId(), List.of());

        RewardDistributionGranter.grant(resolved, QUEST_KEY);

        assertTrue(TrackingRewardType.GRANTED_TO.isEmpty());
    }

    @DisplayName("Multiple rewards for one player are all granted")
    @Test
    void multipleRewards_allGranted() {
        TrackingRewardType.GRANTED_TO.clear();
        PlayerMock player = server.addPlayer();
        UUID playerUUID = player.getUniqueId();

        var r1 = new TrackingRewardType(100);
        var r2 = new TrackingRewardType(200);
        var r3 = new TrackingRewardType(300);
        Map<UUID, List<QuestRewardType>> resolved = Map.of(playerUUID, List.of(r1, r2, r3));

        RewardDistributionGranter.grant(resolved, QUEST_KEY);

        assertEquals(3, TrackingRewardType.GRANTED_TO.size());
        assertTrue(TrackingRewardType.GRANTED_TO.stream().allMatch(uuid -> uuid.equals(playerUUID)));
    }

    @DisplayName("Multiple online players each receive their own rewards")
    @Test
    void multipleOnlinePlayers_eachReceivesOwn() {
        TrackingRewardType.GRANTED_TO.clear();
        PlayerMock player1 = server.addPlayer();
        PlayerMock player2 = server.addPlayer();

        var reward = new TrackingRewardType(500);
        Map<UUID, List<QuestRewardType>> resolved = Map.of(
                player1.getUniqueId(), List.of(reward),
                player2.getUniqueId(), List.of(reward)
        );

        RewardDistributionGranter.grant(resolved, QUEST_KEY);

        assertEquals(2, TrackingRewardType.GRANTED_TO.size());
        assertTrue(TrackingRewardType.GRANTED_TO.contains(player1.getUniqueId()));
        assertTrue(TrackingRewardType.GRANTED_TO.contains(player2.getUniqueId()));
    }
}
