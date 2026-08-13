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
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
    @DisplayName("Constructor stores all fields and getters return them")
    void constructor_storesAllFields() {
        QuestChainDefinition definition = buildDefinition();
        PlayerMock player = server.addPlayer();
        UUID playerUUID = player.getUniqueId();
        QuestChainStep step = definition.getSteps().get(0);

        QuestChainStepRetryEvent event = new QuestChainStepRetryEvent(
                definition, player, playerUUID, step, 2, 5);

        assertSame(definition, event.getChainDefinition());
        assertSame(player, event.getPlayer());
        assertEquals(playerUUID, event.getPlayerUUID());
        assertSame(step, event.getStep());
        assertEquals(2, event.getRetryNumber());
        assertEquals(5, event.getMaxRetries());
    }

    @Test
    @DisplayName("Player can be null for offline players")
    void getPlayer_returnsNull_whenPlayerIsOffline() {
        QuestChainDefinition definition = buildDefinition();
        UUID playerUUID = UUID.randomUUID();
        QuestChainStep step = definition.getSteps().get(0);

        QuestChainStepRetryEvent event = new QuestChainStepRetryEvent(
                definition, null, playerUUID, step, 1, 3);

        assertNull(event.getPlayer());
        assertEquals(playerUUID, event.getPlayerUUID());
    }

    @Test
    @DisplayName("MaxRetries of -1 indicates unlimited retries")
    void getMaxRetries_returnsNegativeOne_forUnlimited() {
        QuestChainDefinition definition = buildDefinition();
        UUID playerUUID = UUID.randomUUID();
        QuestChainStep step = definition.getSteps().get(0);

        QuestChainStepRetryEvent event = new QuestChainStepRetryEvent(
                definition, null, playerUUID, step, 1, -1);

        assertEquals(-1, event.getMaxRetries());
    }

    @Test
    @DisplayName("getHandlerList() returns a non-null static HandlerList")
    void getHandlerList_returnsNonNull() {
        assertNotNull(QuestChainStepRetryEvent.getHandlerList());
    }

    @Test
    @DisplayName("Instance getHandlers() returns the same HandlerList as the static method")
    void getHandlers_matchesStaticHandlerList() {
        QuestChainDefinition definition = buildDefinition();
        UUID playerUUID = UUID.randomUUID();
        QuestChainStep step = definition.getSteps().get(0);

        QuestChainStepRetryEvent event = new QuestChainStepRetryEvent(
                definition, null, playerUUID, step, 1, 1);

        assertSame(QuestChainStepRetryEvent.getHandlerList(), event.getHandlers());
    }
}
