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

@DisplayName("QuestChainAbandonEvent")
class QuestChainAbandonEventTest extends McRPGBaseTest {

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

        QuestChainAbandonEvent event = new QuestChainAbandonEvent(definition, player, playerUUID);

        assertSame(definition, event.getChainDefinition());
        assertSame(player, event.getPlayer());
        assertEquals(playerUUID, event.getPlayerUUID());
    }

    @Test
    @DisplayName("Given a null player (offline), when getPlayer is called, then it returns null")
    void event_getPlayer_returnsNull_whenPlayerOffline() {
        QuestChainDefinition definition = buildDefinition();
        UUID playerUUID = UUID.randomUUID();

        QuestChainAbandonEvent event = new QuestChainAbandonEvent(definition, null, playerUUID);

        assertNull(event.getPlayer());
        assertEquals(playerUUID, event.getPlayerUUID());
    }

    @Test
    @DisplayName("Given a chain abandon event, when getHandlers is called, then it matches the static handler list")
    void event_getHandlers_matchesStaticHandlerList() {
        QuestChainDefinition definition = buildDefinition();
        UUID playerUUID = UUID.randomUUID();

        QuestChainAbandonEvent event = new QuestChainAbandonEvent(definition, null, playerUUID);

        assertEquals(QuestChainAbandonEvent.getHandlerList(), event.getHandlers());
    }
}
