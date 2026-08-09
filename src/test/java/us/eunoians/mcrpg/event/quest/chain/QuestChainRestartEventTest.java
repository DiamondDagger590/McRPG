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
public class QuestChainRestartEventTest extends McRPGBaseTest {

    private static QuestChainDefinition buildDefinition() {
        NamespacedKey chainKey = new NamespacedKey("mcrpg", "test_chain");
        NamespacedKey sourceKey = new NamespacedKey("mcrpg", "manual");
        NamespacedKey triggerKey = new NamespacedKey("mcrpg", "manual");
        List<QuestChainStep> steps = List.of(QuestChainStep.simple(new NamespacedKey("mcrpg", "quest_one")));
        return new QuestChainDefinition.Builder(chainKey, sourceKey, triggerKey, steps).build();
    }

    @Test
    @DisplayName("getters return constructor values with REPEAT_MODE reason")
    void event_getters_returnConstructorValues_whenRepeatMode() {
        QuestChainDefinition definition = buildDefinition();
        PlayerMock player = server.addPlayer();

        QuestChainRestartEvent event = new QuestChainRestartEvent(
                definition, player, player.getUniqueId(), QuestChainRestartEvent.RestartReason.REPEAT_MODE);

        assertSame(definition, event.getChainDefinition());
        assertSame(player, event.getPlayer());
        assertEquals(player.getUniqueId(), event.getPlayerUUID());
        assertEquals(QuestChainRestartEvent.RestartReason.REPEAT_MODE, event.getReason());
    }

    @Test
    @DisplayName("getters return constructor values with QUEST_EXPIRE_RESTART_CHAIN reason")
    void event_getters_returnConstructorValues_whenQuestExpireRestartChain() {
        QuestChainDefinition definition = buildDefinition();
        PlayerMock player = server.addPlayer();

        QuestChainRestartEvent event = new QuestChainRestartEvent(
                definition, player, player.getUniqueId(),
                QuestChainRestartEvent.RestartReason.QUEST_EXPIRE_RESTART_CHAIN);

        assertEquals(QuestChainRestartEvent.RestartReason.QUEST_EXPIRE_RESTART_CHAIN, event.getReason());
    }

    @Test
    @DisplayName("getPlayer returns null when player is offline")
    void getPlayer_returnsNull_whenPlayerOffline() {
        QuestChainDefinition definition = buildDefinition();
        UUID offlineUUID = UUID.randomUUID();

        QuestChainRestartEvent event = new QuestChainRestartEvent(
                definition, null, offlineUUID, QuestChainRestartEvent.RestartReason.REPEAT_MODE);

        assertNull(event.getPlayer());
        assertEquals(offlineUUID, event.getPlayerUUID());
    }

    @Test
    @DisplayName("getHandlers returns static HandlerList")
    void event_getHandlers_returnsHandlerList() {
        QuestChainDefinition definition = buildDefinition();
        PlayerMock player = server.addPlayer();

        QuestChainRestartEvent event = new QuestChainRestartEvent(
                definition, player, player.getUniqueId(), QuestChainRestartEvent.RestartReason.REPEAT_MODE);

        assertEquals(QuestChainRestartEvent.getHandlerList(), event.getHandlers());
    }

    @Nested
    @DisplayName("RestartReason")
    class RestartReasonTest {

        @ParameterizedTest
        @EnumSource(QuestChainRestartEvent.RestartReason.class)
        @DisplayName("all RestartReason values are preserved through event construction")
        void allReasons_arePreserved(QuestChainRestartEvent.RestartReason reason) {
            QuestChainDefinition definition = buildDefinition();
            PlayerMock player = server.addPlayer();

            QuestChainRestartEvent event = new QuestChainRestartEvent(
                    definition, player, player.getUniqueId(), reason);

            assertEquals(reason, event.getReason());
        }
    }
}
