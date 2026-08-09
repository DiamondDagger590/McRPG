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

/**
 * Tests for {@link QuestChainStepRetryEvent}.
 */
public class QuestChainStepRetryEventTest extends McRPGBaseTest {

    private static QuestChainDefinition buildDefinition() {
        NamespacedKey chainKey = new NamespacedKey("mcrpg", "test_chain");
        NamespacedKey sourceKey = new NamespacedKey("mcrpg", "manual");
        NamespacedKey triggerKey = new NamespacedKey("mcrpg", "manual");
        List<QuestChainStep> steps = List.of(QuestChainStep.simple(new NamespacedKey("mcrpg", "quest_one")));
        return new QuestChainDefinition.Builder(chainKey, sourceKey, triggerKey, steps).build();
    }

    @Test
    @DisplayName("getters return constructor values")
    void event_getters_returnConstructorValues() {
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
    @DisplayName("getPlayer returns null when player is offline")
    void getPlayer_returnsNull_whenPlayerOffline() {
        QuestChainDefinition definition = buildDefinition();
        UUID offlineUUID = UUID.randomUUID();
        QuestChainStep step = definition.getSteps().getFirst();

        QuestChainStepRetryEvent event = new QuestChainStepRetryEvent(
                definition, null, offlineUUID, step, 1, 3);

        assertNull(event.getPlayer());
        assertEquals(offlineUUID, event.getPlayerUUID());
    }

    @Test
    @DisplayName("maxRetries returns -1 for unlimited retries")
    void getMaxRetries_returnsNegativeOne_whenUnlimited() {
        QuestChainDefinition definition = buildDefinition();
        PlayerMock player = server.addPlayer();
        QuestChainStep step = definition.getSteps().getFirst();

        QuestChainStepRetryEvent event = new QuestChainStepRetryEvent(
                definition, player, player.getUniqueId(), step, 1, -1);

        assertEquals(-1, event.getMaxRetries());
    }

    @Test
    @DisplayName("retryNumber reflects first retry attempt")
    void getRetryNumber_returnsOne_whenFirstRetry() {
        QuestChainDefinition definition = buildDefinition();
        PlayerMock player = server.addPlayer();
        QuestChainStep step = definition.getSteps().getFirst();

        QuestChainStepRetryEvent event = new QuestChainStepRetryEvent(
                definition, player, player.getUniqueId(), step, 1, 5);

        assertEquals(1, event.getRetryNumber());
    }

    @Test
    @DisplayName("getHandlers returns static HandlerList")
    void event_getHandlers_returnsHandlerList() {
        QuestChainDefinition definition = buildDefinition();
        PlayerMock player = server.addPlayer();
        QuestChainStep step = definition.getSteps().getFirst();

        QuestChainStepRetryEvent event = new QuestChainStepRetryEvent(
                definition, player, player.getUniqueId(), step, 1, 3);

        assertEquals(QuestChainStepRetryEvent.getHandlerList(), event.getHandlers());
    }
}
