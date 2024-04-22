package us.eunoians.mcrpg.exception.quest;

import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.quest.Quest;

public class QuestNotActiveException extends RuntimeException {

    private final Quest quest;

    public QuestNotActiveException(@NotNull Quest quest) {
        this.quest = quest;
    }

    public QuestNotActiveException(@NotNull Quest quest, @NotNull String message) {
        super(message);
        this.quest = quest;
    }

    @NotNull
    public Quest getQuest() {
        return quest;
    }

}
