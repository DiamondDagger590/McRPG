package us.eunoians.mcrpg.quest.chain;

import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class QuestChainPlayerStateTest extends McRPGBaseTest {

    private NamespacedKey chainKey;
    private NamespacedKey questKeyA;
    private NamespacedKey questKeyB;

    @BeforeEach
    public void setUp() {
        chainKey = new NamespacedKey("mcrpg", "test_chain");
        questKeyA = new NamespacedKey("mcrpg", "quest_a");
        questKeyB = new NamespacedKey("mcrpg", "quest_b");
    }

    @Test
    @DisplayName("Given chain and first quest key, When newActive is called, Then state is ACTIVE with 0 completions")
    public void newActive_createsActiveState_withZeroCompletions() {
        var state = QuestChainPlayerState.newActive(chainKey, questKeyA);

        assertEquals(QuestChainState.ACTIVE, state.getState());
        assertEquals(chainKey, state.getChainKey());
        assertTrue(state.getCurrentQuestKey().isPresent());
        assertEquals(questKeyA, state.getCurrentQuestKey().get());
        assertEquals(0, state.getCompletionCount());
        assertFalse(state.getLastCompletedAt().isPresent());
        assertFalse(state.isDirty());
    }

    @Test
    @DisplayName("Given active state, When advance is called, Then currentQuestKey updates and state is dirty")
    public void advance_updatesCurrentQuestKey_andMarksDirty() {
        var state = QuestChainPlayerState.newActive(chainKey, questKeyA);
        state.advance(questKeyB);

        assertEquals(questKeyB, state.getCurrentQuestKey().orElseThrow());
        assertTrue(state.isDirty());
    }

    @Test
    @DisplayName("Given active state, When complete is called, Then state is COMPLETED with incremented count")
    public void complete_setsCompleted_andIncrementsCount() {
        var state = QuestChainPlayerState.newActive(chainKey, questKeyA);
        long timestamp = 1_000_000L;
        state.complete(timestamp);

        assertEquals(QuestChainState.COMPLETED, state.getState());
        assertFalse(state.getCurrentQuestKey().isPresent());
        assertEquals(1, state.getCompletionCount());
        assertEquals(timestamp, state.getLastCompletedAt().orElseThrow());
        assertTrue(state.isDirty());
    }

    @Test
    @DisplayName("Given active state, When abandon is called, Then state is ABANDONED with null currentQuestKey")
    public void abandon_setsAbandoned_andNullsCurrentQuestKey() {
        var state = QuestChainPlayerState.newActive(chainKey, questKeyA);
        state.abandon();

        assertEquals(QuestChainState.ABANDONED, state.getState());
        assertFalse(state.getCurrentQuestKey().isPresent());
        assertTrue(state.isDirty());
    }

    @Test
    @DisplayName("Given active state, When fail is called, Then state is FAILED with null currentQuestKey")
    public void fail_setsFailed_andNullsCurrentQuestKey() {
        var state = QuestChainPlayerState.newActive(chainKey, questKeyA);
        state.fail();

        assertEquals(QuestChainState.FAILED, state.getState());
        assertFalse(state.getCurrentQuestKey().isPresent());
        assertTrue(state.isDirty());
    }

    @Test
    @DisplayName("Given terminal state, When resetToStep is called, Then state is ACTIVE at given step")
    public void resetToStep_setsActive_atGivenStep() {
        var state = QuestChainPlayerState.newActive(chainKey, questKeyA);
        state.fail();
        state.resetToStep(questKeyB);

        assertEquals(QuestChainState.ACTIVE, state.getState());
        assertEquals(questKeyB, state.getCurrentQuestKey().orElseThrow());
        assertTrue(state.isDirty());
    }

    @Test
    @DisplayName("Given completed state with history, When hardReset is called, Then count is cleared and state is ACTIVE")
    public void hardReset_clearsCountAndTimestamp_andSetsActive() {
        var state = QuestChainPlayerState.newActive(chainKey, questKeyA);
        state.complete(999L);
        state.clearDirty();

        state.hardReset(questKeyA);

        assertEquals(QuestChainState.ACTIVE, state.getState());
        assertEquals(0, state.getCompletionCount());
        assertFalse(state.getLastCompletedAt().isPresent());
        assertEquals(questKeyA, state.getCurrentQuestKey().orElseThrow());
        assertTrue(state.isDirty());
    }

    @Test
    @DisplayName("Given dirty state, When clearDirty is called, Then isDirty returns false")
    public void clearDirty_clearsDirtyFlag() {
        var state = QuestChainPlayerState.newActive(chainKey, questKeyA);
        state.advance(questKeyB);
        assertTrue(state.isDirty());

        state.clearDirty();
        assertFalse(state.isDirty());
    }
}
