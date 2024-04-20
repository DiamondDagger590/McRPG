package us.eunoians.mcrpg.api.event.quest;

import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.quest.Quest;

public abstract class QuestEvent extends Event {

    private final Quest quest;

    public QuestEvent(@NotNull Quest quest) {
        this.quest = quest;
    }

    @NotNull
    public Quest getQuest() {
        return quest;
    }
}
