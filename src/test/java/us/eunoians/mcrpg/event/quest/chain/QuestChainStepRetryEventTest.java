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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Tests for {@link QuestChainStepRetryEvent}.
 */
class QuestChainStepRetryEventTest extends McRPGBaseTest {

    private static QuestChainDefinition buildDefinition(String key) {
        NamespacedKey chainKey = new NamespacedKey("mcrpg", key);
        NamespacedKey sourceKey = new NamespacedKey("mcrpg", "manual");
        NamespacedKey triggerKey = new NamespacedKey("mcrpg", "manual");
        NamespacedKey questKey = new NamespacedKey("mcrpg", key + "_quest");
        List<QuestChainStep> steps = List.of(QuestChainStep.simple(questKey));
        return new QuestChainDefinition.Builder(chainKey, sourceKey, triggerKey, steps).build();
    }

    private static QuestChainStep buildStep(String key) {
        return QuestChainStep.simple(new NamespacedKey("mcrpg", key));
    }

    @Nested
    @DisplayName("Constructor and getters")
    class ConstructorAndGetters {

        @Test
        @DisplayName("Given all parameters with online player, When getters are called, Then they return the constructor values")
        void getters_returnConstructorValues_withOnlinePlayer() {
            QuestChainDefinition definition = buildDefinition("retry_online");
            PlayerMock player = server.addPlayer();
            UUID playerUUID = player.getUniqueId();
            QuestChainStep step = buildStep("retry_step");

            QuestChainStepRetryEvent event = new QuestChainStepRetryEvent(
                    definition, player, playerUUID, step, 1, 3);

            assertSame(definition, event.getChainDefinition());
            assertSame(player, event.getPlayer());
            assertEquals(playerUUID, event.getPlayerUUID());
            assertSame(step, event.getStep());
            assertEquals(1, event.getRetryNumber());
            assertEquals(3, event.getMaxRetries());
        }

        @Test
        @DisplayName("Given null player, When getPlayer is called, Then returns null")
        void getPlayer_returnsNull_whenPlayerIsNull() {
            QuestChainDefinition definition = buildDefinition("retry_offline");
            UUID playerUUID = UUID.randomUUID();
            QuestChainStep step = buildStep("retry_offline_step");

            QuestChainStepRetryEvent event = new QuestChainStepRetryEvent(
                    definition, null, playerUUID, step, 2, 5);

            assertNull(event.getPlayer());
        }

        @Test
        @DisplayName("Given null player, When getPlayerUUID is called, Then still returns the UUID")
        void getPlayerUUID_returnsUUID_evenWhenPlayerIsNull() {
            QuestChainDefinition definition = buildDefinition("retry_uuid");
            UUID playerUUID = UUID.randomUUID();
            QuestChainStep step = buildStep("retry_uuid_step");

            QuestChainStepRetryEvent event = new QuestChainStepRetryEvent(
                    definition, null, playerUUID, step, 1, 3);

            assertEquals(playerUUID, event.getPlayerUUID());
        }
    }

    @Nested
    @DisplayName("Retry number")
    class RetryNumberTests {

        @Test
        @DisplayName("Given retryNumber=1, When getRetryNumber is called, Then returns 1")
        void getRetryNumber_returnsOne_forFirstRetry() {
            QuestChainDefinition definition = buildDefinition("retry_first");
            QuestChainStep step = buildStep("retry_first_step");

            QuestChainStepRetryEvent event = new QuestChainStepRetryEvent(
                    definition, null, UUID.randomUUID(), step, 1, 5);

            assertEquals(1, event.getRetryNumber());
        }

        @Test
        @DisplayName("Given retryNumber equal to maxRetries, When getRetryNumber is called, Then returns the max value")
        void getRetryNumber_returnsMax_whenAtLimit() {
            QuestChainDefinition definition = buildDefinition("retry_at_max");
            QuestChainStep step = buildStep("retry_at_max_step");

            QuestChainStepRetryEvent event = new QuestChainStepRetryEvent(
                    definition, null, UUID.randomUUID(), step, 5, 5);

            assertEquals(5, event.getRetryNumber());
        }
    }

    @Nested
    @DisplayName("Max retries")
    class MaxRetriesTests {

        @Test
        @DisplayName("Given maxRetries=-1, When getMaxRetries is called, Then returns -1 for unlimited")
        void getMaxRetries_returnsNegativeOne_forUnlimited() {
            QuestChainDefinition definition = buildDefinition("retry_unlimited");
            QuestChainStep step = buildStep("retry_unlimited_step");

            QuestChainStepRetryEvent event = new QuestChainStepRetryEvent(
                    definition, null, UUID.randomUUID(), step, 3, -1);

            assertEquals(-1, event.getMaxRetries());
        }

        @Test
        @DisplayName("Given maxRetries=0, When getMaxRetries is called, Then returns 0")
        void getMaxRetries_returnsZero() {
            QuestChainDefinition definition = buildDefinition("retry_zero_max");
            QuestChainStep step = buildStep("retry_zero_max_step");

            QuestChainStepRetryEvent event = new QuestChainStepRetryEvent(
                    definition, null, UUID.randomUUID(), step, 1, 0);

            assertEquals(0, event.getMaxRetries());
        }

        @Test
        @DisplayName("Given a positive maxRetries, When getMaxRetries is called, Then returns the positive value")
        void getMaxRetries_returnsPositiveValue() {
            QuestChainDefinition definition = buildDefinition("retry_positive_max");
            QuestChainStep step = buildStep("retry_positive_max_step");

            QuestChainStepRetryEvent event = new QuestChainStepRetryEvent(
                    definition, null, UUID.randomUUID(), step, 1, 10);

            assertEquals(10, event.getMaxRetries());
        }
    }

    @Nested
    @DisplayName("Handler list")
    class HandlerListTests {

        @Test
        @DisplayName("getHandlers returns the static handler list")
        void getHandlers_matchesStaticHandlerList() {
            QuestChainDefinition definition = buildDefinition("retry_hl");
            QuestChainStep step = buildStep("retry_hl_step");

            QuestChainStepRetryEvent event = new QuestChainStepRetryEvent(
                    definition, null, UUID.randomUUID(), step, 1, 3);

            assertSame(QuestChainStepRetryEvent.getHandlerList(), event.getHandlers());
        }
    }
}
