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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("QuestChainExpireEvent")
class QuestChainExpireEventTest extends McRPGBaseTest {

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

        QuestChainExpireEvent event = new QuestChainExpireEvent(definition, playerUUID, player);

        assertSame(definition, event.getChainDefinition());
        assertTrue(event.getPlayer().isPresent());
        assertSame(player, event.getPlayer().get());
        assertEquals(playerUUID, event.getPlayerUUID());
    }

    @Test
    @DisplayName("Given a null player (offline), when getPlayer is called, then it returns empty Optional")
    void event_getPlayer_returnsEmpty_whenPlayerOffline() {
        QuestChainDefinition definition = buildDefinition();
        UUID playerUUID = UUID.randomUUID();

        QuestChainExpireEvent event = new QuestChainExpireEvent(definition, playerUUID, null);

        assertTrue(event.getPlayer().isEmpty());
        assertEquals(playerUUID, event.getPlayerUUID());
    }

    @Test
    @DisplayName("Given a chain expire event, when getHandlers is called, then it matches the static handler list")
    void event_getHandlers_matchesStaticHandlerList() {
        QuestChainDefinition definition = buildDefinition();
        UUID playerUUID = UUID.randomUUID();

        QuestChainExpireEvent event = new QuestChainExpireEvent(definition, playerUUID, null);

        assertEquals(QuestChainExpireEvent.getHandlerList(), event.getHandlers());
    }

    @Test
    @DisplayName("Given a chain expire event, then it is not cancelled by default")
    void event_isNotCancelled_byDefault() {
        QuestChainDefinition definition = buildDefinition();
        UUID playerUUID = UUID.randomUUID();

        QuestChainExpireEvent event = new QuestChainExpireEvent(definition, playerUUID, null);

        assertFalse(event.isCancelled());
    }

    @Test
    @DisplayName("Given a chain expire event, when setCancelled is called, then isCancelled reflects the change")
    void event_setCancelled_updatesState() {
        QuestChainDefinition definition = buildDefinition();
        UUID playerUUID = UUID.randomUUID();

        QuestChainExpireEvent event = new QuestChainExpireEvent(definition, playerUUID, null);

        event.setCancelled(true);
        assertTrue(event.isCancelled());

        event.setCancelled(false);
        assertFalse(event.isCancelled());
    }
}
