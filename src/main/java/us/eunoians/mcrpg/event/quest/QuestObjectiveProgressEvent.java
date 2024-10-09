package us.eunoians.mcrpg.event.quest;

import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.quest.Quest;
import us.eunoians.mcrpg.quest.objective.QuestObjective;

public class QuestObjectiveProgressEvent extends QuestEvent {

    private static final HandlerList handlers = new HandlerList();

    private final QuestObjective objective;
    private final int progress;

    public QuestObjectiveProgressEvent(@NotNull Quest quest, @NotNull QuestObjective objective, int progress) {
        super(quest);
        assert(quest == objective.getQuest());
        this.objective = objective;
        this.progress = Math.max(1, progress);
    }

    @NotNull
    public QuestObjective getObjective() {
        return objective;
    }

    public int getProgress() {
        return progress;
    }

    @Override
    @NotNull
    public HandlerList getHandlers() {
        return handlers;
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return handlers;
    }
}
