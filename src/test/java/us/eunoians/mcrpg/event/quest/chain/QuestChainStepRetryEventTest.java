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

class QuestChainStepRetryEventTest extends McRPGBaseTest {

    private static QuestChainDefinition buildDefinition() {
        NamespacedKey chainKey = new NamespacedKey("mcrpg", "retry_chain");
        NamespacedKey sourceKey = new NamespacedKey("mcrpg", "manual");
        NamespacedKey triggerKey = new NamespacedKey("mcrpg", "manual");
        List<QuestChainStep> steps = List.of(
                QuestChainStep.simple(new NamespacedKey("mcrpg", "step_one")),
                QuestChainStep.simple(new NamespacedKey("mcrpg", "step_two"))
        );
        return new QuestChainDefinition.Builder(chainKey, sourceKey, triggerKey, steps).build();
    }

    @Test
    @DisplayName("Getters return values passed to the constructor")
    void getters_returnConstructorValues() {
        QuestChainDefinition definition = buildDefinition();
        PlayerMock player = server.addPlayer();
        QuestChainStep step = definition.getSteps().getFirst();

        QuestChainStepRetryEvent event = new QuestChainStepRetryEvent(
                definition, player, player.getUniqueId(), step, 2, 5);

        assertSame(definition, event.getChainDefinition());
        assertSame(player, event.getPlayer());
        assertEquals(player.getUniqueId(), event.getPlayerUUID());
        assertSame(step, event.getStep());
        assertEquals(2, event.getRetryNumber());
        assertEquals(5, event.getMaxRetries());
    }

    @Test
    @DisplayName("Null player is returned when constructed for offline player")
    void nullPlayer_returnsNull() {
        QuestChainDefinition definition = buildDefinition();
        UUID offlineUUID = UUID.randomUUID();
        QuestChainStep step = definition.getSteps().getFirst();

        QuestChainStepRetryEvent event = new QuestChainStepRetryEvent(
                definition, null, offlineUUID, step, 1, 3);

        assertNull(event.getPlayer());
        assertEquals(offlineUUID, event.getPlayerUUID());
    }

    @Test
    @DisplayName("maxRetries of -1 indicates unlimited retries")
    void unlimitedRetries_maxRetriesIsNegativeOne() {
        QuestChainDefinition definition = buildDefinition();
        PlayerMock player = server.addPlayer();
        QuestChainStep step = definition.getSteps().getFirst();

        QuestChainStepRetryEvent event = new QuestChainStepRetryEvent(
                definition, player, player.getUniqueId(), step, 1, -1);

        assertEquals(-1, event.getMaxRetries());
    }

    @Test
    @DisplayName("First retry has retryNumber 1")
    void firstRetry_retryNumberIsOne() {
        QuestChainDefinition definition = buildDefinition();
        PlayerMock player = server.addPlayer();
        QuestChainStep step = definition.getSteps().getFirst();

        QuestChainStepRetryEvent event = new QuestChainStepRetryEvent(
                definition, player, player.getUniqueId(), step, 1, 5);

        assertEquals(1, event.getRetryNumber());
    }

    @Test
    @DisplayName("getHandlers and getHandlerList return the same HandlerList")
    void handlers_returnStaticHandlerList() {
        QuestChainDefinition definition = buildDefinition();
        PlayerMock player = server.addPlayer();
        QuestChainStep step = definition.getSteps().getFirst();

        QuestChainStepRetryEvent event = new QuestChainStepRetryEvent(
                definition, player, player.getUniqueId(), step, 1, 3);

        assertSame(QuestChainStepRetryEvent.getHandlerList(), event.getHandlers());
    }
}
