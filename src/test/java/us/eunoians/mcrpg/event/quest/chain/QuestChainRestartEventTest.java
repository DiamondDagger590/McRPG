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
 * Tests for {@link QuestChainRestartEvent} and {@link QuestChainRestartEvent.RestartReason}.
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
    @DisplayName("Given a restart event with an online player, when getters are called, then they return the constructor values")
    void getters_returnConstructorValues() {
        QuestChainDefinition definition = buildDefinition();
        PlayerMock player = server.addPlayer();

        QuestChainRestartEvent event = new QuestChainRestartEvent(definition, player, player.getUniqueId(),
                QuestChainRestartEvent.RestartReason.REPEAT_MODE);

        assertSame(definition, event.getChainDefinition());
        assertSame(player, event.getPlayer());
        assertEquals(player.getUniqueId(), event.getPlayerUUID());
        assertEquals(QuestChainRestartEvent.RestartReason.REPEAT_MODE, event.getReason());
    }

    @Test
    @DisplayName("Given a restart event with a null player, when getPlayer() is called, then null is returned")
    void getPlayer_returnsNull_whenPlayerOffline() {
        QuestChainDefinition definition = buildDefinition();
        UUID offlineUUID = UUID.randomUUID();

        QuestChainRestartEvent event = new QuestChainRestartEvent(definition, null, offlineUUID,
                QuestChainRestartEvent.RestartReason.QUEST_EXPIRE_RESTART_CHAIN);

        assertNull(event.getPlayer());
        assertEquals(offlineUUID, event.getPlayerUUID());
    }

    @ParameterizedTest
    @EnumSource(QuestChainRestartEvent.RestartReason.class)
    @DisplayName("Given each RestartReason, when passed to constructor and retrieved, then the same reason is returned")
    void getReason_returnsProvidedReason(QuestChainRestartEvent.RestartReason reason) {
        QuestChainDefinition definition = buildDefinition();
        UUID uuid = UUID.randomUUID();

        QuestChainRestartEvent event = new QuestChainRestartEvent(definition, null, uuid, reason);

        assertEquals(reason, event.getReason());
    }

    @Test
    @DisplayName("Given a restart event, when getHandlers() is called, then the static HandlerList is returned")
    void getHandlers_returnsHandlerList() {
        QuestChainDefinition definition = buildDefinition();
        UUID uuid = UUID.randomUUID();

        QuestChainRestartEvent event = new QuestChainRestartEvent(definition, null, uuid,
                QuestChainRestartEvent.RestartReason.REPEAT_MODE);

        assertEquals(QuestChainRestartEvent.getHandlerList(), event.getHandlers());
    }

    @Test
    @DisplayName("RestartReason enum has exactly two values")
    void restartReason_hasTwoValues() {
        assertEquals(2, QuestChainRestartEvent.RestartReason.values().length);
    }
}
