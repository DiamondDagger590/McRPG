package us.eunoians.mcrpg.exception.quest;

import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.quest.objective.QuestObjective;

public class QuestObjectiveCompleteException extends RuntimeException {

    private final QuestObjective questObjective;

    public QuestObjectiveCompleteException(@NotNull QuestObjective questObjective) {
        this.questObjective = questObjective;
    }

    public QuestObjectiveCompleteException(@NotNull QuestObjective questObjective, @NotNull String message) {
        super(message);
        this.questObjective = questObjective;
    }

    @NotNull
    public QuestObjective getQuestObjective() {
        return questObjective;
    }
}
