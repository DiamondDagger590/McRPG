package us.eunoians.mcrpg.quest;

import com.google.common.collect.ImmutableSet;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.entity.holder.QuestHolder;
import us.eunoians.mcrpg.exception.quest.QuestNotActiveException;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class QuestManager {

    private Map<UUID, Quest> activeQuests;

    public QuestManager() {
        activeQuests = new HashMap<>();
    }

    @NotNull
    public Set<Quest> getActiveQuests() {
        return ImmutableSet.copyOf(activeQuests.values());
    }

    public boolean isQuestActive(@NotNull UUID uuid) {
        return activeQuests.containsKey(uuid);
    }

    @NotNull
    public Optional<Quest> getActiveQuest(@NotNull UUID uuid) {
        return Optional.ofNullable(activeQuests.get(uuid));
    }

    public void addActiveQuest(@NotNull Quest quest) {
        activeQuests.put(quest.getUUID(), quest);
    }

    public void trackQuestForHolder(@NotNull QuestHolder questHolder, @NotNull Quest quest) {
        if (!isQuestActive(quest.getUUID())) {
            throw new QuestNotActiveException(quest, String.format("Quest with id %s is not active but tried to be tracked", quest.getUUID()));
        }
        quest.addQuestHolder(questHolder);
        questHolder.trackQuest(quest);
    }

    public void removeQuestForHolder(@NotNull QuestHolder questHolder, @NotNull Quest quest) {
        if (!isQuestActive(quest.getUUID())) {
            throw new QuestNotActiveException(quest, String.format("Quest with id %s is not active but tried to be removed", quest.getUUID()));
        }
        quest.removeQuestHolder(questHolder);
        questHolder.stopTrackingQuest(quest);
    }
}
