package us.eunoians.mcrpg.listener.quest;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.event.quest.QuestCompleteEvent;
import us.eunoians.mcrpg.quest.Quest;
import us.eunoians.mcrpg.quest.QuestManager;

public class QuestCompleteListener implements Listener {

    @EventHandler
    public void onQuestComplete(QuestCompleteEvent questCompleteEvent){
        Quest quest = questCompleteEvent.getQuest();
        QuestManager questManager = McRPG.getInstance().getQuestManager();
        questManager.removeActiveQuest(quest);
    }
}
