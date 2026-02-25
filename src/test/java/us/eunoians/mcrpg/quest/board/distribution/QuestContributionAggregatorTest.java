package us.eunoians.mcrpg.quest.board.distribution;

import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.quest.definition.QuestObjectiveDefinition;
import us.eunoians.mcrpg.quest.impl.QuestInstance;
import us.eunoians.mcrpg.quest.impl.objective.QuestObjectiveInstance;
import us.eunoians.mcrpg.quest.impl.stage.QuestStageInstance;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class QuestContributionAggregatorTest extends McRPGBaseTest {

    @DisplayName("fromObjective returns the objective's raw contribution map")
    @Test
    void fromObjectiveReturnsRaw() {
        UUID p1 = UUID.randomUUID();
        var objective = mock(QuestObjectiveInstance.class);
        when(objective.getPlayerContributions()).thenReturn(Map.of(p1, 42L));

        Map<UUID, Long> result = QuestContributionAggregator.fromObjective(objective);
        assertEquals(42L, result.get(p1));
    }

    @DisplayName("fromStage aggregates across all objectives in the stage")
    @Test
    void fromStageAggregatesObjectives() {
        UUID p1 = UUID.randomUUID(), p2 = UUID.randomUUID();

        var obj1 = mock(QuestObjectiveInstance.class);
        when(obj1.getPlayerContributions()).thenReturn(Map.of(p1, 30L, p2, 10L));
        var obj2 = mock(QuestObjectiveInstance.class);
        when(obj2.getPlayerContributions()).thenReturn(Map.of(p1, 20L, p2, 40L));

        var stage = mock(QuestStageInstance.class);
        when(stage.getQuestObjectives()).thenReturn(java.util.List.of(obj1, obj2));

        Map<UUID, Long> result = QuestContributionAggregator.fromStage(stage);
        assertEquals(50L, result.get(p1));
        assertEquals(50L, result.get(p2));
    }

    @DisplayName("fromPhase aggregates across all stages in the phase")
    @Test
    void fromPhaseAggregatesStages() {
        UUID p1 = UUID.randomUUID();

        var obj1 = mock(QuestObjectiveInstance.class);
        when(obj1.getPlayerContributions()).thenReturn(Map.of(p1, 100L));
        var obj2 = mock(QuestObjectiveInstance.class);
        when(obj2.getPlayerContributions()).thenReturn(Map.of(p1, 200L));

        var stage1 = mock(QuestStageInstance.class);
        when(stage1.getQuestObjectives()).thenReturn(java.util.List.of(obj1));
        var stage2 = mock(QuestStageInstance.class);
        when(stage2.getQuestObjectives()).thenReturn(java.util.List.of(obj2));

        var quest = mock(QuestInstance.class);
        when(quest.getStagesForPhase(0)).thenReturn(java.util.List.of(stage1, stage2));

        Map<UUID, Long> result = QuestContributionAggregator.fromPhase(quest, 0);
        assertEquals(300L, result.get(p1));
    }

    @DisplayName("fromQuest aggregates across all stages in the entire quest")
    @Test
    void fromQuestAggregatesAllStages() {
        UUID p1 = UUID.randomUUID(), p2 = UUID.randomUUID();

        var obj1 = mock(QuestObjectiveInstance.class);
        when(obj1.getPlayerContributions()).thenReturn(Map.of(p1, 50L));
        var obj2 = mock(QuestObjectiveInstance.class);
        when(obj2.getPlayerContributions()).thenReturn(Map.of(p1, 30L, p2, 70L));

        var stage1 = mock(QuestStageInstance.class);
        when(stage1.getQuestObjectives()).thenReturn(java.util.List.of(obj1));
        var stage2 = mock(QuestStageInstance.class);
        when(stage2.getQuestObjectives()).thenReturn(java.util.List.of(obj2));

        var quest = mock(QuestInstance.class);
        when(quest.getQuestStageInstances()).thenReturn(java.util.List.of(stage1, stage2));

        Map<UUID, Long> result = QuestContributionAggregator.fromQuest(quest);
        assertEquals(80L, result.get(p1));
        assertEquals(70L, result.get(p2));
    }

    @DisplayName("toSnapshot computes totalProgress from sum of contributions")
    @Test
    void toSnapshotComputesTotal() {
        UUID p1 = UUID.randomUUID(), p2 = UUID.randomUUID();
        Map<UUID, Long> contributions = Map.of(p1, 60L, p2, 40L);
        Set<UUID> members = Set.of(p1, p2);

        ContributionSnapshot snapshot = QuestContributionAggregator.toSnapshot(contributions, members);

        assertEquals(100L, snapshot.totalProgress());
        assertEquals(60L, snapshot.contributions().get(p1));
        assertEquals(40L, snapshot.contributions().get(p2));
        assertEquals(2, snapshot.groupMembers().size());
    }

    @DisplayName("toSnapshot with empty contributions gives zero total")
    @Test
    void toSnapshotEmptyContributions() {
        ContributionSnapshot snapshot = QuestContributionAggregator.toSnapshot(Map.of(), Set.of());
        assertEquals(0L, snapshot.totalProgress());
        assertTrue(snapshot.contributions().isEmpty());
    }
}
