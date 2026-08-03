package us.eunoians.mcrpg.event.quest.chain;

import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.quest.chain.QuestChainDefinition;
import us.eunoians.mcrpg.quest.chain.QuestChainStep;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Tests for {@link QuestChainRestartEvent}.
 */
class QuestChainRestartEventTest extends McRPGBaseTest {

    private static QuestChainDefinition buildDefinition(String key) {
        NamespacedKey chainKey = new NamespacedKey("mcrpg", key);
        NamespacedKey sourceKey = new NamespacedKey("mcrpg", "manual");
        NamespacedKey triggerKey = new NamespacedKey("mcrpg", "manual");
        NamespacedKey questKey = new NamespacedKey("mcrpg", key + "_quest");
        List<QuestChainStep> steps = List.of(QuestChainStep.simple(questKey));
        return new QuestChainDefinition.Builder(chainKey, sourceKey, triggerKey, steps).build();
    }

    @Nested
    @DisplayName("Constructor and getters")
    class ConstructorAndGetters {

        @Test
        @DisplayName("Given all parameters with online player, When getters are called, Then they return the constructor values")
        void getters_returnConstructorValues_withOnlinePlayer() {
            QuestChainDefinition definition = buildDefinition("restart_online");
            PlayerMock player = server.addPlayer();
            UUID playerUUID = player.getUniqueId();

            QuestChainRestartEvent event = new QuestChainRestartEvent(
                    definition, player, playerUUID, QuestChainRestartEvent.RestartReason.REPEAT_MODE);

            assertSame(definition, event.getChainDefinition());
            assertSame(player, event.getPlayer());
            assertEquals(playerUUID, event.getPlayerUUID());
            assertEquals(QuestChainRestartEvent.RestartReason.REPEAT_MODE, event.getReason());
        }

        @Test
        @DisplayName("Given null player, When getPlayer is called, Then returns null")
        void getPlayer_returnsNull_whenPlayerIsNull() {
            QuestChainDefinition definition = buildDefinition("restart_offline");
            UUID playerUUID = UUID.randomUUID();

            QuestChainRestartEvent event = new QuestChainRestartEvent(
                    definition, null, playerUUID, QuestChainRestartEvent.RestartReason.REPEAT_MODE);

            assertNull(event.getPlayer());
        }

        @Test
        @DisplayName("Given null player, When getPlayerUUID is called, Then still returns the UUID")
        void getPlayerUUID_returnsUUID_evenWhenPlayerIsNull() {
            QuestChainDefinition definition = buildDefinition("restart_uuid");
            UUID playerUUID = UUID.randomUUID();

            QuestChainRestartEvent event = new QuestChainRestartEvent(
                    definition, null, playerUUID, QuestChainRestartEvent.RestartReason.QUEST_EXPIRE_RESTART_CHAIN);

            assertEquals(playerUUID, event.getPlayerUUID());
        }
    }

    @Nested
    @DisplayName("RestartReason")
    class RestartReasonTests {

        @ParameterizedTest
        @EnumSource(QuestChainRestartEvent.RestartReason.class)
        @DisplayName("Given each RestartReason, When getReason is called, Then returns the matching reason")
        void getReason_returnsMatchingReason(QuestChainRestartEvent.RestartReason reason) {
            QuestChainDefinition definition = buildDefinition("restart_reason_" + reason.name().toLowerCase());
            UUID playerUUID = UUID.randomUUID();

            QuestChainRestartEvent event = new QuestChainRestartEvent(
                    definition, null, playerUUID, reason);

            assertEquals(reason, event.getReason());
        }
    }

    @Nested
    @DisplayName("Handler list")
    class HandlerListTests {

        @Test
        @DisplayName("getHandlers returns the static handler list")
        void getHandlers_matchesStaticHandlerList() {
            QuestChainDefinition definition = buildDefinition("restart_hl");
            UUID playerUUID = UUID.randomUUID();

            QuestChainRestartEvent event = new QuestChainRestartEvent(
                    definition, null, playerUUID, QuestChainRestartEvent.RestartReason.REPEAT_MODE);

            assertSame(QuestChainRestartEvent.getHandlerList(), event.getHandlers());
        }
    }
}
