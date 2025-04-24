package us.eunoians.mcrpg.entity.holder;

import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.quest.Quest;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

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
        return McRPG.getInstance().registryAccess().registry(McRPGRegistryKey.MANAGER).manager(McRPGManagerKey.QUEST).getQuestsForHolder(this);
    }

    public boolean isQuestActive(@NotNull Quest quest) {
        return isQuestActive(quest.getUUID());
    }

    public boolean isQuestActive(@NotNull UUID questUUID) {
        return McRPG.getInstance().registryAccess().registry(McRPGRegistryKey.MANAGER).manager(McRPGManagerKey.QUEST).doesHolderHaveQuest(uuid, questUUID);
    }

    public void trackQuest(@NotNull Quest quest) {
        trackQuest(quest.getUUID());
    }

    public void trackQuest(@NotNull UUID questUUID) {
        McRPG.getInstance().registryAccess().registry(McRPGRegistryKey.MANAGER).manager(McRPGManagerKey.QUEST).addHolderToQuest(uuid, questUUID);
    }

    public void stopTrackingQuest(@NotNull Quest quest) {
        stopTrackingQuest(quest.getUUID());
    }

    public void stopTrackingQuest(@NotNull UUID questUUID) {
        McRPG.getInstance().registryAccess().registry(McRPGRegistryKey.MANAGER).manager(McRPGManagerKey.QUEST).removeHolderFromQuest(uuid, questUUID);
    }

}
