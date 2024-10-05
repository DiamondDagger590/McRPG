package us.eunoians.mcrpg.event.event.quest;

import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.quest.Quest;
import us.eunoians.mcrpg.quest.objective.QuestObjective;

public class QuestObjectiveCompleteEvent extends QuestEvent{

    private static final HandlerList handlers = new HandlerList();

    private final QuestObjective objective;

    public QuestObjectiveCompleteEvent(@NotNull Quest quest, @NotNull QuestObjective objective) {
        super(quest);
        this.objective = objective;
    }

    @NotNull
    public QuestObjective getObjective() {
        return objective;
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
