package us.eunoians.mcrpg.event.quest.chain;

import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.quest.chain.QuestChainDefinition;
import us.eunoians.mcrpg.quest.chain.QuestChainStep;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link QuestChainExpireEvent}.
 */
public class QuestChainExpireEventTest extends McRPGBaseTest {

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

        QuestChainExpireEvent event = new QuestChainExpireEvent(definition, player.getUniqueId(), player);

        assertSame(definition, event.getChainDefinition());
        assertEquals(player.getUniqueId(), event.getPlayerUUID());
        assertTrue(event.getPlayer().isPresent());
        assertSame(player, event.getPlayer().get());
    }

    @Test
    @DisplayName("getPlayer returns empty Optional when player is offline")
    void getPlayer_returnsEmpty_whenPlayerOffline() {
        QuestChainDefinition definition = buildDefinition();
        UUID offlineUUID = UUID.randomUUID();

        QuestChainExpireEvent event = new QuestChainExpireEvent(definition, offlineUUID, null);

        assertEquals(Optional.empty(), event.getPlayer());
        assertEquals(offlineUUID, event.getPlayerUUID());
    }

    @Test
    @DisplayName("event is not cancelled by default")
    void isCancelled_returnsFalse_byDefault() {
        QuestChainDefinition definition = buildDefinition();
        PlayerMock player = server.addPlayer();

        QuestChainExpireEvent event = new QuestChainExpireEvent(definition, player.getUniqueId(), player);

        assertFalse(event.isCancelled());
    }

    @Test
    @DisplayName("setCancelled changes cancelled state")
    void setCancelled_updatesCancelledState() {
        QuestChainDefinition definition = buildDefinition();
        PlayerMock player = server.addPlayer();

        QuestChainExpireEvent event = new QuestChainExpireEvent(definition, player.getUniqueId(), player);

        event.setCancelled(true);
        assertTrue(event.isCancelled());

        event.setCancelled(false);
        assertFalse(event.isCancelled());
    }

    @Test
    @DisplayName("getHandlers returns static HandlerList")
    void event_getHandlers_returnsHandlerList() {
        QuestChainDefinition definition = buildDefinition();
        PlayerMock player = server.addPlayer();

        QuestChainExpireEvent event = new QuestChainExpireEvent(definition, player.getUniqueId(), player);

        assertEquals(QuestChainExpireEvent.getHandlerList(), event.getHandlers());
    }
}
