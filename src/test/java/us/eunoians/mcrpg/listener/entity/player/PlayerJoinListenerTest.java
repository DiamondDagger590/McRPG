package us.eunoians.mcrpg.listener.entity.player;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.quest.reward.PendingReward;
import us.eunoians.mcrpg.quest.reward.QuestRewardType;
import us.eunoians.mcrpg.quest.reward.QuestRewardTypeRegistry;
import us.eunoians.mcrpg.util.McRPGMethods;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

/**
 * Tests the login grant loop's failure isolation: a reward whose type is unregistered or whose
 * grant throws must be retained (never added to the delete set), while successful grants are reported.
 */
public class PlayerJoinListenerTest extends McRPGBaseTest {

    private PlayerJoinListener listener;
    private Player player;
    private QuestRewardTypeRegistry registry;

    @BeforeEach
    void setUp() {
        listener = new PlayerJoinListener();
        player = mock(Player.class);
        registry = new QuestRewardTypeRegistry();
    }

    private PendingReward pending(@NotNull NamespacedKey rewardTypeKey) {
        return new PendingReward(
                UUID.randomUUID(),
                UUID.randomUUID(),
                rewardTypeKey,
                Map.of(),
                new NamespacedKey("mcrpg", "test_quest"),
                0L,
                Long.MAX_VALUE);
    }

    @Test
    @DisplayName("grantRewards returns only successfully granted ids")
    void grantRewards_returnsGrantedId_whenGrantSucceeds() {
        NamespacedKey key = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "ok_reward");
        registry.register(new RecordingRewardType(key));
        PendingReward reward = pending(key);

        Set<UUID> granted = listener.grantRewards(McRPG.getInstance(), player, List.of(reward), registry);

        assertEquals(Set.of(reward.getId()), granted);
    }

    @Test
    @DisplayName("grantRewards retains a reward whose type is not registered")
    void grantRewards_retainsReward_whenTypeUnregistered() {
        PendingReward reward = pending(new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "missing_reward"));

        Set<UUID> granted = listener.grantRewards(McRPG.getInstance(), player, List.of(reward), registry);

        // Unregistered type -> not granted, so its row id is never returned for deletion.
        assertTrue(granted.isEmpty());
    }

    @Test
    @DisplayName("grantRewards isolates a throwing reward and still grants the others")
    void grantRewards_isolatesFailure_whenGrantThrows() {
        NamespacedKey throwingKey = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "throwing_reward");
        NamespacedKey okKey = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "ok_reward");
        registry.register(new ThrowingRewardType(throwingKey));
        registry.register(new RecordingRewardType(okKey));

        PendingReward throwing = pending(throwingKey);
        PendingReward ok = pending(okKey);

        Set<UUID> granted = listener.grantRewards(McRPG.getInstance(), player, List.of(throwing, ok), registry);

        // The throwing reward is retained; the good reward is still granted.
        assertFalse(granted.contains(throwing.getId()));
        assertTrue(granted.contains(ok.getId()));
    }

    /** Reward type that grants without side effects. */
    private static class RecordingRewardType implements QuestRewardType {
        private final NamespacedKey key;

        RecordingRewardType(@NotNull NamespacedKey key) {
            this.key = key;
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

    /** Reward type whose grant always throws, simulating faulty third-party reward code. */
    private static class ThrowingRewardType extends RecordingRewardType {
        ThrowingRewardType(@NotNull NamespacedKey key) {
            super(key);
        }

        @Override
        public void grant(@NotNull Player player) {
            throw new IllegalStateException("boom");
        }
    }
}
