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

class QuestChainRestartEventTest extends McRPGBaseTest {

    private static QuestChainDefinition buildDefinition() {
        NamespacedKey chainKey = new NamespacedKey("mcrpg", "restart_chain");
        NamespacedKey sourceKey = new NamespacedKey("mcrpg", "manual");
        NamespacedKey triggerKey = new NamespacedKey("mcrpg", "manual");
        List<QuestChainStep> steps = List.of(QuestChainStep.simple(new NamespacedKey("mcrpg", "quest_one")));
        return new QuestChainDefinition.Builder(chainKey, sourceKey, triggerKey, steps).build();
    }

    @Test
    @DisplayName("Getters return values passed to the constructor")
    void getters_returnConstructorValues() {
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
    @DisplayName("Null player is returned when constructed for offline player")
    void nullPlayer_returnsNull() {
        QuestChainDefinition definition = buildDefinition();
        UUID offlineUUID = UUID.randomUUID();

        QuestChainRestartEvent event = new QuestChainRestartEvent(
                definition, null, offlineUUID, QuestChainRestartEvent.RestartReason.REPEAT_MODE);

        assertNull(event.getPlayer());
        assertEquals(offlineUUID, event.getPlayerUUID());
    }

    @ParameterizedTest
    @EnumSource(QuestChainRestartEvent.RestartReason.class)
    @DisplayName("Each RestartReason is correctly returned by getReason")
    void restartReason_allValues_returnedCorrectly(QuestChainRestartEvent.RestartReason reason) {
        QuestChainDefinition definition = buildDefinition();
        PlayerMock player = server.addPlayer();

        QuestChainRestartEvent event = new QuestChainRestartEvent(
                definition, player, player.getUniqueId(), reason);

        assertEquals(reason, event.getReason());
    }

    @Test
    @DisplayName("getHandlers and getHandlerList return the same HandlerList")
    void handlers_returnStaticHandlerList() {
        QuestChainDefinition definition = buildDefinition();
        PlayerMock player = server.addPlayer();

        QuestChainRestartEvent event = new QuestChainRestartEvent(
                definition, player, player.getUniqueId(), QuestChainRestartEvent.RestartReason.REPEAT_MODE);

        assertSame(QuestChainRestartEvent.getHandlerList(), event.getHandlers());
    }
}
