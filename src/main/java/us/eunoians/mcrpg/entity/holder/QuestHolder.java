package us.eunoians.mcrpg.entity.holder;

import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.quest.Quest;

import java.util.Set;
import java.util.UUID;

public class QuestHolder {

    private final UUID uuid;

    public QuestHolder(UUID uuid) {
        this.uuid = uuid;
    }

    @NotNull
    public UUID getUUID() {
        return uuid;
    }

    @NotNull
    public Set<UUID> getActiveQuests() {
        return McRPG.getInstance().getQuestManager().getQuestsForHolder(this);
    }

    public boolean isQuestActive(@NotNull Quest quest) {
        return isQuestActive(quest.getUUID());
    }

    public boolean isQuestActive(@NotNull UUID questUUID) {
        return McRPG.getInstance().getQuestManager().doesHolderHaveQuest(uuid, questUUID);
    }

    public void trackQuest(@NotNull Quest quest) {
        trackQuest(quest.getUUID());
    }

    public void trackQuest(@NotNull UUID questUUID) {
        McRPG.getInstance().getQuestManager().addHolderToQuest(uuid, questUUID);
    }

    public void stopTrackingQuest(@NotNull Quest quest) {
        stopTrackingQuest(quest.getUUID());
    }

    public void stopTrackingQuest(@NotNull UUID questUUID) {
        McRPG.getInstance().getQuestManager().removeHolderFromQuest(uuid, questUUID);
    }

}
