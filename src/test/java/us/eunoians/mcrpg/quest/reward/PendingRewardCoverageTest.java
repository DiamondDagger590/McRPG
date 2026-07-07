package us.eunoians.mcrpg.quest.reward;

import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PendingRewardCoverageTest extends McRPGBaseTest {

    private static final UUID REWARD_ID = UUID.randomUUID();
    private static final UUID PLAYER_UUID = UUID.randomUUID();
    private static final NamespacedKey REWARD_TYPE_KEY = new NamespacedKey("mcrpg", "experience");
    private static final NamespacedKey QUEST_KEY = new NamespacedKey("mcrpg", "test_quest");
    private static final long CREATED_AT = 1_700_000_000_000L;
    private static final long EXPIRES_AT = 1_700_086_400_000L;

    private PendingReward createDefault() {
        return new PendingReward(REWARD_ID, PLAYER_UUID, REWARD_TYPE_KEY,
                Map.of("skill", "MINING", "amount", 500), QUEST_KEY, CREATED_AT, EXPIRES_AT);
    }

    @Nested
    @DisplayName("Constructor and Getters")
    class ConstructorAndGetterTests {

        @Test
        @DisplayName("getId returns constructor value")
        void getId_returnsConstructorValue() {
            PendingReward reward = createDefault();
            assertEquals(REWARD_ID, reward.getId());
        }

        @Test
        @DisplayName("getPlayerUUID returns constructor value")
        void getPlayerUUID_returnsConstructorValue() {
            PendingReward reward = createDefault();
            assertEquals(PLAYER_UUID, reward.getPlayerUUID());
        }

        @Test
        @DisplayName("getRewardTypeKey returns constructor value")
        void getRewardTypeKey_returnsConstructorValue() {
            PendingReward reward = createDefault();
            assertEquals(REWARD_TYPE_KEY, reward.getRewardTypeKey());
        }

        @Test
        @DisplayName("getQuestKey returns constructor value")
        void getQuestKey_returnsConstructorValue() {
            PendingReward reward = createDefault();
            assertEquals(QUEST_KEY, reward.getQuestKey());
        }

        @Test
        @DisplayName("getCreatedAt returns constructor value")
        void getCreatedAt_returnsConstructorValue() {
            PendingReward reward = createDefault();
            assertEquals(CREATED_AT, reward.getCreatedAt());
        }

        @Test
        @DisplayName("getExpiresAt returns constructor value")
        void getExpiresAt_returnsConstructorValue() {
            PendingReward reward = createDefault();
            assertEquals(EXPIRES_AT, reward.getExpiresAt());
        }
    }

    @Nested
    @DisplayName("Serialized Config")
    class SerializedConfigTests {

        @Test
        @DisplayName("getSerializedConfig returns expected entries")
        void getSerializedConfig_returnsExpectedEntries() {
            PendingReward reward = createDefault();
            Map<String, Object> config = reward.getSerializedConfig();
            assertEquals("MINING", config.get("skill"));
            assertEquals(500, config.get("amount"));
        }

        @Test
        @DisplayName("getSerializedConfig returns immutable copy")
        void getSerializedConfig_returnsImmutableCopy() {
            PendingReward reward = createDefault();
            Map<String, Object> config = reward.getSerializedConfig();
            assertThrows(UnsupportedOperationException.class, () -> config.put("extra", "value"));
        }

        @Test
        @DisplayName("original map mutation does not affect pending reward")
        void originalMapMutation_doesNotAffectReward() {
            Map<String, Object> originalConfig = new HashMap<>();
            originalConfig.put("skill", "MINING");
            originalConfig.put("amount", 500);
            PendingReward reward = new PendingReward(REWARD_ID, PLAYER_UUID, REWARD_TYPE_KEY,
                    originalConfig, QUEST_KEY, CREATED_AT, EXPIRES_AT);
            originalConfig.put("extra", "should not appear");
            assertEquals(2, reward.getSerializedConfig().size());
        }

        @Test
        @DisplayName("empty config is supported")
        void emptyConfig_isSupported() {
            PendingReward reward = new PendingReward(REWARD_ID, PLAYER_UUID, REWARD_TYPE_KEY,
                    Map.of(), QUEST_KEY, CREATED_AT, EXPIRES_AT);
            assertNotNull(reward.getSerializedConfig());
            assertTrue(reward.getSerializedConfig().isEmpty());
        }
    }

    @Nested
    @DisplayName("Timestamp Semantics")
    class TimestampSemanticsTests {

        @Test
        @DisplayName("expiresAt after createdAt represents valid window")
        void expiresAfterCreated_isValidWindow() {
            PendingReward reward = createDefault();
            assertTrue(reward.getExpiresAt() > reward.getCreatedAt());
        }

        @Test
        @DisplayName("zero timestamps are preserved")
        void zeroTimestamps_arePreserved() {
            PendingReward reward = new PendingReward(REWARD_ID, PLAYER_UUID, REWARD_TYPE_KEY,
                    Map.of(), QUEST_KEY, 0L, 0L);
            assertEquals(0L, reward.getCreatedAt());
            assertEquals(0L, reward.getExpiresAt());
        }

        @Test
        @DisplayName("negative timestamps are preserved")
        void negativeTimestamps_arePreserved() {
            PendingReward reward = new PendingReward(REWARD_ID, PLAYER_UUID, REWARD_TYPE_KEY,
                    Map.of(), QUEST_KEY, -1L, -1L);
            assertEquals(-1L, reward.getCreatedAt());
            assertEquals(-1L, reward.getExpiresAt());
        }
    }
}
