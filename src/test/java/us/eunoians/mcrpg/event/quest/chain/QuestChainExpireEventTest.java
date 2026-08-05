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

/**
 * Tests for {@link QuestChainExpireEvent}.
 */
class QuestChainExpireEventTest extends McRPGBaseTest {

    private static QuestChainDefinition buildDefinition() {
        NamespacedKey chainKey = new NamespacedKey("mcrpg", "expire_chain");
        NamespacedKey sourceKey = new NamespacedKey("mcrpg", "manual");
        NamespacedKey triggerKey = new NamespacedKey("mcrpg", "manual");
        NamespacedKey questKey = new NamespacedKey("mcrpg", "expire_quest");
        List<QuestChainStep> steps = List.of(QuestChainStep.simple(questKey));
        return new QuestChainDefinition.Builder(chainKey, sourceKey, triggerKey, steps).build();
    }

    @Test
    @DisplayName("Given an online player, When getters are called, Then they return the values passed to the constructor")
    void event_getters_returnConstructorValues() {
        QuestChainDefinition definition = buildDefinition();
        PlayerMock player = server.addPlayer();

        QuestChainExpireEvent event = new QuestChainExpireEvent(
                definition, player.getUniqueId(), player);

        assertSame(definition, event.getChainDefinition());
        assertEquals(player.getUniqueId(), event.getPlayerUUID());
        assertTrue(event.getPlayer().isPresent());
        assertSame(player, event.getPlayer().orElseThrow());
    }

    @Test
    @DisplayName("Given an offline player, When getPlayer() is called, Then it returns empty Optional")
    void event_getPlayer_returnsEmpty_whenOffline() {
        QuestChainDefinition definition = buildDefinition();
        UUID offlineUUID = UUID.randomUUID();

        QuestChainExpireEvent event = new QuestChainExpireEvent(
                definition, offlineUUID, null);

        assertTrue(event.getPlayer().isEmpty());
        assertEquals(offlineUUID, event.getPlayerUUID());
    }

    @Test
    @DisplayName("Given a new event, When isCancelled() is called, Then it defaults to false")
    void event_isCancelled_defaultsFalse() {
        QuestChainDefinition definition = buildDefinition();
        PlayerMock player = server.addPlayer();

        QuestChainExpireEvent event = new QuestChainExpireEvent(
                definition, player.getUniqueId(), player);

        assertFalse(event.isCancelled());
    }

    @Test
    @DisplayName("Given an event, When setCancelled(true) is called, Then isCancelled() returns true")
    void event_setCancelled_updatesState() {
        QuestChainDefinition definition = buildDefinition();
        PlayerMock player = server.addPlayer();

        QuestChainExpireEvent event = new QuestChainExpireEvent(
                definition, player.getUniqueId(), player);
        event.setCancelled(true);

        assertTrue(event.isCancelled());
    }

    @Test
    @DisplayName("Given a cancelled event, When setCancelled(false) is called, Then isCancelled() returns false")
    void event_setCancelledFalse_revertsState() {
        QuestChainDefinition definition = buildDefinition();
        PlayerMock player = server.addPlayer();

        QuestChainExpireEvent event = new QuestChainExpireEvent(
                definition, player.getUniqueId(), player);
        event.setCancelled(true);
        event.setCancelled(false);

        assertFalse(event.isCancelled());
    }

    @Test
    @DisplayName("Given an expire event, When getHandlers() is called, Then a HandlerList is returned")
    void event_getHandlers_returnsHandlerList() {
        QuestChainDefinition definition = buildDefinition();
        PlayerMock player = server.addPlayer();

        QuestChainExpireEvent event = new QuestChainExpireEvent(
                definition, player.getUniqueId(), player);

        assertEquals(QuestChainExpireEvent.getHandlerList(), event.getHandlers());
    }
}
