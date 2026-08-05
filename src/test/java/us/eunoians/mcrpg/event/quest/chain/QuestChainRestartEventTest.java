package us.eunoians.mcrpg.event.quest.chain;

import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.DisplayName;
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

    private static QuestChainDefinition buildDefinition() {
        NamespacedKey chainKey = new NamespacedKey("mcrpg", "restart_chain");
        NamespacedKey sourceKey = new NamespacedKey("mcrpg", "manual");
        NamespacedKey triggerKey = new NamespacedKey("mcrpg", "manual");
        NamespacedKey questKey = new NamespacedKey("mcrpg", "restart_quest");
        List<QuestChainStep> steps = List.of(QuestChainStep.simple(questKey));
        return new QuestChainDefinition.Builder(chainKey, sourceKey, triggerKey, steps).build();
    }

    @Test
    @DisplayName("Given an online player with REPEAT_MODE reason, When getters are called, Then they return constructor values")
    void event_getters_returnConstructorValues_repeatMode() {
        QuestChainDefinition definition = buildDefinition();
        PlayerMock player = server.addPlayer();

        QuestChainRestartEvent event = new QuestChainRestartEvent(
                definition, player, player.getUniqueId(),
                QuestChainRestartEvent.RestartReason.REPEAT_MODE);

        assertSame(definition, event.getChainDefinition());
        assertSame(player, event.getPlayer());
        assertEquals(player.getUniqueId(), event.getPlayerUUID());
        assertEquals(QuestChainRestartEvent.RestartReason.REPEAT_MODE, event.getReason());
    }

    @Test
    @DisplayName("Given an offline player, When getPlayer() is called, Then it returns null")
    void event_getPlayer_returnsNull_whenOffline() {
        QuestChainDefinition definition = buildDefinition();
        UUID offlineUUID = UUID.randomUUID();

        QuestChainRestartEvent event = new QuestChainRestartEvent(
                definition, null, offlineUUID,
                QuestChainRestartEvent.RestartReason.QUEST_EXPIRE_RESTART_CHAIN);

        assertNull(event.getPlayer());
        assertEquals(offlineUUID, event.getPlayerUUID());
    }

    @ParameterizedTest
    @EnumSource(QuestChainRestartEvent.RestartReason.class)
    @DisplayName("Given each RestartReason variant, When getReason() is called, Then it returns the expected value")
    void event_getReason_returnsExpectedValue(QuestChainRestartEvent.RestartReason reason) {
        QuestChainDefinition definition = buildDefinition();
        PlayerMock player = server.addPlayer();

        QuestChainRestartEvent event = new QuestChainRestartEvent(
                definition, player, player.getUniqueId(), reason);

        assertEquals(reason, event.getReason());
    }

    @Test
    @DisplayName("Given a restart event, When getHandlers() is called, Then a HandlerList is returned")
    void event_getHandlers_returnsHandlerList() {
        QuestChainDefinition definition = buildDefinition();
        PlayerMock player = server.addPlayer();

        QuestChainRestartEvent event = new QuestChainRestartEvent(
                definition, player, player.getUniqueId(),
                QuestChainRestartEvent.RestartReason.REPEAT_MODE);

        assertEquals(QuestChainRestartEvent.getHandlerList(), event.getHandlers());
    }
}
