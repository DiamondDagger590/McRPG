package us.eunoians.mcrpg.event.quest;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.quest.chain.CascadeContext;

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
        List<CascadeContext.CascadeCompletedStep> steps = List.of(
                new CascadeContext.CascadeCompletedStep(QUEST_A, "Quest A"),
                new CascadeContext.CascadeCompletedStep(QUEST_B, "Quest B")
        );
        CascadeFinalizeEvent event = new CascadeFinalizeEvent(
                CHAIN_KEY, playerUUID, null, steps, QUEST_B);

        assertEquals(2, event.getAutoCompletedSteps().size());
        assertEquals(QUEST_A, event.getAutoCompletedSteps().get(0).questKey());
        assertEquals(QUEST_B, event.getAutoCompletedSteps().get(1).questKey());
    }

    @Test
    @DisplayName("Given a CascadeFinalizeEvent with auto-completed steps, when hadAutoCompletedSteps is called, then it returns true")
    public void hadAutoCompletedSteps_returnsTrue_whenStepsExist() {
        CascadeFinalizeEvent event = new CascadeFinalizeEvent(
                CHAIN_KEY, UUID.randomUUID(), null,
                List.of(new CascadeContext.CascadeCompletedStep(QUEST_A, "Quest A")), null);

        assertTrue(event.hadAutoCompletedSteps());
    }

    @Test
    @DisplayName("Given a CascadeFinalizeEvent with no auto-completed steps, when hadAutoCompletedSteps is called, then it returns false")
    public void hadAutoCompletedSteps_returnsFalse_whenNoSteps() {
        CascadeFinalizeEvent event = new CascadeFinalizeEvent(
                CHAIN_KEY, UUID.randomUUID(), null, List.of(), null);

        assertFalse(event.hadAutoCompletedSteps());
    }

    @Test
    @DisplayName("Given a CascadeFinalizeEvent with a last started quest key, when getLastStartedQuestKey is called, then it returns the key")
    public void getLastStartedQuestKey_returnsKey() {
        CascadeFinalizeEvent event = new CascadeFinalizeEvent(
                CHAIN_KEY, UUID.randomUUID(), null, List.of(), QUEST_A);

        assertEquals(Optional.of(QUEST_A), event.getLastStartedQuestKey());
    }

    @Test
    @DisplayName("Given a CascadeFinalizeEvent with no last started quest key, when getLastStartedQuestKey is called, then it returns empty")
    public void getLastStartedQuestKey_returnsEmpty_whenNull() {
        CascadeFinalizeEvent event = new CascadeFinalizeEvent(
                CHAIN_KEY, UUID.randomUUID(), null, List.of(), null);

        assertEquals(Optional.empty(), event.getLastStartedQuestKey());
    }

    @Test
    @DisplayName("Given a CascadeFinalizeEvent with a player, when getPlayer is called, then it returns the player")
    public void getPlayer_returnsPlayer() {
        Player player = mock(Player.class);
        CascadeFinalizeEvent event = new CascadeFinalizeEvent(
                CHAIN_KEY, UUID.randomUUID(), player, List.of(), null);

        assertEquals(player, event.getPlayer());
    }

    @Test
    @DisplayName("Given a CascadeFinalizeEvent with null player, when getPlayer is called, then it returns null")
    public void getPlayer_returnsNull_whenOffline() {
        CascadeFinalizeEvent event = new CascadeFinalizeEvent(
                CHAIN_KEY, UUID.randomUUID(), null, List.of(), null);

        assertNull(event.getPlayer());
    }

    @Test
    @DisplayName("Given a CascadeFinalizeEvent, when getAutoCompletedSteps is called, then the returned list is unmodifiable")
    public void getAutoCompletedSteps_returnsUnmodifiableList() {
        CascadeFinalizeEvent event = new CascadeFinalizeEvent(
                CHAIN_KEY, UUID.randomUUID(), null,
                List.of(new CascadeContext.CascadeCompletedStep(QUEST_A, "A")), null);

        assertThrows(UnsupportedOperationException.class,
                () -> event.getAutoCompletedSteps().add(
                        new CascadeContext.CascadeCompletedStep(QUEST_B, "B")));
    }

    @Test
    @DisplayName("Given a CascadeFinalizeEvent, when getHandlers is called, then it returns a non-null handler list")
    public void getHandlers_returnsNonNull() {
        CascadeFinalizeEvent event = new CascadeFinalizeEvent(
                CHAIN_KEY, UUID.randomUUID(), null, List.of(), null);

        assertNotNull(event.getHandlers());
    }

    @Test
    @DisplayName("Given the CascadeFinalizeEvent class, when getHandlerList is called statically, then it returns a non-null handler list")
    public void getHandlerList_static_returnsNonNull() {
        assertNotNull(CascadeFinalizeEvent.getHandlerList());
    }
}
