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
 * Tests for {@link QuestChainFailEvent}.
 */
public class QuestChainFailEventTest extends McRPGBaseTest {

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

        QuestChainFailEvent event = new QuestChainFailEvent(definition, player, player.getUniqueId());

        assertSame(definition, event.getChainDefinition());
        assertSame(player, event.getPlayer());
        assertEquals(player.getUniqueId(), event.getPlayerUUID());
    }

    @Test
    @DisplayName("getPlayer returns null when player is offline")
    void getPlayer_returnsNull_whenPlayerOffline() {
        QuestChainDefinition definition = buildDefinition();
        UUID offlineUUID = UUID.randomUUID();

        QuestChainFailEvent event = new QuestChainFailEvent(definition, null, offlineUUID);

        assertNull(event.getPlayer());
        assertEquals(offlineUUID, event.getPlayerUUID());
    }

    @Test
    @DisplayName("getHandlers returns static HandlerList")
    void event_getHandlers_returnsHandlerList() {
        QuestChainDefinition definition = buildDefinition();
        PlayerMock player = server.addPlayer();

        QuestChainFailEvent event = new QuestChainFailEvent(definition, player, player.getUniqueId());

        assertEquals(QuestChainFailEvent.getHandlerList(), event.getHandlers());
    }

    @Test
    @DisplayName("getChainDefinition returns provided definition")
    void getChainDefinition_returnsProvidedDefinition() {
        QuestChainDefinition definition = buildDefinition();
        UUID uuid = UUID.randomUUID();

        QuestChainFailEvent event = new QuestChainFailEvent(definition, null, uuid);

        assertSame(definition, event.getChainDefinition());
    }
}
