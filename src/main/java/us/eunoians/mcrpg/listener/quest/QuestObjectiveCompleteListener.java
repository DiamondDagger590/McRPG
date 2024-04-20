package us.eunoians.mcrpg.listener.quest;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.api.event.quest.QuestObjectiveCompleteEvent;
import us.eunoians.mcrpg.quest.Quest;
import us.eunoians.mcrpg.quest.QuestManager;

public class QuestObjectiveCompleteListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuestComplete(QuestObjectiveCompleteEvent questObjectiveCompleteEvent) {
        QuestManager questManager = McRPG.getInstance().getQuestManager();
        Quest quest = questObjectiveCompleteEvent.getQuest();
        if (quest.isCompleted()) {

        }
    }
}
