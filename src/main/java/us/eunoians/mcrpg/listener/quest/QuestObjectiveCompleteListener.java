package us.eunoians.mcrpg.listener.quest;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import us.eunoians.mcrpg.event.event.quest.QuestObjectiveCompleteEvent;

public class QuestObjectiveCompleteListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuestObjectiveComplete(QuestObjectiveCompleteEvent questObjectiveCompleteEvent) {
        // TODO send players a message
    }
}
