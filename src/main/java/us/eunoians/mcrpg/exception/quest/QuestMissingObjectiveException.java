package us.eunoians.mcrpg.exception.quest;

import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.quest.Quest;
import us.eunoians.mcrpg.quest.objective.QuestObjective;

public class QuestMissingObjectiveException extends RuntimeException {

    private final Quest quest;
    private final QuestObjective questObjective;

    public QuestMissingObjectiveException(@NotNull Quest quest, @NotNull QuestObjective questObjective) {
        this.quest = quest;
        this.questObjective = questObjective;
    }

    public QuestMissingObjectiveException(@NotNull Quest quest, @NotNull QuestObjective questObjective, @NotNull String message) {
        super(message);
        this.quest = quest;
        this.questObjective = questObjective;
    }

    @NotNull
    public Quest getQuest() {
        return quest;
    }

    @NotNull
    public QuestObjective getQuestObjective() {
        return questObjective;
    }
}
