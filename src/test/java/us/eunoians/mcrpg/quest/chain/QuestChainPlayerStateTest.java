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
        assertTrue(state.isDirty());
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

    @Test
    @DisplayName("Given active state, When expire is called, Then state is EXPIRED with null currentQuestKey")
    public void expire_setsExpired_andNullsCurrentQuestKey() {
        var state = QuestChainPlayerState.newActive(chainKey, questKeyA);
        state.expire();

        assertEquals(QuestChainState.EXPIRED, state.getState());
        assertFalse(state.getCurrentQuestKey().isPresent());
        assertTrue(state.isDirty());
    }

    @Test
    @DisplayName("Given a dirty state, When clearDirtyIfCurrent is called with the correct snapshot version, Then dirty is cleared")
    public void clearDirtyIfCurrent_clearsDirty_whenVersionMatches() {
        var state = QuestChainPlayerState.newActive(chainKey, questKeyA);
        state.advance(questKeyB);
        int snapshot = state.getDirtyVersion();

        boolean cleared = state.clearDirtyIfCurrent(snapshot);

        assertTrue(cleared, "clearDirtyIfCurrent should return true when version matches");
        assertFalse(state.isDirty(), "State should no longer be dirty after successful CAS");
    }

    @Test
    @DisplayName("Given a mutation after snapshot, When clearDirtyIfCurrent is called with the stale version, Then dirty is retained")
    public void clearDirtyIfCurrent_retainsDirty_whenVersionStale() {
        var state = QuestChainPlayerState.newActive(chainKey, questKeyA);
        state.advance(questKeyB);
        int staleSnapshot = state.getDirtyVersion();

        state.abandon();

        boolean cleared = state.clearDirtyIfCurrent(staleSnapshot);

        assertFalse(cleared, "clearDirtyIfCurrent should return false when a newer mutation has occurred");
        assertTrue(state.isDirty(), "State should remain dirty when the snapshot version is stale");
    }

    @Test
    @DisplayName("Given a new active state, When newActive returns, Then isDirty is true")
    public void newActive_returnsDirtyState() {
        var state = QuestChainPlayerState.newActive(chainKey, questKeyA);

        assertTrue(state.isDirty(), "newActive must return a dirty state so fast-quit persists it");
        assertTrue(state.getDirtyVersion() > 0, "Dirty version must be positive after newActive");
    }

    @Test
    @DisplayName("Given a state with no advancements, When recordAdvancement is called, Then getPendingAdvancements returns one entry")
    public void recordAdvancement_addsEntry_toPendingList() {
        var state = QuestChainPlayerState.newActive(chainKey, questKeyA);
        assertTrue(state.getPendingAdvancements().isEmpty());

        state.recordAdvancement(questKeyA, 1000L, 1);

        assertEquals(1, state.getPendingAdvancements().size());
        var adv = state.getPendingAdvancements().get(0);
        assertEquals(questKeyA, adv.questKey());
        assertEquals(1000L, adv.completedAt());
        assertEquals(1, adv.completionNumber());
    }

    @Test
    @DisplayName("Given a state with pending advancements, When clearPendingAdvancements is called, Then list is empty")
    public void clearPendingAdvancements_removesAllEntries() {
        var state = QuestChainPlayerState.newActive(chainKey, questKeyA);
        state.recordAdvancement(questKeyA, 1000L, 1);
        state.recordAdvancement(questKeyB, 2000L, 1);
        assertEquals(2, state.getPendingAdvancements().size());

        state.clearPendingAdvancements();

        assertTrue(state.getPendingAdvancements().isEmpty());
    }

    @Test
    @DisplayName("Given multiple advancements recorded, When getPendingAdvancements is called, Then entries preserve insertion order")
    public void recordAdvancement_preservesInsertionOrder() {
        var state = QuestChainPlayerState.newActive(chainKey, questKeyA);
        state.recordAdvancement(questKeyA, 1000L, 1);
        state.recordAdvancement(questKeyB, 2000L, 1);

        var advancements = state.getPendingAdvancements();
        assertEquals(2, advancements.size());
        assertEquals(questKeyA, advancements.get(0).questKey());
        assertEquals(questKeyB, advancements.get(1).questKey());
    }

    @Test
    @DisplayName("Given a clean state, When recordAdvancement is called, Then isDirty returns true")
    public void recordAdvancement_marksDirty() {
        var state = new QuestChainPlayerState(chainKey, questKeyA, QuestChainState.ACTIVE, 0, null);
        assertFalse(state.isDirty(), "State must start clean");

        state.recordAdvancement(questKeyA, 1000L, 1);

        assertTrue(state.isDirty(),
                "recordAdvancement must mark state dirty so logout flush picks it up even without a normal state mutation");
    }
}
