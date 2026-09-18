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
    @DisplayName("Given an expire event with an online player, when getters are called, then they return the constructor values")
    void getters_returnConstructorValues() {
        QuestChainDefinition definition = buildDefinition();
        PlayerMock player = server.addPlayer();

        QuestChainExpireEvent event = new QuestChainExpireEvent(definition, player.getUniqueId(), player);

        assertSame(definition, event.getChainDefinition());
        assertEquals(player.getUniqueId(), event.getPlayerUUID());
        assertEquals(Optional.of(player), event.getPlayer());
    }

    @Test
    @DisplayName("Given an expire event with a null player, when getPlayer() is called, then empty Optional is returned")
    void getPlayer_returnsEmpty_whenPlayerOffline() {
        QuestChainDefinition definition = buildDefinition();
        UUID offlineUUID = UUID.randomUUID();

        QuestChainExpireEvent event = new QuestChainExpireEvent(definition, offlineUUID, null);

        assertEquals(Optional.empty(), event.getPlayer());
        assertEquals(offlineUUID, event.getPlayerUUID());
    }

    @Test
    @DisplayName("Given an expire event, when isCancelled() is called before setCancelled, then it returns false")
    void isCancelled_defaultsFalse() {
        QuestChainDefinition definition = buildDefinition();

        QuestChainExpireEvent event = new QuestChainExpireEvent(definition, UUID.randomUUID(), null);

        assertFalse(event.isCancelled());
    }

    @Test
    @DisplayName("Given an expire event, when setCancelled(true) is called, then isCancelled() returns true")
    void setCancelled_true_makesIsCancelledReturnTrue() {
        QuestChainDefinition definition = buildDefinition();

        QuestChainExpireEvent event = new QuestChainExpireEvent(definition, UUID.randomUUID(), null);
        event.setCancelled(true);

        assertTrue(event.isCancelled());
    }

    @Test
    @DisplayName("Given an expire event that was cancelled, when setCancelled(false) is called, then isCancelled() returns false")
    void setCancelled_false_revertsCancel() {
        QuestChainDefinition definition = buildDefinition();

        QuestChainExpireEvent event = new QuestChainExpireEvent(definition, UUID.randomUUID(), null);
        event.setCancelled(true);
        event.setCancelled(false);

        assertFalse(event.isCancelled());
    }

    @Test
    @DisplayName("Given an expire event, when getHandlers() is called, then the static HandlerList is returned")
    void getHandlers_returnsHandlerList() {
        QuestChainDefinition definition = buildDefinition();

        QuestChainExpireEvent event = new QuestChainExpireEvent(definition, UUID.randomUUID(), null);

        assertEquals(QuestChainExpireEvent.getHandlerList(), event.getHandlers());
    }
}
