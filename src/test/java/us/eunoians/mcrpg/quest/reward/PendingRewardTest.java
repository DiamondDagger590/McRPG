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
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("PendingReward")
@SuppressWarnings("deprecation")
class PendingRewardTest extends McRPGBaseTest {

    private static final UUID REWARD_ID = UUID.randomUUID();
    private static final UUID PLAYER_UUID = UUID.randomUUID();
    private static final NamespacedKey REWARD_TYPE_KEY = new NamespacedKey("mcrpg", "command");
    private static final NamespacedKey QUEST_KEY = new NamespacedKey("mcrpg", "test_quest");
    private static final long CREATED_AT = 1000L;
    private static final long EXPIRES_AT = 2000L;

    @Nested
    @DisplayName("accessors")
    class Accessors {

        @Test
        @DisplayName("getId returns the provided ID")
        void getId_returnsProvidedId() {
            PendingReward reward = createReward(Map.of("cmd", "give diamonds"));
            assertEquals(REWARD_ID, reward.getId());
        }

        @Test
        @DisplayName("getPlayerUUID returns the provided player UUID")
        void getPlayerUUID_returnsProvidedUUID() {
            PendingReward reward = createReward(Map.of("cmd", "give diamonds"));
            assertEquals(PLAYER_UUID, reward.getPlayerUUID());
        }

        @Test
        @DisplayName("getRewardTypeKey returns the provided type key")
        void getRewardTypeKey_returnsProvidedKey() {
            PendingReward reward = createReward(Map.of("cmd", "give diamonds"));
            assertEquals(REWARD_TYPE_KEY, reward.getRewardTypeKey());
        }

        @Test
        @DisplayName("getQuestKey returns the provided quest key")
        void getQuestKey_returnsProvidedKey() {
            PendingReward reward = createReward(Map.of("cmd", "give diamonds"));
            assertEquals(QUEST_KEY, reward.getQuestKey());
        }

        @Test
        @DisplayName("getCreatedAt returns the provided timestamp")
        void getCreatedAt_returnsProvidedTimestamp() {
            PendingReward reward = createReward(Map.of("cmd", "give diamonds"));
            assertEquals(CREATED_AT, reward.getCreatedAt());
        }

        @Test
        @DisplayName("getExpiresAt returns the provided timestamp")
        void getExpiresAt_returnsProvidedTimestamp() {
            PendingReward reward = createReward(Map.of("cmd", "give diamonds"));
            assertEquals(EXPIRES_AT, reward.getExpiresAt());
        }
    }

    @Nested
    @DisplayName("serialized config immutability")
    class SerializedConfigImmutability {

        @Test
        @DisplayName("getSerializedConfig returns the correct entries")
        void getSerializedConfig_returnsCorrectEntries() {
            Map<String, Object> config = Map.of("command", "give {player} diamond 5", "amount", 5);
            PendingReward reward = createReward(config);
            assertEquals(config, reward.getSerializedConfig());
        }

        @Test
        @DisplayName("modifications to the source map do not affect the reward")
        void getSerializedConfig_sourceMapModification_doesNotAffectReward() {
            Map<String, Object> mutableConfig = new HashMap<>();
            mutableConfig.put("key", "original");

            PendingReward reward = createReward(mutableConfig);
            mutableConfig.put("key", "modified");
            mutableConfig.put("extra", "sneaky");

            assertEquals("original", reward.getSerializedConfig().get("key"));
            assertEquals(1, reward.getSerializedConfig().size());
        }

        @Test
        @DisplayName("returned config map is unmodifiable")
        void getSerializedConfig_returnedMap_isUnmodifiable() {
            PendingReward reward = createReward(Map.of("cmd", "test"));
            Map<String, Object> returnedConfig = reward.getSerializedConfig();
            assertThrows(UnsupportedOperationException.class, () -> returnedConfig.put("new", "value"));
        }

        @Test
        @DisplayName("empty config map is handled correctly")
        void getSerializedConfig_emptyMap_isHandled() {
            PendingReward reward = createReward(Map.of());
            assertEquals(0, reward.getSerializedConfig().size());
        }

        @Test
        @DisplayName("returned config is a defensive copy, not the internal reference")
        void getSerializedConfig_returnedMap_isDefensiveCopy() {
            Map<String, Object> config = new HashMap<>();
            config.put("key", "value");
            PendingReward reward = createReward(config);
            assertNotSame(config, reward.getSerializedConfig());
        }
    }

    private PendingReward createReward(Map<String, Object> config) {
        return new PendingReward(REWARD_ID, PLAYER_UUID, REWARD_TYPE_KEY, config, QUEST_KEY, CREATED_AT, EXPIRES_AT);
    }
}
