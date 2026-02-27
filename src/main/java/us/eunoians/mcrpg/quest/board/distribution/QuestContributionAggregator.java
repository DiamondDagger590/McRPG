package us.eunoians.mcrpg.quest.board.distribution;

import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.quest.impl.QuestInstance;
import us.eunoians.mcrpg.quest.impl.objective.QuestObjectiveInstance;
import us.eunoians.mcrpg.quest.impl.stage.QuestStageInstance;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Stateless utility that aggregates per-player contributions at different levels
 * of the quest hierarchy. Each method returns a map of player UUID to total
 * contribution at that level.
 */
public final class QuestContributionAggregator {

    private QuestContributionAggregator() {
    }

    /**
     * Returns the raw per-player contributions for a single objective.
     */
    @NotNull
    public static Map<UUID, Long> fromObjective(@NotNull QuestObjectiveInstance objective) {
        return objective.getPlayerContributions();
    }

    /**
     * Aggregates contributions across all objectives in a stage.
     */
    @NotNull
    public static Map<UUID, Long> fromStage(@NotNull QuestStageInstance stage) {
        Map<UUID, Long> aggregated = new HashMap<>();
        for (QuestObjectiveInstance objective : stage.getQuestObjectives()) {
            objective.getPlayerContributions().forEach(
                    (uuid, amount) -> aggregated.merge(uuid, amount, Long::sum));
        }
        return aggregated;
    }

    /**
     * Aggregates contributions across all stages in a specific phase.
     */
    @NotNull
    public static Map<UUID, Long> fromPhase(@NotNull QuestInstance quest, int phaseIndex) {
        Map<UUID, Long> aggregated = new HashMap<>();
        for (QuestStageInstance stage : quest.getStagesForPhase(phaseIndex)) {
            fromStage(stage).forEach(
                    (uuid, amount) -> aggregated.merge(uuid, amount, Long::sum));
        }
        return aggregated;
    }

    /**
     * Aggregates contributions across all stages in the entire quest.
     */
    @NotNull
    public static Map<UUID, Long> fromQuest(@NotNull QuestInstance quest) {
        Map<UUID, Long> aggregated = new HashMap<>();
        for (QuestStageInstance stage : quest.getQuestStageInstances()) {
            fromStage(stage).forEach(
                    (uuid, amount) -> aggregated.merge(uuid, amount, Long::sum));
        }
        return aggregated;
    }

    /**
     * Creates a {@link ContributionSnapshot} from a contributions map and group members set.
     * Computes {@code totalProgress} as the sum of all contribution values.
     */
    @NotNull
    public static ContributionSnapshot toSnapshot(@NotNull Map<UUID, Long> contributions,
                                                   @NotNull Set<UUID> groupMembers) {
        long total = contributions.values().stream().mapToLong(Long::longValue).sum();
        return new ContributionSnapshot(contributions, total, groupMembers, null);
    }
}
