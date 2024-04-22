package us.eunoians.mcrpg.entity.holder;

import com.google.common.collect.ImmutableSet;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.quest.Quest;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class QuestHolder {

    private final UUID uuid;
    private final Set<UUID> activeQuests;

    public QuestHolder(UUID uuid) {
        this.uuid = uuid;
        this.activeQuests = new HashSet<>();
    }

    public QuestHolder(UUID uuid, Set<UUID> activeQuests) {
        this.uuid = uuid;
        this.activeQuests = activeQuests;
    }

    @NotNull
    public UUID getUUID() {
        return uuid;
    }

    @NotNull
    public Set<UUID> getActiveQuests() {
        return ImmutableSet.copyOf(activeQuests);
    }

    public boolean isQuestActive(@NotNull Quest quest) {
        return isQuestActive(quest.getUUID());
    }

    public boolean isQuestActive(@NotNull UUID uuid) {
        return activeQuests.contains(uuid);
    }

    public void trackQuest(@NotNull Quest quest) {
        trackQuest(quest.getUUID());
    }

    public void trackQuest(@NotNull UUID questUUID) {
        activeQuests.add(questUUID);
    }

    public void stopTrackingQuest(@NotNull Quest quest) {
        stopTrackingQuest(quest.getUUID());
    }

    public void stopTrackingQuest(@NotNull UUID questUUID) {
        activeQuests.remove(questUUID);
    }

}
