package us.eunoians.mcrpg.event.quest.chain;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

public class CascadeFinalizeEventTest {

    private static final NamespacedKey CHAIN_KEY = new NamespacedKey("mcrpg", "test_chain");
    private static final NamespacedKey QUEST_A = new NamespacedKey("mcrpg", "quest_a");
    private static final NamespacedKey QUEST_B = new NamespacedKey("mcrpg", "quest_b");

    @Test
    @DisplayName("Given a CascadeFinalizeEvent with auto-completed steps, when getAutoCompletedSteps is called, then it returns the steps")
    public void getAutoCompletedSteps_returnsSteps() {
        UUID playerUUID = UUID.randomUUID();
        List<CascadeCompletedStep> steps = List.of(
                new CascadeCompletedStep(QUEST_A, "Quest A"),
                new CascadeCompletedStep(QUEST_B, "Quest B")
        );
        CascadeFinalizeEvent event = new CascadeFinalizeEvent(
                CHAIN_KEY, playerUUID, null, steps, QUEST_B, CascadeOutcome.SUCCESS);

        assertEquals(2, event.getAutoCompletedSteps().size());
        assertEquals(QUEST_A, event.getAutoCompletedSteps().get(0).questKey());
        assertEquals(QUEST_B, event.getAutoCompletedSteps().get(1).questKey());
    }

    @Test
    @DisplayName("Given a CascadeFinalizeEvent with auto-completed steps, when hadAutoCompletedSteps is called, then it returns true")
    public void hadAutoCompletedSteps_returnsTrue_whenStepsExist() {
        CascadeFinalizeEvent event = new CascadeFinalizeEvent(
                CHAIN_KEY, UUID.randomUUID(), null,
                List.of(new CascadeCompletedStep(QUEST_A, "Quest A")), null,
                CascadeOutcome.SUCCESS);

        assertTrue(event.hadAutoCompletedSteps());
    }

    @Test
    @DisplayName("Given a CascadeFinalizeEvent with no auto-completed steps, when hadAutoCompletedSteps is called, then it returns false")
    public void hadAutoCompletedSteps_returnsFalse_whenNoSteps() {
        CascadeFinalizeEvent event = new CascadeFinalizeEvent(
                CHAIN_KEY, UUID.randomUUID(), null, List.of(), null,
                CascadeOutcome.SUCCESS);

        assertFalse(event.hadAutoCompletedSteps());
    }

    @Test
    @DisplayName("Given a CascadeFinalizeEvent with a last started quest key, when getLastStartedQuestKey is called, then it returns the key")
    public void getLastStartedQuestKey_returnsKey() {
        CascadeFinalizeEvent event = new CascadeFinalizeEvent(
                CHAIN_KEY, UUID.randomUUID(), null, List.of(), QUEST_A,
                CascadeOutcome.SUCCESS);

        assertEquals(Optional.of(QUEST_A), event.getLastStartedQuestKey());
    }

    @Test
    @DisplayName("Given a CascadeFinalizeEvent with no last started quest key, when getLastStartedQuestKey is called, then it returns empty")
    public void getLastStartedQuestKey_returnsEmpty_whenNull() {
        CascadeFinalizeEvent event = new CascadeFinalizeEvent(
                CHAIN_KEY, UUID.randomUUID(), null, List.of(), null,
                CascadeOutcome.SUCCESS);

        assertEquals(Optional.empty(), event.getLastStartedQuestKey());
    }

    @Test
    @DisplayName("Given a CascadeFinalizeEvent with a player, when getPlayer is called, then it returns the player")
    public void getPlayer_returnsPlayer() {
        Player player = mock(Player.class);
        CascadeFinalizeEvent event = new CascadeFinalizeEvent(
                CHAIN_KEY, UUID.randomUUID(), player, List.of(), null,
                CascadeOutcome.SUCCESS);

        assertEquals(player, event.getPlayer());
    }

    @Test
    @DisplayName("Given a CascadeFinalizeEvent with null player, when getPlayer is called, then it returns null")
    public void getPlayer_returnsNull_whenOffline() {
        CascadeFinalizeEvent event = new CascadeFinalizeEvent(
                CHAIN_KEY, UUID.randomUUID(), null, List.of(), null,
                CascadeOutcome.SUCCESS);

        assertNull(event.getPlayer());
    }

    @Test
    @DisplayName("Given a CascadeFinalizeEvent, when getAutoCompletedSteps is called, then the returned list is unmodifiable")
    public void getAutoCompletedSteps_returnsUnmodifiableList() {
        CascadeFinalizeEvent event = new CascadeFinalizeEvent(
                CHAIN_KEY, UUID.randomUUID(), null,
                List.of(new CascadeCompletedStep(QUEST_A, "A")), null,
                CascadeOutcome.SUCCESS);

        assertThrows(UnsupportedOperationException.class,
                () -> event.getAutoCompletedSteps().add(
                        new CascadeCompletedStep(QUEST_B, "B")));
    }

    @Test
    @DisplayName("Given a CascadeFinalizeEvent with SUCCESS outcome, when getOutcome is called, then it returns SUCCESS")
    public void getOutcome_returnsSuccess() {
        CascadeFinalizeEvent event = new CascadeFinalizeEvent(
                CHAIN_KEY, UUID.randomUUID(), null, List.of(), null,
                CascadeOutcome.SUCCESS);

        assertEquals(CascadeOutcome.SUCCESS, event.getOutcome());
    }

    @Test
    @DisplayName("Given a CascadeFinalizeEvent with DEPTH_LIMIT_REACHED outcome, when getOutcome is called, then it returns DEPTH_LIMIT_REACHED")
    public void getOutcome_returnsDepthLimit() {
        CascadeFinalizeEvent event = new CascadeFinalizeEvent(
                CHAIN_KEY, UUID.randomUUID(), null, List.of(), null,
                CascadeOutcome.DEPTH_LIMIT_REACHED);

        assertEquals(CascadeOutcome.DEPTH_LIMIT_REACHED, event.getOutcome());
    }

    @Test
    @DisplayName("Given a CascadeFinalizeEvent with ERROR outcome, when getOutcome is called, then it returns ERROR")
    public void getOutcome_returnsError() {
        CascadeFinalizeEvent event = new CascadeFinalizeEvent(
                CHAIN_KEY, UUID.randomUUID(), null, List.of(), null,
                CascadeOutcome.ERROR);

        assertEquals(CascadeOutcome.ERROR, event.getOutcome());
    }

    @Test
    @DisplayName("Given a CascadeFinalizeEvent, when getHandlers is called, then it returns a non-null handler list")
    public void getHandlers_returnsNonNull() {
        CascadeFinalizeEvent event = new CascadeFinalizeEvent(
                CHAIN_KEY, UUID.randomUUID(), null, List.of(), null,
                CascadeOutcome.SUCCESS);

        assertNotNull(event.getHandlers());
    }

    @Test
    @DisplayName("Given the CascadeFinalizeEvent class, when getHandlerList is called statically, then it returns a non-null handler list")
    public void getHandlerList_static_returnsNonNull() {
        assertNotNull(CascadeFinalizeEvent.getHandlerList());
    }
}
