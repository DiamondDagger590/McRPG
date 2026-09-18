package us.eunoians.mcrpg.event.quest.chain;

import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import us.eunoians.mcrpg.McRPGBaseTest;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for the cascade event types: {@link CascadeStartEvent}, {@link CascadeFinalizeEvent},
 * {@link CascadeCompletedStep}, {@link CascadeOutcome}, and {@link ChainCompletionSource}.
 */
public class CascadeEventTest extends McRPGBaseTest {

    @Nested
    @DisplayName("CascadeStartEvent")
    class CascadeStartEventTests {

        @Test
        @DisplayName("Given a cascade start event with an online player, when getters are called, then they return the constructor values")
        void getters_returnConstructorValues() {
            NamespacedKey chainKey = new NamespacedKey("mcrpg", "test_chain");
            PlayerMock player = server.addPlayer();

            CascadeStartEvent event = new CascadeStartEvent(chainKey, player.getUniqueId(), player);

            assertEquals(chainKey, event.getChainKey());
            assertEquals(player.getUniqueId(), event.getPlayerUUID());
            assertSame(player, event.getPlayer());
        }

        @Test
        @DisplayName("Given a cascade start event with a null player, when getPlayer() is called, then null is returned")
        void getPlayer_returnsNull_whenPlayerOffline() {
            NamespacedKey chainKey = new NamespacedKey("mcrpg", "test_chain");
            UUID offlineUUID = UUID.randomUUID();

            CascadeStartEvent event = new CascadeStartEvent(chainKey, offlineUUID, null);

            assertNull(event.getPlayer());
            assertEquals(offlineUUID, event.getPlayerUUID());
        }

        @Test
        @DisplayName("Given a cascade start event, when getHandlers() is called, then the static HandlerList is returned")
        void getHandlers_returnsHandlerList() {
            NamespacedKey chainKey = new NamespacedKey("mcrpg", "test_chain");

            CascadeStartEvent event = new CascadeStartEvent(chainKey, UUID.randomUUID(), null);

            assertEquals(CascadeStartEvent.getHandlerList(), event.getHandlers());
        }
    }

    @Nested
    @DisplayName("CascadeFinalizeEvent")
    class CascadeFinalizeEventTests {

        @Test
        @DisplayName("Given a finalize event with auto-completed steps, when getters are called, then they return the constructor values")
        void getters_returnConstructorValues() {
            NamespacedKey chainKey = new NamespacedKey("mcrpg", "test_chain");
            PlayerMock player = server.addPlayer();
            NamespacedKey questKey = new NamespacedKey("mcrpg", "quest_one");
            List<CascadeCompletedStep> steps = List.of(new CascadeCompletedStep(questKey, "First Quest"));
            NamespacedKey lastStarted = new NamespacedKey("mcrpg", "quest_two");

            CascadeFinalizeEvent event = new CascadeFinalizeEvent(chainKey, player.getUniqueId(), player,
                    steps, lastStarted, CascadeOutcome.SUCCESS);

            assertEquals(chainKey, event.getChainKey());
            assertEquals(player.getUniqueId(), event.getPlayerUUID());
            assertSame(player, event.getPlayer());
            assertEquals(steps, event.getAutoCompletedSteps());
            assertEquals(Optional.of(lastStarted), event.getLastStartedQuestKey());
            assertEquals(CascadeOutcome.SUCCESS, event.getOutcome());
        }

        @Test
        @DisplayName("Given a finalize event with auto-completed steps, when hadAutoCompletedSteps() is called, then true is returned")
        void hadAutoCompletedSteps_returnsTrue_whenStepsExist() {
            NamespacedKey chainKey = new NamespacedKey("mcrpg", "test_chain");
            NamespacedKey questKey = new NamespacedKey("mcrpg", "quest_one");
            List<CascadeCompletedStep> steps = List.of(new CascadeCompletedStep(questKey, "Quest"));

            CascadeFinalizeEvent event = new CascadeFinalizeEvent(chainKey, UUID.randomUUID(), null,
                    steps, null, CascadeOutcome.SUCCESS);

            assertTrue(event.hadAutoCompletedSteps());
        }

        @Test
        @DisplayName("Given a finalize event with no auto-completed steps, when hadAutoCompletedSteps() is called, then false is returned")
        void hadAutoCompletedSteps_returnsFalse_whenNoSteps() {
            NamespacedKey chainKey = new NamespacedKey("mcrpg", "test_chain");

            CascadeFinalizeEvent event = new CascadeFinalizeEvent(chainKey, UUID.randomUUID(), null,
                    List.of(), null, CascadeOutcome.SUCCESS);

            assertFalse(event.hadAutoCompletedSteps());
        }

