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

class QuestChainExpireEventTest extends McRPGBaseTest {

    private static QuestChainDefinition buildDefinition() {
        NamespacedKey chainKey = new NamespacedKey("mcrpg", "expire_chain");
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

        QuestChainExpireEvent event = new QuestChainExpireEvent(definition, player.getUniqueId(), player);

        assertSame(definition, event.getChainDefinition());
        assertEquals(player.getUniqueId(), event.getPlayerUUID());
        assertTrue(event.getPlayer().isPresent());
        assertSame(player, event.getPlayer().get());
    }

    @Test
    @DisplayName("getPlayer returns empty Optional when constructed with null player")
    void nullPlayer_returnsEmptyOptional() {
        QuestChainDefinition definition = buildDefinition();
        UUID offlineUUID = UUID.randomUUID();

        QuestChainExpireEvent event = new QuestChainExpireEvent(definition, offlineUUID, null);

        assertTrue(event.getPlayer().isEmpty());
        assertEquals(offlineUUID, event.getPlayerUUID());
    }

    @Test
    @DisplayName("Event defaults to not cancelled")
    void event_defaultsNotCancelled() {
        QuestChainDefinition definition = buildDefinition();
        PlayerMock player = server.addPlayer();

        QuestChainExpireEvent event = new QuestChainExpireEvent(definition, player.getUniqueId(), player);

        assertFalse(event.isCancelled());
    }

    @Test
    @DisplayName("setCancelled toggles cancellation state")
    void setCancelled_togglesState() {
        QuestChainDefinition definition = buildDefinition();
        PlayerMock player = server.addPlayer();

        QuestChainExpireEvent event = new QuestChainExpireEvent(definition, player.getUniqueId(), player);

        event.setCancelled(true);
        assertTrue(event.isCancelled());

        event.setCancelled(false);
        assertFalse(event.isCancelled());
    }

    @Test
    @DisplayName("getHandlers and getHandlerList return the same HandlerList")
    void handlers_returnStaticHandlerList() {
        QuestChainDefinition definition = buildDefinition();
        PlayerMock player = server.addPlayer();

        QuestChainExpireEvent event = new QuestChainExpireEvent(definition, player.getUniqueId(), player);

        assertSame(QuestChainExpireEvent.getHandlerList(), event.getHandlers());
    }
}
