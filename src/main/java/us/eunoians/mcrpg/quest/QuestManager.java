package us.eunoians.mcrpg.quest;

import com.google.common.collect.ImmutableSet;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.entity.holder.QuestHolder;
import us.eunoians.mcrpg.exception.quest.QuestNotActiveException;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class QuestManager {

    private final Map<UUID, Quest> activeQuests;
    private final Map<UUID, Set<UUID>> questsToHolders;
    private final Map<UUID, Set<UUID>> holdersToQuests;

    public QuestManager() {
        this.activeQuests = new HashMap<>();
        this.questsToHolders = new HashMap<>();
        this.holdersToQuests = new HashMap<>();
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

    public void removeActiveQuest(@NotNull Quest quest) {
        UUID questUUID = quest.getUUID();
        activeQuests.remove(questUUID);
        Set<UUID> questHolders = questsToHolders.containsKey(questUUID) ? questsToHolders.remove(questUUID) : ImmutableSet.of();
        for (UUID questHolderUUID : questHolders) {
            removeHolderFromQuest(questHolderUUID, questUUID);
        }
    }

    public void addHolderToQuest(@NotNull QuestHolder questHolder, @NotNull Quest quest) {
        addHolderToQuest(questHolder.getUUID(), quest.getUUID());
    }

    public void addHolderToQuest(@NotNull UUID questHolderUUID, @NotNull UUID questUUID) {
        if (!activeQuests.containsKey(questUUID)) {
            throw new QuestNotActiveException(questUUID, String.format("Tried to add a holder with UUID %s to a quest with UUID %sthat doesn't exist.", questHolderUUID, questUUID));
        }
        Set<UUID> holders = questsToHolders.getOrDefault(questUUID, new HashSet<>());
        holders.add(questHolderUUID);
        questsToHolders.put(questUUID, holders);
        Set<UUID> quests = holdersToQuests.getOrDefault(questHolderUUID, new HashSet<>());
        quests.add(questUUID);
        holdersToQuests.put(questHolderUUID, quests);
    }

    public void removeHolderFromQuest(@NotNull QuestHolder questHolder, @NotNull Quest quest) {
        removeHolderFromQuest(questHolder.getUUID(), quest.getUUID());
    }

    public void removeHolderFromQuest(@NotNull UUID questHolderUUID, @NotNull UUID questUUID) {
        if (questsToHolders.containsKey(questUUID)) {
            questsToHolders.get(questUUID).remove(questHolderUUID);
            if (questsToHolders.get(questUUID).isEmpty()) {
                questsToHolders.remove(questUUID);
            }
        }
        if (holdersToQuests.containsKey(questHolderUUID)) {
            holdersToQuests.get(questHolderUUID).remove(questUUID);
            if (holdersToQuests.get(questHolderUUID).isEmpty()) {
                holdersToQuests.remove(questHolderUUID);
            }
        }
    }

    public boolean doesHolderHaveQuest(@NotNull QuestHolder questHolder, @NotNull Quest quest) {
        return doesHolderHaveQuest(questHolder.getUUID(), quest.getUUID());
    }

    public boolean doesHolderHaveQuest(@NotNull UUID questHolderUUID, @NotNull UUID questUUID) {
        return holdersToQuests.containsKey(questHolderUUID) && holdersToQuests.get(questHolderUUID).contains(questUUID);
    }

    public boolean doesQuestHaveHolder(@NotNull QuestHolder questHolder, @NotNull Quest quest) {
        return doesQuestHaveHolder(questHolder.getUUID(), quest.getUUID());
    }

    public boolean doesQuestHaveHolder(@NotNull UUID questHolderUUID, @NotNull UUID questUUID) {
        return questsToHolders.containsKey(questUUID) && questsToHolders.get(questUUID).contains(questHolderUUID);
    }

    @NotNull
    public Set<UUID> getQuestsForHolder(@NotNull QuestHolder questHolder) {
        return getQuestsForHolder(questHolder.getUUID());
    }

    @NotNull
    public Set<UUID> getQuestsForHolder(@NotNull UUID questHolderUUID) {
        return holdersToQuests.containsKey(questHolderUUID) ? ImmutableSet.copyOf(holdersToQuests.get(questHolderUUID)) : ImmutableSet.of();
    }
    @NotNull
    public Set<UUID> getQuestHoldersForQuest(@NotNull Quest quest) {
        return getQuestHoldersForQuest(quest.getUUID());
    }

    @NotNull
    public Set<UUID> getQuestHoldersForQuest(@NotNull UUID questUUID) {
        return questsToHolders.containsKey(questUUID) ? ImmutableSet.copyOf(questsToHolders.get(questUUID)) : ImmutableSet.of();
    }
}