        @Test
        @DisplayName("Given a finalize event with a null lastStartedQuestKey, when getLastStartedQuestKey() is called, then empty Optional is returned")
        void getLastStartedQuestKey_returnsEmpty_whenNull() {
            NamespacedKey chainKey = new NamespacedKey("mcrpg", "test_chain");

            CascadeFinalizeEvent event = new CascadeFinalizeEvent(chainKey, UUID.randomUUID(), null,
                    List.of(), null, CascadeOutcome.DEPTH_LIMIT_REACHED);

            assertEquals(Optional.empty(), event.getLastStartedQuestKey());
        }

        @Test
        @DisplayName("Given a finalize event, when the auto-completed steps list is modified, then UnsupportedOperationException is thrown")
        void autoCompletedSteps_isUnmodifiable() {
            NamespacedKey chainKey = new NamespacedKey("mcrpg", "test_chain");
            NamespacedKey questKey = new NamespacedKey("mcrpg", "quest_one");
            List<CascadeCompletedStep> mutableSteps = new ArrayList<>();
            mutableSteps.add(new CascadeCompletedStep(questKey, "Quest"));

            CascadeFinalizeEvent event = new CascadeFinalizeEvent(chainKey, UUID.randomUUID(), null,
                    mutableSteps, null, CascadeOutcome.SUCCESS);

            assertThrows(UnsupportedOperationException.class, () ->
                    event.getAutoCompletedSteps().add(new CascadeCompletedStep(questKey, "Another")));
        }

        @Test
        @DisplayName("Given a finalize event with a null player, when getPlayer() is called, then null is returned")
        void getPlayer_returnsNull_whenPlayerOffline() {
            NamespacedKey chainKey = new NamespacedKey("mcrpg", "test_chain");

            CascadeFinalizeEvent event = new CascadeFinalizeEvent(chainKey, UUID.randomUUID(), null,
                    List.of(), null, CascadeOutcome.ERROR);

            assertNull(event.getPlayer());
        }

        @Test
        @DisplayName("Given a finalize event, when getHandlers() is called, then the static HandlerList is returned")
        void getHandlers_returnsHandlerList() {
            NamespacedKey chainKey = new NamespacedKey("mcrpg", "test_chain");

            CascadeFinalizeEvent event = new CascadeFinalizeEvent(chainKey, UUID.randomUUID(), null,
                    List.of(), null, CascadeOutcome.SUCCESS);

            assertEquals(CascadeFinalizeEvent.getHandlerList(), event.getHandlers());
        }
    }

    @Nested
    @DisplayName("CascadeCompletedStep")
    class CascadeCompletedStepTests {

        @Test
        @DisplayName("Given a CascadeCompletedStep record, when accessors are called, then they return the constructor values")
        void accessors_returnConstructorValues() {
            NamespacedKey questKey = new NamespacedKey("mcrpg", "quest_one");
            String displayName = "First Quest";

            CascadeCompletedStep step = new CascadeCompletedStep(questKey, displayName);

            assertEquals(questKey, step.questKey());
            assertEquals(displayName, step.displayName());
        }

        @Test
        @DisplayName("Given two CascadeCompletedStep records with the same values, when equals is called, then they are equal")
        void equals_returnsTrue_forSameValues() {
            NamespacedKey questKey = new NamespacedKey("mcrpg", "quest_one");
            CascadeCompletedStep step1 = new CascadeCompletedStep(questKey, "Quest");
            CascadeCompletedStep step2 = new CascadeCompletedStep(questKey, "Quest");

            assertEquals(step1, step2);
            assertEquals(step1.hashCode(), step2.hashCode());
        }
    }

    @Nested
    @DisplayName("CascadeOutcome")
    class CascadeOutcomeTests {

        @Test
        @DisplayName("CascadeOutcome enum has exactly three values")
        void hasThreeValues() {
            assertEquals(3, CascadeOutcome.values().length);
        }

        @ParameterizedTest
        @EnumSource(CascadeOutcome.class)
        @DisplayName("Given each CascadeOutcome, when valueOf is called with its name, then the same constant is returned")
        void valueOf_roundTrips(CascadeOutcome outcome) {
            assertEquals(outcome, CascadeOutcome.valueOf(outcome.name()));
        }
    }

    @Nested
    @DisplayName("ChainCompletionSource")
    class ChainCompletionSourceTests {

        @Test
        @DisplayName("ChainCompletionSource enum has exactly three values")
        void hasThreeValues() {
            assertEquals(3, ChainCompletionSource.values().length);
        }

        @ParameterizedTest
        @EnumSource(ChainCompletionSource.class)
        @DisplayName("Given each ChainCompletionSource, when valueOf is called with its name, then the same constant is returned")
        void valueOf_roundTrips(ChainCompletionSource source) {
            assertEquals(source, ChainCompletionSource.valueOf(source.name()));
        }
    }
}
