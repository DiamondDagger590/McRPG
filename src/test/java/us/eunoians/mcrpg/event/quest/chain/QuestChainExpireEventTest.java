package us.eunoians.mcrpg.event.quest.chain;

import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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

    private static QuestChainDefinition buildDefinition(String key) {
        NamespacedKey chainKey = new NamespacedKey("mcrpg", key);
        NamespacedKey sourceKey = new NamespacedKey("mcrpg", "manual");
        NamespacedKey triggerKey = new NamespacedKey("mcrpg", "manual");
        NamespacedKey questKey = new NamespacedKey("mcrpg", key + "_quest");
        List<QuestChainStep> steps = List.of(QuestChainStep.simple(questKey));
        return new QuestChainDefinition.Builder(chainKey, sourceKey, triggerKey, steps).build();
    }

    @Nested
    @DisplayName("Constructor and getters")
    class ConstructorAndGetters {

        @Test
        @DisplayName("Given all parameters with online player, When getters are called, Then they return the constructor values")
        void getters_returnConstructorValues_withOnlinePlayer() {
            QuestChainDefinition definition = buildDefinition("expire_online");
            PlayerMock player = server.addPlayer();
            UUID playerUUID = player.getUniqueId();

            QuestChainExpireEvent event = new QuestChainExpireEvent(definition, playerUUID, player);

            assertSame(definition, event.getChainDefinition());
            assertEquals(playerUUID, event.getPlayerUUID());
            assertTrue(event.getPlayer().isPresent());
            assertSame(player, event.getPlayer().get());
        }

        @Test
        @DisplayName("Given null player, When getPlayer is called, Then returns empty Optional")
        void getPlayer_returnsEmpty_whenPlayerIsNull() {
            QuestChainDefinition definition = buildDefinition("expire_offline");
            UUID playerUUID = UUID.randomUUID();

            QuestChainExpireEvent event = new QuestChainExpireEvent(definition, playerUUID, null);

            assertTrue(event.getPlayer().isEmpty());
        }

        @Test
        @DisplayName("Given null player, When getPlayerUUID is called, Then still returns the UUID")
        void getPlayerUUID_returnsUUID_evenWhenPlayerIsNull() {
            QuestChainDefinition definition = buildDefinition("expire_uuid");
            UUID playerUUID = UUID.randomUUID();

            QuestChainExpireEvent event = new QuestChainExpireEvent(definition, playerUUID, null);

            assertEquals(playerUUID, event.getPlayerUUID());
        }
    }

    @Nested
    @DisplayName("Cancellation")
    class Cancellation {

        @Test
        @DisplayName("Given a new event, When isCancelled is called, Then returns false")
        void isCancelled_returnsFalse_byDefault() {
            QuestChainDefinition definition = buildDefinition("expire_cancel_def");
            UUID playerUUID = UUID.randomUUID();

            QuestChainExpireEvent event = new QuestChainExpireEvent(definition, playerUUID, null);

            assertFalse(event.isCancelled());
        }

        @Test
        @DisplayName("Given setCancelled(true), When isCancelled is called, Then returns true")
        void isCancelled_returnsTrue_afterSetCancelledTrue() {
            QuestChainDefinition definition = buildDefinition("expire_cancel_set");
            UUID playerUUID = UUID.randomUUID();

            QuestChainExpireEvent event = new QuestChainExpireEvent(definition, playerUUID, null);
            event.setCancelled(true);

            assertTrue(event.isCancelled());
        }

        @Test
        @DisplayName("Given setCancelled toggled, When isCancelled is called, Then reflects the latest state")
        void isCancelled_reflectsLatestState_afterToggle() {
            QuestChainDefinition definition = buildDefinition("expire_cancel_toggle");
            UUID playerUUID = UUID.randomUUID();

            QuestChainExpireEvent event = new QuestChainExpireEvent(definition, playerUUID, null);
            event.setCancelled(true);
            event.setCancelled(false);

            assertFalse(event.isCancelled());
        }
    }

    @Nested
    @DisplayName("Handler list")
    class HandlerListTests {

        @Test
        @DisplayName("getHandlers returns the static handler list")
        void getHandlers_matchesStaticHandlerList() {
            QuestChainDefinition definition = buildDefinition("expire_hl");
            UUID playerUUID = UUID.randomUUID();

            QuestChainExpireEvent event = new QuestChainExpireEvent(definition, playerUUID, null);

            assertSame(QuestChainExpireEvent.getHandlerList(), event.getHandlers());
        }
    }
}
