package us.eunoians.mcrpg.event.quest.chain;

import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.quest.chain.QuestChainDefinition;
import us.eunoians.mcrpg.quest.chain.QuestChainStep;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

@DisplayName("QuestChainStepRetryEvent")
class QuestChainStepRetryEventTest extends McRPGBaseTest {

    private static QuestChainDefinition buildDefinition() {
        NamespacedKey chainKey = new NamespacedKey("mcrpg", "test_chain");
        NamespacedKey sourceKey = new NamespacedKey("mcrpg", "manual");
        NamespacedKey triggerKey = new NamespacedKey("mcrpg", "manual");
        NamespacedKey questKey = new NamespacedKey("mcrpg", "example_quest");
        List<QuestChainStep> steps = List.of(QuestChainStep.simple(questKey));
        return new QuestChainDefinition.Builder(chainKey, sourceKey, triggerKey, steps).build();
    }

    @Test
    @DisplayName("Given an online player, when getters are called, then they return the values passed to the constructor")
    void event_getters_returnConstructorValues_whenPlayerOnline() {
        QuestChainDefinition definition = buildDefinition();
        PlayerMock player = server.addPlayer();
        UUID playerUUID = player.getUniqueId();
        QuestChainStep step = definition.getSteps().get(0);

        QuestChainStepRetryEvent event = new QuestChainStepRetryEvent(definition, player, playerUUID, step, 2, 5);

        assertSame(definition, event.getChainDefinition());
        assertSame(player, event.getPlayer());
        assertEquals(playerUUID, event.getPlayerUUID());
        assertSame(step, event.getStep());
        assertEquals(2, event.getRetryNumber());
        assertEquals(5, event.getMaxRetries());
    }

    @Test
    @DisplayName("Given a null player (offline), when getPlayer is called, then it returns null")
    void event_getPlayer_returnsNull_whenPlayerOffline() {
        QuestChainDefinition definition = buildDefinition();
        UUID playerUUID = UUID.randomUUID();
        QuestChainStep step = definition.getSteps().get(0);

        QuestChainStepRetryEvent event = new QuestChainStepRetryEvent(definition, null, playerUUID, step, 1, 3);

        assertNull(event.getPlayer());
        assertEquals(playerUUID, event.getPlayerUUID());
    }

    @Test
    @DisplayName("Given unlimited retries (-1), when getMaxRetries is called, then it returns -1")
    void event_getMaxRetries_returnsNegativeOne_whenUnlimited() {
        QuestChainDefinition definition = buildDefinition();
        UUID playerUUID = UUID.randomUUID();
        QuestChainStep step = definition.getSteps().get(0);

        QuestChainStepRetryEvent event = new QuestChainStepRetryEvent(definition, null, playerUUID, step, 1, -1);

        assertEquals(-1, event.getMaxRetries());
    }

    @Test
    @DisplayName("Given a first retry, when getRetryNumber is called, then it returns 1")
    void event_getRetryNumber_returnsOne_whenFirstRetry() {
        QuestChainDefinition definition = buildDefinition();
        UUID playerUUID = UUID.randomUUID();
        QuestChainStep step = definition.getSteps().get(0);

        QuestChainStepRetryEvent event = new QuestChainStepRetryEvent(definition, null, playerUUID, step, 1, 3);

        assertEquals(1, event.getRetryNumber());
    }

    @Test
    @DisplayName("Given a step retry event, when getHandlers is called, then it matches the static handler list")
    void event_getHandlers_matchesStaticHandlerList() {
        QuestChainDefinition definition = buildDefinition();
        UUID playerUUID = UUID.randomUUID();
        QuestChainStep step = definition.getSteps().get(0);

        QuestChainStepRetryEvent event = new QuestChainStepRetryEvent(definition, null, playerUUID, step, 1, 3);

        assertEquals(QuestChainStepRetryEvent.getHandlerList(), event.getHandlers());
    }
}
