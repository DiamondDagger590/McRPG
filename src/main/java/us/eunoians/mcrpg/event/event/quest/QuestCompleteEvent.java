package us.eunoians.mcrpg.event.event.quest;

import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.quest.Quest;

public class QuestCompleteEvent extends QuestEvent {

    private static final HandlerList handlers = new HandlerList();

    public QuestCompleteEvent(@NotNull Quest quest) {
        super(quest);
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
