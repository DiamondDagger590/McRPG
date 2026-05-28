package us.eunoians.mcrpg.quest.chain;

import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;

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

    @DisplayName("Given a chain state, When putChainState is called, Then it is retrievable via getChainState")
    @Test
    public void putChainState_addsState_retrievableViaGet() {
        var state = QuestChainPlayerState.newActive(chainKeyA, questKeyA);
        data.putChainState(state);

        assertTrue(data.getChainState(chainKeyA).isPresent());
        assertEquals(state, data.getChainState(chainKeyA).get());
    }

    @DisplayName("Given an unknown chain key, When getChainState is called, Then it returns empty")
    @Test
    public void getChainState_returnsEmpty_whenKeyNotFound() {
        assertTrue(data.getChainState(chainKeyA).isEmpty());
    }

    @DisplayName("Given a state, When removeChainState is called, Then it is no longer retrievable")
    @Test
    public void removeChainState_removesState_subsequentGetReturnsEmpty() {
        data.putChainState(QuestChainPlayerState.newActive(chainKeyA, questKeyA));
        data.removeChainState(chainKeyA);

        assertTrue(data.getChainState(chainKeyA).isEmpty());
    }

    @DisplayName("Given active and completed states, When getActiveChains is called, Then only ACTIVE state returned")
    @Test
    public void getActiveChains_returnsOnlyActiveStates() {
        var active = QuestChainPlayerState.newActive(chainKeyA, questKeyA);
        var completed = QuestChainPlayerState.newActive(chainKeyB, questKeyB);
        completed.complete(1000L);

        data.putChainState(active);
        data.putChainState(completed);

        var activeChains = data.getActiveChains();
        assertEquals(1, activeChains.size());
        assertEquals(chainKeyA, activeChains.get(0).getChainKey());
    }

    @DisplayName("Given dirty and clean states, When getDirtyStates is called, Then only dirty states returned")
    @Test
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

    @DisplayName("Given active chain with currentQuestKey, When putChainState, Then questKeyToChainKey index is updated")
    @Test
    public void putChainState_updatesQuestKeyIndex_forActiveState() {
        data.putChainState(QuestChainPlayerState.newActive(chainKeyA, questKeyA));

        assertTrue(data.getChainKeyForCurrentQuest(questKeyA).isPresent());
        assertEquals(chainKeyA, data.getChainKeyForCurrentQuest(questKeyA).get());
    }

    @DisplayName("Given terminal state, When putChainState, Then questKeyToChainKey index is not populated")
    @Test
    public void putChainState_doesNotPopulateIndex_forTerminalState() {
        var state = QuestChainPlayerState.newActive(chainKeyA, questKeyA);
        state.complete(1000L);
        data.putChainState(state);

        assertTrue(data.getChainKeyForCurrentQuest(questKeyA).isEmpty());
    }

    @DisplayName("Given chain with a currentQuestKey in index, When removeChainState, Then index no longer contains it")
    @Test
    public void removeChainState_rebuildsIndex_removedChainQuestKeyNoLongerPresent() {
        data.putChainState(QuestChainPlayerState.newActive(chainKeyA, questKeyA));
        data.removeChainState(chainKeyA);

        assertTrue(data.getChainKeyForCurrentQuest(questKeyA).isEmpty());
    }

    @DisplayName("Given two active chains, When rebuildQuestKeyIndex, Then each questKey maps to correct chainKey")
    @Test
    public void rebuildQuestKeyIndex_mapsEachQuestKeyToCorrectChainKey() {
        data.putChainState(QuestChainPlayerState.newActive(chainKeyA, questKeyA));
        data.putChainState(QuestChainPlayerState.newActive(chainKeyB, questKeyB));

        data.rebuildQuestKeyIndex();

        assertEquals(chainKeyA, data.getChainKeyForCurrentQuest(questKeyA).orElseThrow());
        assertEquals(chainKeyB, data.getChainKeyForCurrentQuest(questKeyB).orElseThrow());
    }

    @DisplayName("Given state with advanced questKey, When rebuildQuestKeyIndex, Then index reflects new key")
    @Test
    public void rebuildQuestKeyIndex_updatesIndex_afterManualAdvance() {
        var state = QuestChainPlayerState.newActive(chainKeyA, questKeyA);
        data.putChainState(state);

        state.advance(questKeyB);
        data.rebuildQuestKeyIndex();

        assertFalse(data.getChainKeyForCurrentQuest(questKeyA).isPresent());
        assertEquals(chainKeyA, data.getChainKeyForCurrentQuest(questKeyB).orElseThrow());
    }
}
