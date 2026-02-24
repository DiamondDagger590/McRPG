package us.eunoians.mcrpg.quest.objective.type;

/**
 * Base class for progress contexts that are passed from Bukkit event listeners to
 * {@link QuestObjectiveType} instances. Each built-in objective type defines its own
 * concrete subclass (e.g. {@code BlockBreakQuestContext}, {@code MobKillQuestContext})
 * that wraps the relevant event data.
 */
public abstract class QuestObjectiveProgressContext {
}
