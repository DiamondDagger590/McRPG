package us.eunoians.mcrpg.quest.chain;

import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class QuestChainPlayerDataTest extends McRPGBaseTest {

    private QuestChainPlayerData data;
    private NamespacedKey chainKeyA;
    private NamespacedKey chainKeyB;
    private NamespacedKey questKeyA;
    private NamespacedKey questKeyB;

    @BeforeEach
    public void setUp() {
        data = new QuestChainPlayerData();
        chainKeyA = new NamespacedKey("mcrpg", "chain_a");
        chainKeyB = new NamespacedKey("mcrpg", "chain_b");
        questKeyA = new NamespacedKey("mcrpg", "quest_a");
        questKeyB = new NamespacedKey("mcrpg", "quest_b");
    }

    @Test
    @DisplayName("Given a chain state, When putChainState is called, Then it is retrievable via getChainState")
    public void putChainState_addsState_retrievableViaGet() {
        var state = QuestChainPlayerState.newActive(chainKeyA, questKeyA);
        data.putChainState(state);

        assertTrue(data.getChainState(chainKeyA).isPresent());
        assertEquals(state, data.getChainState(chainKeyA).orElseThrow());
    }

    @Test
    @DisplayName("Given an unknown chain key, When getChainState is called, Then it returns empty")
    public void getChainState_returnsEmpty_whenKeyNotFound() {
        assertTrue(data.getChainState(chainKeyA).isEmpty());
    }

    @Test
    @DisplayName("Given a state, When removeChainState is called, Then it is no longer retrievable")
    public void removeChainState_removesState_subsequentGetReturnsEmpty() {
        data.putChainState(QuestChainPlayerState.newActive(chainKeyA, questKeyA));
        data.removeChainState(chainKeyA);

        assertTrue(data.getChainState(chainKeyA).isEmpty());
    }

    @Test
    @DisplayName("Given active and completed states, When getActiveChains is called, Then only ACTIVE state returned")
    public void getActiveChains_returnsOnlyActiveStates() {
        var active = QuestChainPlayerState.newActive(chainKeyA, questKeyA);
        var completed = QuestChainPlayerState.newActive(chainKeyB, questKeyB);
        completed.complete(Instant.ofEpochMilli(1000L));

        data.putChainState(active);
        data.putChainState(completed);

        var activeChains = data.getActiveChains();
        assertEquals(1, activeChains.size());
        assertEquals(chainKeyA, activeChains.get(0).getChainKey());
    }

    @Test
    @DisplayName("Given dirty and clean states, When getDirtyStates is called, Then only dirty states returned")
    public void getDirtyStates_returnsOnlyDirtyStates() {
        var stateA = QuestChainPlayerState.newActive(chainKeyA, questKeyA);
        stateA.advance(questKeyB);

        var stateB = new QuestChainPlayerState(chainKeyB, questKeyB, QuestChainState.ACTIVE, 0, null);

        data.putChainState(stateA);
        data.putChainState(stateB);

        var dirty = data.getDirtyStates();
        assertEquals(1, dirty.size());
        assertEquals(chainKeyA, dirty.get(0).getChainKey());
    }

    @Test
    @DisplayName("Given active chain with currentQuestKey, When putChainState, Then questKeyToChainKey index is updated")
    public void putChainState_updatesQuestKeyIndex_forActiveState() {
        data.putChainState(QuestChainPlayerState.newActive(chainKeyA, questKeyA));

        assertTrue(data.getChainKeyForCurrentQuest(questKeyA).isPresent());
        assertEquals(chainKeyA, data.getChainKeyForCurrentQuest(questKeyA).orElseThrow());
    }

    @Test
    @DisplayName("Given terminal state, When putChainState, Then questKeyToChainKey index is not populated")
    public void putChainState_doesNotPopulateIndex_forTerminalState() {
        var state = QuestChainPlayerState.newActive(chainKeyA, questKeyA);
        state.complete(Instant.ofEpochMilli(1000L));
        data.putChainState(state);

        assertTrue(data.getChainKeyForCurrentQuest(questKeyA).isEmpty());
    }

    @Test
    @DisplayName("Given chain with a currentQuestKey in index, When removeChainState, Then index no longer contains it")
    public void removeChainState_rebuildsIndex_removedChainQuestKeyNoLongerPresent() {
        data.putChainState(QuestChainPlayerState.newActive(chainKeyA, questKeyA));
        data.removeChainState(chainKeyA);

        assertTrue(data.getChainKeyForCurrentQuest(questKeyA).isEmpty());
    }

    @Test
    @DisplayName("Given two active chains, When rebuildQuestKeyIndex, Then each questKey maps to correct chainKey")
    public void rebuildQuestKeyIndex_mapsEachQuestKeyToCorrectChainKey() {
        data.putChainState(QuestChainPlayerState.newActive(chainKeyA, questKeyA));
        data.putChainState(QuestChainPlayerState.newActive(chainKeyB, questKeyB));

        data.rebuildQuestKeyIndex();

        assertEquals(chainKeyA, data.getChainKeyForCurrentQuest(questKeyA).orElseThrow());
        assertEquals(chainKeyB, data.getChainKeyForCurrentQuest(questKeyB).orElseThrow());
    }

    @Test
    @DisplayName("Given state with advanced questKey, When rebuildQuestKeyIndex, Then index reflects new key")
    public void rebuildQuestKeyIndex_updatesIndex_afterManualAdvance() {
        var state = QuestChainPlayerState.newActive(chainKeyA, questKeyA);
        data.putChainState(state);

        state.advance(questKeyB);
        data.rebuildQuestKeyIndex();

        assertFalse(data.getChainKeyForCurrentQuest(questKeyA).isPresent());
        assertEquals(chainKeyA, data.getChainKeyForCurrentQuest(questKeyB).orElseThrow());
    }

    @Test
    @DisplayName("Given multiple states, When putChainStateBatch is called, Then all states are retrievable")
    public void putChainStateBatch_addsAllStates() {
        var stateA = QuestChainPlayerState.newActive(chainKeyA, questKeyA);
        var stateB = QuestChainPlayerState.newActive(chainKeyB, questKeyB);

        data.putChainStateBatch(List.of(stateA, stateB));

        assertTrue(data.getChainState(chainKeyA).isPresent());
        assertTrue(data.getChainState(chainKeyB).isPresent());
    }

    @Test
    @DisplayName("Given active states in batch, When putChainStateBatch is called, Then quest key index is rebuilt")
    public void putChainStateBatch_rebuildsQuestKeyIndex() {
        var stateA = QuestChainPlayerState.newActive(chainKeyA, questKeyA);
        var stateB = QuestChainPlayerState.newActive(chainKeyB, questKeyB);

        data.putChainStateBatch(List.of(stateA, stateB));

        assertEquals(chainKeyA, data.getChainKeyForCurrentQuest(questKeyA).orElseThrow());
        assertEquals(chainKeyB, data.getChainKeyForCurrentQuest(questKeyB).orElseThrow());
    }

    @Test
    @DisplayName("Given an empty list, When putChainStateBatch is called, Then no states are added")
    public void putChainStateBatch_addsNothing_whenListIsEmpty() {
        data.putChainStateBatch(List.of());

        assertTrue(data.getAllStates().isEmpty());
    }

    @Test
    @DisplayName("Given populated data, When getAllStates is called, Then all states are returned")
    public void getAllStates_returnsAllStates() {
        var stateA = QuestChainPlayerState.newActive(chainKeyA, questKeyA);
        var completed = QuestChainPlayerState.newActive(chainKeyB, questKeyB);
        completed.complete(Instant.ofEpochMilli(1000L));

        data.putChainState(stateA);
        data.putChainState(completed);

        Collection<QuestChainPlayerState> allStates = data.getAllStates();
        assertEquals(2, allStates.size());
    }

    @Test
    @DisplayName("Given empty data, When getAllStates is called, Then an empty collection is returned")
    public void getAllStates_returnsEmptyCollection_whenNoStates() {
        assertTrue(data.getAllStates().isEmpty());
    }

    @Test
    @DisplayName("Given an active state, When updateQuestKeyIndex is called directly, Then the index is updated")
    public void updateQuestKeyIndex_updatesIndex_forActiveState() {
        var state = QuestChainPlayerState.newActive(chainKeyA, questKeyA);
        data.putChainState(state);

        state.advance(questKeyB);
        data.updateQuestKeyIndex(state);

        assertFalse(data.getChainKeyForCurrentQuest(questKeyA).isPresent());
        assertEquals(chainKeyA, data.getChainKeyForCurrentQuest(questKeyB).orElseThrow());
    }

    @Test
    @DisplayName("Given a completed state, When updateQuestKeyIndex is called, Then the quest key is removed from the index")
    public void updateQuestKeyIndex_removesFromIndex_forTerminalState() {
        var state = QuestChainPlayerState.newActive(chainKeyA, questKeyA);
        data.putChainState(state);
        assertTrue(data.getChainKeyForCurrentQuest(questKeyA).isPresent());

        state.complete(Instant.ofEpochMilli(1000L));
        data.updateQuestKeyIndex(state);

        assertFalse(data.getChainKeyForCurrentQuest(questKeyA).isPresent());
    }

    @Test
    @DisplayName("Given an existing state, When putChainState overwrites with new state, Then old state is replaced")
    public void putChainState_overwritesExistingState() {
        var oldState = QuestChainPlayerState.newActive(chainKeyA, questKeyA);
        data.putChainState(oldState);

        var newState = QuestChainPlayerState.newActive(chainKeyA, questKeyB);
        data.putChainState(newState);

        assertEquals(newState, data.getChainState(chainKeyA).orElseThrow());
        assertEquals(questKeyB, data.getChainState(chainKeyA).orElseThrow().getCurrentQuestKey().orElseThrow());
    }

    @Test
    @DisplayName("Given an overwritten state, When putChainState replaces, Then quest key index reflects new quest key")
    public void putChainState_updatesIndex_whenOverwriting() {
        data.putChainState(QuestChainPlayerState.newActive(chainKeyA, questKeyA));
        assertTrue(data.getChainKeyForCurrentQuest(questKeyA).isPresent());

        data.putChainState(QuestChainPlayerState.newActive(chainKeyA, questKeyB));

        assertFalse(data.getChainKeyForCurrentQuest(questKeyA).isPresent());
        assertEquals(chainKeyA, data.getChainKeyForCurrentQuest(questKeyB).orElseThrow());
    }

    @Test
    @DisplayName("Given a non-existent chain key, When removeChainState is called, Then nothing changes")
    public void removeChainState_doesNothing_whenKeyNotFound() {
        data.putChainState(QuestChainPlayerState.newActive(chainKeyA, questKeyA));

        data.removeChainState(chainKeyB);

        assertTrue(data.getChainState(chainKeyA).isPresent());
        assertEquals(1, data.getAllStates().size());
    }
}
